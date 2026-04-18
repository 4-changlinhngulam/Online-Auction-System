public class ItemFactory {

    /**
     * 1. Enum ItemType giúp chuẩn hóa các loại sản phẩm hợp lệ trong hệ thống.
     * 2. Ngăn chặn triệt để lỗi do truyền sai chuỗi (ví dụ: "ELECTRONIC" thay vì "ELECTRONICS").
     */
    public enum ItemType {
        ELECTRONICS, ART, VEHICLE
    }

    /**
     * 3. Factory Method nhận Enum làm đầu vào và khởi tạo đối tượng Item tương ứng.
     * 4. Áp dụng Switch Expression (Dấu mũi tên '->') giúp code gọn gàng, trả về (return) trực tiếp
     * mà không cần viết lặp lại chữ 'return' hay 'break' ở từng case.
     * 5. Khởi tạo đối tượng ngay lập tức với các thông số id, name, startingPrice thông qua constructor.
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