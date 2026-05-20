package com.auction.server.service;

import com.auction.server.dao.UserDAO;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.User;
import com.auction.shared.protocol.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        // Tự tiêm Mock vào Service qua Constructor
        userService = new UserService(userDAO);
    }

    // ==========================================
    // TEST LOGIN
    // ==========================================
    @Test
    @DisplayName("Đăng nhập: Thành công")
    void testLogin_Success() throws Exception {
        User mockUser = new Bidder("testuser", org.mindrot.jbcrypt.BCrypt.hashpw("password123", org.mindrot.jbcrypt.BCrypt.gensalt()), "test@gmail.com");
        when(userDAO.findByUsername("testuser")).thenReturn(mockUser);

        Response response = userService.login("testuser", "password123");

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertNull(((User) response.getData()).getPassword(), "Mật khẩu phải bị che (null) trước khi gửi về client");
    }

    @Test
    @DisplayName("Đăng nhập: Thất bại do bỏ trống thông tin")
    void testLogin_Fail_EmptyCredentials() throws Exception {
        Response response = userService.login("", "  ");
        assertFalse(response.isSuccess());
        assertEquals("Tên đăng nhập và mật khẩu không được để trống.", response.getMessage());
    }

    // ==========================================
    // TEST REGISTER
    // ==========================================
    @Test
    @DisplayName("Đăng ký: Thành công")
    void testRegister_Success() {
        User newUser = new Bidder("newuser", "pass123", "new@gmail.com");

        // Giả lập DB chưa có user này
        when(userDAO.findByUsername("newuser")).thenReturn(null);
        doNothing().when(userDAO).save(any(User.class));

        Response response = userService.register(newUser);

        assertTrue(response.isSuccess());
        assertEquals("Đăng ký tài khoản thành công.", response.getMessage());
        assertNotNull(((User) response.getData()).getId(), "Hệ thống phải tự tạo UUID nếu ID trống");
    }

    @Test
    @DisplayName("Đăng ký: Thất bại do trùng Username")
    void testRegister_Fail_DuplicateUsername() {
        User existingUser = new Bidder("testuser", "oldpass", "old@gmail.com");
        User newUser = new Bidder("testuser", "newpass", "new@gmail.com");

        // Giả lập DB đã có người dùng tên testuser
        when(userDAO.findByUsername("testuser")).thenReturn(existingUser);

        Response response = userService.register(newUser);

        assertFalse(response.isSuccess());
        assertEquals("Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.", response.getMessage());
        verify(userDAO, never()).save(any(User.class)); // Đảm bảo không lưu xuống DB
    }

    // ==========================================
    // TEST GET USER BY ID
    // ==========================================
    @Test
    @DisplayName("Lấy User bằng ID: Thành công")
    void testGetUserById_Success() {
        User mockUser = new Bidder("testuser", "pass123", "test@gmail.com");
        when(userDAO.findById("USR_01")).thenReturn(mockUser);

        Response response = userService.getUserById("USR_01");

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Lấy User bằng ID: Thất bại do ID rỗng")
    void testGetUserById_Fail_EmptyId() {
        Response response = userService.getUserById("");
        assertFalse(response.isSuccess());
        assertEquals("ID người dùng không được để trống.", response.getMessage());
    }
}