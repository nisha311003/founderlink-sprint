package com.founderlink.investmentService.dto;

import com.founderlink.investmentService.entity.InvestmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvestmentResponse {

    private Long id;
    private Long startupId;
    private String startupName;
    private Long investorId;
    private String investorEmail;
    private Long founderId;
    private Double amount;
    private InvestmentStatus status;
    private String note;
    private LocalDateTime createdAt;
}
