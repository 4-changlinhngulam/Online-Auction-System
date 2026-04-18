/**
 * 1. Lớp User kế thừa từ Entity và là lớp trừu tượng đại diện cho mọi đối tượng người dùng trong hệ thống.
 * 2. Thuộc tính bao gồm thông tin xác thực cơ bản: name, password, và role (vai trò: Bidder, Seller, Admin).
 * 3. Phương thức getRole() và showMenu() được khai báo abstract để ép buộc các lớp con (vai trò cụ thể) phải tự triển khai logic hiển thị và phân quyền riêng biệt.
 * 4. Chứa logic xác thực đăng nhập chung `login()` có thể được sử dụng cho tất cả các loại người dùng.
 */
package com.auction.shared.model.entity;
public abstract class User extends Entity {
    protected String name; // Sử dụng protected hoặc tạo getter/setter để lớp con có thể truy cập
    protected String password;
    protected String role;

    public User(String name, String password, String role) {
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return this.name;
    }

    public boolean login(String user, String pass) {
        return this.name.equals(user) && this.password.equals(pass);
    }

    public abstract String getRole();
    public abstract void showMenu();
}