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
        Response response1 = bidService.placeBid(null, "USR_001", 2000.0);
        assertFalse(response1.isSuccess());
        assertEquals("Thông tin phiên đấu giá hoặc người dùng không hợp lệ.", response1.getMessage());

        Response response2 = bidService.placeBid("AUC_123", null, 2000.0);
        assertFalse(response2.isSuccess());
        assertEquals("Thông tin phiên đấu giá hoặc người dùng không hợp lệ.", response2.getMessage());

        verify(auctionManager, never()).processNewBid(anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("Đặt giá: Thất bại do nhập số tiền biên dưới hoặc biên bằng (BVA: <= 0)")
    void testPlaceBid_BVA_InvalidAmount() {
        // Biên dưới: -0.01
        Response responseNegative = bidService.placeBid("AUC_123", "USR_001", -0.01);
        assertFalse(responseNegative.isSuccess());
        assertEquals("Số tiền đặt giá phải lớn hơn 0.", responseNegative.getMessage());

        // Biên bằng: 0.0
        Response responseZero = bidService.placeBid("AUC_123", "USR_001", 0.0);
        assertFalse(responseZero.isSuccess());
        assertEquals("Số tiền đặt giá phải lớn hơn 0.", responseZero.getMessage());

        verify(auctionManager, never()).processNewBid(anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("Đặt giá: Thành công khi số tiền biên trên sát nút (BVA: > 0)")
    void testPlaceBid_BVA_ValidBoundaryAmount() {
        // Biên trên: 0.01
        when(auctionManager.processNewBid("AUC_123", "USR_001", 0.01))
                .thenReturn(new Response(true, "Đặt giá thành công!", null));

        Response response = bidService.placeBid("AUC_123", "USR_001", 0.01);

        assertTrue(response.isSuccess());
        assertEquals("Đặt giá thành công!", response.getMessage());
        verify(auctionManager, times(1)).processNewBid("AUC_123", "USR_001", 0.01);
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
        assertEquals("Lấy lịch sử thành công.", response.getMessage());
    }

    @Test
    @DisplayName("Lấy lịch sử đấu giá: Thất bại do mã phiên rỗng")
    void testGetBidHistory_Fail_EmptyAuctionId() {
        Response response = bidService.getBidHistory("   ");
        assertFalse(response.isSuccess());
        assertEquals("Mã phiên đấu giá không hợp lệ.", response.getMessage());
        verify(bidTransactionDAO, never()).getBidsByAuctionId(anyString());
    }

    @Test
    @DisplayName("Lấy lịch sử đấu giá: Thất bại do lỗi hệ thống (Exception)")
    void testGetBidHistory_Fail_SystemError() throws Exception {
        when(bidTransactionDAO.getBidsByAuctionId("AUC_123")).thenThrow(new RuntimeException("Lỗi kết nối DB"));

        Response response = bidService.getBidHistory("AUC_123");

        assertFalse(response.isSuccess());
        assertEquals("Lỗi máy chủ khi lấy lịch sử.", response.getMessage());
    }
}