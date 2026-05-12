package com.auction.server.service;

import com.auction.server.dao.ItemDAO;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Item;
import com.auction.shared.protocol.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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
        @DisplayName("Tạo Item thành công")
        void testCreateItem_Success() throws Exception {
            Item item = new Electronics("Điện thoại", "Hàng mới", 500.0);

            // Cấu hình mock (ItemDAO.save trả về void, nên doNothing() là mặc định)
            doNothing().when(itemDAO).save(any(Item.class));

            Response response = itemService.createItem(item);

            assertTrue(response.isSuccess());
            verify(itemDAO, times(1)).save(item);
        }

        @Test
        @DisplayName("Lấy danh sách tất cả Items")
        void testGetAllItems_Success() throws Exception {
            Item item1 = new Electronics("Laptop", "Mới", 1000.0);
            Item item2 = new Electronics("Tablet", "Cũ", 300.0);

            when(itemDAO.findAll()).thenReturn(Arrays.asList(item1, item2));

            Response response = itemService.getAllItems();

            assertTrue(response.isSuccess());
            List<?> items = (List<?>) response.getData();
            assertEquals(2, items.size());
            verify(itemDAO, times(1)).findAll();
        }
}
