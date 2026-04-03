public class ItemFactory {

    // Enum để chuẩn hóa đầu vào
    public enum ItemType {
        ELECTRONICS, ART, VEHICLE
    }

    // Factory method
    public static Item createItem(ItemType type, String id, String name, double startingPrice) {
        if (type == null) {
            throw new IllegalArgumentException("Item type cannot be null");
        }

        // If-else nhưng gọn hơn, lấy đầu vào ItemType type và trả về đối tượng tương ứng
        switch (type) {
            case ELECTRONICS:
                return new Electronics(id, name, startingPrice);
            case ART:
                return new Art(id, name, startingPrice);
            case VEHICLE:
                return new Vehicle(id, name, startingPrice);
            default:
                throw new IllegalArgumentException("Unknown Item Type: " + type);
        }
    }
}