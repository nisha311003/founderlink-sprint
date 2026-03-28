package com.founderlink.investmentService.service;

import com.founderlink.investmentService.dto.InvestmentRequest;
import com.founderlink.investmentService.dto.InvestmentResponse;
import com.founderlink.investmentService.entity.Investment;
import com.founderlink.investmentService.entity.InvestmentStatus;
import com.founderlink.investmentService.exception.InvestmentNotFoundException;
import com.founderlink.investmentService.repository.InvestmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private InvestmentService investmentService;

    @Test
    void createInvestment_shouldSavePendingInvestment() {
        InvestmentRequest request = new InvestmentRequest();
        request.setStartupId(10L);
        request.setStartupName("FounderLink");
        request.setFounderId(20L);
        request.setAmount(50000.0);
        request.setNote("Seed round");

        InvestmentResponse response = InvestmentResponse.builder()
                .startupId(10L)
                .investorId(1L)
                .status(InvestmentStatus.PENDING)
                .build();

        when(modelMapper.map(any(Investment.class), any(Class.class))).thenReturn(response);

        InvestmentResponse result = investmentService.createInvestment(1L, "investor@example.com", request);

        assertEquals(InvestmentStatus.PENDING, result.getStatus());
        verify(investmentRepository).save(any(Investment.class));
    }

    @Test
    void getInvestment_shouldThrowWhenMissing() {
        when(investmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InvestmentNotFoundException.class, () -> investmentService.getInvestment(99L));
    }

    @Test
    void approveInvestment_shouldUpdateStatus_whenFounderMatchesAndPending() {
        Investment investment = Investment.builder()
                .id(1L)
                .founderId(20L)
                .status(InvestmentStatus.PENDING)
                .build();
        InvestmentResponse response = InvestmentResponse.builder()
                .id(1L)
                .status(InvestmentStatus.APPROVED)
                .build();

        when(investmentRepository.findById(1L)).thenReturn(Optional.of(investment));
        when(modelMapper.map(investment, InvestmentResponse.class)).thenReturn(response);

        InvestmentResponse result = investmentService.approveInvestment(1L, 20L);

        assertEquals(InvestmentStatus.APPROVED, investment.getStatus());
        assertEquals(InvestmentStatus.APPROVED, result.getStatus());
        verify(investmentRepository).save(investment);
    }

    @Test
    void completeInvestment_shouldRejectWhenStatusIsNotApproved() {
        Investment investment = Investment.builder()
                .id(1L)
                .founderId(20L)
                .status(InvestmentStatus.PENDING)
                .build();

        when(investmentRepository.findById(1L)).thenReturn(Optional.of(investment));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> investmentService.completeInvestment(1L, 20L));

        assertEquals("Only approved investments can be completed", exception.getMessage());
    }
}
