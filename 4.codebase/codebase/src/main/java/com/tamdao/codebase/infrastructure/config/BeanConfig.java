package com.tamdao.codebase.infrastructure.config;

/**
 * Cấu hình Dependency Injection — Kết nối Adapter vào Port.
 *
 * <p>Đây là nơi Spring Boot biết phải inject Implementation nào
 * cho mỗi Repository Interface. Ví dụ:
 * <ul>
 *   <li>{@code FlightRepository} ← {@code FlightRepositoryImpl}</li>
 *   <li>{@code BookingRepository} ← {@code BookingRepositoryImpl}</li>
 *   <li>{@code PassengerRepository} ← {@code PassengerRepositoryImpl}</li>
 * </ul>
 *
 * <p>Trong thực tế có thể dùng {@code @Component}/{@code @Repository}
 * trực tiếp trên Implementation, hoặc khai báo tường minh bằng
 * {@code @Bean} trong class {@code @Configuration} này.
 */
public class BeanConfig {

    // @Bean
    // public FlightRepository flightRepository() {
    //     return new FlightRepositoryImpl();
    // }

    // @Bean
    // public BookingRepository bookingRepository() {
    //     return new BookingRepositoryImpl();
    // }

    // @Bean
    // public PassengerRepository passengerRepository() {
    //     return new PassengerRepositoryImpl();
    // }
}
