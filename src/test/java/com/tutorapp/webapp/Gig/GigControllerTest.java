package com.tutorapp.webapp.Gig;

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
class GigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createGigWithoutAuthFails() throws Exception {
        mockMvc.perform(post("/api/auth/create-gig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"Math\",\"amount\":50.0,\"grade\":12}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getGigsWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/auth/gigs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchGigsWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/auth/gigs/search")
                        .param("subject", "Math"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchGigsWithEmptyParamsWorks() throws Exception {
        mockMvc.perform(get("/api/auth/gigs/search")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteGigWithoutAuthFails() throws Exception {
        mockMvc.perform(delete("/api/auth/delete-gigs/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void editGigWithoutAuthFails() throws Exception {
        mockMvc.perform(put("/api/auth/edit-gigs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"Physics\",\"amount\":60.0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getGigByIdWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/auth/gigs/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void myGigsWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/auth/my-gigs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestsWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/auth/requests/testuser"))
                .andExpect(status().isForbidden());
    }
}
