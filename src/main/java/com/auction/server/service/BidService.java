package com.auction.server.service;

import com.auction.server.dao.BidTransactionDAO;
import com.auction.shared.model.entity.BidTransaction;
import com.auction.shared.protocol.Response;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BidService {

    private static final Logger LOGGER = Logger.getLogger(BidService.class.getName());

    private final BidTransactionDAO bidDAO;
    private final AuctionManager auctionManager;

    public BidService(AuctionManager auctionManager) {
        this.bidDAO = new BidTransactionDAO();
        this.auctionManager = auctionManager;
    }

    public BidService(AuctionManager auctionManager, BidTransactionDAO bidDAO) {
        this.auctionManager = auctionManager;
        this.bidDAO = bidDAO;
    }

    public Response placeBid(String auctionId, String bidderId, double amount) {
        if (amount <= 0) {
            return new Response(false, "Số tiền đặt giá phải lớn hơn 0.", null);
        }
        if (auctionId == null || bidderId == null) {
            return new Response(false, "Thông tin phiên đấu giá hoặc người dùng không hợp lệ.", null);
        }

        return auctionManager.processNewBid(auctionId, bidderId, amount);
    }

    public Response getBidHistory(String auctionId) {
        if (auctionId == null || auctionId.trim().isEmpty()) {
            return new Response(false, "Mã phiên đấu giá không hợp lệ.", null);
        }

        try {
            List<BidTransaction> history = bidDAO.getBidsByAuctionId(auctionId);
            return new Response(true, "Lấy lịch sử thành công.", history);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy lịch sử giá: " + e.getMessage(), e);
            return new Response(false, "Lỗi máy chủ khi lấy lịch sử.", null);
        }
    }
}
