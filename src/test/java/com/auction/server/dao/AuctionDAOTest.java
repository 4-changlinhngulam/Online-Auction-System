package com.auction.server.dao;

import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.exception.EntityNotFoundException;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.enums.AuctionStatus;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionDAOTest {

    @Mock
    private DatabaseConnection mockDbConnection;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseConnection> mockedStaticDb;
    private AuctionDAO auctionDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        auctionDAO = new AuctionDAO();

        mockedStaticDb = mockStatic(DatabaseConnection.class);
        mockedStaticDb.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        mockedStaticDb.close();
    }

    @Test
    @DisplayName("Lưu Auction: Thành công")
    void testSave_Success() throws Exception {
        Item item = new Electronics();
        item.setId("ITEM_01");

        Auction auction = new Auction();
        auction.setId("AUC_01");
        auction.setItem(item);
        auction.setCurrentPrice(100.0);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusDays(1));
        auction.setStatus(AuctionStatus.OPEN);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> auctionDAO.save(auction));

        verify(mockPreparedStatement).setString(1, "AUC_01");
        verify(mockPreparedStatement).setString(2, "ITEM_01");
        verify(mockPreparedStatement).setDouble(3, 100.0);
        verify(mockPreparedStatement).setString(7, "OPEN");
    }

    @Test
    @DisplayName("Lưu Auction: Thất bại do không có Item")
    void testSave_Fail_NoItem() throws Exception {
        Auction auction = new Auction();
        auction.setId("AUC_01");
        auction.setItem(null);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> auctionDAO.save(auction));
        assertEquals("Phiên đấu giá phải có Item đính kèm!", exception.getMessage());
    }

    @Test
    @DisplayName("Cập nhật Auction: Thành công")
    void testUpdate_Success() throws Exception {
        Item item = new Electronics();
        item.setId("ITEM_01");

        Auction auction = new Auction();
        auction.setId("AUC_01");
        auction.setItem(item);
        auction.setCurrentPrice(200.0);
        auction.setStatus(AuctionStatus.OPEN);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> auctionDAO.update(auction));

        verify(mockPreparedStatement).setString(1, "ITEM_01");
        verify(mockPreparedStatement).setDouble(2, 200.0);
        verify(mockPreparedStatement).setString(6, "OPEN");
        verify(mockPreparedStatement).setString(7, "AUC_01");
    }

    @Test
    @DisplayName("Tìm Auction bằng ID: Thành công")
    void testFindById_Success() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("id")).thenReturn("AUC_01");
        when(mockResultSet.getDouble("current_price")).thenReturn(150.0);
        when(mockResultSet.getString("status")).thenReturn("OPEN");
        when(mockResultSet.getString("item_type")).thenReturn("ELECTRONICS");
        when(mockResultSet.getString("item_id")).thenReturn("ITEM_01");
        when(mockResultSet.getString("item_name")).thenReturn("Laptop");
        when(mockResultSet.getDouble("starting_price")).thenReturn(100.0);
        when(mockResultSet.getString("current_winner_id")).thenReturn("WIN_01");
        when(mockResultSet.getString("winner_username")).thenReturn("winner_user");

        Auction result = auctionDAO.findById("AUC_01");

        assertNotNull(result);
        assertEquals("AUC_01", result.getId());
        assertEquals(150.0, result.getCurrentPrice());
        assertEquals("Laptop", result.getItem().getName());
        assertEquals("winner_user", result.getCurrentWinner().getName());
    }

    @Test
    @DisplayName("Tìm Auction bằng ID: Thất bại do không tồn tại")
    void testFindById_NotFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> auctionDAO.findById("AUC_99"));
        assertEquals("Không tìm thấy phiên đấu giá có ID: AUC_99", exception.getMessage());
    }

    @Test
    @DisplayName("Lấy tất cả Auction")
    void testFindAll() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("id")).thenReturn("AUC_01");
        when(mockResultSet.getDouble("current_price")).thenReturn(150.0);
        when(mockResultSet.getString("status")).thenReturn("OPEN");
        when(mockResultSet.getString("item_type")).thenReturn("ELECTRONICS");
        when(mockResultSet.getString("item_id")).thenReturn("ITEM_01");
        when(mockResultSet.getString("item_name")).thenReturn("Laptop");
        when(mockResultSet.getDouble("starting_price")).thenReturn(100.0);

        List<Auction> results = auctionDAO.findAll();

        assertEquals(1, results.size());
        assertEquals("AUC_01", results.get(0).getId());
    }

    @Test
    @DisplayName("Xóa Auction: Thành công")
    void testDelete_Success() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> auctionDAO.delete("AUC_01"));
    }

    @Test
    @DisplayName("Xóa Auction: Thất bại do không tồn tại")
    void testDelete_NotFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> auctionDAO.delete("AUC_99"));
        assertEquals("Không thể xóa: Không tìm thấy phiên đấu giá ID 'AUC_99'", exception.getMessage());
    }
}
