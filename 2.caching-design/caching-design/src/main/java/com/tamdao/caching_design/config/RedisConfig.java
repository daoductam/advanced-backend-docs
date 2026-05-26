package com.tamdao.caching_design.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Cấu hình RedisTemplate — cầu nối giữa Spring Boot và Redis Server.
 *
 * Tại sao cần cấu hình riêng thay vì dùng mặc định?
 *
 * Mặc định, Spring dùng JdkSerializationRedisSerializer → lưu dữ liệu dưới dạng binary khó đọc.
 * Chúng ta cấu hình lại để:
 *   - KEY     (String):  Dễ đọc, dễ debug trên Redis CLI (ví dụ: "user:1")
 *   - VALUE   (Object):  Serialize thành JSON → dễ đọc, tương thích đa ngôn ngữ
 */
@Configuration
public class RedisConfig {

    /**
     * @param connectionFactory: Spring Boot tự inject từ application.yaml
     *        (host: localhost, port: 6379)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer: StringRedisSerializer → lưu key là plain text
        // Ví dụ key: "user:1", "user:42"
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        // Value Serializer: GenericJackson2JsonRedisSerializer → chuyển object → JSON
        // Ví dụ value: {"id":1,"name":"Tam","age":25,"email":"tam@mail.com"}
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
