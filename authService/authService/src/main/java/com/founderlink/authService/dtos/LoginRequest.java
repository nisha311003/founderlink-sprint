package com.founderlink.authService.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class
LoginRequest {

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password is mandatory")
    private String password;
}
