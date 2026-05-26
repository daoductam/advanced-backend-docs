package com.tamdao.codebase.application.passenger.usecase;

import com.tamdao.codebase.application.passenger.dto.PassengerResponse;
import com.tamdao.codebase.application.passenger.dto.RegisterPassengerRequest;
import com.tamdao.codebase.domain.passenger.repository.PassengerRepository;

/**
 * Use Case — Đăng ký hành khách mới.
 *
 * <p>Nhận DTO request → tạo Entity Passenger + Value Objects → lưu qua Repository.
 */
public class RegisterPassengerUseCase {

    private final PassengerRepository passengerRepository;

    public RegisterPassengerUseCase(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    // public PassengerResponse execute(RegisterPassengerRequest request) { ... }
}
