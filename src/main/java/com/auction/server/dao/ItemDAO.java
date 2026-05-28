package com.auction.server.dao;

import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.exception.EntityNotFoundException;
import com.auction.shared.model.entity.Art;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.entity.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemDAO {

    private static final Logger LOGGER = Logger.getLogger(ItemDAO.class.getName());

    // - Design Pattern: Strategy / Mapper Registry.
    // - Lý do: Tránh dùng nhiều lệnh if-else (instanceof Electronics) vi phạm OCP
    // (Open-Closed Principle).
    // Tách logic map SQL thành các ItemMapper, khi thêm loại Item mới chỉ cần đăng
    // ký thêm Mapper, không phải sửa code.
    private interface ItemMapper {
        void mapToSave(PreparedStatement pstmt, Item item) throws SQLException;

        void mapToUpdate(PreparedStatement pstmt, Item item) throws SQLException;

        Item mapFromResultSet(ResultSet rs) throws SQLException;
    }

    private final Map<Class<? extends Item>, ItemMapper> classMappers = new HashMap<>();
    private final Map<String, ItemMapper> typeMappers = new HashMap<>();

    public ItemDAO() {
        registerMappers();
    }

    private void registerMappers() {
        // Mapper cho Electronics
        ItemMapper electronicsMapper = new ItemMapper() {
            @Override
            public void mapToSave(PreparedStatement pstmt, Item item) throws SQLException {
                pstmt.setString(5, "ELECTRONICS");
                pstmt.setInt(6, ((Electronics) item).getWarrantyMonths());
                pstmt.setNull(7, java.sql.Types.BIGINT);
            }

            @Override
            public void mapToUpdate(PreparedStatement pstmt, Item item) throws SQLException {
                pstmt.setString(4, "ELECTRONICS");
                pstmt.setInt(5, ((Electronics) item).getWarrantyMonths());
                pstmt.setNull(6, java.sql.Types.BIGINT);
            }

            @Override
            public Item mapFromResultSet(ResultSet rs) throws SQLException {
                Electronics item = new Electronics();
                item.setWarrantyMonths(rs.getInt("warranty_months"));
                return item;
            }
        };
        classMappers.put(Electronics.class, electronicsMapper);
        typeMappers.put("ELECTRONICS", electronicsMapper);

        // Mapper cho Vehicle
        ItemMapper vehicleMapper = new ItemMapper() {
            @Override
            public void mapToSave(PreparedStatement pstmt, Item item) throws SQLException {
                pstmt.setString(5, "VEHICLE");
                pstmt.setNull(6, java.sql.Types.INTEGER);
                pstmt.setLong(7, ((Vehicle) item).getMileage());
            }

            @Override
            public void mapToUpdate(PreparedStatement pstmt, Item item) throws SQLException {
                pstmt.setString(4, "VEHICLE");
                pstmt.setNull(5, java.sql.Types.INTEGER);
                pstmt.setLong(6, ((Vehicle) item).getMileage());
            }

            @Override
            public Item mapFromResultSet(ResultSet rs) throws SQLException {
                Vehicle item = new Vehicle();
                item.setMileage(rs.getLong("mileage"));
                return item;
            }
        };
        classMappers.put(Vehicle.class, vehicleMapper);
        typeMappers.put("VEHICLE", vehicleMapper);

        // Mapper cho Art
        ItemMapper artMapper = new ItemMapper() {
            @Override
            public void mapToSave(PreparedStatement pstmt, Item item) throws SQLException {
                pstmt.setString(5, "ART");
                pstmt.setNull(6, java.sql.Types.INTEGER);
                pstmt.setNull(7, java.sql.Types.BIGINT);
            }

            @Override
            public void mapToUpdate(PreparedStatement pstmt, Item item) throws SQLException {
                pstmt.setString(4, "ART");
                pstmt.setNull(5, java.sql.Types.INTEGER);
                pstmt.setNull(6, java.sql.Types.BIGINT);
            }

            @Override
            public Item mapFromResultSet(ResultSet rs) throws SQLException {
                return new Art();
            }
        };
        classMappers.put(Art.class, artMapper);
        typeMappers.put("ART", artMapper);
    }

    // 1. THÊM MỚI SẢN PHẨM (SAVE)
    public void save(Item item) throws DataPersistenceException {
        if (item == null || item.getId() == null) {
            throw new IllegalArgumentException("Item và ID không được phép null");
        }

        ItemMapper mapper = classMappers.get(item.getClass());
        if (mapper == null) {
            throw new IllegalArgumentException("Loại Item không được hỗ trợ để lưu: " + item.getClass().getName());
        }

        String sql = "INSERT INTO items (id, name, description, starting_price, item_type, "
                   + "warranty_months, mileage, owner_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            pstmt.setDouble(4, item.getStartingPrice());

            // Ủy quyền map thuộc tính đặc thù cho Mapper
            mapper.mapToSave(pstmt, item);
            
            pstmt.setString(8, item.getOwnerId());
            pstmt.setString(9, item.getStatus());

            pstmt.executeUpdate();
            LOGGER.log(Level.INFO, "Đã lưu Sản phẩm {0} lên Database!", item.getName());

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Sản phẩm với ID '" + item.getId() + "' đã tồn tại!");
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi lưu Item vào Database", e);
        }
    }

    // 2. CẬP NHẬT SẢN PHẨM (UPDATE)
    public void update(Item item) throws DataPersistenceException, EntityNotFoundException {
        if (item == null || item.getId() == null) {
            throw new IllegalArgumentException("Item và ID không được phép null");
        }

        ItemMapper mapper = classMappers.get(item.getClass());
        if (mapper == null) {
            throw new IllegalArgumentException("Loại Item không được hỗ trợ để cập nhật: " + item.getClass().getName());
        }

        String sql = "UPDATE items SET name = ?, description = ?, starting_price = ?, "
                   + "item_type = ?, warranty_months = ?, mileage = ?, owner_id = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setDouble(3, item.getStartingPrice());

            // Ủy quyền map thuộc tính đặc thù
            mapper.mapToUpdate(pstmt, item);

            pstmt.setString(7, item.getOwnerId());
            pstmt.setString(8, item.getStatus());
            pstmt.setString(9, item.getId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new EntityNotFoundException("Không tìm thấy Sản phẩm ID: " + item.getId() + " để cập nhật.");
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi cập nhật Item", e);
        }
    }

    // 3. TÌM THEO ID (FIND BY ID)
    public Item findById(String id) throws DataPersistenceException, EntityNotFoundException {
        String sql = "SELECT * FROM items WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                } else {
                    throw new EntityNotFoundException("Không tìm thấy Sản phẩm có ID: " + id);
                }
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi tìm Item theo ID", e);
        }
    }

    // 4. LẤY TẤT CẢ SẢN PHẨM (FIND ALL)
    public List<Item> findAll() throws DataPersistenceException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi lấy danh sách Item", e);
        }
        return items;
    }

    // 5. XÓA SẢN PHẨM (DELETE)
    public void delete(String id) throws DataPersistenceException, EntityNotFoundException {
        String sql = "DELETE FROM items WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new EntityNotFoundException("Không tìm thấy Sản phẩm ID: " + id + " để xóa.");
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Lỗi khi xóa Item", e);
        }
    }

    // 7. TÌM BẰNG TỪ KHÓA
    public List<Item> searchByName(String keyword) {
        List<Item> resultList = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultList.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tìm kiếm Item theo tên: " + e.getMessage(), e);
        }
        return resultList;
    }

    // --- HÀM HỖ TRỢ: CHUYỂN ĐỔI DỮ LIỆU TỪ DB SANG JAVA OBJECT ---
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        String type = rs.getString("item_type");
        ItemMapper mapper = typeMappers.get(type);

        if (mapper == null) {
            throw new SQLException("ItemType không hợp lệ trong Database: " + type);
        }

        // Ủy quyền khởi tạo class con cho Mapper
        Item item = mapper.mapFromResultSet(rs);

        // Đổ dữ liệu chung vào
        item.setId(rs.getString("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setStartingPrice(rs.getDouble("starting_price"));
        item.setOwnerId(rs.getString("owner_id"));
        item.setStatus(rs.getString("status"));

        return item;
    }
}
