package com.tutorapp.webapp.Bid;

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
class BidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void placeBidWithoutAuthFails() throws Exception {
        mockMvc.perform(post("/api/auth/placebid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gigId\":1,\"bidAmount\":50.0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void placeBidWithInvalidDataFails() throws Exception {
        mockMvc.perform(post("/api/auth/placebid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bidAmount\":50.0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void withdrawBidWithoutAuthFails() throws Exception {
        mockMvc.perform(delete("/api/auth/withdrawbid/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBidWithoutAuthFails() throws Exception {
        mockMvc.perform(delete("/api/auth/deletebids/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentPlacedBidWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/auth/studentplacedbid"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBidsByStudentIdWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/auth/bids/student/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void completeBidWithoutAuthFails() throws Exception {
        mockMvc.perform(put("/api/auth/bids/1/complete"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateBidStatusWithoutAuthFails() throws Exception {
        mockMvc.perform(put("/api/auth/bids/1/status")
                        .param("status", "ACCEPTED"))
                .andExpect(status().isForbidden());
    }
}
