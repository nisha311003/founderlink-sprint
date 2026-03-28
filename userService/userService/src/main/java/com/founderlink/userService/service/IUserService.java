package com.founderlink.userService.service;

import com.founderlink.userService.dto.UserProfileRequest;
import com.founderlink.userService.dto.UserProfileResponse;
import com.founderlink.userService.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {

    public UserProfileResponse createProfile(Long userId, String email, String role, UserProfileRequest request);

    public UserProfileResponse getProfile(Long userId) throws UserNotFoundException;

    public UserProfileResponse updateProfile(Long userId, UserProfileRequest request) throws UserNotFoundException;

    public Page<UserProfileResponse> getAllProfiles(Pageable pageable);
}
