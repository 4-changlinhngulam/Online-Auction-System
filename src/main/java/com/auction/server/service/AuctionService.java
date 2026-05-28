package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.shared.model.entity.Auction;
import com.auction.shared.protocol.Response;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionService {

    private static final Logger LOGGER = Logger.getLogger(AuctionService.class.getName());
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
            AuctionManager.getInstance().addAuction(auction);
            return new Response(true, "Tạo phiên đấu giá thành công (Đang chờ duyệt).", auction);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi tạo phiên đấu giá: " + e.getMessage(), e);
            return Response.error("Lỗi máy chủ khi tạo phiên đấu giá: " + e.getMessage());
        }
    }

    public Response startAuction(String auctionId) {
        if (auctionId == null || auctionId.trim().isEmpty()) {
            return Response.error("ID phiên đấu giá không hợp lệ.");
        }
        try {
            Auction auction = auctionDAO.findById(auctionId);
            if (auction == null) {
                return Response.error("Không tìm thấy phiên đấu giá.");
            }
            if (auction.getStatus() != com.auction.shared.model.enums.AuctionStatus.OPEN) {
                return Response.error("Chỉ có thể bắt đầu phiên đang ở trạng thái OPEN.");
            }
            
            auction.startAuction();
            auctionDAO.update(auction);
            AuctionManager.getInstance().addAuction(auction); // Cập nhật lại vào cache
            AuctionManager.getInstance().scheduleAuctionEnd(auction);
            
            return new Response(true, "Đã bắt đầu phiên đấu giá thành công.", auction);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi bắt đầu phiên đấu giá: " + e.getMessage(), e);
            return Response.error("Lỗi máy chủ khi bắt đầu phiên đấu giá: " + e.getMessage());
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
            LOGGER.log(Level.SEVERE, "Lỗi tải phiên đấu giá: " + e.getMessage(), e);
            return Response.error("Không tìm thấy phiên đấu giá.");
        }
    }

    public Response getAllAuctions() {
        try {
            List<Auction> auctions = auctionDAO.findAll();
            return new Response(true, "Lấy danh sách phiên đấu giá thành công.", auctions);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi tải danh sách phiên đấu giá: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Lỗi đóng phiên đấu giá: " + e.getMessage(), e);
            return Response.error("Lỗi máy chủ khi đóng phiên đấu giá.");
        }
    }
}
