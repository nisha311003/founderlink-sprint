package com.founderlink.userService.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {
    private Long userId;
    private String name;
    private String email;
    private String bio;
    private String skills;
    private String experience;
    private String portfolioLink;
    private String role;
}
