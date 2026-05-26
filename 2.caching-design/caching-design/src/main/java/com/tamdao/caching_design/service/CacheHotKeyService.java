package com.tamdao.caching_design.service;

import com.tamdao.caching_design.model.UserProfile;
import com.tamdao.caching_design.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service minh họa các giải pháp xử lý Problem 05 - Hot Keys (Key siêu nóng).
 *
 * Hot Keys xảy ra khi 1 key cụ thể (ví dụ: sản phẩm flash sale, bài viết hot của KOL)
 * nhận hàng triệu request trong thời gian rất ngắn. Dù có phân mảnh Redis (Redis Cluster),
 * key này vẫn chỉ nằm trên 1 node Redis duy nhất khiến node đó quá tải băng thông/CPU.
 *
 * Giải pháp:
 * 1. Local Cache (Bộ nhớ đệm cục bộ): Sử dụng bộ nhớ RAM của chính Application JVM (ví dụ dùng ConcurrentHashMap/Caffeine)
 *    để giảm tải 99.9% request gọi tới Redis.
 * 2. Hot Key Replication/Sharding: Sao chép Hot Key thành nhiều bản ghi phụ (ví dụ: user:100_1, user:100_2...)
 *    phân tán ra nhiều node Redis khác nhau. Client random hậu tố từ _1 đến _N để dàn đều tải.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheHotKeyService {

    private final UserProfileRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random();

    // Định nghĩa số lượng bản sao để sharding hotkey trong Redis
    private static final int SHARD_COUNT = 3; 
    private static final String CACHE_KEY_PREFIX = "user:profile:";

    // Local Cache đơn giản bằng Map trong bộ nhớ RAM của JVM (sử dụng ConcurrentHashMap)
    // Thực tế dự án lớn nên dùng thư viện Caffeine Cache để tự động giải phóng bộ nhớ khi đầy.
    private final Map<String, LocalCacheValue> localCache = new ConcurrentHashMap<>();

    // Class đại diện cấu trúc dữ liệu lưu trong Local Cache bao gồm data + expiry time
    private record LocalCacheValue(UserProfile data, long expireAt) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }

    /**
     * 1. Giải pháp Local Cache (Bộ nhớ đệm cục bộ trong RAM của ứng dụng)
     * Thích hợp cho dữ liệu cực kỳ hot, chấp nhận độ trễ đồng bộ nhẹ (vài giây).
     */
    public UserProfile getUserWithLocalCache(Long id) {
        String key = CACHE_KEY_PREFIX + id;
        long now = System.currentTimeMillis();

        // Bước A: Kiểm tra Local Cache trước tiên (hoàn toàn chạy trong RAM JVM, ko qua Network)
        LocalCacheValue localVal = localCache.get(key);
        if (localVal != null && !localVal.isExpired()) {
            log.info("[LOCAL CACHE HIT] Đọc thành công từ RAM JVM cho User ID = {}", id);
            return localVal.data();
        }

        log.warn("[LOCAL CACHE MISS] RAM JVM trống hoặc hết hạn. Đi tiếp xuống Redis cho ID = {}", id);

        // Bước B: Đọc từ Distributed Cache (Redis)
        UserProfile cached = (UserProfile) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("[REDIS HIT] Tìm thấy trên Redis cho User ID = {}. Nạp ngược lại vào Local Cache.", id);
            // Nạp vào Local Cache trong RAM của server hiện tại với TTL ngắn (10 giây)
            localCache.put(key, new LocalCacheValue(cached, now + 10000));
            return cached;
        }

        // Bước C: Cache Miss hoàn toàn -> Query DB
        log.warn("[REDIS MISS] Query DB cho User ID = {}", id);
        Optional<UserProfile> dbResult = userRepository.findById(id);
        if (dbResult.isPresent()) {
            UserProfile user = dbResult.get();
            // Lưu Redis với TTL 10 phút
            redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(10));
            // Lưu Local Cache JVM với TTL 10 giây
            localCache.put(key, new LocalCacheValue(user, now + 10000));
            return user;
        }

        return null;
    }

    /**
     * 2. Giải pháp Hot Key Replication / Sharding (Nhân bản key)
     * Thích hợp khi dữ liệu hot thay đổi liên tục và chúng ta muốn phân tán tải đọc
     * cho toàn bộ các node trong Redis Cluster thay vì tập trung vào 1 node duy nhất.
     */
    public UserProfile getUserWithReplicatedKey(Long id) {
        // Sinh ngẫu nhiên một suffix từ 1 đến SHARD_COUNT (Ví dụ: user:profile:1_1, user:profile:1_2, ...)
        int shardIndex = random.nextInt(SHARD_COUNT) + 1;
        String replicatedKey = CACHE_KEY_PREFIX + id + "_" + shardIndex;

        log.info("[REPLICATION] Đang truy xuất qua Key Shard ngẫu nhiên: {}", replicatedKey);

        // 1. Đọc từ bản sao ngẫu nhiên trong Redis
        UserProfile cached = (UserProfile) redisTemplate.opsForValue().get(replicatedKey);
        if (cached != null) {
            log.info("[REPLICATION HIT] Tìm thấy dữ liệu tại key shard: {}", replicatedKey);
            return cached;
        }

        // 2. Cache Miss -> Đồng bộ hóa việc ghi (chỉ 1 request ghi đè lên các Shard tránh ghi trùng lặp liên tục)
        String baseKey = CACHE_KEY_PREFIX + id;
        synchronized (this) {
            // Đọc lại bản gốc hoặc kiểm tra DB
            Optional<UserProfile> dbResult = userRepository.findById(id);
            if (dbResult.isPresent()) {
                UserProfile user = dbResult.get();
                // Ghi đè dữ liệu giống nhau lên toàn bộ các bản sao (Shards) để đảm bảo client random trúng bản nào cũng có dữ liệu
                for (int i = 1; i <= SHARD_COUNT; i++) {
                    String targetKey = CACHE_KEY_PREFIX + id + "_" + i;
                    redisTemplate.opsForValue().set(targetKey, user, Duration.ofMinutes(10));
                }
                log.info("[REPLICATION WRITE] Đã ghi đồng bộ dữ liệu User ID = {} lên {} key shards.", id, SHARD_COUNT);
                return user;
            }
        }
        return null;
    }
}
