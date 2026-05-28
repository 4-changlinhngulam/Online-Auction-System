package com.auction.shared.model.entity;

import com.auction.shared.model.enums.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuctionConcurrencyTest {

    private Auction testAuction;

    @BeforeEach
    public void setup() {
        testAuction = new Auction();
        testAuction.setId("TEST-AUCTION-001");
        testAuction.setStatus(AuctionStatus.RUNNING);
        testAuction.setCurrentPrice(1000000);
        testAuction.setEndTime(LocalDateTime.now().plusHours(1));
    }

    @Test
    public void testConcurrentBiddingPreventsRaceCondition() throws InterruptedException {
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successfulBids = new AtomicInteger(0);
        AtomicInteger failedBids = new AtomicInteger(0);

        // 100 luồng thi nhau đặt giá gần bằng nhau cùng một thời điểm
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadNum = i;
            executorService.submit(() -> {
                try {
                    Bidder bidder = new Bidder();
                    bidder.setId("BIDDER-" + threadNum);
                    
                    // Giả lập mức giá: 1,100,000 + (0..99)*10
                    double bidAmount = 1100000 + (threadNum * 10);
                    
                    boolean success = testAuction.handleNewBid(bidder, bidAmount);
                    if (success) {
                        successfulBids.incrementAndGet();
                    } else {
                        failedBids.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        System.out.println("Tổng số lượt đặt giá thành công (Ghi nhận trên RAM): " + successfulBids.get());
        System.out.println("Tổng số lượt đặt giá thất bại (Do bị từ chối/race condition): " + failedBids.get());
        System.out.println("Giá chốt cuối cùng: " + testAuction.getCurrentPrice());

        // Nếu Race condition xảy ra, có thể nhiều thread cùng đọc currentPrice = 1M
        // Và cùng overwrite giá mới thành công, dẫn đến failedBids = 0 và History có nhiều mức giá thấp chồng chéo.
        // Bằng cách sử dụng synchronized, chỉ có 1 thread được duyệt vào handleNewBid 1 thời điểm.
        // Thread phía sau sẽ thấy giá đã tăng và bị return false.
        
        // Kiểm tra tính nhất quán:
        // Giá hiện tại phải là mức giá lớn nhất trong số các bid thành công (thực tế >= 1.100.000)
        assertTrue(testAuction.getCurrentPrice() >= 1100000, "Giá cuối cùng không đúng do lỗi đồng bộ.");
        
        // Số lượng bid ghi vào history phải đúng bằng số successfulBids đếm được
        assertEquals(successfulBids.get(), testAuction.getBidHistory().size(), "Lịch sử đặt giá bị sai lệch do Race condition");
        
        // Phải có thread thành công và thất bại
        assertTrue(successfulBids.get() > 0, "Phải có luồng thành công");
    }
}
