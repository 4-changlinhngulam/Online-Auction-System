package com.auction.server.service;

import com.auction.server.dao.UserDAO;
import com.auction.shared.exception.AuthenticationException;
import com.auction.shared.model.entity.User;
import com.auction.shared.protocol.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Nghiệp vụ User: đăng ký, đăng nhập, phân quyền. */
public class UserService {

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Response login(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return Response.error("Tên đăng nhập và mật khẩu không được để trống.");
        }

        try {
            User user = userDAO.findByUsername(username);

            if (user == null) {
                throw new AuthenticationException("Tài khoản không tồn tại.");
            }

            if (!BCrypt.checkpw(password, user.getPassword())) {
                throw new AuthenticationException("Sai mật khẩu.");
            }

            user.setPassword(null);

            return new Response(true, "Đăng nhập thành công.", user);

        } catch (AuthenticationException authEx) {
            return Response.error(authEx.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi đăng nhập: " + e.getMessage(), e);
            return Response.error("Đã xảy ra lỗi máy chủ trong quá trình đăng nhập.");
        }
    }

    public Response register(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.error("Thông tin đăng ký không hợp lệ.");
        }

        try {
            User existingUser = userDAO.findByUsername(user.getUsername());
            if (existingUser != null) {
                return Response.error("Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.");
            }

            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);

            userDAO.save(user);

            user.setPassword(null);

            return new Response(true, "Đăng ký tài khoản thành công.", user);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi đăng ký: " + e.getMessage(), e);
            return Response.error("Đã xảy ra lỗi máy chủ trong quá trình đăng ký.");
        }
    }

    public Response getUserById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Response.error("ID người dùng không được để trống.");
        }

        try {
            User user = userDAO.findById(id);

            if (user != null) {
                user.setPassword(null);
                return new Response(true, "Lấy thông tin người dùng thành công.", user);
            } else {
                return Response.error("Không tìm thấy người dùng với ID này.");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi tải thông tin User: " + e.getMessage(), e);
            return Response.error("Đã xảy ra lỗi máy chủ khi tải thông tin người dùng.");
        }
    }

    public Response updateProfile(User user) {
        if (user == null || user.getId() == null) {
            return Response.error("Dữ liệu người dùng không hợp lệ.");
        }
        try {
            User existingUser = userDAO.findById(user.getId());

            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setPassword(existingUser.getPassword());
            } else {
                String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
                user.setPassword(hashedPassword);
            }

            userDAO.update(user);
            user.setPassword(null);
            return new Response(true, "Cập nhật thông tin thành công.", user);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi cập nhật User: " + e.getMessage(), e);
            return Response.error("Lỗi máy chủ khi cập nhật thông tin.");
        }
    }

    public Response getAllUsers() {
        try {
            java.util.List<User> users = userDAO.findAll();
            for (User u : users) {
                u.setPassword(null);
            }
            return new Response(true, "Lấy danh sách người dùng thành công.", users);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi tải danh sách User: " + e.getMessage(), e);
            return Response.error("Lỗi máy chủ khi tải danh sách người dùng.");
        }
    }

    public Response banUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Response.error("ID người dùng không được để trống.");
        }
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                return Response.error("Không tìm thấy người dùng.");
            }
            if ("ADMIN".equals(user.getRole())) {
                return Response.error("Không thể khóa tài khoản Quản trị viên.");
            }

            user.setStatus(com.auction.shared.model.enums.UserStatus.BANNED);
            userDAO.update(user);

            return new Response(true, "Khóa người dùng thành công.", null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi khóa User: " + e.getMessage(), e);
            return Response.error("Lỗi máy chủ khi thao tác khóa người dùng.");
        }
    }
}
