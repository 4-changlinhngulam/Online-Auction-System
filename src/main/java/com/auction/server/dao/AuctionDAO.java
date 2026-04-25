package com.auction.server.dao;

import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.exception.EntityNotFoundException;
import com.auction.shared.model.entity.Auction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.*;

/**
 * DAO cho Auction - Quản lý lưu trữ và truy xuất dữ liệu phiên đấu giá
 * Sử dụng Serialization để đọc/ghi dữ liệu từ file
 *
 * Exception-based error handling:
 * - DataPersistenceException: Thrown for I/O and serialization errors
 * - EntityNotFoundException: Thrown when auction is not found
 * - IllegalArgumentException: Thrown for invalid input
 */
public class AuctionDAO {
    private static final Logger logger = LoggerFactory.getLogger(AuctionDAO.class);
    private static final String DATA_FILE = "data/auctions.dat";

    /**
     * Lưu một phiên đấu giá vào file (hoặc cập nhật nếu đã tồn tại)
     * @param auction - Phiên đấu giá cần lưu
     * @throws DataPersistenceException nếu có lỗi khi ghi file
     */
    public void save(Auction auction) throws DataPersistenceException {
        if (auction == null || auction.getId() == null) {
            throw new IllegalArgumentException("Auction và ID không được phép null");
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

        } catch (DataPersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new DataPersistenceException("Lỗi khi lưu phiên đấu giá", e);
        }
    }

    /**
     * Tìm phiên đấu giá theo ID
     * @param id - ID của phiên đấu giá
     * @return Auction nếu tìm thấy
     * @throws EntityNotFoundException nếu không tìm thấy
     * @throws DataPersistenceException nếu có lỗi khi đọc file
     */
    public Auction findById(String id) throws EntityNotFoundException, DataPersistenceException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID không được phép null hoặc rỗng");
        }

        try {
            List<Auction> auctions = findAll();
            Auction result = auctions.stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (result != null) {
                logger.debug("Tìm thấy phiên đấu giá ID: {}", id);
                return result;
            } else {
                throw new EntityNotFoundException("Không tìm thấy phiên đấu giá với ID: " + id);
            }

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (DataPersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new DataPersistenceException("Lỗi khi tìm phiên đấu giá ID: " + id, e);
        }
    }

    /**
     * Lấy tất cả phiên đấu giá
     * @return List danh sách tất cả Auction, rỗng nếu không có
     * @throws DataPersistenceException nếu có lỗi khi đọc file
     */
    public List<Auction> findAll() throws DataPersistenceException {
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
            throw new DataPersistenceException("Lỗi khi lấy danh sách phiên đấu giá", e);
        }
    }

    /**
     * Cập nhật một phiên đấu giá hiện có
     * @param auction - Phiên đấu giá cần cập nhật
     * @throws EntityNotFoundException nếu phiên đấu giá không tồn tại
     * @throws DataPersistenceException nếu có lỗi khi ghi file
     */
    public void update(Auction auction) throws EntityNotFoundException, DataPersistenceException {
        if (auction == null || auction.getId() == null) {
            throw new IllegalArgumentException("Auction và ID không được phép null");
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
                throw new EntityNotFoundException("Không tìm thấy phiên đấu giá với ID: " + auction.getId());
            }

            writeToFile(auctions);
            logger.info("✓ Cập nhật phiên đấu giá thành công!");

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (DataPersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new DataPersistenceException("Lỗi khi cập nhật phiên đấu giá", e);
        }
    }

    /**
     * Xóa một phiên đấu giá theo ID
     * @param id - ID của phiên đấu giá cần xóa
     * @throws EntityNotFoundException nếu phiên đấu giá không tồn tại
     * @throws DataPersistenceException nếu có lỗi khi ghi file
     */
    public void delete(String id) throws EntityNotFoundException, DataPersistenceException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID không được phép null hoặc rỗng");
        }

        try {
            List<Auction> auctions = findAll();
            boolean removed = auctions.removeIf(a -> a.getId().equals(id));

            if (!removed) {
                throw new EntityNotFoundException("Không tìm thấy phiên đấu giá với ID: " + id);
            }

            writeToFile(auctions);
            logger.info("✓ Xóa phiên đấu giá ID: {} thành công!", id);

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (DataPersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new DataPersistenceException("Lỗi khi xóa phiên đấu giá ID: " + id, e);
        }
    }

    /**
     * Lấy số lượng phiên đấu giá
     * @return Số lượng phiên đấu giá
     * @throws DataPersistenceException nếu có lỗi khi đọc file
     */
    public int getCount() throws DataPersistenceException {
        try {
            int count = findAll().size();
            logger.debug("Tổng số phiên đấu giá: {}", count);
            return count;
        } catch (DataPersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new DataPersistenceException("Lỗi khi đếm phiên đấu giá", e);
        }
    }

    /**
     * === PRIVATE HELPER METHODS ===
     */

    /**
     * Ghi danh sách Auction vào file bằng Serialization
     * @throws DataPersistenceException nếu có lỗi I/O
     */
    private void writeToFile(List<Auction> auctions) throws DataPersistenceException {
        try {
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
        } catch (IOException e) {
            throw new DataPersistenceException("Lỗi khi ghi dữ liệu vào file", e);
        }
    }

    /**
     * Đọc danh sách Auction từ file bằng Serialization
     * @throws DataPersistenceException nếu có lỗi I/O hoặc deserialization
     */
    @SuppressWarnings("unchecked")
    private List<Auction> readFromFile() throws DataPersistenceException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(DATA_FILE))) {
            return (List<Auction>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new DataPersistenceException("Lỗi khi đọc dữ liệu từ file", e);
        }
    }

    /**
     * Kiểm tra xem file dữ liệu có tồn tại hay không
     * @return true nếu file tồn tại, false nếu không
     */
    public boolean dataFileExists() {
        boolean exists = new File(DATA_FILE).exists();
        logger.debug("File dữ liệu tồn tại: {}", exists);
        return exists;
    }

    /**
     * Xóa toàn bộ dữ liệu (reset file)
     * @throws DataPersistenceException nếu có lỗi khi ghi file
     */
    public void clearAll() throws DataPersistenceException {
        try {
            File file = new File(DATA_FILE);
            if (file.exists()) {
                writeToFile(new ArrayList<>());
                logger.info("✓ Xóa toàn bộ dữ liệu thành công!");
            }
        } catch (DataPersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new DataPersistenceException("Lỗi khi xóa dữ liệu", e);
        }
    }
}