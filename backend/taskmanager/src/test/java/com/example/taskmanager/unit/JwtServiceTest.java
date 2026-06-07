package com.example.taskmanager.unit;


import com.example.taskmanager.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-min-32-chars-long-enough!!");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, 0);
        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void shouldExtractUserId() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, 0);
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void shouldExtractTokenVersion() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, 3);
        assertThat(jwtService.extractTokenVersion(token)).isEqualTo(3);
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertThat(jwtService.isValid("invalid.token.here")).isFalse();
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        JwtService shortLivedService = new JwtService();
        ReflectionTestUtils.setField(shortLivedService, "secret", "test-secret-min-32-chars-long-enough!!");
        ReflectionTestUtils.setField(shortLivedService, "expirationMs", -1000L);

        String token = shortLivedService.generateToken(UUID.randomUUID(), 0);
        assertThat(shortLivedService.isValid(token)).isFalse();
    }
}