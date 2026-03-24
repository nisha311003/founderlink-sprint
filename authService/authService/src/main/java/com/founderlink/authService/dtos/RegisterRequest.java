package com.founderlink.authService.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is mandatory")
    @Size(min = 3, message = "Name should be at least 3 characters")
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Email
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Minimum length is 8")
    private String password;

    private String role;
}
