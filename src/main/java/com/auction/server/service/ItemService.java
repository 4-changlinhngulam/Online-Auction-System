package com.auction.server.service;

import com.auction.server.dao.ItemDAO;
import com.auction.shared.model.entity.Item;
import com.auction.shared.protocol.Response;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** CRUD nghiệp vụ cho Item. */
public class ItemService {

    private static final Logger LOGGER = Logger.getLogger(ItemService.class.getName());
    private final ItemDAO itemDAO;

    public ItemService() {
        this.itemDAO = new ItemDAO();
    }

    public ItemService(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

    public Response createItem(Item newItem) {
        if (newItem == null) {
            return Response.error("Dữ liệu sản phẩm không hợp lệ.");
        }

        if (newItem.getName() == null || newItem.getName().trim().isEmpty()) {
            return Response.error("Tên sản phẩm không được để trống.");
        }

        if (newItem.getStartingPrice() <= 0) {
            return Response.error("Giá khởi điểm phải lớn hơn 0.");
        }

        try {
            itemDAO.save(newItem);
            return new Response(true, "Sản phẩm đã được tạo thành công: " + newItem.getName(), null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu Item: " + e.getMessage(), e);
            return Response.error("Đã xảy ra lỗi trên máy chủ khi tạo sản phẩm.");
        }
    }

    public Response getAllItems() {
        try {
            List<Item> items = itemDAO.findAll();

            if (items.isEmpty()) {
                return new Response(true, "Hiện tại chưa có sản phẩm nào trong hệ thống.", null);
            }
            return new Response(true, "Lấy danh sách sản phẩm thành công.", items);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy danh sách Item: " + e.getMessage(), e);
            return Response.error("Lỗi máy chủ khi tải danh sách sản phẩm.");
        }
    }

    public Response getItem(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Response.error("ID sản phẩm không được để trống.");
        }

        try {
            Item item = itemDAO.findById(id);
            if (item != null) {
                return new Response(true, "Lấy thông tin sản phẩm thành công.", item);
            } else {
                return Response.error("Không tìm thấy sản phẩm với ID: " + id);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy thông tin Item: " + e.getMessage(), e);
            return Response.error("Đã xảy ra lỗi trên máy chủ khi tải thông tin sản phẩm.");
        }
    }

    public Response updateItem(Item item) {
        if (item == null || item.getId() == null || item.getId().trim().isEmpty()) {
            return Response.error("Dữ liệu cập nhật hoặc ID sản phẩm không hợp lệ.");
        }

        if (item.getName() == null || item.getName().trim().isEmpty()) {
            return Response.error("Tên sản phẩm không được để trống.");
        }

        if (item.getStartingPrice() <= 0) {
            return Response.error("Giá khởi điểm phải lớn hơn 0.");
        }

        try {
            itemDAO.update(item);
            return new Response(true, "Cập nhật sản phẩm thành công.", item);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật Item: " + e.getMessage(), e);
            return Response.error("Đã xảy ra lỗi trên máy chủ khi cập nhật sản phẩm.");
        }
    }

    public Response deleteItem(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Response.error("ID sản phẩm không được để trống.");
        }

        try {
            itemDAO.delete(id);
            return new Response(true, "Xóa sản phẩm thành công.", null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa Item: " + e.getMessage(), e);
            return Response.error("Đã xảy ra lỗi trên máy chủ khi xóa sản phẩm.");
        }
    }

    public Response searchItemsByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Response.error("Vui lòng nhập từ khóa tìm kiếm.");
        }

        try {
            List<Item> items = itemDAO.searchByName(keyword.trim());

            if (items.isEmpty()) {
                return new Response(true, "Không tìm thấy sản phẩm nào khớp với từ khóa: " + keyword, null);
            }
            return new Response(true, "Tìm thấy " + items.size() + " sản phẩm.", items);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi Server khi tìm kiếm sản phẩm: " + e.getMessage(), e);
            return Response.error("Đã xảy ra lỗi máy chủ trong quá trình tìm kiếm.");
        }
    }
}
