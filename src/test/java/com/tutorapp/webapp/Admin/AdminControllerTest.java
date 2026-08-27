package com.tutorapp.webapp.Admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminUsersWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminGigsWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/admin/gigs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeleteUserWithoutAuthFails() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeleteGigWithoutAuthFails() throws Exception {
        mockMvc.perform(delete("/api/admin/gigs/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminGetUserWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isForbidden());
    }
}
