package com.tamdao.codebase.presentation.passenger;

import com.tamdao.codebase.application.passenger.usecase.RegisterPassengerUseCase;

/**
 * Controller (Presentation Layer) — API hành khách.
 */
public class PassengerController {

    private final RegisterPassengerUseCase registerPassengerUseCase;

    public PassengerController(RegisterPassengerUseCase registerPassengerUseCase) {
        this.registerPassengerUseCase = registerPassengerUseCase;
    }

    // @PostMapping("/passengers")
    // public PassengerResponse register(RegisterPassengerRequest request) { ... }
}
