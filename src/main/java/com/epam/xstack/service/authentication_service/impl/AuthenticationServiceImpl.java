package com.epam.xstack.service.authentication_service.impl;

import com.epam.xstack.aspects.authentication_aspects.annotations.AuthenticationChangeLoginAspectAnnotation;
import com.epam.xstack.configuration.jwt_config.JwtService;
import com.epam.xstack.exceptions.exception.UserNameOrPasswordNotCorrectException;
import com.epam.xstack.exceptions.generator.PasswordUserNameGenerator;
import com.epam.xstack.mapper.authentication_mapper.AuthenticationChangeLoginRequestMapper;
import com.epam.xstack.models.dto.authentication_dto.AuthenticationChangeLoginRequestDTO;
import com.epam.xstack.models.dto.authentication_dto.AuthenticationChangeLoginResponseDTO;
import com.epam.xstack.models.dto.authentication_dto.AuthenticationRequestDTO;
import com.epam.xstack.models.dto.authentication_dto.AuthenticationResponseDTO;
import com.epam.xstack.models.entity.User;
import com.epam.xstack.models.enums.Code;
import com.epam.xstack.repository.UserRepository;
import com.epam.xstack.service.authentication_service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordUserNameGenerator generator;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationChangeLoginRequestMapper requestMapper;

    @Override
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserName(),
                        request.getPassword()
                ));

        var traineeInDb = userRepository.findByUserName(request.getUserName()).orElseThrow();
        var jwtToken = jwtService.generateToken(traineeInDb);
        return AuthenticationResponseDTO
                .builder()
                .code(Code.STATUS_200_OK)
                .token(jwtToken)
                .data("You are authenticated as user with username: " + traineeInDb.getUsername())
                .build();
    }


    @Override
    @AuthenticationChangeLoginAspectAnnotation
    public AuthenticationChangeLoginResponseDTO authenticationChangeLogin(UUID id, AuthenticationChangeLoginRequestDTO requestDTO) {
        User userToBeUpdated = userRepository.findById(id).get();
        User user = requestMapper.toEntity(requestDTO);
        String generatedPassword = generator.generateRandomPassword();

        if (!userToBeUpdated.getPassword().equals(user.getPassword())) {
            userToBeUpdated.setUserName(requestDTO.getUserName());
            userToBeUpdated.setPassword(requestDTO.getNewPassword());
            userToBeUpdated.setPassword(passwordEncoder.encode(generatedPassword));

            userRepository.save(userToBeUpdated);
            requestMapper.toDto(user);
            return AuthenticationChangeLoginResponseDTO
                    .builder()
                    .response("Login and password changed")
                    .code(Code.STATUS_200_OK)
                    .build();
        } else {
            throw UserNameOrPasswordNotCorrectException
                    .builder()
                    .codeStatus(Code.USER_NOT_FOUND)
                    .message("User not exists in database")
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }
}
