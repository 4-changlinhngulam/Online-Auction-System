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
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.ByteArrayInputStream;

public class ProductFormController {

    public static Item editingItem; // Thêm cờ để biết là đang tạo mới hay sửa


    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField priceField;
    @FXML private Label errorLabel;

    @FXML private ImageView productImageView;
    @FXML private Label imagePlaceholderLabel;
    @FXML private Label imagePathLabel;
    private File selectedImageFile;

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

        // Nếu đang trong chế độ sửa, điền sẵn thông tin cũ
        if (editingItem != null) {
            nameField.setText(editingItem.getName());
            descField.setText(editingItem.getDescription());
            priceField.setText(String.format("%.0f", editingItem.getStartingPrice()));
            
            if (editingItem instanceof Art) categoryCombo.setValue("ART");
            else if (editingItem instanceof Vehicle) categoryCombo.setValue("VEHICLE");
            else categoryCombo.setValue("ELECTRONICS");
            categoryCombo.setDisable(true); // Không cho phép đổi loại sản phẩm khi đã tạo

            if (editingItem.getPreferredStartTime() != null) {
                startDatePicker.setValue(editingItem.getPreferredStartTime().toLocalDate());
                startTimeField.setText(editingItem.getPreferredStartTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            if (editingItem.getPreferredEndTime() != null) {
                endDatePicker.setValue(editingItem.getPreferredEndTime().toLocalDate());
                endTimeField.setText(editingItem.getPreferredEndTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            if (editingItem.getImageBytes() != null) {
                Image img = new Image(new ByteArrayInputStream(editingItem.getImageBytes()));
                productImageView.setImage(img);
                imagePlaceholderLabel.setVisible(false);
                imagePathLabel.setText("(Đã có ảnh cũ)");
            }
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files (PNG, JPG, GIF)", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(nameField.getScene().getWindow());

        if (file != null) {
            selectedImageFile = file;
            imagePathLabel.setText(file.getName());

            Image image = new Image(file.toURI().toString());
            if (image.isError()) {
                errorLabel.setText("Lỗi: Định dạng ảnh không được hỗ trợ (VD: webp, jfif) hoặc file hỏng.");
                selectedImageFile = null;
                imagePathLabel.setText("Chưa chọn ảnh");
                productImageView.setImage(null);
                imagePlaceholderLabel.setVisible(true);
                return;
            }
            
            errorLabel.setText("");
            productImageView.setImage(image);
            imagePlaceholderLabel.setVisible(false);
        }
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();
        String category = categoryCombo.getValue();
        String priceStr = priceField.getText().trim();

        if (name.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin cơ bản!");
            return;
        }

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
            if (java.time.Duration.between(startDateTime, endDateTime).toDays() > 7) {
                errorLabel.setText("Lỗi: Thời gian đấu giá tối đa là 7 ngày!");
                return;
            }

        } catch (DateTimeParseException e) {
            errorLabel.setText("Định dạng giờ không hợp lệ! Vui lòng nhập kiểu HH:mm (VD: 14:30)");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                errorLabel.setText("Giá khởi điểm phải lớn hơn 0!");
                return;
            }

            Item item;
            if (editingItem != null) {
                item = editingItem;
            } else {
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
            }

            item.setName(name);
            item.setDescription(desc);
            item.setStartingPrice(price);
            
            item.setPreferredStartTime(startDateTime);
            item.setPreferredEndTime(endDateTime);

            if (selectedImageFile != null) {
                try {
                    byte[] imageBytes = Files.readAllBytes(selectedImageFile.toPath());
                    item.setImageBytes(imageBytes);
                } catch (IOException e) {
                    errorLabel.setText("Lỗi khi đọc file ảnh: " + e.getMessage());
                    return;
                }
            }

            if (editingItem != null) {
                item.setStatus("PENDING");
            }

            RequestType reqType = (editingItem != null) ? RequestType.UPDATE_ITEM : RequestType.CREATE_ITEM;

            ServerConnection.getInstance().sendRequestAsync(
                    new Request(reqType, item),
                    response -> {
                        Platform.runLater(() -> {
                            if (response.isSuccess()) {
                                showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                                        editingItem != null ? "Cập nhật sản phẩm thành công, chờ Admin duyệt lại!" : "Lưu sản phẩm thành công!");
                                editingItem = null;
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
        editingItem = null;
        SceneManager.switchTo("/com/auction/fxml/product/product-manage.fxml");
    }

}

