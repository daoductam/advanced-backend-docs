package com.tamdao.codebase.domain.flight.valueobject;

/**
 * Value Object — Tuyến bay (sân bay đi → sân bay đến).
 *
 * <p>Đặc điểm Value Object:
 * <ul>
 *   <li>Không có ID định danh riêng.</li>
 *   <li>Bất biến (Immutable): sau khi tạo không thể sửa đổi.</li>
 *   <li>So sánh bằng giá trị: hai {@code Route} giống nhau khi
 *       {@code departureAirport} và {@code arrivalAirport} trùng nhau.</li>
 *   <li>Khi cần thay đổi → tạo đối tượng {@code Route} mới.</li>
 * </ul>
 */
public class Route {

    private final String departureAirport;
    private final String arrivalAirport;

    public Route(String departureAirport, String arrivalAirport) {
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
    }
}
