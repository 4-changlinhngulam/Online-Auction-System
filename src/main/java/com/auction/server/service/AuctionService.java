package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.protocol.Response;

import java.util.List;

public class AuctionService {

    private final AuctionDAO auctionDAO;

    public AuctionService() {
        this.auctionDAO = new AuctionDAO();
    }

    public Response createAuction(Auction auction) {
        if (auction == null || auction.getItem() == null) {
            return Response.error("Thông tin phiên đấu giá không hợp lệ.");
        }
        try {
            auctionDAO.save(auction);
            // Thêm vào memory manager và lên lịch kết thúc
            AuctionManager.getInstance().addAuction(auction);
            AuctionManager.getInstance().scheduleAuctionEnd(auction);
            return new Response(true, "Tạo phiên đấu giá thành công.", auction);
        } catch (Exception e) {
            System.err.println("Lỗi tạo phiên đấu giá: " + e.getMessage());
            return Response.error("Lỗi máy chủ khi tạo phiên đấu giá: " + e.getMessage());
        }
    }

    public Response getAuction(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Response.error("ID phiên đấu giá không hợp lệ.");
        }
        try {
            Auction auction = auctionDAO.findById(id);
            return new Response(true, "Lấy thông tin phiên đấu giá thành công.", auction);
        } catch (Exception e) {
            System.err.println("Lỗi tải phiên đấu giá: " + e.getMessage());
            return Response.error("Không tìm thấy phiên đấu giá.");
        }
    }

    public Response getAllAuctions() {
        try {
            List<Auction> auctions = auctionDAO.findAll();
            return new Response(true, "Lấy danh sách phiên đấu giá thành công.", auctions);
        } catch (Exception e) {
            System.err.println("Lỗi tải danh sách phiên đấu giá: " + e.getMessage());
            return Response.error("Lỗi máy chủ khi tải danh sách phiên đấu giá.");
        }
    }

    public Response closeAuction(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Response.error("ID phiên đấu giá không hợp lệ.");
        }
        try {
            AuctionManager.getInstance().endAuction(id);
            return new Response(true, "Đã đóng phiên đấu giá thành công.", null);
        } catch (Exception e) {
            System.err.println("Lỗi đóng phiên đấu giá: " + e.getMessage());
            return Response.error("Lỗi máy chủ khi đóng phiên đấu giá.");
        }
    }
}
