package io.github.tiennnk.trustflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.tiennnk.trustflow.dto.AuthResponse;
import io.github.tiennnk.trustflow.dto.LoginRequest;
import io.github.tiennnk.trustflow.dto.RegisterRequest;
import io.github.tiennnk.trustflow.entity.Role;
import io.github.tiennnk.trustflow.entity.User;
import io.github.tiennnk.trustflow.exception.CustomException;
import io.github.tiennnk.trustflow.repository.UserRepository;
import io.github.tiennnk.trustflow.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_newEmail_returnsToken() {
        RegisterRequest request = new RegisterRequest("testnew@gmail.com", "12345678");

        when(userRepository.existsByEmail("testnew@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("hashedPassword");
        when(jwtService.generateToken(any(), eq("testnew@gmail.com"), eq("USER"))).thenReturn("token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.register(request);

        assertEquals("token", response.accessToken());
        assertEquals(3600L, response.expiresInSeconds());
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void register_existingEmail_throwsConflict() {
        RegisterRequest request = new RegisterRequest("testexisting@gmail.com", "12345678");

        when(userRepository.existsByEmail("testexisting@gmail.com")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class, () -> authService.register(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void register_raceCondition_throwsConflict() {
        RegisterRequest request = new RegisterRequest("testduplicateinsert@gmail.com", "12345678");

        when(userRepository.existsByEmail("testduplicateinsert@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("hashedPassword");
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        CustomException exception = assertThrows(CustomException.class, () -> authService.register(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void login_correctCredentials_returnsToken() {
        LoginRequest request = new LoginRequest("testuser@gmail.com", "12345678");
        User user = new User("testuser@gmail.com", "correctHash", Role.USER);

        when(userRepository.findByEmail("testuser@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("12345678", "correctHash")).thenReturn(true);
        when(jwtService.generateToken(any(), eq("testuser@gmail.com"), eq("USER"))).thenReturn("token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(request);

        assertEquals("token", response.accessToken());
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        LoginRequest request = new LoginRequest("testuser@gmail.com", "wrongPassword");
        User user = new User("testuser@gmail.com", "correctHash", Role.USER);

        when(userRepository.findByEmail("testuser@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "correctHash")).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void login_nonExistentEmail_throwsUnauthorized() {
        LoginRequest request = new LoginRequest("testnobody@gmail.com", "12345678");

        when(userRepository.findByEmail("testnobody@gmail.com")).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }
}
