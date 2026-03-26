package com.founderlink.investmentService.service;

import com.founderlink.investmentService.dto.InvestmentRequest;
import com.founderlink.investmentService.dto.InvestmentResponse;
import com.founderlink.investmentService.entity.Investment;
import com.founderlink.investmentService.entity.InvestmentStatus;
import com.founderlink.investmentService.exception.InvestmentNotFoundException;
import com.founderlink.investmentService.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestmentService implements IInvestmentService{

    private final InvestmentRepository investmentRepository;
    private final ModelMapper modelMapper;

    @Override
    public InvestmentResponse createInvestment(Long investorId, String investorEmail, InvestmentRequest request) {
        Investment investment = Investment.builder()
                .startupId(request.getStartupId())
                .startupName(request.getStartupName())
                .investorId(investorId)
                .investorEmail(investorEmail)
                .founderId(request.getFounderId())
                .amount(request.getAmount())
                .note(request.getNote())
                .status(InvestmentStatus.PENDING)
                .build();

        investmentRepository.save(investment);
        return modelMapper.map(investment, InvestmentResponse.class);
    }

    @Override
    public InvestmentResponse getInvestment(Long id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(()-> new InvestmentNotFoundException("Investment not found with id: "+id));
        return modelMapper.map(investment, InvestmentResponse.class);
    }

    @Override
    public Page<InvestmentResponse> getInvestmentsByStartup(Long startupId, Pageable pageable) {
        return investmentRepository.findByStartupId(startupId, pageable)
                .map(investment -> modelMapper.map(investment, InvestmentResponse.class));
    }

    @Override
    public Page<InvestmentResponse> getInvestmentsByInvestor(Long investorId, Pageable pageable) {
        return investmentRepository.findByInvestorId(investorId, pageable)
                .map(investment -> modelMapper.map(investment, InvestmentResponse.class));
    }

    @Override
    public Page<InvestmentResponse> getInvestmentsByFounder(Long founderId, Pageable pageable) {
        return investmentRepository.findByFounderId(founderId, pageable)
                .map(investment -> modelMapper.map(investment, InvestmentResponse.class));
    }

    @Override
    public InvestmentResponse approveInvestment(Long id, Long founderId) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(()-> new InvestmentNotFoundException("Investment not found with id: "+id));

        if (!investment.getFounderId().equals(founderId)) {
            throw new RuntimeException("Founder does not have permission to approve this investment");
        }

        if (investment.getStatus() != InvestmentStatus.PENDING) {
            throw new RuntimeException("Only pending investments can be approved");
        }

        investment.setStatus(InvestmentStatus.APPROVED);
        investmentRepository.save(investment);
        return modelMapper.map(investment, InvestmentResponse.class);
    }

    @Override
    public InvestmentResponse rejectInvestment(Long id, Long founderId) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(()-> new InvestmentNotFoundException("Investment not found with id: "+id));

        if (!investment.getFounderId().equals(founderId)) {
            throw new RuntimeException("Founder does not have permission to reject this investment");
        }

        if (investment.getStatus() != InvestmentStatus.PENDING) {
            throw new RuntimeException("Only pending investments can be rejected");
        }

        investment.setStatus(InvestmentStatus.REJECTED);
        investmentRepository.save(investment);
        return modelMapper.map(investment, InvestmentResponse.class);
    }

    @Override
    public InvestmentResponse completeInvestment(Long id, Long founderId) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(()-> new InvestmentNotFoundException("Investment not found with id: "+id));

        if (!investment.getFounderId().equals(founderId)) {
            throw new RuntimeException("Founder does not have permission to complete this investment");
        }

        if (investment.getStatus() != InvestmentStatus.APPROVED) {
            throw new RuntimeException("Only approved investments can be completed");
        }

        investment.setStatus(InvestmentStatus.COMPLETED);
        investmentRepository.save(investment);
        return modelMapper.map(investment, InvestmentResponse.class);
    }

    @Override
    public Page<InvestmentResponse> getPendingInvestments(Pageable pageable) {
        return investmentRepository.findByStatus(InvestmentStatus.PENDING, pageable)
                .map(investment -> modelMapper.map(investment, InvestmentResponse.class));
    }
}
