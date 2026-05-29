package com.auction.server.dao;

import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.model.entity.BidTransaction;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BidTransactionDAOTest {

    @Mock
    private DatabaseConnection mockDbConnection;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseConnection> mockedStaticDb;
    private BidTransactionDAO bidTransactionDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        bidTransactionDAO = new BidTransactionDAO();

        mockedStaticDb = mockStatic(DatabaseConnection.class);
        mockedStaticDb.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        mockedStaticDb.close();
    }

    @Test
    @DisplayName("Lưu BidTransaction: Thành công")
    void testSave_Success() throws Exception {
        BidTransaction tx = new BidTransaction();
        tx.setId("TX_01");
        tx.setAuctionId("AUC_01");
        tx.setBidderId("BID_01");
        tx.setAmount(150.0);
        tx.setTimestamp(LocalDateTime.now());

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> bidTransactionDAO.save(tx));

        verify(mockPreparedStatement).setString(1, "TX_01");
        verify(mockPreparedStatement).setString(2, "AUC_01");
        verify(mockPreparedStatement).setString(3, "BID_01");
        verify(mockPreparedStatement).setDouble(4, 150.0);
    }

    @Test
    @DisplayName("Lưu BidTransaction: Thất bại do ID null")
    void testSave_Fail_NullId() {
        BidTransaction tx = new BidTransaction();
        tx.setId(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bidTransactionDAO.save(null));
        assertEquals("BidTransaction và ID không được null", exception.getMessage());
    }

    @Test
    @DisplayName("Lấy lịch sử đấu giá theo Auction ID")
    void testGetBidsByAuctionId() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("id")).thenReturn("TX_01");
        when(mockResultSet.getString("auction_id")).thenReturn("AUC_01");
        when(mockResultSet.getString("bidder_id")).thenReturn("BID_01");
        when(mockResultSet.getDouble("amount")).thenReturn(150.0);
        when(mockResultSet.getTimestamp("timestamp")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        List<BidTransaction> list = bidTransactionDAO.getBidsByAuctionId("AUC_01");

        assertEquals(1, list.size());
        assertEquals("TX_01", list.get(0).getId());
        assertEquals("AUC_01", list.get(0).getAuctionId());
    }
}
