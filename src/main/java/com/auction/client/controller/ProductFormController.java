package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import com.auction.client.network.ServerConnection;
import com.auction.shared.model.entity.Item;
import com.auction.shared.model.entity.Art;
import com.auction.shared.model.entity.Electronics;
import com.auction.shared.model.entity.Vehicle;
import com.auction.shared.protocol.Request;
import com.auction.shared.protocol.RequestType;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ProductFormController {

    // ================= CÁC TRƯỜNG THÔNG TIN CƠ BẢN =================
    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField priceField;
    @FXML private Label errorLabel;

    // ================= THÀNH PHẦN CHỌN ẢNH =================
    @FXML private ImageView productImageView;
    @FXML private Label imagePlaceholderLabel;
    @FXML private Label imagePathLabel;
    private File selectedImageFile;

    // ================= THÀNH PHẦN THỜI GIAN =================
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField endTimeField;

    @FXML
    public void initialize() {
        // Khởi tạo ComboBox
        categoryCombo.setItems(FXCollections.observableArrayList(
                "ELECTRONICS", "ART", "VEHICLE"));
        categoryCombo.setValue("ELECTRONICS");

        // Reset nhãn lỗi
        errorLabel.setText("");

        // Thiết lập giá trị mặc định cho DatePicker (Ngày hôm nay)
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(7)); // Mặc định đấu giá 1 tuần
        startTimeField.setText("08:00");
        endTimeField.setText("20:00");
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");

        // Lọc chỉ cho phép chọn tệp hình ảnh
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Lấy cửa sổ hiện tại (Window) để hiển thị dialog
        File file = fileChooser.showOpenDialog(nameField.getScene().getWindow());

        if (file != null) {
            selectedImageFile = file;
            imagePathLabel.setText(file.getName());

            // Hiển thị ảnh lên giao diện
            Image image = new Image(file.toURI().toString());
            productImageView.setImage(image);
            imagePlaceholderLabel.setVisible(false); // Ẩn chữ "Chưa chọn ảnh"
        }
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();
        String category = categoryCombo.getValue();
        String priceStr = priceField.getText().trim();

        // 1. Validate thông tin cơ bản
        if (name.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin cơ bản!");
            return;
        }

        // 2. Validate và Parse Thời gian
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String startTimeStr = startTimeField.getText().trim();
            String endTimeStr = endTimeField.getText().trim();

            if (startDate == null || endDate == null || startTimeStr.isEmpty() || endTimeStr.isEmpty()) {
                errorLabel.setText("Vui lòng nhập đầy đủ ngày và giờ!");
                return;
            }

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime startTime = LocalTime.parse(startTimeStr, timeFormatter);
            LocalTime endTime = LocalTime.parse(endTimeStr, timeFormatter);

            startDateTime = LocalDateTime.of(startDate, startTime);
            endDateTime = LocalDateTime.of(endDate, endTime);

            if (!startDateTime.isBefore(endDateTime)) {
                errorLabel.setText("Lỗi: Thời gian bắt đầu phải trước thời gian kết thúc!");
                return;
            }
            if (endDateTime.isBefore(LocalDateTime.now())) {
                errorLabel.setText("Lỗi: Thời gian kết thúc không được trong quá khứ!");
                return;
            }

        } catch (DateTimeParseException e) {
            errorLabel.setText("Định dạng giờ không hợp lệ! Vui lòng nhập kiểu HH:mm (VD: 14:30)");
            return;
        }

        // 3. Xử lý giá tiền và Khởi tạo Item
        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                errorLabel.setText("Giá khởi điểm phải lớn hơn 0!");
                return;
            }

            Item item = null;
            switch (category) {
                case "ART":
                    item = new Art();
                    break;
                case "ELECTRONICS":
                    item = new Electronics();
                    break;
                case "VEHICLE":
                    item = new Vehicle();
                    break;
                default:
                    errorLabel.setText("Danh mục không hợp lệ!");
                    return;
            }

            // Gán các thông tin
            item.setName(name);
            item.setDescription(desc);
            item.setStartingPrice(price);

            /* * TODO CẦN LƯU Ý CHO BACKEND:
             * Nếu class `Item` hoặc `Auction` của bạn có hỗ trợ lưu trữ thời gian và ảnh,
             * bạn cần gán thêm các thuộc tính ở đây. Ví dụ:
             * * item.setStartTime(startDateTime);
             * item.setEndTime(endDateTime);
             * * Nếu selectedImageFile != null, bạn có thể truyền đường dẫn ảnh hoặc
             * chuyển ảnh sang dạng Base64 để gửi qua mạng tùy thuộc vào cách server thiết kế:
             * item.setImageUrl(selectedImageFile.getAbsolutePath());
             */

            // 4. Gửi Request lên Server
            ServerConnection.getInstance().sendRequestAsync(
                    new Request(RequestType.CREATE_ITEM, item),
                    response -> {
                        Platform.runLater(() -> {
                            if (response.isSuccess()) {
                                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Lưu sản phẩm thành công!");
                                SceneManager.switchTo("/com/auction/fxml/product/product-manage.fxml");
                            } else {
                                errorLabel.setText(response.getMessage());
                            }
                        });
                    }
            );

        } catch (NumberFormatException e) {
            errorLabel.setText("Giá không hợp lệ! Vui lòng chỉ nhập số.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        SceneManager.switchTo("/com/auction/fxml/product/product-manage.fxml");
    }

}

