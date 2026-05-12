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
    @DisplayName("Cập nhật Item: Thất bại do thiếu ID")
    void testUpdateItem_Fail_EmptyId() {
        Item item = new Electronics("", "Laptop V2", 2000.0); // ID rỗng

        Response response = itemService.updateItem(item);

        assertFalse(response.isSuccess());
        assertEquals("Dữ liệu cập nhật hoặc ID sản phẩm không hợp lệ.", response.getMessage());
    }

    @Test
    @DisplayName("Xóa Item: Thất bại do ID rỗng")
    void testDeleteItem_Fail_EmptyId() {
        Response response = itemService.deleteItem("   ");

        assertFalse(response.isSuccess());
        assertEquals("ID sản phẩm không được để trống.", response.getMessage());
    }

    @Test
    @DisplayName("Tìm kiếm Item: Thất bại do từ khóa trống")
    void testSearchItems_Fail_EmptyKeyword() {
        Response response = itemService.searchItemsByName("");

        assertFalse(response.isSuccess());
        assertEquals("Vui lòng nhập từ khóa tìm kiếm.", response.getMessage());
        verify(itemDAO, never()).searchByName(anyString());
    }

    @Test
    @DisplayName("Tìm kiếm Item: Thành công nhưng không có kết quả")
    void testSearchItems_Success_NoResult() {
        when(itemDAO.searchByName("Không Tồn Tại")).thenReturn(Arrays.asList());

        Response response = itemService.searchItemsByName("Không Tồn Tại");

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("Không tìm thấy sản phẩm nào"));
    }
}