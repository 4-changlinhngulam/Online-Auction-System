package com.auction.server.service;

import com.auction.server.dao.BidTransactionDAO;
import com.auction.shared.model.entity.BidTransaction;
import com.auction.shared.protocol.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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

    @Test
    @DisplayName("Lấy lịch sử đấu giá thành công")
    void testGetBidHistory_Success() throws Exception {
        String auctionId = "AUC_123";
        BidTransaction tx1 = new BidTransaction();
        tx1.setAuctionId(auctionId);
        tx1.setAmount(1500.0);

        BidTransaction tx2 = new BidTransaction();
        tx2.setAuctionId(auctionId);
        tx2.setAmount(2000.0);

        when(bidTransactionDAO.getBidsByAuctionId(auctionId)).thenReturn(Arrays.asList(tx2, tx1));

        Response response = bidService.getBidHistory(auctionId);

        assertTrue(response.isSuccess());
        List<?> history = (List<?>) response.getData();
        assertEquals(2, history.size());
        verify(bidTransactionDAO, times(1)).getBidsByAuctionId(auctionId);
    }

    @Test
    @DisplayName("Lấy lịch sử đấu giá trống")
    void testGetBidHistory_Empty() throws Exception {
        String auctionId = "AUC_EMPTY";
        when(bidTransactionDAO.getBidsByAuctionId(auctionId)).thenReturn(Arrays.asList());

        Response response = bidService.getBidHistory(auctionId);

        assertTrue(response.isSuccess());
        verify(bidTransactionDAO, times(1)).getBidsByAuctionId(auctionId);
    }
}