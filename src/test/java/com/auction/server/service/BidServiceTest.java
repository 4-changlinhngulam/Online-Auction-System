package com.auction.server.service;

import com.auction.server.dao.BidTransactionDAO;
import com.auction.shared.model.entity.BidTransaction;
import com.auction.shared.protocol.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BidServiceTest {

    @Mock
    private BidTransactionDAO bidTransactionDAO;

    @Mock
    private AuctionManager auctionManager;

    private BidService bidService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bidService = new BidService(auctionManager, bidTransactionDAO);
    }

    // ==========================================
    // TEST ĐẶT GIÁ (PLACE BID)
    // ==========================================
    @Test
    @DisplayName("Đặt giá: Đẩy việc qua AuctionManager thành công")
    void testPlaceBid_Success() {
        when(auctionManager.processNewBid("AUC_123", "USR_001", 2500.0))
                .thenReturn(new Response(true, "Đặt giá thành công!", null));

        Response response = bidService.placeBid("AUC_123", "USR_001", 2500.0);

        assertTrue(response.isSuccess());
        verify(auctionManager, times(1)).processNewBid(anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("Đặt giá: Thất bại do thiếu thông tin (Null ID)")
    void testPlaceBid_Fail_NullIds() {
        // Cố tình truyền null vào ID
        Response response = bidService.placeBid(null, "USR_001", 2000.0);
        assertFalse(response.isSuccess());
        assertEquals("Thông tin phiên đấu giá hoặc người dùng không hợp lệ.", response.getMessage());

        Response response2 = bidService.placeBid("AUC_123", null, 2000.0);
        assertFalse(response2.isSuccess());

        // Xác minh không gọi xuống tầng Manager
        verify(auctionManager, never()).processNewBid(anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("Đặt giá: Thất bại do nhập số tiền âm")
    void testPlaceBid_Fail_InvalidAmount() {
        Response response = bidService.placeBid("AUC_123", "USR_001", -500.0);
        assertFalse(response.isSuccess());
        assertEquals("Số tiền đặt giá phải lớn hơn 0.", response.getMessage());
    }

    // ==========================================
    // TEST LẤY LỊCH SỬ (GET BID HISTORY)
    // ==========================================
    @Test
    @DisplayName("Lấy lịch sử đấu giá: Thành công")
    void testGetBidHistory_Success() throws Exception {
        BidTransaction tx1 = new BidTransaction();
        when(bidTransactionDAO.getBidsByAuctionId("AUC_123")).thenReturn(Arrays.asList(tx1));

        Response response = bidService.getBidHistory("AUC_123");

        assertTrue(response.isSuccess());
        assertEquals(1, ((List<?>) response.getData()).size());
    }

    @Test
    @DisplayName("Lấy lịch sử đấu giá: Thất bại do mã phiên rỗng")
    void testGetBidHistory_Fail_EmptyAuctionId() {
        Response response = bidService.getBidHistory("   "); // Chuỗi toàn khoảng trắng
        assertFalse(response.isSuccess());
        assertEquals("Mã phiên đấu giá không hợp lệ.", response.getMessage());
        verify(bidTransactionDAO, never()).getBidsByAuctionId(anyString());
    }
}