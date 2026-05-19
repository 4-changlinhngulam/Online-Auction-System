package com.auction.shared.model.entity;

import com.auction.server.service.ItemFactory;
import com.auction.shared.model.enums.ItemType;
import com.auction.shared.model.enums.UserRole;

/**
 * Lớp Seller kế thừa User, đóng vai trò là người cung cấp sản phẩm để đấu giá.
 */
public class Seller extends User {

    public Seller(String username, String password, String email) {
        super(username, password, UserRole.SELLER, email);
    }

    public Seller() {
        super();
        // Constructor mặc định cho các thư viện mapping
    }

    @Override
    public String getRole() {
        return "SELLER";
    }

    public Item postNewProduct(ItemType itemType, String id, String name, double startingPrice) {
        // Xử lý kiểu của Item thông qua Factory
        Item newItem = ItemFactory.createItem(itemType, id, name, startingPrice);
        newItem.setStatus("PENDING");
        return newItem;
    }
}
