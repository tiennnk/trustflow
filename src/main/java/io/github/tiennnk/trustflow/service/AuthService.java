package io.github.tiennnk.trustflow.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.tiennnk.trustflow.dto.AuthResponse;
import io.github.tiennnk.trustflow.dto.LoginRequest;
import io.github.tiennnk.trustflow.dto.RegisterRequest;
import io.github.tiennnk.trustflow.entity.Role;
import io.github.tiennnk.trustflow.entity.User;
import io.github.tiennnk.trustflow.exception.CustomException;
import io.github.tiennnk.trustflow.repository.UserRepository;
import io.github.tiennnk.trustflow.security.JwtService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(HttpStatus.CONFLICT, "Email is exist!");
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()), Role.USER);

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(HttpStatus.CONFLICT, "Email is exist!");
        }

        return issueToken(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "Email or password is invalid!"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Email or password is invalid!");
        }

        return issueToken(user);
    }

    private AuthResponse issueToken(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }
}
