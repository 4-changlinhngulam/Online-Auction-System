package com.auction.server.service;
import com.auction.shared.model.entity.*;
import java.util.Map;
/** Factory Method – tạo Item đúng loại theo category. */
public class ItemFactory {
    public static Item createItem(String category, Map<String, Object> params) {
        // TODO: switch(category) { case "ELECTRONICS" → new Electronics(...); ... }
        throw new IllegalArgumentException("Unknown category: " + category);
    }
}
