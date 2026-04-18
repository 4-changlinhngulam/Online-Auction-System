package com.auction.shared.model.entity;

/**
 * Lớp Seller kế thừa User, đóng vai trò là người cung cấp sản phẩm để đấu giá.
 */
public class Seller extends User {

    public Seller(String username) {
        super(username);
    }

    @Override
    public String getRole() {
        return "SELLER";
    }

    @Override
    public void showMenu() {
        // Logic hiển thị menu của người bán
    }

    public Item postNewProduct(ItemFactory.ItemType itemType, String id, String name, double startingPrice) {
        Item newItem = ItemFactory.createItem(itemType, id, name, startingPrice); // Xử lý kiểu của Item sẽ đẩy vào main
        if (newItem != null) {
            newItem.setStatus("PENDING");
        }
        return newItem;
    }

    public void editProduct() {
        // Chỉnh sửa thông tin sản phẩm
    }

    public void removeProduct() {
        // Xóa sản phẩm
    }

    public void trackMyAuctions() {
        // Xem các phiên đấu giá của mình
    }
}
