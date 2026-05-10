package com.auction.server.service;

import com.auction.server.dao.UserDAO;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.User;
import com.auction.shared.protocol.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userDAO);
    }

    @Test
    @DisplayName("Đăng nhập thành công với thông tin hợp lệ")
    void testLogin_Success() throws Exception {
        // Mock dữ liệu
        User mockUser = new Bidder("testuser", "password123", "test@gmail.com");
        when(userDAO.findByUsername("testuser")).thenReturn(mockUser);

        // Thực thi
        Response response = userService.login("testuser", "password123");

        // Kiểm tra
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        verify(userDAO, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Đăng nhập thất bại do sai mật khẩu")
    void testLogin_Fail_WrongPassword() throws Exception {
        User mockUser = new Bidder("testuser", "password123", "test@gmail.com");
        when(userDAO.findByUsername("testuser")).thenReturn(mockUser);

        Response response = userService.login("testuser", "wrongpassword");

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("mật khẩu")); // Dựa trên exception AuthenticationException
    }

    @Test
    @DisplayName("Đăng nhập thất bại do tài khoản không tồn tại")
    void testLogin_Fail_UserNotFound() throws Exception {
        when(userDAO.findByUsername("unknown")).thenReturn(null);

        Response response = userService.login("unknown", "password123");

        assertFalse(response.isSuccess());
        assertNull(response.getData());
    }
}