package com.auction.server.dao;

import com.auction.shared.model.entity.Auction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.*;

/**
 * DAO cho Auction - Quản lý lưu trữ và truy xuất dữ liệu phiên đấu giá
 * Sử dụng Serialization để đọc/ghi dữ liệu từ file
 */
public class AuctionDAO {
    private static final Logger logger = LoggerFactory.getLogger(AuctionDAO.class);
    private static final String DATA_FILE = "data/auctions.dat";

    /**
     * Lưu một phiên đấu giá vào file (hoặc cập nhật nếu đã tồn tại)
     * @param auction - Phiên đấu giá cần lưu
     * @return true nếu lưu thành công, false nếu thất bại
     */
    public boolean save(Auction auction) {
        if (auction == null || auction.getId() == null) {
            logger.error("Lỗi: Auction hoặc ID không hợp lệ!");
            return false;
        }

        try {
            List<Auction> auctions = findAll();

            boolean exists = auctions.stream()
                    .anyMatch(a -> a.getId().equals(auction.getId()));

            if (exists) {
                for (int i = 0; i < auctions.size(); i++) {
                    if (auctions.get(i).getId().equals(auction.getId())) {
                        auctions.set(i, auction);
                        break;
                    }
                }
                logger.info("Cập nhật phiên đấu giá ID: {}", auction.getId());
            } else {
                auctions.add(auction);
                logger.info("Thêm mới phiên đấu giá ID: {}", auction.getId());
            }

            writeToFile(auctions);
            logger.info("✓ Lưu phiên đấu giá thành công!");
            return true;

        } catch (Exception e) {
            logger.error("✗ Lỗi khi lưu phiên đấu giá", e);
            return false;
        }
    }

    /**
     * Tìm phiên đấu giá theo ID
     * @param id - ID của phiên đấu giá
     * @return Auction nếu tìm thấy, null nếu không
     */
    public Auction findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            logger.warn("Tìm kiếm với ID không hợp lệ");
            return null;
        }

        try {
            List<Auction> auctions = findAll();
            Auction result = auctions.stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (result != null) {
                logger.debug("Tìm thấy phiên đấu giá ID: {}", id);
            } else {
                logger.warn("Không tìm thấy phiên đấu giá ID: {}", id);
            }
            return result;

        } catch (Exception e) {
            logger.error("✗ Lỗi khi tìm phiên đấu giá ID: {}", id, e);
            return null;
        }
    }

    /**
     * Lấy tất cả phiên đấu giá
     * @return List danh sách tất cả Auction, rỗng nếu không có
     */
    public List<Auction> findAll() {
        try {
            File file = new File(DATA_FILE);

            if (!file.exists()) {
                logger.debug("File dữ liệu chưa tồn tại, khởi tạo danh sách rỗng");
                return new ArrayList<>();
            }

            List<Auction> auctions = readFromFile();
            logger.debug("Lấy {} phiên đấu giá từ file", auctions.size());
            return auctions;

        } catch (Exception e) {
            logger.error("✗ Lỗi khi lấy danh sách phiên đấu giá", e);
            return new ArrayList<>();
        }
    }

    /**
     * Cập nhật một phiên đấu giá hiện có
     * @param auction - Phiên đấu giá cần cập nhật
     * @return true nếu cập nhật thành công, false nếu không
     */
    public boolean update(Auction auction) {
        if (auction == null || auction.getId() == null) {
            logger.error("Lỗi: Auction hoặc ID không hợp lệ!");
            return false;
        }

        try {
            List<Auction> auctions = findAll();
            boolean found = false;

            for (int i = 0; i < auctions.size(); i++) {
                if (auctions.get(i).getId().equals(auction.getId())) {
                    auctions.set(i, auction);
                    found = true;
                    logger.info("Cập nhật phiên đấu giá ID: {}", auction.getId());
                    break;
                }
            }

            if (!found) {
                logger.warn("✗ Không tìm thấy phiên đấu giá với ID: {}", auction.getId());
                return false;
            }

            writeToFile(auctions);
            logger.info("✓ Cập nhật phiên đấu giá thành công!");
            return true;

        } catch (Exception e) {
            logger.error("✗ Lỗi khi cập nhật phiên đấu giá", e);
            return false;
        }
    }

    /**
     * Xóa một phiên đấu giá theo ID
     * @param id - ID của phiên đấu giá cần xóa
     * @return true nếu xóa thành công, false nếu không
     */
    public boolean delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            logger.error("Lỗi: ID không hợp lệ!");
            return false;
        }

        try {
            List<Auction> auctions = findAll();
            boolean removed = auctions.removeIf(a -> a.getId().equals(id));

            if (!removed) {
                logger.warn("✗ Không tìm thấy phiên đấu giá với ID: {}", id);
                return false;
            }

            writeToFile(auctions);
            logger.info("✓ Xóa phiên đấu giá ID: {} thành công!", id);
            return true;

        } catch (Exception e) {
            logger.error("✗ Lỗi khi xóa phiên đấu giá ID: {}", id, e);
            return false;
        }
    }

    /**
     * Lấy số lượng phiên đấu giá
     * @return Số lượng phiên đấu giá
     */
    public int getCount() {
        try {
            int count = findAll().size();
            logger.debug("Tổng số phiên đấu giá: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("✗ Lỗi khi đếm phiên đấu giá", e);
            return 0;
        }
    }

    /**
     * === PRIVATE HELPER METHODS ===
     */

    /**
     * Ghi danh sách Auction vào file bằng Serialization
     */
    private void writeToFile(List<Auction> auctions) throws IOException {
        File dir = new File("data");
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                logger.debug("Tạo thư mục data thành công");
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(DATA_FILE))) {
            oos.writeObject(auctions);
            oos.flush();
            logger.debug("Ghi {} phiên đấu giá vào file thành công", auctions.size());
        }
    }

    /**
     * Đọc danh sách Auction từ file bằng Serialization
     */
    @SuppressWarnings("unchecked")
    private List<Auction> readFromFile() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(DATA_FILE))) {
            return (List<Auction>) ois.readObject();
        }
    }

    /**
     * Kiểm tra xem file dữ liệu có tồn tại hay không
     */
    public boolean dataFileExists() {
        boolean exists = new File(DATA_FILE).exists();
        logger.debug("File dữ liệu tồn tại: {}", exists);
        return exists;
    }

    /**
     * Xóa toàn bộ dữ liệu (reset file)
     */
    public boolean clearAll() {
        try {
            File file = new File(DATA_FILE);
            if (file.exists()) {
                writeToFile(new ArrayList<>());
                logger.info("✓ Xóa toàn bộ dữ liệu thành công!");
                return true;
            }
            return false;
        } catch (IOException e) {
            logger.error("✗ Lỗi khi xóa dữ liệu", e);
            return false;
        }
    }
}