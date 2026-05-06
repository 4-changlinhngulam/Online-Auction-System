package com.auction.server.service;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.model.entity.Bidder;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.enums.AuctionStatus;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
class   AuctionServiceTest {
    private Auction mockAuction;
    private Bidder mockBidder;

    @BeforeEach void setUp() {
        //Tạo món đồ đấu giá
        Item mockItem = new Electronics();
        mockItem.setId("01");
        mockItem.setName("Tàu con thoi");
        mockItem.setStartingPrice(500);
        mockItem.setDescription("Ngon");

        //Tạo phiên đấu giá đang chạy
        mockAuction = new Auction();
        mockAuction.setId("AUC_01");
        mockAuction.setItem(mockItem);
        mockAuction.setCurrentPrice(2000);
        mockAuction.setStatus(AuctionStatus.RUNNING);
        mockAuction.setEndTime(LocalDateTime.now().plusHours(1));

        //Tạo người mua
        mockBidder = new Bidder();
        mockBidder.setId("BIDDER_001");
        mockBidder.setName("NguoiMuaTest");
    }
    @Test
    @DisplayName("Nên đặt giá thành công khi số tiền hợp lệ")
    void testPlaceBid_ValidAmount_ShouldSucceed() {
        double bidAmount = 2500;
        boolean result = mockAuction.handleNewBid(mockBidder, bidAmount);

        assertTrue(result, "Việc đặt giá phải thành công");
        assertEquals(bidAmount, mockAuction.getCurrentPrice());
        assertEquals(mockBidder, mockAuction.getCurrentWinner());
    }
    @Test
    @DisplayName("Nên thất bại khi đặt giá thấp hơn hoặc bằng giá hiện tại")
    void testPlaceBid_AmountLowerThanCurrent_ShouldThrowInvalidBidException() {
        double lowerBid = 1500;
        boolean result = mockAuction.handleNewBid(mockBidder, lowerBid);

        assertFalse(result, "Không được phép dặt giá thấp hơn giá hiện tại");
        assertEquals(2000, mockAuction.getCurrentPrice(), "Giá hiện tại không được thay đổi" );
    }
    @Test
    @DisplayName("Nên thất bại khi phiên đấu giá đã đóng")
    void testPlaceBid_AuctionClosed_ShouldThrowAuctionClosedException() {
        mockAuction.setStatus(AuctionStatus.FINISHED);
        boolean result = mockAuction.handleNewBid(mockBidder, 5000);

        assertFalse(result, "Không được phép đặt giá khi phiên đã đóng");
    }
    @Test
    @DisplayName("Kiểm tra xác định đúng người thắng cuộc khi kết thúc")
    void testCloseAuction_ShouldDetermineWinnerCorrectly() {
        // 1. Giả lập một người mua khác (Bidder B) tham gia
        Bidder bidderB = new Bidder();
        bidderB.setId("BIDDER_002");
        bidderB.setName("Giang");

        // 2. Thực hiện đặt giá: Bidder cũ (mockBidder) đang giữ 2000, Bidder B đặt 3000
        mockAuction.handleNewBid(bidderB, 3000);

        // 3. Kiểm tra xem người thắng hiện tại có phải là Bidder B không
        assertEquals(bidderB, mockAuction.getCurrentWinner(), "Người thắng phải là người đặt giá cao nhất cuối cùng");
        assertEquals(3000, mockAuction.getCurrentPrice(), "Giá hiện tại phải cập nhật theo người thắng");

        // 4. Giả lập kết thúc phiên (Close Auction)
        mockAuction.setStatus(AuctionStatus.FINISHED);

        // Kiểm tra lần cuối sau khi đóng, Winner vẫn phải giữ nguyên là Bidder B
        assertNotNull(mockAuction.getCurrentWinner());
        assertEquals("BIDDER_002", mockAuction.getCurrentWinner().getId());
    }
}
