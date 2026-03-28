package com.founderlink.startupService.service;

import com.founderlink.startupService.dto.StartupRequest;
import com.founderlink.startupService.dto.StartupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IStartupService {

    StartupResponse createStartup(Long founderId, String founderEmail, StartupRequest request);

    StartupResponse getStartup(Long id);

    Page<StartupResponse> getAllStartups(String industry, String stage, Pageable pageable);

    StartupResponse updateStartup(Long id, Long founderId, StartupRequest request);

    String deleteStartup(Long id, Long founderId);

    StartupResponse approveStartup(Long id);

    Page<StartupResponse> getStartupsByFounder(Long founderId, Pageable pageable);

    Page<StartupResponse> getAllApprovedStartups(
            String industry,
            String stage,
            Pageable pageable);

    Page<StartupResponse> getPendingStartups(Pageable pageable);
}
