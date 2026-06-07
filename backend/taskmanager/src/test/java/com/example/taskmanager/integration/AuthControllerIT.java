package com.example.taskmanager.integration;

import com.example.taskmanager.model.VerificationToken;
import com.example.taskmanager.repository.VerificationTokenRepository;
import com.example.taskmanager.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired VerificationTokenRepository verificationTokenRepository;

    @MockBean EmailService emailService;

    @Test
    void register_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "test@test.com",
                            "password": "Password1!"
                        }
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    void register_shouldReturn409WhenEmailExists() throws Exception {
        String body = """
                {
                    "email": "test@test.com",
                    "password": "Password1!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void register_shouldReturn400WhenInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "notanemail",
                            "password": "Password1!"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400WhenWeakPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "test@test.com",
                            "password": "weak"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn401WhenNotVerified() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "test@test.com",
                            "password": "Password1!"
                        }
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "test@test.com",
                            "password": "Password1!"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn401WhenWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "test@test.com",
                            "password": "Password1!"
                        }
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "test@test.com",
                            "password": "WrongPass1!"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldClearCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void fullAuthFlow_shouldRegisterVerifyLoginLogout() throws Exception {
        // register
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "test@test.com",
                            "password": "Password1!"
                        }
                        """))
                .andExpect(status().isCreated());

        // pobierz token z bazy
        VerificationToken vt = verificationTokenRepository.findAll().get(0);

        // verify
        mockMvc.perform(get("/api/auth/verify")
                        .param("token", vt.getToken().toString()))
                .andExpect(status().isOk());

        // login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "test@test.com",
                            "password": "Password1!"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"));

        // logout
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"));
    }
}