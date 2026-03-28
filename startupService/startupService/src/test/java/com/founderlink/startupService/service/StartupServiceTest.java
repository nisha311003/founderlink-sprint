package com.founderlink.startupService.service;

import com.founderlink.startupService.dto.StartupRequest;
import com.founderlink.startupService.dto.StartupResponse;
import com.founderlink.startupService.entity.Startup;
import com.founderlink.startupService.entity.StartupStage;
import com.founderlink.startupService.exception.StartupNotFoundException;
import com.founderlink.startupService.repository.StartupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartupServiceTest {

    @Mock
    private StartupRepository startupRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private StartupService startupService;

    @Test
    void createStartup_shouldPersistStartupWithFounderAndStage() {
        StartupRequest request = new StartupRequest();
        request.setName("FounderLink");
        request.setDescription("Platform");
        request.setIndustry("Tech");
        request.setProblemStatement("Problem");
        request.setSolution("Solution");
        request.setFundingGoal(100000.0);
        request.setStage("mvp");

        Startup startup = Startup.builder().name("FounderLink").build();
        StartupResponse response = StartupResponse.builder()
                .name("FounderLink")
                .stage("MVP")
                .founderId(1L)
                .approved(false)
                .build();

        when(modelMapper.map(request, Startup.class)).thenReturn(startup);
        when(modelMapper.map(startup, StartupResponse.class)).thenReturn(response);

        StartupResponse result = startupService.createStartup(1L, "founder@example.com", request);

        assertEquals(1L, startup.getFounderId());
        assertEquals("founder@example.com", startup.getFounderEmail());
        assertEquals(StartupStage.MVP, startup.getStage());
        assertFalse(startup.isApproved());
        assertEquals("FounderLink", result.getName());
        verify(startupRepository).save(startup);
    }

    @Test
    void createStartup_shouldRejectInvalidStage() {
        StartupRequest request = new StartupRequest();
        request.setStage("wrong-stage");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> startupService.createStartup(1L, "founder@example.com", request));

        assertEquals("Invalid stage: Use: IDEA, MVP, EARLY_TRACTION, SCALING", exception.getMessage());
    }

    @Test
    void deleteStartup_shouldRejectWhenFounderDoesNotOwnStartup() {
        Startup startup = Startup.builder().id(1L).founderId(2L).build();
        when(startupRepository.findById(1L)).thenReturn(Optional.of(startup));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> startupService.deleteStartup(1L, 99L));

        assertEquals("You can only delete your own startup", exception.getMessage());
    }

    @Test
    void getStartup_shouldThrowWhenMissing() {
        when(startupRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(StartupNotFoundException.class, () -> startupService.getStartup(50L));
    }
}
