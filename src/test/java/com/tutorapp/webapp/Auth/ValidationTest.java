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
class ValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginWithBlankUsernameReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"encryptedPassword\":\"test\",\"passwordKeyId\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void loginWithBlankPasswordReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"encryptedPassword\":\"\",\"passwordKeyId\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.encryptedPassword").exists());
    }

    @Test
    void loginWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void placeBidWithoutAuthReturns403() throws Exception {
        mockMvc.perform(post("/api/auth/placebid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gigId\":1,\"bidAmount\":50.0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void placeBidWithNullGigIdWithoutAuthReturns403() throws Exception {
        mockMvc.perform(post("/api/auth/placebid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bidAmount\":50.0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void healthEndpointReturnsValidJson() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void passwordKeyEndpointReturnsValidStructure() throws Exception {
        mockMvc.perform(get("/api/auth/password-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyId").isString())
                .andExpect(jsonPath("$.publicKey").isString())
                .andExpect(jsonPath("$.algorithm").value("RSA-OAEP-256"));
    }
}
