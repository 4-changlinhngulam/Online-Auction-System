package com.auction.shared.model.entity;

/**
 * 1. Admin là lớp có quyền hạn cao nhất trong hệ thống để quản lý tài khoản và luồng hoạt động, kế thừa từ User.
 * 2. Có quyền kiểm duyệt (moderateItem) để phê duyệt hoặc từ chối sản phẩm được đăng lên.
 * 3. Quản lý an toàn hệ thống thông qua các hàm như cấm người dùng (banUser) hoặc gỡ bỏ các phiên đấu giá vi phạm (removeInvalidAuction).
 * 4. Có khả năng truy xuất lịch sử toàn bộ hệ thống bằng hàm `reviewAllTransactions()`.
 */
public class Admin extends User {

    public Admin(String username) {
        super(username);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public void showMenu() {
        // Logic hiển thị menu cho admin
    }

    public void banUser() {
        // Quản lý/khóa người dùng vi phạm
    }

    public void moderateItem() {
        // Quản lý/kiểm duyệt sản phẩm (chuyển PENDING sang APPROVED hoặc REJECTED)
    }

    public void removeInvalidAuction() {
        // Quản lý phiên đấu giá, xóa bỏ phiên không hợp lệ
    }

    public void reviewAllTransactions() {
        // Xem lại toàn bộ giao dịch
    }
}
