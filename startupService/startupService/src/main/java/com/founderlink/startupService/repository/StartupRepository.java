package com.founderlink.startupService.repository;

import com.founderlink.startupService.entity.Startup;
import com.founderlink.startupService.entity.StartupStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StartupRepository extends JpaRepository<Startup, Long> {

    Page<Startup> findByFounderId(Long founderId, Pageable pageable);

    Page<Startup> findByIndustryContainingIgnoreCase(String industry, Pageable pageable);

    Page<Startup> findByStage(StartupStage stage, Pageable pageable);

    Page<Startup> findByIndustryContainingIgnoreCaseAndStage(String industry, StartupStage stage, Pageable pageable);

    Page<Startup> findByApprovedTrue(Pageable pageable);

    Page<Startup> findByApprovedTrueAndIndustryContainingIgnoreCase(
            String industry, Pageable pageable);

    Page<Startup> findByApprovedTrueAndStage(
            StartupStage stage, Pageable pageable);

    Page<Startup> findByApprovedTrueAndIndustryContainingIgnoreCaseAndStage(
            String industry, StartupStage stage, Pageable pageable);

    Page<Startup> findByApprovedFalse(Pageable pageable);

}
