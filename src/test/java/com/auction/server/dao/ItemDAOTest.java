package com.auction.server.dao;

import com.auction.shared.exception.DataPersistenceException;
import com.auction.shared.exception.EntityNotFoundException;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.entity.Vehicle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemDAOTest {

    @Mock
    private DatabaseConnection mockDbConnection;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseConnection> mockedStaticDb;
    private ItemDAO itemDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        itemDAO = new ItemDAO();

        mockedStaticDb = mockStatic(DatabaseConnection.class);
        mockedStaticDb.when(DatabaseConnection::getInstance).thenReturn(mockDbConnection);
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        mockedStaticDb.close();
    }

    @Test
    @DisplayName("Lưu Item: Thành công (Electronics)")
    void testSave_Electronics_Success() throws Exception {
        Electronics item = new Electronics();
        item.setId("ITEM_01");
        item.setName("Laptop");
        item.setDescription("Dell XPS");
        item.setStartingPrice(1500.0);
        item.setWarrantyMonths(12);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> itemDAO.save(item));

        verify(mockPreparedStatement).setString(1, "ITEM_01");
        verify(mockPreparedStatement).setString(2, "Laptop");
        verify(mockPreparedStatement).setString(3, "Dell XPS");
        verify(mockPreparedStatement).setDouble(4, 1500.0);
        verify(mockPreparedStatement).setString(5, "ELECTRONICS");
        verify(mockPreparedStatement).setInt(6, 12);
    }

    @Test
    @DisplayName("Lưu Item: Thất bại do ID null")
    void testSave_Fail_NullId() {
        Electronics item = new Electronics();
        item.setId(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> itemDAO.save(item));
        assertEquals("Item và ID không được phép null", exception.getMessage());
    }

    @Test
    @DisplayName("Lưu Item: Thất bại do Item null")
    void testSave_Fail_NullItem() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> itemDAO.save(null));
        assertEquals("Item và ID không được phép null", exception.getMessage());
    }

    @Test
    @DisplayName("Cập nhật Item: Thành công (Vehicle)")
    void testUpdate_Vehicle_Success() throws Exception {
        Vehicle item = new Vehicle();
        item.setId("ITEM_02");
        item.setName("Car");
        item.setDescription("Tesla Model S");
        item.setStartingPrice(50000.0);
        item.setMileage(10000L);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> itemDAO.update(item));

        verify(mockPreparedStatement).setString(1, "Car");
        verify(mockPreparedStatement).setString(2, "Tesla Model S");
        verify(mockPreparedStatement).setDouble(3, 50000.0);
        verify(mockPreparedStatement).setString(4, "VEHICLE");
        verify(mockPreparedStatement).setLong(6, 10000L);
        verify(mockPreparedStatement).setString(12, "ITEM_02");
    }

    @Test
    @DisplayName("Cập nhật Item: Thất bại do không tìm thấy")
    void testUpdate_NotFound() throws Exception {
        Electronics item = new Electronics();
        item.setId("ITEM_99");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> itemDAO.update(item));
        assertEquals("Không tìm thấy Sản phẩm ID: ITEM_99 để cập nhật.", exception.getMessage());
    }

    @Test
    @DisplayName("Tìm Item bằng ID: Thành công (Electronics)")
    void testFindById_Success() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("item_type")).thenReturn("ELECTRONICS");
        when(mockResultSet.getString("id")).thenReturn("ITEM_01");
        when(mockResultSet.getString("name")).thenReturn("Laptop");
        when(mockResultSet.getString("description")).thenReturn("Dell XPS");
        when(mockResultSet.getDouble("starting_price")).thenReturn(1500.0);
        when(mockResultSet.getInt("warranty_months")).thenReturn(12);

        Item result = itemDAO.findById("ITEM_01");

        assertNotNull(result);
        assertTrue(result instanceof Electronics);
        assertEquals("ITEM_01", result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(12, ((Electronics) result).getWarrantyMonths());
    }

    @Test
    @DisplayName("Tìm Item bằng ID: Thất bại")
    void testFindById_NotFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> itemDAO.findById("ITEM_99"));
        assertEquals("Không tìm thấy Sản phẩm có ID: ITEM_99", exception.getMessage());
    }

    @Test
    @DisplayName("Lấy tất cả Item")
    void testFindAll() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("item_type")).thenReturn("ELECTRONICS", "VEHICLE");
        when(mockResultSet.getString("id")).thenReturn("ITEM_01", "ITEM_02");
        when(mockResultSet.getString("name")).thenReturn("Laptop", "Car");
        when(mockResultSet.getString("description")).thenReturn("Dell XPS", "Tesla");
        when(mockResultSet.getDouble("starting_price")).thenReturn(1500.0, 50000.0);
        when(mockResultSet.getInt("warranty_months")).thenReturn(12, 0);
        when(mockResultSet.getLong("mileage")).thenReturn(0L, 10000L);

        List<Item> items = itemDAO.findAll();

        assertEquals(2, items.size());
        assertTrue(items.get(0) instanceof Electronics);
        assertTrue(items.get(1) instanceof Vehicle);
    }

    @Test
    @DisplayName("Xóa Item: Thành công")
    void testDelete_Success() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> itemDAO.delete("ITEM_01"));
    }

    @Test
    @DisplayName("Xóa Item: Thất bại do không tìm thấy")
    void testDelete_NotFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> itemDAO.delete("ITEM_99"));
        assertEquals("Không tìm thấy Sản phẩm ID: ITEM_99 để xóa.", exception.getMessage());
    }

    @Test
    @DisplayName("Tìm kiếm Item theo tên")
    void testSearchByName() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("item_type")).thenReturn("ELECTRONICS");
        when(mockResultSet.getString("id")).thenReturn("ITEM_01");
        when(mockResultSet.getString("name")).thenReturn("Laptop");
        when(mockResultSet.getString("description")).thenReturn("Dell XPS");
        when(mockResultSet.getDouble("starting_price")).thenReturn(1500.0);
        when(mockResultSet.getInt("warranty_months")).thenReturn(12);

        List<Item> results = itemDAO.searchByName("Lap");

        assertEquals(1, results.size());
        assertEquals("Laptop", results.get(0).getName());
    }
}
