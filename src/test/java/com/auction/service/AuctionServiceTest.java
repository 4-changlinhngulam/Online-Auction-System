package com.auction.service;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class AuctionServiceTest {
    @BeforeEach void setUp() { /* TODO: khởi tạo AuctionManager và data test */ }
    @Test void testPlaceBid_ValidAmount_ShouldSucceed() { /* TODO */ }
    @Test void testPlaceBid_AmountLowerThanCurrent_ShouldThrowInvalidBidException() { /* TODO */ }
    @Test void testPlaceBid_AuctionClosed_ShouldThrowAuctionClosedException() { /* TODO */ }
    @Test void testCloseAuction_ShouldDetermineWinnerCorrectly() { /* TODO */ }
}
