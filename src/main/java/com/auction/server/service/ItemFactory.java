package com.auction.server.service;

import com.auction.shared.model.entity.*;
import com.auction.shared.model.enums.ItemType;

public class ItemFactory {

    /**
     * 1. Factory Method nhận Enum làm đầu vào và khởi tạo đối tượng Item tương ứng.
     * 2. Khởi tạo đối tượng ngay lập tức với các thông số id, name, startingPrice thông qua constructor.
     */
    public static Item createItem(ItemType type, String id, String name, double startingPrice) {
        if (type == null) {
            throw new IllegalArgumentException("Loại sản phẩm không được để trống!");
        }

        return switch (type) {
            case ELECTRONICS -> new Electronics(id, name, startingPrice);
            case ART         -> new Art(id, name, startingPrice);
            case VEHICLE     -> new Vehicle(id, name, startingPrice);
            default          -> throw new IllegalArgumentException("Không hỗ trợ: " + type);
        };
    }
}