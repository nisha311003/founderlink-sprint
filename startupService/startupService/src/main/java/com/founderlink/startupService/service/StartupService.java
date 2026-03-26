package com.founderlink.startupService.service;

import com.founderlink.startupService.dto.StartupRequest;
import com.founderlink.startupService.dto.StartupResponse;
import com.founderlink.startupService.entity.Startup;
import com.founderlink.startupService.entity.StartupStage;
import com.founderlink.startupService.exception.StartupNotFoundException;
import com.founderlink.startupService.repository.StartupRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StartupService {

    private final StartupRepository startupRepository;
    private final ModelMapper modelMapper;

    public StartupResponse createStartup(Long founderId, String founderEmail, StartupRequest request){
        StartupStage stage;
        try{
            stage = StartupStage.valueOf(request.getStage().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid stage: Use: IDEA, MVP, EARLY_TRACTION, SCALING"
            );
        }
        Startup startup = modelMapper.map(request, Startup.class);
        startup.setFounderId(founderId);
        startup.setFounderEmail(founderEmail);
        startup.setStage(stage);
        startup.setApproved(false);

        startupRepository.save(startup);

        return modelMapper.map(startup, StartupResponse.class);
    }

    public StartupResponse getStartup(Long id){
        Startup startup = startupRepository.findById(id)
                .orElseThrow(()-> new StartupNotFoundException("Startup not found with id: "+id));
        return modelMapper.map(startup, StartupResponse.class);
    }

    public Page<StartupResponse> getAllStartups(String industry, String stage, Pageable pageable){
        Page<Startup> startups;
        if(industry != null && stage != null){
            StartupStage startupStage = StartupStage.valueOf(stage.toUpperCase());
            startups = startupRepository.findByIndustryContainingIgnoreCaseAndStage(industry, startupStage, pageable);
        }else if(industry != null){
            startups = startupRepository.findByIndustryContainingIgnoreCase(industry, pageable);
        }
        else if(stage != null){
            StartupStage startupStage = StartupStage.valueOf(stage.toUpperCase());
            startups = startupRepository.findByStage(startupStage, pageable);
        }else{
            startups = startupRepository.findAll(pageable);
        }
        return startups.map(startup -> modelMapper.map(startup, StartupResponse.class));
    }
    public StartupResponse updateStartup(Long id, Long founderId, StartupRequest request){
        Startup startup = startupRepository.findById(id)
                .orElseThrow(()-> new StartupNotFoundException(("Startup not found with id: "+id)));

        if(!startup.getFounderId().equals(founderId)){
            throw new RuntimeException("You can only update your own startup");
        }

        modelMapper.map(request, startup);
        startup.setFounderId(founderId);

        if(request.getStage() != null){
            startup.setStage(StartupStage.valueOf(request.getStage().toUpperCase()));
        }
        startupRepository.save(startup);
        return modelMapper.map(startup, StartupResponse.class);

    }

    public String deleteStartup(Long id, Long founderId){
        Startup startup = startupRepository.findById(id)
                .orElseThrow(()-> new StartupNotFoundException(("Startup not found with id: "+id)));

        if(!startup.getFounderId().equals(founderId)){
            throw new RuntimeException("You can only delete your own startup");
        }

        startupRepository.delete(startup);
        return "Startup deleted successfully";
    }

    public StartupResponse approveStartup(Long id){
        Startup startup = startupRepository.findById(id)
                .orElseThrow(()-> new StartupNotFoundException(("Startup not found with id: "+id)));

        startup.setApproved(true);
        startupRepository.save(startup);
        return modelMapper.map(startup, StartupResponse.class);

    }

    public Page<StartupResponse> getStartupsByFounder(Long founderId, Pageable pageable){
        return startupRepository.findByFounderId(founderId, pageable)
                .map(startup -> modelMapper.map(startup, StartupResponse.class));
    }

    public Page<StartupResponse> getAllApprovedStartups(
            String industry,
            String stage,
            Pageable pageable) {

        Page<Startup> startups;

        if (industry != null && stage != null) {
            StartupStage startupStage = StartupStage.valueOf(
                    stage.toUpperCase());
            startups = startupRepository
                    .findByApprovedTrueAndIndustryContainingIgnoreCaseAndStage(
                            industry, startupStage, pageable);

        } else if (industry != null) {
            startups = startupRepository
                    .findByApprovedTrueAndIndustryContainingIgnoreCase(
                            industry, pageable);

        } else if (stage != null) {
            StartupStage startupStage = StartupStage.valueOf(
                    stage.toUpperCase());
            startups = startupRepository
                    .findByApprovedTrueAndStage(startupStage, pageable);

        } else {
            startups = startupRepository.findByApprovedTrue(pageable);
        }

        return startups.map(startup ->
                modelMapper.map(startup, StartupResponse.class));
    }

    public Page<StartupResponse> getPendingStartups(Pageable pageable) {
        return startupRepository.findByApprovedFalse(pageable)
                .map(startup ->
                        modelMapper.map(startup, StartupResponse.class));
    }

}
