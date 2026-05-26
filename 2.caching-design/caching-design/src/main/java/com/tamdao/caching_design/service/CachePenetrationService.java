package com.tamdao.caching_design.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.tamdao.caching_design.model.UserProfile;
import com.tamdao.caching_design.repository.UserProfileRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Service minh họa các giải pháp giải quyết Problem 04 - Cache Penetration (Xuyên thủng Cache).
 *
 * Cache Penetration xảy ra khi Client liên tục request các key "không tồn tại" (cả trong Cache lẫn DB).
 * Do key không tồn tại, cache luôn bị MISS, dẫn đến mọi request đều đi thẳng xuống DB để truy vấn,
 * làm DB quá tải (đặc biệt khi có tấn công DDOS).
 *
 * Service này triển khai 3 giải pháp:
 * 1. Request Validation (Xác thực đầu vào): Chặn ngay tại App nếu tham số không hợp lệ (ví dụ: id <= 0).
 * 2. Cache Null Value (Lưu giá trị rỗng/mặc định): Khi DB báo không có dữ liệu, lưu giá trị sentinel vào Redis
 *    với TTL ngắn để các request sau đụng Cache trước và phản hồi ngay, không làm phiền DB.
 * 3. Bloom Filter (Bộ lọc Bloom): Một cấu trúc dữ liệu lưu trong bộ nhớ (hoặc Redis) cho biết một key
 *    chắc chắn KHÔNG tồn tại hoặc CÓ THỂ tồn tại. Nếu chắc chắn không có, chặn ngay không truy vấn DB.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CachePenetrationService {

    private final UserProfileRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // Sentinel value để lưu vào Redis khi dữ liệu không tồn tại ở DB
    private static final String NULL_SENTINEL = "NULL_VALUE";
    private static final String CACHE_KEY_PREFIX = "user:profile:";
    
    // Bloom Filter sử dụng Guava cho demo trong RAM (để tối giản, thực tế có thể dùng Redis Bloom)
    // Thiết lập số lượng phần tử dự kiến là 10,000 và tỷ lệ lỗi sai (false positive rate) là 1% (0.01)
    private BloomFilter<Long> userBloomFilter;

    @PostConstruct
    public void initBloomFilter() {
        log.info("[BLOOM FILTER] Đang khởi tạo Bloom Filter...");
        // 1. Tạo Bloom Filter cho kiểu dữ liệu Long (User ID)
        userBloomFilter = BloomFilter.create(
                Funnels.longFunnel(),
                10000, // Expected insertions
                0.01   // False positive probability (1%)
        );

        // 2. Load toàn bộ User ID đang có trong DB nạp vào Bloom Filter
        List<UserProfile> allUsers = userRepository.findAll();
        for (UserProfile user : allUsers) {
            userBloomFilter.put(user.getId());
        }
        log.info("[BLOOM FILTER] Đã nạp {} User ID vào Bloom Filter.", allUsers.size());
    }

    /**
     * Thêm một ID mới vào Bloom Filter (ví dụ khi tạo mới user thành công)
     */
    public void addToBloomFilter(Long userId) {
        if (userBloomFilter != null) {
            userBloomFilter.put(userId);
            log.info("[BLOOM FILTER] Đã nạp thêm User ID = {} vào Bloom Filter.", userId);
        }
    }

    /**
     * Cách tiếp cận Cũ (Chưa bảo vệ):
     * Nếu key ma liên tục được gửi, DB sẽ bị query liên tục vì không lưu cache null.
     */
    public UserProfile getUserLegacy(Long id) {
        String key = CACHE_KEY_PREFIX + id;
        
        // 1. Đọc từ Cache
        UserProfile cached = (UserProfile) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("[LEGACY] Cache HIT cho User ID = {}", id);
            return cached;
        }

        // 2. Cache MISS -> Query DB
        log.warn("[LEGACY] Cache MISS. Query DB cho User ID = {}", id);
        Optional<UserProfile> dbResult = userRepository.findById(id);

        if (dbResult.isPresent()) {
            UserProfile user = dbResult.get();
            // Lưu cache bình thường
            redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(10));
            return user;
        }

        // TRẢ VỀ NULL VÀ KHÔNG LƯU GÌ VÀO CACHE -> Gây Penetration!
        return null;
    }

    /**
     * Solution 1 + 2: Validate Request + Cache Null Value
     * - Validate đầu vào ngay lập tức để loại bỏ request lỗi/phá hoại thô sơ.
     * - Nếu DB không tìm thấy, ta lưu một giá trị đại diện (Sentinel) kèm TTL ngắn (ví dụ: 1-5 phút)
     *   để chặn các request sau đụng DB.
     */
    public UserProfile getUserWithCacheNull(Long id) {
        // Solution 1: Validate Request
        if (id == null || id <= 0) {
            log.warn("[VALIDATION] Request bị chặn do ID không hợp lệ: id = {}", id);
            throw new IllegalArgumentException("ID người dùng không hợp lệ. Phải lớn hơn 0.");
        }

        String key = CACHE_KEY_PREFIX + id;

        // 1. Đọc từ Cache
        Object cachedValue = redisTemplate.opsForValue().get(key);
        if (cachedValue != null) {
            // Kiểm tra xem có phải là sentinel báo hiệu dữ liệu rỗng hay không
            if (NULL_SENTINEL.equals(cachedValue)) {
                log.info("[CACHE NULL] Cache HIT (Sentinel NULL) cho User ID = {}. Trả về null ngay lập tức.", id);
                return null;
            }
            log.info("[CACHE NULL] Cache HIT cho User ID = {}", id);
            return (UserProfile) cachedValue;
        }

        // 2. Cache MISS -> Query DB
        log.warn("[CACHE NULL] Cache MISS. Query DB cho User ID = {}", id);
        Optional<UserProfile> dbResult = userRepository.findById(id);

        if (dbResult.isPresent()) {
            UserProfile user = dbResult.get();
            redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(10));
            return user;
        } else {
            // Solution 2: Cache Null Value (Lưu giá trị sentinel với TTL ngắn)
            log.warn("[CACHE NULL] DB không có dữ liệu cho User ID = {}. Tiến hành lưu NULL_SENTINEL vào cache trong 2 phút.", id);
            redisTemplate.opsForValue().set(key, NULL_SENTINEL, Duration.ofMinutes(2));
            return null;
        }
    }

    /**
     * Solution 3: Bloom Filter
     * - Bloom Filter chứa danh sách tất cả các ID có trong DB.
     * - Nếu Bloom Filter nói ID KHÔNG tồn tại -> Chắc chắn 100% không tồn tại -> Trả về null ngay!
     * - Nếu Bloom Filter nói ID CÓ tồn tại -> Có thể tồn tại (có tỉ lệ nhỏ false positive) -> Tiếp tục check Cache & DB.
     */
    public UserProfile getUserWithBloomFilter(Long id) {
        // Solution 1: Vẫn kết hợp validate đầu vào
        if (id == null || id <= 0) {
            log.warn("[BLOOM + VALIDATION] Chặn ID không hợp lệ: id = {}", id);
            throw new IllegalArgumentException("ID người dùng không hợp lệ.");
        }

        // Solution 3: Kiểm tra qua Bloom Filter trước khi làm bất kỳ hành động nào khác
        if (!userBloomFilter.mightContain(id)) {
            // Chắc chắn 100% không có trong DB!
            log.warn("[BLOOM FILTER] ID = {} KHÔNG tồn tại (chắc chắn 100%). Chặn ngay lập tức!", id);
            return null;
        }

        log.info("[BLOOM FILTER] ID = {} có khả năng tồn tại. Tiếp tục quy trình Cache...", id);

        String key = CACHE_KEY_PREFIX + id;

        // Tiến hành quy trình check cache thông thường
        Object cachedValue = redisTemplate.opsForValue().get(key);
        if (cachedValue != null) {
            if (NULL_SENTINEL.equals(cachedValue)) {
                log.info("[BLOOM + CACHE] Cache HIT (Sentinel NULL) cho User ID = {}", id);
                return null;
            }
            log.info("[BLOOM + CACHE] Cache HIT cho User ID = {}", id);
            return (UserProfile) cachedValue;
        }

        // Cache MISS -> Query DB
        log.warn("[BLOOM + CACHE] Cache MISS. Query DB cho User ID = {}", id);
        Optional<UserProfile> dbResult = userRepository.findById(id);

        if (dbResult.isPresent()) {
            UserProfile user = dbResult.get();
            redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(10));
            return user;
        } else {
            // Trường hợp Bloom Filter báo có nhưng DB không có (False Positive)
            log.warn("[BLOOM FILTER] False Positive! Bloom filter báo có nhưng DB trống cho ID = {}. Lưu cache null.", id);
            redisTemplate.opsForValue().set(key, NULL_SENTINEL, Duration.ofMinutes(2));
            return null;
        }
    }
}
