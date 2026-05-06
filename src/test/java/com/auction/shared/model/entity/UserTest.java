package com.auction.shared.model.entity;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Kiểm tra Role của người mua (Bidder)")
    void testBidderRole() {
        Bidder bidder = new Bidder("bidder", "password123", "bidder@gmail.com");
        assertEquals("BIDDER", bidder.getRole());
        // Kiểm tra logic login kế thừa từ lớp User
        assertTrue(bidder.login("bidder", "password123"));
    }

    @Test
    @DisplayName("Kiểm tra Role của người bán (Seller)")
    void testSellerRole() {
        Seller seller = new Seller("seller", "pass456", "seller@gmail.com");
        assertEquals("SELLER", seller.getRole());
        assertFalse(seller.login("seller", "wrong_pass"));
    }
}