package com.example.taskmanager.service;

import com.example.taskmanager.config.JwtService;
import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.RegisterRequest;
import com.example.taskmanager.exception.*;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.VerificationToken;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;


    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())){
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID());
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(
                user.getEmail(),
                frontendUrl + "/verify?token=" + verificationToken.getToken()
        );
    }

    @Transactional
    public void verify(UUID token) {
        VerificationToken verificationToken = verificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new InvalidTokenException());

        if (verificationToken.isUsed() ||
                verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException();
        }

        verificationToken.setUsed(true);
        verificationToken.getUser().setEnabled(true);

        verificationTokenRepository.save(verificationToken);
        userRepository.save(verificationToken.getUser());
    }


    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!user.isAccountNonLocked()) {
            throw new AccountLockedException();
        }

        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(5));
            }
            userRepository.save(user);
            throw new InvalidCredentialsException();
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        return jwtService.generateToken(user.getId(), user.getTokenVersion());
    }


    public void logoutAllDevices(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow();
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }
}
