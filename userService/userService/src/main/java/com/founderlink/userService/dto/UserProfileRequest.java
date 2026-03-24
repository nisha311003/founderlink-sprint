package com.founderlink.userService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileRequest {
    @NotBlank(message = "Name is mandatory")
    @Size(min = 3, message = "Name must be at least 3 characters")
    private String name;

    private String bio;
    private String skills;
    private String experience;
    private String portfolioLink;
}
