package com.auction.server.dao;

import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.exception.EntityNotFoundException;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDAOTest {

    @Mock
    private DatabaseConnection mockDbConnection;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseConnection> mockedStaticDb;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        userDAO = new UserDAO();

        // Giả mạo Singleton DatabaseConnection
        mockedStaticDb = mockStatic(DatabaseConnection.class);
        mockedStaticDb.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        // Bắt buộc đóng MockedStatic sau mỗi test để không bị lỗi xung đột bộ nhớ
        mockedStaticDb.close();
    }

    // ==========================================
    // TEST LƯU USER (SAVE)
    // ==========================================
    @Test
    @DisplayName("Lưu User: Thành công")
    void testSave_Success() throws Exception {
        User user = new Bidder("testuser", "pass123", "test@gmail.com");
        user.setId("USR_01");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> userDAO.save(user));

        verify(mockPreparedStatement).setString(1, "USR_01");
        verify(mockPreparedStatement).setString(2, "testuser");
        verify(mockPreparedStatement).setString(3, "pass123");
        verify(mockPreparedStatement).setString(4, "test@gmail.com");
        verify(mockPreparedStatement).setString(5, "BIDDER");
    }

    @Test
    @DisplayName("Lưu User: Thất bại do User hoặc ID rỗng")
    void testSave_Fail_NullUser() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> userDAO.save(null));
        assertEquals("User và ID không được phép null", ex1.getMessage());

        User userWithoutId = new Bidder();
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> userDAO.save(userWithoutId));
        assertEquals("User và ID không được phép null", ex2.getMessage());
    }

    // ==========================================
    // TEST TÌM THEO ID (FIND BY ID)
    // ==========================================
    @Test
    @DisplayName("Tìm User bằng ID: Thành công")
    void testFindById_Success() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Giả lập ResultSet có 1 dòng dữ liệu
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("role")).thenReturn("BIDDER");
        when(mockResultSet.getString("id")).thenReturn("USR_01");
        when(mockResultSet.getString("username")).thenReturn("testuser");
        when(mockResultSet.getString("password")).thenReturn("pass123");
        when(mockResultSet.getString("email")).thenReturn("test@gmail.com");

        User result = userDAO.findById("USR_01");

        assertNotNull(result);
        assertEquals("USR_01", result.getId());
        // DAO hiện tại đang gọi user.setName() từ trường username trong DB
        assertEquals("testuser", result.getName());
    }

    @Test
    @DisplayName("Tìm User bằng ID: Thất bại (Không tìm thấy)")
    void testFindById_Fail_NotFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Giả lập ResultSet không có dữ liệu (trả về false)
        when(mockResultSet.next()).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userDAO.findById("USR_99"));
        assertEquals("Không tìm thấy User có ID: USR_99", exception.getMessage());
    }

    // ==========================================
    // TEST CẬP NHẬT (UPDATE)
    // ==========================================
    @Test
    @DisplayName("Cập nhật User: Thành công")
    void testUpdate_Success() throws Exception {
        User user = new Bidder();
        user.setId("USR_01");
        user.setName("newname");
        user.setPassword("newpass");
        user.setEmail("new@gmail.com");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Cập nhật được 1 dòng

        assertDoesNotThrow(() -> userDAO.update(user));
    }

    @Test
    @DisplayName("Cập nhật User: Thất bại (Không tìm thấy ID)")
    void testUpdate_Fail_NotFound() throws Exception {
        User user = new Bidder();
        user.setId("USR_01");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // Cập nhật được 0 dòng

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userDAO.update(user));
        assertEquals("Không tìm thấy User có ID: USR_01 để cập nhật.", exception.getMessage());
    }

    // ==========================================
    // TEST XÓA (DELETE)
    // ==========================================
    @Test
    @DisplayName("Xóa User bằng ID: Thành công")
    void testDeleteById_Success() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> userDAO.deleteByID("USR_01"));
    }

    @Test
    @DisplayName("Xóa User bằng ID: Lỗi kết nối (Exception chung)")
    void testDeleteById_Fail_SQLException() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        // Giả lập lỗi SQL
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Mất kết nối"));

        DataPersistenceException exception = assertThrows(DataPersistenceException.class, () -> userDAO.deleteByID("USR_01"));
        assertEquals("Lỗi khi xóa User", exception.getMessage());
    }
}