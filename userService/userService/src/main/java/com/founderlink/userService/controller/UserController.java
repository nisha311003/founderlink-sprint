package com.founderlink.userService.controller;


import com.founderlink.userService.dto.UserProfileRequest;
import com.founderlink.userService.dto.UserProfileResponse;
import com.founderlink.userService.exception.UserNotFoundException;
import com.founderlink.userService.service.IUserService;
import com.founderlink.userService.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@RestController
@RequestMapping({"/api/users", "/api/users/"})
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PostMapping("/create")
    public ResponseEntity<UserProfileResponse> createProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody UserProfileRequest request
            ){
        System.out.println("USER ID: " + userId);
        System.out.println("EMAIL: " + email);
        System.out.println("ROLE: " + role);
        return ResponseEntity.ok(userService.createProfile(userId, email, role, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long id) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getProfile(id));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_FOUNDER') or "+
            "hasAuthority('ROLE_INVESTOR') or "+
            "hasAuthority('ROLE_COFOUNDER')")
    public ResponseEntity<UserProfileResponse> updateProfile(@PathVariable Long id, @RequestBody UserProfileRequest request) throws UserNotFoundException {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<UserProfileResponse>> getAllProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction

    ){
        Sort sort = direction.equalsIgnoreCase("desc")? Sort.by(sortBy).descending():
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(userService.getAllProfiles(pageable));

    }

}
