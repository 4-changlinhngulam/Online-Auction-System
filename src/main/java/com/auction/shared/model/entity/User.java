package com.auction.shared.model.entity;
import com.auction.shared.model.enums.*;

/**
 * 1. Lớp User kế thừa từ Entity và là lớp trừu tượng đại diện cho mọi đối tượng người dùng trong hệ thống.
 * 2. Thuộc tính bao gồm thông tin xác thực cơ bản: name, password, và role (vai trò: Bidder, Seller, Admin).
 * 3. Phương thức getRole() và showMenu() được khai báo abstract để ép buộc các lớp con (vai trò cụ thể) phải tự triển khai logic hiển thị và phân quyền riêng biệt.
 * 4. Chứa logic xác thực đăng nhập chung `login()` có thể được sử dụng cho tất cả các loại người dùng.
 */
public abstract class User extends Entity {
    protected String name; // Sử dụng protected hoặc tạo getter/setter để lớp con có thể truy cập
    protected String password;
    protected UserRole role;
    protected String email;

    public User(String name, String password, UserRole role,  String email) {
        this.name = name;
        this.password = password;
        this.role = role;
        this.email = email;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {}

    public String getUsername() {
        return this.name;
    }

    public boolean login(String user, String pass) {
        return this.name.equals(user) && this.password.equals(pass);
    }

    public abstract String getRole();
    public abstract void showMenu();
}
