package com.tutorapp.webapp.Wishlist.DTO;

public class WishlistRequestDTO {
    
       private Long userId;
    private Long gigId;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGigId() {
        return gigId;
    }
    public void setGigId(Long gigId) {
        this.gigId = gigId;
    }
}
