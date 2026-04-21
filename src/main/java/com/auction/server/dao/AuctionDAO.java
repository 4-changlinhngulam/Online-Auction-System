package com.auction.server.dao;

import com.auction.shared.model.entity.Auction;
import java.io.*;
import java.util.*;

public class AuctionDAO {
    private static final String DATA_FILE = "data/auctions.dat";
    // TODO: save / findById / findAll / update / delete

    /**
     * Lưu một phiên đấu giá vào file (hoặc cập nhật nếu đã tồn tại)
     * @param auction - Phiên đấu giá cần lưu
     * @return true nếu lưu thành công, false nếu thất bại
     */
    public boolean save(Auction auction) {
        if (auction == null || auction.getId() == null) {
            System.out.println("Lỗi: Auction hoặc ID không hợp lệ!");
            return false;
        }

        try {
            // Lấy danh sách hiện tại
            List<Auction> auctions = findAll();

            // Nếu đã tồn tại thì cập nhật, nếu không thì thêm mới
            boolean exists = auctions.stream()
                    .anyMatch(a -> a.getId().equals(auction.getId()));

            if (exists) {
                for (int i = 0; i < auctions.size(); i++) {
                    if (auctions.get(i).getId().equals(auction.getId())) {
                        auctions.set(i, auction);
                        break;
                    }
                }
            } else {
                auctions.add(auction);
            }

            writeToFile(auctions);
            System.out.println("✓ Lưu phiên đấu giá thành công!");
            return true;

        } catch (Exception e) {
            System.out.println("✗ Lỗi khi lưu phiên đấu giá: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("Lỗi: ID không hợp lệ!");
            return null;
        }

        try {
            List<Auction> auctions = findAll();
            return auctions.stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElse(null);

        } catch (Exception e) {
            System.out.println("✗ Lỗi khi tìm phiên đấu giá: " + e.getMessage());
            e.printStackTrace();
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
                System.out.println("File dữ liệu chưa tồn tại, khởi tạo danh sách rỗng.");
                return new ArrayList<>();
            }

            return readFromFile();

        } catch (Exception e) {
            System.out.println("✗ Lỗi khi lấy danh sách phiên đấu giá: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("Lỗi: Auction hoặc ID không hợp lệ!");
            return false;
        }

        try {
            List<Auction> auctions = findAll();
            boolean found = false;

            for (int i = 0; i < auctions.size(); i++) {
                if (auctions.get(i).getId().equals(auction.getId())) {
                    auctions.set(i, auction);
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("✗ Không tìm thấy phiên đấu giá với ID: " + auction.getId());
                return false;
            }

            writeToFile(auctions);
            System.out.println("✓ Cập nhật phiên đấu giá thành công!");
            return true;

        } catch (Exception e) {
            System.out.println("✗ Lỗi khi cập nhật phiên đấu giá: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("Lỗi: ID không hợp lệ!");
            return false;
        }

        try {
            List<Auction> auctions = findAll();
            boolean removed = auctions.removeIf(a -> a.getId().equals(id));

            if (!removed) {
                System.out.println("✗ Không tìm thấy phiên đấu giá với ID: " + id);
                return false;
            }

            writeToFile(auctions);
            System.out.println("✓ Xóa phiên đấu giá thành công!");
            return true;

        } catch (Exception e) {
            System.out.println("✗ Lỗi khi xóa phiên đấu giá: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy số lượng phiên đấu giá
     * @return Số lượng phiên đấu giá
     */
    public int getCount() {
        try {
            return findAll().size();
        } catch (Exception e) {
            System.out.println("✗ Lỗi khi đếm phiên đấu giá: " + e.getMessage());
            return 0;
        }
    }

    /**
     * === PRIVATE HELPER METHODS ===
     */

    /**
     * Ghi danh sách Auction vào file bằng Serialization
     * @param auctions - Danh sách cần ghi
     * @throws IOException nếu có lỗi I/O
     */
    private void writeToFile(List<Auction> auctions) throws IOException {
        // Tạo thư mục nếu chưa tồn tại
        File dir = new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(DATA_FILE))) {
            oos.writeObject(auctions);
            oos.flush();
        }
    }

    /**
     * Đọc danh sách Auction từ file bằng Serialization
     * @return Danh sách Auction đã đọc
     * @throws IOException nếu có lỗi I/O
     * @throws ClassNotFoundException nếu không tìm thấy class
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
     * @return true nếu file tồn tại, false nếu không
     */
    public boolean dataFileExists() {
        return new File(DATA_FILE).exists();
    }

    /**
     * Xóa toàn bộ dữ liệu (reset file)
     * @return true nếu xóa thành công
     */
    public boolean clearAll() {
        try {
            File file = new File(DATA_FILE);
            if (file.exists()) {
                writeToFile(new ArrayList<>());
                System.out.println("✓ Xóa toàn bộ dữ liệu thành công!");
                return true;
            }
            return false;
        } catch (IOException e) {
            System.out.println("✗ Lỗi khi xóa dữ liệu: " + e.getMessage());
            return false;
        }
    }
}