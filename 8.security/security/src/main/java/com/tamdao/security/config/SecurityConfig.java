package com.tamdao.security.config;

import com.tamdao.security.hashing.Argon2Hasher;
import com.tamdao.security.hashing.PasswordHasher;
import com.tamdao.security.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public SecurityConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Bean
    public PasswordHasher passwordHasher() {
        // By default, we use state-of-the-art Argon2id for secure password hashing
        return new Argon2Hasher();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply rate limiting interceptor to all API routes
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
    }
}
