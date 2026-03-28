package com.founderlink.startupService.controller;


import com.founderlink.startupService.dto.StartupRequest;
import com.founderlink.startupService.dto.StartupResponse;
import com.founderlink.startupService.service.IStartupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping({"/api/startups", "/api/startups/"})
@RequiredArgsConstructor
@Tag(name = "Startup Management", description = "APIs for managing startups")
@SecurityRequirement(name = "Bearer Authentication")
public class StartupController {

    private final IStartupService startupService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_FOUNDER')")
    @Operation(summary = "Create a new startup", description = "Allows founders to create a new startup entry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Startup created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<StartupResponse> createStartup(
            @Parameter(description = "User ID from header") @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "User email from header") @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody StartupRequest request
    ){
        return ResponseEntity.ok(startupService.createStartup(userId, email, request));
    }

    @GetMapping
    @Operation(summary = "Get all startups", description = "Retrieve a paginated list of all startups with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    })
    public ResponseEntity<Page<StartupResponse>> getAllStartups(
            @Parameter(description = "Filter by industry") @RequestParam(required = false) String industry,
            @Parameter(description = "Filter by stage") @RequestParam(required = false) String stage,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "desc") String direction
    ){
        Sort sort = direction.equalsIgnoreCase("desc")? Sort.by(sortBy).descending():
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page,size,sort);
        return ResponseEntity.ok(startupService.getAllStartups(industry,stage,pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get startup by ID", description = "Retrieve details of a specific startup")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Startup found"),
        @ApiResponse(responseCode = "404", description = "Startup not found")
    })
    public ResponseEntity<StartupResponse> getStartup(@Parameter(description = "Startup ID") @PathVariable Long id){
        return ResponseEntity.ok(startupService.getStartup(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_FOUNDER')")
    @Operation(summary = "Update startup", description = "Update an existing startup (founder only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Startup updated"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Startup not found")
    })
    public ResponseEntity<StartupResponse> updateStartup(@Parameter(description = "Startup ID") @PathVariable Long id,
                                                         @Parameter(description = "User ID from header") @RequestHeader("X-User-Id") Long userId,
                                                         @Valid @RequestBody StartupRequest request){
        return ResponseEntity.ok(startupService.updateStartup(id,userId,request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_FOUNDER')")
    @Operation(summary = "Delete startup", description = "Delete a startup (founder only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Startup deleted"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Startup not found")
    })
    public ResponseEntity<String> deleteStartup(@Parameter(description = "Startup ID") @PathVariable Long id,
                                                @Parameter(description = "User ID from header") @RequestHeader("X-User-Id") Long userId){
        return ResponseEntity.ok(startupService.deleteStartup(id,userId));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Approve startup", description = "Approve a pending startup (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Startup approved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<StartupResponse> approveStartup(@Parameter(description = "Startup ID") @PathVariable Long id){
        return ResponseEntity.ok(startupService.approveStartup(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_FOUNDER')")
    @Operation(summary = "Get my startups", description = "Retrieve startups created by the current founder")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<StartupResponse>> getMyStartups(
            @Parameter(description = "User ID from header") @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ){

        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(startupService.getStartupsByFounder(userId,pageable));
    }

    @GetMapping("/approved")
    @PreAuthorize("hasAuthority('ROLE_INVESTOR') or " +
            "hasAuthority('ROLE_COFOUNDER') or " +
            "hasAuthority('ROLE_FOUNDER')")
    @Operation(summary = "Get approved startups", description = "Retrieve approved startups with filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<StartupResponse>> getAllApprovedStartups(
            @Parameter(description = "Filter by industry") @RequestParam(required = false) String industry,
            @Parameter(description = "Filter by stage") @RequestParam(required = false) String stage,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                startupService.getAllApprovedStartups(industry, stage, pageable));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get pending startups", description = "Retrieve pending startups for approval (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List retrieved"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<StartupResponse>> getPendingStartups(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").ascending()); // oldest first
        return ResponseEntity.ok(
                startupService.getPendingStartups(pageable));
    }
}
