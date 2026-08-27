package com.tutorapp.webapp.Bid.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BidRequest {

    @NotNull(message = "Gig ID is required")
    private Long gigId;

    @NotNull(message = "Bid amount is required")
    @Min(value = 0, message = "Bid amount must be non-negative")
    private Double bidAmount;

    // Getters and setters
    public Long getGigId() { return gigId; }
    public void setGigId(Long gigId) { this.gigId = gigId; }

    public Double getBidAmount() { return bidAmount; }
    public void setBidAmount(Double bidAmount) { this.bidAmount = bidAmount; }
}
