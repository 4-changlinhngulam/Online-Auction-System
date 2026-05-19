package com.auction.server.service;
import com.auction.server.dao.UserDAO;
import com.auction.shared.exception.AuthenticationException;
import com.auction.shared.model.entity.User;
import com.auction.shared.protocol.Response;

import java.util.UUID;

/** Nghiệp vụ User: đăng ký, đăng nhập, phân quyền. */
public class UserService {
    // TODO: Inject UserDAO
    // TODO: login(username, password) throws AuthenticationException
    // TODO: register(User user)
    // TODO: getUserById(String id)

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Xử lý đăng nhập
     */
    public Response login(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return Response.error("Tên đăng nhập và mật khẩu không được để trống.");
        }

        try {
            User user = userDAO.findByUsername(username);

            if (user == null) {
                throw new AuthenticationException("Tài khoản không tồn tại.");
            }

            if (!user.getPassword().equals(password)) {
                throw new AuthenticationException("Sai mật khẩu.");
            }

            // Che/Xóa mật khẩu trước khi đóng gói gửi về Client qua mạng
            user.setPassword(null);

            return new Response(true, "Đăng nhập thành công.", user);

        } catch (AuthenticationException authEx) {
            return Response.error(authEx.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi đăng nhập: " + e.getMessage());
            return Response.error("Đã xảy ra lỗi máy chủ trong quá trình đăng nhập.");
        }
    }

    /**
     * Xử lý đăng ký tài khoản mới
     */
    public Response register(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.error("Thông tin đăng ký không hợp lệ.");
        }

        try {
            // 1. Kiểm tra xem username đã có người dùng chưa
            User existingUser = userDAO.findByUsername(user.getUsername());
            if (existingUser != null) {
                return Response.error("Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.");
            }

            // 2. Tạo ID tự động nếu Client chưa tạo
            if (user.getId() == null || user.getId().isEmpty()) {
                user.setId(UUID.randomUUID().toString());
            }
            
            userDAO.save(user);

            // 4. Che mật khẩu trước khi trả về
            user.setPassword(null);

            return new Response(true, "Đăng ký tài khoản thành công.", user);

        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi đăng ký: " + e.getMessage());
            return Response.error("Đã xảy ra lỗi máy chủ trong quá trình đăng ký.");
        }
    }

    /**
     * Lấy thông tin user theo ID
     */
    public Response getUserById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Response.error("ID người dùng không được để trống.");
        }

        try {
            User user = userDAO.findById(id);

            if (user != null) {
                user.setPassword(null); // Che mật khẩu
                return new Response(true, "Lấy thông tin người dùng thành công.", user);
            } else {
                return Response.error("Không tìm thấy người dùng với ID này.");
            }

        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi tải thông tin User: " + e.getMessage());
            return Response.error("Đã xảy ra lỗi máy chủ khi tải thông tin người dùng.");
        }
    }

    /**
     * Cập nhật thông tin User
     */
    public Response updateProfile(User user) {
        if (user == null || user.getId() == null) {
            return Response.error("Dữ liệu người dùng không hợp lệ.");
        }
        try {
            userDAO.update(user);
            user.setPassword(null);
            return new Response(true, "Cập nhật thông tin thành công.", user);
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi cập nhật User: " + e.getMessage());
            return Response.error("Lỗi máy chủ khi cập nhật thông tin.");
        }
    }

    /**
     * Lấy danh sách tất cả User (thường dùng cho Admin)
     */
    public Response getAllUsers() {
        try {
            java.util.List<User> users = userDAO.findAll();
            // Che mật khẩu
            for (User u : users) {
                u.setPassword(null);
            }
            return new Response(true, "Lấy danh sách người dùng thành công.", users);
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi tải danh sách User: " + e.getMessage());
            return Response.error("Lỗi máy chủ khi tải danh sách người dùng.");
        }
    }

    /**
     * Khóa User (Chuyển trạng thái sang BANNED) thay vì xóa hoàn toàn
     */
    public Response banUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Response.error("ID người dùng không được để trống.");
        }
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                return Response.error("Không tìm thấy người dùng.");
            }
            
            user.setStatus(com.auction.shared.model.enums.UserStatus.BANNED);
            userDAO.update(user);
            
            return new Response(true, "Khóa người dùng thành công.", null);
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi khóa User: " + e.getMessage());
            return Response.error("Lỗi máy chủ khi thao tác khóa người dùng.");
        }
    }
}

