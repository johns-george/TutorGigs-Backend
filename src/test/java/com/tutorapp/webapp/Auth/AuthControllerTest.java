package com.tutorapp.webapp.Auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginEndpointExists() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"encryptedPassword\":\"test\",\"passwordKeyId\":\"test\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void passwordKeyEndpointReturnsKey() throws Exception {
        mockMvc.perform(get("/api/auth/password-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyId").exists())
                .andExpect(jsonPath("$.publicKey").exists())
                .andExpect(jsonPath("$.algorithm").value("RSA-OAEP-256"));
    }

    @Test
    void refreshTokenWithoutTokenFails() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void loginWithBlankUsernameFails() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"encryptedPassword\":\"test\",\"passwordKeyId\":\"test\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithMissingFieldsFails() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPasswordWithInvalidEmailReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/forgot/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nonexistent@test.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkUsernameEndpointWorks() throws Exception {
        mockMvc.perform(get("/api/auth/check-username/testuser"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointWithoutTokenReturns403() throws Exception {
        mockMvc.perform(get("/api/auth/my-gigs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpointWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/my-gigs")
                        .header("Authorization", "Bearer invalidtoken123"))
                .andExpect(status().isUnauthorized());
    }
}
