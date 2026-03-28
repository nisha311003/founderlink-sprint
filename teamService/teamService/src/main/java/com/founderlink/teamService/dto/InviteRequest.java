package com.founderlink.teamService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteRequest {

    @NotNull(message = "Startup ID is mandatory")
    private Long startupId;

    @NotBlank(message = "Startup name is mandatory")
    private String startupName;

    @NotNull(message = "Invited user ID is mandatory")
    private Long invitedUserId;

    @NotBlank(message = "Invited user email is mandatory")
    private String invitedUserEmail;

    @NotBlank(message = "Role is mandatory")
    private String role;
}
