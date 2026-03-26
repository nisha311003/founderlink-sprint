package com.founderlink.investmentService.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InvestmentRequest {

    @NotNull(message = "Startup ID is mandatory")
    private Long startupId;

    @NotNull(message = "Startup name is mandatory")
    private String startupName;

    @NotNull(message = "Founder ID is mandatory")
    private Long founderId;

    @NotNull(message = "Amount is mandatory")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private String note;
}
