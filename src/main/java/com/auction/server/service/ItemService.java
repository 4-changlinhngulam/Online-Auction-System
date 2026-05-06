package com.auction.server.service;
import com.auction.server.dao.ItemDAO;
import com.auction.shared.model.entity.Item;
import com.auction.shared.protocol.Response;

import java.util.List;

/** CRUD nghiệp vụ cho Item. */
public class ItemService {
    // TODO: Inject ItemDAO
    // TODO: createItem / getItem / updateItem / deleteItem / getAllItems

    private final ItemDAO itemDAO;

    public ItemService() {
        this.itemDAO = new ItemDAO();
    }

    // Xử lý yêu cầu tạo sản phẩm mới.
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
            System.err.println("Lỗi khi lưu Item: " + e.getMessage());
            return Response.error("Đã xảy ra lỗi trên máy chủ khi tạo sản phẩm.");
        }
    }


    // Lấy danh sách tất cả sản phẩm.

    public Response getAllItems() {
        try {
            List<Item> items = itemDAO.findAll();

            if (items.isEmpty()) {
                return new Response(true, "Hiện tại chưa có sản phẩm nào trong hệ thống.", null);
            }
            return new Response(true, "Lấy danh sách sản phẩm thành công.", items);

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách Item: " + e.getMessage());
            return Response.error("Lỗi máy chủ khi tải danh sách sản phẩm.");
        }
    }

    // Lấy thông tin chi tiết của một sản phẩm dựa vào ID.

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
            System.err.println("Lỗi khi lấy thông tin Item: " + e.getMessage());
            return Response.error("Đã xảy ra lỗi trên máy chủ khi tải thông tin sản phẩm.");
        }
    }

    // Cập nhật thông tin của một sản phẩm đã có.

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
            System.err.println("Lỗi khi cập nhật Item: " + e.getMessage());
            return Response.error("Đã xảy ra lỗi trên máy chủ khi cập nhật sản phẩm.");
        }
    }

    // Xóa một sản phẩm khỏi hệ thống dựa vào ID.

    public Response deleteItem(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Response.error("ID sản phẩm không được để trống.");
        }

        try {
            itemDAO.delete(id);
            return new Response(true, "Xóa sản phẩm thành công.", null);
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa Item: " + e.getMessage());
            return Response.error("Đã xảy ra lỗi trên máy chủ khi xóa sản phẩm.");
        }
    }

    public Response searchItemsByName(String keyword) {
        // Validation: Kiểm tra từ khóa
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
            System.err.println("Lỗi Server khi tìm kiếm sản phẩm: " + e.getMessage());
            return Response.error("Đã xảy ra lỗi máy chủ trong quá trình tìm kiếm.");
        }
    }
}
