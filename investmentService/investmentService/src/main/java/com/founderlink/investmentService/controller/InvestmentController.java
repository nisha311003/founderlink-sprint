package com.founderlink.investmentService.controller;

import com.founderlink.investmentService.dto.InvestmentRequest;
import com.founderlink.investmentService.dto.InvestmentResponse;
import com.founderlink.investmentService.service.IInvestmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/investments", "/api/investments/"})
@RequiredArgsConstructor
@Tag(name = "Investment", description = "Investment management endpoints")
public class InvestmentController {

    private final IInvestmentService investmentService;

    @Operation(summary = "Create investment request",
            description = "Investor creates an investment request for a startup")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Investment created"),
            @ApiResponse(responseCode = "403", description = "Only investors can invest")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_INVESTOR')")
    public ResponseEntity<InvestmentResponse> createInvestment(
            @RequestHeader("X-User-Id") Long investorId,
            @RequestHeader("X-User-Email") String investorEmail,
            @Valid @RequestBody InvestmentRequest request) {

        return ResponseEntity.ok(investmentService.createInvestment(investorId, investorEmail, request));
    }

    @Operation(summary = "Get investment by ID")
    @GetMapping("/{id}")
    public ResponseEntity<InvestmentResponse> getInvestment(@PathVariable Long id) {
        return ResponseEntity.ok(investmentService.getInvestment(id));
    }

    @Operation(summary = "Get investments for a startup")
    @GetMapping("/startup/{startupId}")
    public ResponseEntity<Page<InvestmentResponse>> getByStartup(
            @PathVariable Long startupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(investmentService.getInvestmentsByStartup(startupId, pageable));
    }

    @Operation(summary = "Get investor portfolio")
    @GetMapping("/investor/{investorId}")
    @PreAuthorize("hasAuthority('ROLE_INVESTOR') or " +
            "hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<InvestmentResponse>> getByInvestor(
            @PathVariable Long investorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(investmentService.getInvestmentsByInvestor(investorId, pageable));
    }

    @Operation(summary = "Get investments received by founder")
    @GetMapping("/founder/{founderId}")
    @PreAuthorize("hasAuthority('ROLE_FOUNDER')")
    public ResponseEntity<Page<InvestmentResponse>> getByFounder(
            @PathVariable Long founderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(investmentService.getInvestmentsByFounder(founderId, pageable));
    }

    @Operation(summary = "Approve investment",
            description = "Founder approves a pending investment")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_FOUNDER')")
    public ResponseEntity<InvestmentResponse> approveInvestment(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long founderId) {

        return ResponseEntity.ok(investmentService.approveInvestment(id, founderId));
    }

    @Operation(summary = "Reject investment",
            description = "Founder rejects a pending investment")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_FOUNDER')")
    public ResponseEntity<InvestmentResponse> rejectInvestment(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long founderId) {

        return ResponseEntity.ok(investmentService.rejectInvestment(id, founderId));
    }

    @Operation(summary = "Complete investment",
            description = "Founder marks an approved investment as completed")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('ROLE_FOUNDER')")
    public ResponseEntity<InvestmentResponse> completeInvestment(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long founderId) {

        return ResponseEntity.ok(investmentService.completeInvestment(id, founderId));
    }

    @Operation(summary = "Get all pending investments",
            description = "Admin views all pending investment requests")
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<InvestmentResponse>> getPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return ResponseEntity.ok(investmentService.getPendingInvestments(pageable));
    }

}
