package com.founderlink.authService.service;

import com.founderlink.authService.dtos.AuthResponse;
import com.founderlink.authService.dtos.LoginRequest;
import com.founderlink.authService.dtos.RegisterRequest;
import com.founderlink.authService.entity.Role;
import com.founderlink.authService.entity.User;
import com.founderlink.authService.exception.InvalidCredentialsException;
import com.founderlink.authService.repository.UserRepository;
import com.founderlink.authService.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;

    @Override
    public String register(RegisterRequest request){
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email already registered");
        }
        Role role;
        try{
            String rawRole = request.getRole() == null ? "ROLE_FOUNDER": request.getRole().toUpperCase();
            if(!rawRole.startsWith("ROLE_")) rawRole = "ROLE_"+rawRole;
            role = Role.valueOf(rawRole);
        }catch (IllegalArgumentException e){
            throw new RuntimeException("Invalid role.");
        }
        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        userRepository.save(user);

        return "User registered successfully";
    }
    @Override
    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("No account found with this email"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new InvalidCredentialsException("Incorrect Password");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}
