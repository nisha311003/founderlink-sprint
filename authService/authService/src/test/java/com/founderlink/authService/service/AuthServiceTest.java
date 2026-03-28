package com.founderlink.authService.service;

import com.founderlink.authService.dtos.AuthResponse;
import com.founderlink.authService.dtos.LoginRequest;
import com.founderlink.authService.dtos.RegisterRequest;
import com.founderlink.authService.entity.Role;
import com.founderlink.authService.entity.User;
import com.founderlink.authService.exception.InvalidCredentialsException;
import com.founderlink.authService.repository.UserRepository;
import com.founderlink.authService.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldPersistUserWithEncodedPasswordAndNormalizedRole() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPassword("plainPassword");
        request.setRole("founder");

        User mappedUser = User.builder().name("Alice").email("alice@example.com").build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(modelMapper.map(request., User.class)).thenReturn(mappedUser);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        String result = authService.register(request);

        assertEquals("User registered successfully", result);
        assertEquals("encodedPassword", mappedUser.getPassword());
        assertEquals(Role.ROLE_FOUNDER, mappedUser.getRole());
        verify(userRepository).save(mappedUser);
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alice@example.com");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(User.builder().email(request.getEmail()).build()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));

        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("plainPassword");

        User user = User.builder()
                .id(1L)
                .email("alice@example.com")
                .password("encodedPassword")
                .role(Role.ROLE_FOUNDER)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId())).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals("ROLE_FOUNDER", response.getRole());
    }

    @Test
    void login_shouldThrowWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("wrongPassword");

        User user = User.builder()
                .email("alice@example.com")
                .password("encodedPassword")
                .role(Role.ROLE_FOUNDER)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_shouldThrowWhenUserIsMissing() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(request));
    }
}
