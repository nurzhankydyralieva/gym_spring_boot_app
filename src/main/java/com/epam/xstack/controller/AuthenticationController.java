package com.epam.xstack.controller;

import com.epam.xstack.exceptions.validator.NotNullValidation;
import com.epam.xstack.models.dto.authentication_dto.AuthenticationChangeLoginRequestDTO;
import com.epam.xstack.models.dto.authentication_dto.AuthenticationChangeLoginResponseDTO;
import com.epam.xstack.models.dto.authentication_dto.AuthenticationRequestDTO;
import com.epam.xstack.models.dto.authentication_dto.AuthenticationResponseDTO;
import com.epam.xstack.models.entity.User;
import com.epam.xstack.service.authentication_service.AuthenticationService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService service;
    private final NotNullValidation validation;
    private List<User> userList;
    private final MeterRegistry registry;

    public Supplier<Number> fetchUserCount() {
        return () -> userList.size();
    }

    @Autowired
    public AuthenticationController(AuthenticationService service, NotNullValidation validation, MeterRegistry registry) {
        this.service = service;
        this.validation = validation;
        this.registry = registry;
        Gauge.builder("authentication.controller.user.count", fetchUserCount())
                .tag("version", "gym.app")
                .description("Authentication Controller description")
                .register(registry);
    }

    @Operation(summary = "This request changes login and password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User authenticated"),
                    @ApiResponse(responseCode = "401", description = "Bad credentials"),
                    @ApiResponse(responseCode = "422", description = "User or password is null"),
                    @ApiResponse(responseCode = "403", description = "Access denied, check user name or id"),
                    @ApiResponse(responseCode = "404", description = "User with user name or id not found")})
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(@Valid @RequestBody AuthenticationRequestDTO request, BindingResult bindingResult) {
        validation.nullValidation(bindingResult);
        return ResponseEntity.ok(service.authenticate(request));
    }

    @Operation(summary = "This request changes login and password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User password and login successfully updated"),
                    @ApiResponse(responseCode = "401", description = "Bad credentials"),
                    @ApiResponse(responseCode = "422", description = "User or password is null"),
                    @ApiResponse(responseCode = "403", description = "Access denied, check user name or id"),
                    @ApiResponse(responseCode = "404", description = "User with user name or id not found")})
    @PutMapping("/update/{id}")
    public ResponseEntity<AuthenticationChangeLoginResponseDTO> updateLogin(@PathVariable("id") UUID id, @Valid @RequestBody AuthenticationChangeLoginRequestDTO requestDTO, BindingResult bindingResult) {
        validation.nullValidation(bindingResult);
        return new ResponseEntity<>(service.authenticationChangeLogin(id, requestDTO), HttpStatus.OK);
    }

}
