package com.founderlink.userService.service;

import com.founderlink.userService.dto.UserProfileRequest;
import com.founderlink.userService.dto.UserProfileResponse;
import com.founderlink.userService.entity.UserProfile;
import com.founderlink.userService.exception.UserNotFoundException;
import com.founderlink.userService.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createProfile_shouldSaveAndReturnMappedResponse_whenProfileDoesNotExist() {
        Long userId = 1L;
        String email = "founder@example.com";
        String role = "ROLE_FOUNDER";

        UserProfileRequest request = new UserProfileRequest();
        request.setName("Founder");
        request.setBio("Building products");

        UserProfile profile = UserProfile.builder().name("Founder").bio("Building products").build();
        UserProfileResponse response = UserProfileResponse.builder()
                .userId(userId)
                .name("Founder")
                .email(email)
                .role(role)
                .bio("Building products")
                .build();

        when(userProfileRepository.existsById(userId)).thenReturn(false);
        when(modelMapper.map(request, UserProfile.class)).thenReturn(profile);
        when(modelMapper.map(profile, UserProfileResponse.class)).thenReturn(response);

        UserProfileResponse result = userService.createProfile(userId, email, role, request);

        assertEquals(userId, result.getUserId());
        assertEquals(email, result.getEmail());
        assertEquals(role, result.getRole());
        verify(userProfileRepository).save(profile);
    }

    @Test
    void getProfile_shouldReturnMappedResponse_whenUserExists() throws UserNotFoundException {
        Long userId = 1L;
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .name("Founder")
                .email("founder@example.com")
                .build();
        UserProfileResponse response = UserProfileResponse.builder()
                .userId(userId)
                .name("Founder")
                .email("founder@example.com")
                .build();

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(modelMapper.map(profile, UserProfileResponse.class)).thenReturn(response);

        UserProfileResponse result = userService.getProfile(userId);

        assertEquals(userId, result.getUserId());
        assertEquals("Founder", result.getName());
        verify(userProfileRepository).findById(userId);
    }

    @Test
    void getProfile_shouldThrowException_whenUserDoesNotExist() {
        Long userId = 99L;
        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getProfile(userId)
        );

        assertEquals("No profile found with id: 99", exception.getMessage());
        verify(userProfileRepository).findById(userId);
    }
}
