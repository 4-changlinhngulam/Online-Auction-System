package com.auction.client.util;

import com.auction.shared.model.entity.User;

/** Singleton – Lưu thông tin phiên đăng nhập (user, token). */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;
    private String token;
    private com.auction.shared.model.entity.Auction currentAuction;

    private SessionManager() {
        // Constructor private để đảm bảo tính chất Singleton
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User u) {
        this.currentUser = u;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String t) {
        this.token = t;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
        token = null;
        currentAuction = null;
    }

    public com.auction.shared.model.entity.Auction getCurrentAuction() {
        return currentAuction;
    }

    public void setCurrentAuction(com.auction.shared.model.entity.Auction currentAuction) {
        this.currentAuction = currentAuction;
    }
}
