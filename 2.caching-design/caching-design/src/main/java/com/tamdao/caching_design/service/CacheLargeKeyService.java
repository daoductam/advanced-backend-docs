package com.tamdao.caching_design.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Service minh họa các giải pháp xử lý Problem 06 - Large Key (Key quá khổ / Big Key).
 *
 * Khi lưu trữ các Object/List có kích thước quá lớn (ví dụ: vài MB) vào Redis:
 * - Gây tốn băng thông đường truyền (Network I/O).
 * - Làm block luồng đơn (Single-thread) xử lý của Redis Server, gây trễ (Latency) cho các request khác.
 *
 * Giải pháp:
 * 1. Compression (Nén dữ liệu): Sử dụng thuật toán GZIP nén dữ liệu kiểu JSON thành byte array trước khi lưu,
 *    giải nén khi đọc lên.
 * 2. Splitting (Chia nhỏ dữ liệu): Chia list dữ liệu lớn thành nhiều chunk nhỏ, lưu vào nhiều key khác nhau,
 *    và truy xuất song song thông qua CompletableFuture (hoặc MGET).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheLargeKeyService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper; // Dùng để serialize JSON

    private static final String COMPRESSED_KEY_PREFIX = "large:compressed:";
    private static final String SPLIT_KEY_PREFIX = "large:split:";

    // Dữ liệu giả lập lớn (Ví dụ: danh sách chứa 50,000 phần tử mô phỏng cho sản phẩm)
    public List<Map<String, Object>> generateLargeDataset() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 50000; i++) {
            list.add(Map.of(
                    "productId", (long) i,
                    "productName", "Product Model Name " + i,
                    "price", 100.0 + i,
                    "sku", "SKU-ABC-XYZ-12345-" + i,
                    "description", "This is a detailed description of the product with ID " + i + ". It contains some mock long texts to increase the payload size."
            ));
        }
        return list;
    }

    // ==========================================
    // SOLUTION 1: COMPRESSION (NÉN DỮ LIỆU GZIP)
    // ==========================================

    /**
     * Lưu trữ dữ liệu lớn bằng cách nén GZIP
     */
    public void saveLargeDataCompressed(String key, List<Map<String, Object>> data) {
        String fullKey = COMPRESSED_KEY_PREFIX + key;
        try {
            // 1. Serialize dữ liệu thành String JSON
            String jsonStr = objectMapper.writeValueAsString(data);
            byte[] originalBytes = jsonStr.getBytes();

            // 2. Nén mảng byte bằng GZIP
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
                gzip.write(originalBytes);
            }
            byte[] compressedBytes = bos.toByteArray();

            log.info("[COMPRESSION] Kích thước gốc: {} bytes | Kích thước sau nén GZIP: {} bytes (Giảm {:.1f}%)",
                    originalBytes.length,
                    compressedBytes.length,
                    (100.0 * (originalBytes.length - compressedBytes.length) / originalBytes.length));

            // 3. Lưu byte array đã nén vào Redis
            redisTemplate.opsForValue().set(fullKey, compressedBytes, Duration.ofMinutes(10));
            log.info("[COMPRESSION] Đã lưu key {} vào Redis.", fullKey);

        } catch (IOException e) {
            log.error("Lỗi khi nén dữ liệu lớn", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Đọc dữ liệu lớn và giải nén GZIP
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getLargeDataCompressed(String key) {
        String fullKey = COMPRESSED_KEY_PREFIX + key;
        byte[] compressedBytes = (byte[]) redisTemplate.opsForValue().get(fullKey);

        if (compressedBytes == null) {
            log.warn("[COMPRESSION] Cache MISS cho key: {}", fullKey);
            return null;
        }

        try {
            // 1. Giải nén GZIP
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedBytes);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
                byte[] buffer = new byte[256];
                int len;
                while ((len = gzip.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
            }
            byte[] decompressedBytes = bos.toByteArray();

            // 2. Deserialize ngược lại thành List Object
            String jsonStr = new String(decompressedBytes);
            log.info("[COMPRESSION] Cache HIT & Giải nén thành công cho key: {}", fullKey);
            return objectMapper.readValue(jsonStr, new TypeReference<List<Map<String, Object>>>() {});

        } catch (IOException e) {
            log.error("Lỗi khi giải nén dữ liệu lớn", e);
            throw new RuntimeException(e);
        }
    }

    // ==========================================
    // SOLUTION 2: SPLITTING (CHIA NHỎ DỮ LIỆU)
    // ==========================================

    /**
     * Lưu trữ dữ liệu lớn bằng cách chia nhỏ thành các chunks
     */
    public void saveLargeDataSplit(String key, List<Map<String, Object>> data) {
        String baseKey = SPLIT_KEY_PREFIX + key;
        int totalSize = data.size();
        
        // Định nghĩa kích thước của mỗi chunk (ví dụ 10,000 item / chunk)
        int chunkSize = 10000;
        int chunkCount = (int) Math.ceil((double) totalSize / chunkSize);

        log.info("[SPLITTING] Chia danh sách {} phần tử thành {} chunks nhỏ (mỗi chunk tối đa {})", 
                totalSize, chunkCount, chunkSize);

        // Lưu thông tin Metadata của key chính (số lượng chunks) vào Redis
        redisTemplate.opsForValue().set(baseKey + ":meta", chunkCount, Duration.ofMinutes(10));

        // Lưu các chunk song song
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < chunkCount; i++) {
            int chunkIndex = i;
            int fromIndex = chunkIndex * chunkSize;
            int toIndex = Math.min(fromIndex + chunkSize, totalSize);
            List<Map<String, Object>> subList = data.subList(fromIndex, toIndex);

            futures.add(CompletableFuture.runAsync(() -> {
                String subKey = baseKey + ":" + chunkIndex;
                redisTemplate.opsForValue().set(subKey, subList, Duration.ofMinutes(10));
            }));
        }

        // Đợi tất cả chunk ghi xong
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("[SPLITTING] Đã ghi song song {} chunks thành công lên Redis.", chunkCount);
    }

    /**
     * Truy xuất dữ liệu lớn bằng cách lấy song song các chunks và gộp lại
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getLargeDataSplit(String key) {
        String baseKey = SPLIT_KEY_PREFIX + key;
        
        // 1. Đọc metadata để biết số lượng chunks
        Integer chunkCount = (Integer) redisTemplate.opsForValue().get(baseKey + ":meta");
        if (chunkCount == null) {
            log.warn("[SPLITTING] Cache MISS (Metadata rỗng) cho key: {}", baseKey);
            return null;
        }

        log.info("[SPLITTING] Tìm thấy {} chunks cần lấy. Bắt đầu đọc song song...", chunkCount);

        // 2. Tạo mảng CompletableFuture để đọc song song các chunks từ Redis
        CompletableFuture<List<Map<String, Object>>>[] futures = new CompletableFuture[chunkCount];
        for (int i = 0; i < chunkCount; i++) {
            int chunkIndex = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                String subKey = baseKey + ":" + chunkIndex;
                return (List<Map<String, Object>>) redisTemplate.opsForValue().get(subKey);
            });
        }

        // 3. Đợi tất cả tiến trình hoàn thành và tiến hành gộp dữ liệu
        CompletableFuture.allOf(futures).join();

        List<Map<String, Object>> combinedList = new ArrayList<>();
        try {
            for (CompletableFuture<List<Map<String, Object>>> future : futures) {
                List<Map<String, Object>> chunkData = future.get();
                if (chunkData != null) {
                    combinedList.addAll(chunkData);
                }
            }
            log.info("[SPLITTING] Đọc và gộp thành công {} chunks. Tổng số phần tử: {}", chunkCount, combinedList.size());
            return combinedList;
        } catch (Exception e) {
            log.error("Lỗi khi đọc song song các chunks dữ liệu", e);
            throw new RuntimeException(e);
        }
    }
}
