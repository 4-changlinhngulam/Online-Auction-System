package com.auction.server.service;

import com.auction.server.dao.ItemDAO;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Item;
import com.auction.shared.protocol.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    @Mock
    private ItemDAO itemDAO;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        itemService = new ItemService(itemDAO);
    }


    @Test
    @DisplayName("Tạo Item: Thành công")
    void testCreateItem_Success() throws Exception {
        Item item = new Electronics("ITM_01", "Laptop Gaming", 1500.0);

        doNothing().when(itemDAO).save(any(Item.class));

        Response response = itemService.createItem(item);

        assertTrue(response.isSuccess());
        verify(itemDAO, times(1)).save(item);
    }

    @Test
    @DisplayName("Tạo Item: Thất bại khi giá khởi điểm biên bằng hoặc biên dưới sát nút (BVA: <= 0)")
    void testCreateItem_BVA_InvalidPrices() {
        // Biên bằng: 0.0
        Item itemZero = new Electronics("ITM_01", "Hàng biên 0", 0.0);
        Response responseZero = itemService.createItem(itemZero);
        assertFalse(responseZero.isSuccess());
        assertEquals("Giá khởi điểm phải lớn hơn 0.", responseZero.getMessage());

        // Biên dưới: -0.01
        Item itemNegative = new Electronics("ITM_01", "Hàng biên âm", -0.01);
        Response responseNegative = itemService.createItem(itemNegative);
        assertFalse(responseNegative.isSuccess());
        assertEquals("Giá khởi điểm phải lớn hơn 0.", responseNegative.getMessage());

        verify(itemDAO, never()).save(any(Item.class));
    }

    @Test
    @DisplayName("Tạo Item: Thành công khi giá khởi điểm biên trên sát nút (BVA: > 0)")
    void testCreateItem_BVA_ValidBoundaryPrice() throws Exception {
        // Biên trên: 0.01
        Item itemValid = new Electronics("ITM_01", "Hàng biên dương nhỏ", 0.01);
        doNothing().when(itemDAO).save(any(Item.class));

        Response response = itemService.createItem(itemValid);

        assertTrue(response.isSuccess());
        verify(itemDAO, times(1)).save(itemValid);
    }

    @Test
    @DisplayName("Tạo Item: Thất bại do tên trống")
    void testCreateItem_Fail_EmptyName() {
        Item item = new Electronics("ITM_01", "   ", 100.0);

        Response response = itemService.createItem(item);

        assertFalse(response.isSuccess());
        assertEquals("Tên sản phẩm không được để trống.", response.getMessage());
        verify(itemDAO, never()).save(any(Item.class));
    }


    @Test
    @DisplayName("Lấy chi tiết Item: Thành công")
    void testGetItem_Success() throws Exception {
        Item item = new Electronics("ITM_01", "Tai nghe", 50.0);
        when(itemDAO.findById("ITM_01")).thenReturn(item);

        Response response = itemService.getItem("ITM_01");

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("Tai nghe", ((Item) response.getData()).getName());
    }

    @Test
    @DisplayName("Lấy chi tiết Item: Không tìm thấy (ID không tồn tại)")
    void testGetItem_Fail_NotFound() throws Exception {
        when(itemDAO.findById("FAKE_ID")).thenReturn(null);

        Response response = itemService.getItem("FAKE_ID");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Không tìm thấy sản phẩm"));
    }


    @Test
    @DisplayName("Cập nhật Item: Thành công")
    void testUpdateItem_Success() throws Exception {
        Item item = new Electronics("ITM_01", "Laptop V2", 2000.0);
        doNothing().when(itemDAO).update(item);

        Response response = itemService.updateItem(item);

        assertTrue(response.isSuccess());
        assertEquals("Cập nhật sản phẩm thành công.", response.getMessage());
        verify(itemDAO, times(1)).update(item);
    }

    // ============================================
    // TEST DELETE ITEM
    // ============================================

    @Test
    @DisplayName("Xóa Item: Thành công")
    void testDeleteItem_Success() throws Exception {
        doNothing().when(itemDAO).delete("ITM_01");

        Response response = itemService.deleteItem("ITM_01");

        assertTrue(response.isSuccess());
        verify(itemDAO, times(1)).delete("ITM_01");
    }


    @Test
    @DisplayName("Tìm kiếm Item: Có dữ liệu trả về")
    void testSearchItems_Success() throws Exception {
        Item item1 = new Electronics("1", "Asus Laptop", 1000);
        Item item2 = new Electronics("2", "Dell Laptop", 1200);

        when(itemDAO.searchByName("Laptop")).thenReturn(Arrays.asList(item1, item2));

        Response response = itemService.searchItemsByName("Laptop");

        assertTrue(response.isSuccess());
        List<?> results = (List<?>) response.getData();
        assertEquals(2, results.size());
    }
}