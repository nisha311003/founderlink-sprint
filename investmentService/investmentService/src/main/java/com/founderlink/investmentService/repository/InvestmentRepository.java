package com.founderlink.investmentService.repository;

import com.founderlink.investmentService.entity.Investment;
import com.founderlink.investmentService.entity.InvestmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    // get all investments for a startup
    Page<Investment> findByStartupId(
            Long startupId, Pageable pageable);

    // get all investments by an investor
    Page<Investment> findByInvestorId(
            Long investorId, Pageable pageable);

    // get all investments for a founder
    Page<Investment> findByFounderId(
            Long founderId, Pageable pageable);

    // get investments by status
    Page<Investment> findByStatus(
            InvestmentStatus status, Pageable pageable);

    // get investments by investor and status
    Page<Investment> findByInvestorIdAndStatus(
            Long investorId,
            InvestmentStatus status,
            Pageable pageable);
}
