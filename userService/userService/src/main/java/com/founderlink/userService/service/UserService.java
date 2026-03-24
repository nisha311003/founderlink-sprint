package com.founderlink.userService.service;

import com.founderlink.userService.dto.UserProfileRequest;
import com.founderlink.userService.dto.UserProfileResponse;
import com.founderlink.userService.entity.UserProfile;
import com.founderlink.userService.exception.UserNotFoundException;
import com.founderlink.userService.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final ModelMapper modelMapper;

    public UserProfileResponse createProfile(Long userId, String email, String role, UserProfileRequest request){
        if(userProfileRepository.existsById(userId)){
            throw new RuntimeException("Profile already exists for this user");
        }

        UserProfile profile = modelMapper.map(request, UserProfile.class);

        profile.setUserId(userId);
        profile.setEmail(email);
        profile.setRole(role);

        userProfileRepository.save(profile);

        return modelMapper.map(profile, UserProfileResponse.class);

    }

    public UserProfileResponse getProfile(Long userId) throws UserNotFoundException {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("No profile found with id: "+userId));
        return modelMapper.map(profile, UserProfileResponse.class);
    }

    public UserProfileResponse updateProfile(Long userId, UserProfileRequest request) throws UserNotFoundException {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("User not found with id: "+userId));

        modelMapper.map(request, profile);

        profile.setUserId(userId);
        profile.setEmail(profile.getEmail());
        profile.setRole(profile.getRole());

        userProfileRepository.save(profile);
        return modelMapper.map(profile, UserProfileResponse.class);

    }
    public Page<UserProfileResponse> getAllProfiles(Pageable pageable){
        return userProfileRepository.findAll(pageable)
                .map(profile ->
                        modelMapper.map(profile, UserProfileResponse.class));
    }

}
