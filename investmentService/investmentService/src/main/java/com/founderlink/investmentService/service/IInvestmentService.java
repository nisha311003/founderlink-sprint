package com.founderlink.investmentService.service;

import com.founderlink.investmentService.dto.InvestmentRequest;
import com.founderlink.investmentService.dto.InvestmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IInvestmentService {

    InvestmentResponse createInvestment(
            Long investorId,
            String investorEmail,
            InvestmentRequest request);

    InvestmentResponse getInvestment(Long id);

    Page<InvestmentResponse> getInvestmentsByStartup(
            Long startupId, Pageable pageable);

    Page<InvestmentResponse> getInvestmentsByInvestor(
            Long investorId, Pageable pageable);

    Page<InvestmentResponse> getInvestmentsByFounder(
            Long founderId, Pageable pageable);

    InvestmentResponse approveInvestment(
            Long id, Long founderId);

    InvestmentResponse rejectInvestment(
            Long id, Long founderId);

    InvestmentResponse completeInvestment(
            Long id, Long founderId);

    Page<InvestmentResponse> getPendingInvestments(
            Pageable pageable);
}
