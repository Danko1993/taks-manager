package com.example.taskmanager.unit;

import com.example.taskmanager.config.JwtService;
import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.RegisterRequest;
import com.example.taskmanager.exception.*;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.VerificationToken;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.repository.VerificationTokenRepository;
import com.example.taskmanager.service.AuthService;
import com.example.taskmanager.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock VerificationTokenRepository verificationTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock EmailService emailService;

    @InjectMocks AuthService authService;

    // REGISTER
    @Test
    void register_shouldSaveUserAndSendEmail() {
        RegisterRequest request = new RegisterRequest("test@test.com", "Password1!");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(verificationTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("test@test.com"), anyString());
    }

    @Test
    void register_shouldThrowWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("test@test.com", "Password1!");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    // VERIFY
    @Test
    void verify_shouldEnableUser() {
        UUID token = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setUsed(false);
        vt.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(vt));

        authService.verify(token);

        assertThat(user.isEnabled()).isTrue();
        assertThat(vt.isUsed()).isTrue();
    }

    @Test
    void verify_shouldThrowWhenTokenExpired() {
        UUID token = UUID.randomUUID();
        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUsed(false);
        vt.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(vt));

        assertThatThrownBy(() -> authService.verify(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verify_shouldThrowWhenTokenAlreadyUsed() {
        UUID token = UUID.randomUUID();
        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUsed(true);
        vt.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(vt));

        assertThatThrownBy(() -> authService.verify(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    // LOGIN
    @Test
    void login_shouldReturnJwt() {
        LoginRequest request = new LoginRequest("test@test.com", "Password1!");
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPasswordHash("hashed");
        user.setEnabled(true);
        user.setFailedLoginAttempts(0);
        user.setTokenVersion(0);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "hashed")).thenReturn(true);
        when(jwtService.generateToken(any(), anyInt())).thenReturn("jwt-token");

        String result = authService.login(request);

        assertThat(result).isEqualTo("jwt-token");
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        LoginRequest request = new LoginRequest("test@test.com", "Password1!");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_shouldThrowWhenAccountNotVerified() {
        LoginRequest request = new LoginRequest("test@test.com", "Password1!");
        User user = new User();
        user.setEnabled(false);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AccountNotVerifiedException.class);
    }

    @Test
    void login_shouldThrowWhenAccountLocked() {
        LoginRequest request = new LoginRequest("test@test.com", "Password1!");
        User user = new User();
        user.setEnabled(true);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(20));

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    void login_shouldIncrementFailedAttemptsOnWrongPassword() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongpass");
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEnabled(true);
        user.setPasswordHash("hashed");
        user.setFailedLoginAttempts(0);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        verify(userRepository).save(user);
    }

    @Test
    void login_shouldLockAccountAfter5FailedAttempts() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongpass");
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEnabled(true);
        user.setPasswordHash("hashed");
        user.setFailedLoginAttempts(4);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(user.getLockedUntil()).isNotNull();
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now());
    }
}