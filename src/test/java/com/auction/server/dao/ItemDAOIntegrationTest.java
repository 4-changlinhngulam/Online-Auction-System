package com.auction.server.dao;

import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.entity.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemDAOIntegrationTest {

    private ItemDAO itemDAO;

    @BeforeEach
    void setUp() throws Exception {
        itemDAO = new ItemDAO();

        Connection conn = DatabaseConnection.getInstance().getConnection();
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(50), " +
                "description VARCHAR(255), " +
                "starting_price DOUBLE, " +
                "item_type VARCHAR(20), " +
                "warranty_months INT, " +
                "mileage BIGINT, " +
                "owner_id VARCHAR(50), " +
                "status VARCHAR(20), " +
                "image_bytes BLOB, " +
                "preferred_start_time TIMESTAMP, " +
                "preferred_end_time TIMESTAMP)");
        stmt.execute("TRUNCATE TABLE items");
    }

    @Test
    @DisplayName("Test luồng thật: Lưu và Tìm Item bằng ID")
    void testSaveAndFindById_RealDB() throws Exception {
        Electronics item = new Electronics();
        item.setId("ITEM_REAL_01");
        item.setName("SmartTV");
        item.setDescription("Samsung 4K");
        item.setStartingPrice(800.0);
        item.setWarrantyMonths(24);

        assertDoesNotThrow(() -> itemDAO.save(item));

        Item fetched = itemDAO.findById("ITEM_REAL_01");
        assertNotNull(fetched);
        assertTrue(fetched instanceof Electronics);
        assertEquals("SmartTV", fetched.getName());
        assertEquals(24, ((Electronics) fetched).getWarrantyMonths());
    }

    @Test
    @DisplayName("Test luồng thật: Cập nhật Item")
    void testUpdate_RealDB() throws Exception {
        Vehicle item = new Vehicle();
        item.setId("ITEM_REAL_02");
        item.setName("OldCar");
        item.setDescription("Toyota");
        item.setStartingPrice(5000.0);
        item.setMileage(150000L);

        itemDAO.save(item);

        item.setName("NewCar");
        item.setMileage(160000L);
        itemDAO.update(item);

        Item fetched = itemDAO.findById("ITEM_REAL_02");
        assertEquals("NewCar", fetched.getName());
        assertEquals(160000L, ((Vehicle) fetched).getMileage());
    }

    @Test
    @DisplayName("Test luồng thật: Tìm kiếm Item theo tên")
    void testSearchByName_RealDB() throws Exception {
        Electronics item = new Electronics();
        item.setId("ITEM_REAL_03");
        item.setName("Macbook Pro");
        item.setDescription("Apple");
        item.setStartingPrice(2000.0);
        item.setWarrantyMonths(12);

        itemDAO.save(item);

        List<Item> results = itemDAO.searchByName("Macbook");
        assertEquals(1, results.size());
        assertEquals("Macbook Pro", results.get(0).getName());
    }
}
