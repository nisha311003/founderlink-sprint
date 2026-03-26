package com.founderlink.startupService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StartupRequest {

    @NotBlank(message = "Startup name is mandatory")
    private String name;

    @NotBlank(message = "Description is mandatory")
    private String description;

    @NotBlank(message = "Industry is mandatory")
    private String industry;

    @NotBlank(message = "Problem statement is mandatory")
    private String problemStatement;

    @NotBlank(message = "Solution is mandatory")
    private String solution;

    @NotNull(message = "Funding goal is mandatory")
    @Positive(message = "Funding goal must be positive")
    private Double fundingGoal;
    private String stage;

}
