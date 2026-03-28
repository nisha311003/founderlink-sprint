package com.founderlink.authService.service;

import com.founderlink.authService.dtos.AuthResponse;
import com.founderlink.authService.dtos.LoginRequest;
import com.founderlink.authService.dtos.RegisterRequest;

public interface IAuthService {

    String register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
