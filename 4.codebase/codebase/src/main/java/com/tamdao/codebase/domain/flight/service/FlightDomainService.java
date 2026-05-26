package com.tamdao.codebase.domain.flight.service;

/**
 * Domain Service — Dịch vụ miền cho chuyến bay.
 *
 * <p>Chứa logic nghiệp vụ <b>không thuộc về</b> riêng một Entity hay Value Object nào.
 * Ví dụ: kiểm tra ghế trống trên chuyến bay, tính toán thời gian bay.
 *
 * <p><b>Stateless</b> — không lưu trạng thái nội bộ,
 * chỉ điều phối giữa các Entity/VO trong cùng Bounded Context.
 */
public class FlightDomainService {

    // Ví dụ: kiểm tra còn ghế trống hay không
    // public boolean hasAvailableSeat(Flight flight, String seatClass) { ... }
}
