package com.auction.shared.model.entity;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    @Test
    @DisplayName("Admin phải có đúng Role và thông tin khởi tạo")
    void testAdminInitialization() {
        Admin admin = new Admin("admin_root", "root123", "admin@auction.com");
        assertEquals("ADMIN", admin.getRole());
        assertEquals("admin_root", admin.getUsername());
    }
}