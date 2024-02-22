package com.epam.xstack.service;

import com.epam.xstack.models.entity.AuthenticationRequest;
import com.epam.xstack.models.entity.AuthenticationResponse;
import com.epam.xstack.models.entity.RegisterRequest;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

}
