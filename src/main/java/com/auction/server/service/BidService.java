package com.auction.server.service;

import com.auction.server.dao.BidTransactionDAO;
import com.auction.shared.model.entity.BidTransaction;
import com.auction.shared.protocol.Response;
import java.util.List;

public class BidService {
    private final BidTransactionDAO bidDAO;
    // Inject thêm AuctionManager để điều phối các tác vụ "nóng"
    private final AuctionManager auctionManager;

    public BidService(AuctionManager auctionManager) {
        this.bidDAO = new BidTransactionDAO();
        this.auctionManager = auctionManager;
    }

    // --- NHÓM NÓNG ---
    public Response placeBid(String auctionId, String bidderId, double amount) {
        // Validate dữ liệu sơ bộ
        if (amount <= 0) {
            return new Response(false, "Số tiền đặt giá phải lớn hơn 0.", null);
        }
        if (auctionId == null || bidderId == null) {
            return new Response(false, "Thông tin phiên đấu giá hoặc người dùng không hợp lệ.", null);
        }

        // Chuyển việc khó cho "Trưởng phòng" AuctionManager lo (check giá, lưu DB, notify)
        // Lưu ý: Bên trong hàm xử lý của AuctionManager, bạn sẽ gọi bidDAO.save(...) để chốt giao dịch
        return auctionManager.processNewBid(auctionId, bidderId, amount);
    }

    // --- NHÓM NGUỘI ---
    public Response getBidHistory(String auctionId) {
        if (auctionId == null || auctionId.trim().isEmpty()) {
            return new Response(false, "Mã phiên đấu giá không hợp lệ.", null);
        }

        try {
            // Gọi thủ kho DAO lấy dữ liệu
            List<BidTransaction> history = bidDAO.getBidsByAuctionId(auctionId);
            return new Response(true, "Lấy lịch sử thành công.", history);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy lịch sử giá: " + e.getMessage());
            return new Response(false, "Lỗi máy chủ khi lấy lịch sử.", null);
        }
    }
}