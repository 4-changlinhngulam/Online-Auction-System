# Online Auction System – LTNC 2026

Hệ thống đấu giá trực tuyến được xây dựng theo kiến trúc **Client–Server**, sử dụng **Java**, **JavaFX** làm giao diện người dùng, cấu trúc thiết kế **MVC**, kết nối truyền thông qua **Java Sockets** (TCP/IP), và cơ sở dữ liệu **MySQL**.

---

## 1. Mô tả bài toán & Phạm vi hệ thống

### Mô tả bài toán
Dự án giải quyết bài toán quản lý và thực hiện đấu giá trực tuyến thời gian thực. Hệ thống cho phép người dùng đăng bán sản phẩm, tham gia đấu giá các sản phẩm đang mở, đặt cấu hình tự động đấu giá (Auto-bidding) khi không online, và theo dõi biến động giá cả trực tiếp mà không cần tải lại trang. Đồng thời, hệ thống cung cấp quyền quản trị (Admin) để kiểm soát danh sách người dùng và hoạt động đấu giá nhằm đảm bảo tính minh bạch, lành mạnh.

### Phạm vi hệ thống
- **Mô hình kết nối**: Một Server trung tâm xử lý đồng thời nhiều kết nối từ các Client (Multi-threaded Server).
- **Đồng bộ thời gian thực**: Cập nhật giá thầu, trạng thái phiên đấu giá ngay lập tức tới toàn bộ các Client đang đăng ký theo dõi phiên đấu giá đó thông qua Socket.
- **Lưu trữ dữ liệu**: Hệ thống sử dụng cơ sở dữ liệu quan hệ (MySQL) lưu trữ thông tin lâu dài (Persistent Data) như người dùng, sản phẩm, phiên đấu giá, lịch sử đặt giá và cấu hình tự động.
- **Phân quyền người dùng**: Phân chia rõ ràng 2 vai trò: **User** (người dùng thông thường) và **Admin** (quản trị viên).

---

## 2. Công nghệ sử dụng & Yêu cầu cài đặt

### Công nghệ sử dụng
- **Ngôn ngữ**: Java 17+
- **Giao diện Client**: JavaFX 21 & ControlsFX (hiển thị thông báo Notification)
- **Truyền thông mạng**: TCP Sockets (sử dụng Object Serialization để gửi nhận `Request` / `Response`)
- **Cơ sở dữ liệu**: MySQL (được cấu hình lưu trữ đám mây hoặc local)
- **Thư viện bảo mật**: BCrypt (`jbcrypt`) để mã hóa mật khẩu người dùng
- **Trình quản lý dự án**: Maven 3.8+
- **Kiểm thử**: JUnit 5, Mockito & H2 Database (cho các bài kiểm tra tích hợp chạy bộ nhớ tạm)
- **Định dạng code**: Checkstyle (kiểm soát coding convention)

### Yêu cầu môi trường chạy
- **Java Development Kit (JDK)**: Phiên bản 17 trở lên
- **Apache Maven**: Phiên bản 3.8 trở lên
- **MySQL Server** (nếu tự thiết lập Database local) hoặc sử dụng kết nối Cloud MySQL được cấu hình sẵn trong dự án.

---

## 3. Cấu trúc thư mục dự án

```
Online-Auction-System
├── src
│   ├── main
│   │   ├── java/com/auction
│   │   │   ├── client          # Ứng dụng phía Client & các Controller JavaFX
│   │   │   │   ├── controller  # Điều khiển giao diện (Login, Register, Auction List...)
│   │   │   │   ├── network     # Quản lý kết nối Socket tới Server (ServerConnection)
│   │   │   │   └── util        # Các lớp tiện ích hỗ trợ phía Client
│   │   │   ├── server          # Ứng dụng phía Server & Xử lý Nghiệp vụ
│   │   │   │   ├── dao         # Truy cập Database (User, Item, Auction, BidTransaction...)
│   │   │   │   ├── handler     # Luồng xử lý Socket Client (ClientHandler)
│   │   │   │   └── service     # Logic nghiệp vụ (AuctionManager, UserService, BidService...)
│   │   │   └── shared          # Lớp dùng chung giữa Client & Server
│   │   │       ├── exception   # Định nghĩa các ngoại lệ tùy biến
│   │   │       ├── model       # Các đối tượng thực thể (User, Item, Auction, Bid...)
│   │   │       └── protocol    # Giao thức truyền thông (Request, Response, RequestType)
│   │   └── resources
│   │       ├── application.properties  # Cấu hình kết nối Database
│   │       └── com/auction/fxml        # Giao diện thiết kế FXML cho JavaFX Client
│   └── test                    # Unit & Integration Tests (JUnit 5)
├── checkstyle.xml              # Cấu hình quy tắc kiểm tra định dạng code
└── pom.xml                     # Tệp cấu hình dự án Maven
```

---

## 4. Hướng dẫn chạy chương trình (Thứ tự cụ thể)

Để chương trình hoạt động chính xác, vui lòng thực hiện đúng theo các bước và thứ tự dưới đây:

### Bước 1: Cấu hình Cơ sở dữ liệu (Database)
- Đảm bảo tệp `src/main/resources/application.properties` đã cấu hình đúng URL kết nối, username và password tới cơ sở dữ liệu MySQL của bạn.
- *Lưu ý*: Hiện tại dự án đang cấu hình sẵn kết nối tới Cloud MySQL của nhóm (Aiven Cloud), nên bạn có thể bỏ qua bước cài đặt MySQL local nếu có kết nối mạng internet.

### Bước 2: Khởi động Server
Server cần phải chạy trước để mở Socket lắng nghe kết nối từ các Client.

Mở terminal tại thư mục gốc của dự án và chạy lệnh sau (chạy được trên **Windows, Linux, macOS**):
```bash
mvn clean compile exec:java "-Dexec.mainClass=com.auction.server.AuctionServer"
```
*Lưu ý*: Đối với Windows PowerShell, bạn bắt buộc phải để tham số `-D` trong dấu ngoặc kép `""` như lệnh bên trên (hoặc dùng `cmd` thay thế) để tránh lỗi "Unknown lifecycle phase". Lệnh này tương thích cho cả CMD và Terminal trên macOS/Linux.

### Bước 3: Khởi động Client
Sau khi Server thông báo khởi động thành công và đang lắng nghe (thường là cổng `8080` hoặc cổng được cấu hình), bạn có thể chạy một hoặc nhiều ứng dụng Client.

Mở terminal mới tại thư mục gốc dự án và chạy:
```bash
mvn javafx:run
```
*(Hoặc dùng lệnh đầy đủ tương tự Server):*
```bash
mvn exec:java "-Dexec.mainClass=com.auction.client.AuctionClientApp"
```

---

## 5. Kiểm thử & Đảm bảo mã nguồn (Testing)

Dự án tích hợp các công cụ kiểm thử tự động và kiểm tra định dạng mã nguồn để đảm bảo chất lượng.

### Chạy toàn bộ Test Suite (Unit & Integration Test)
```bash
mvn test
```

### Chạy kiểm tra định dạng code (Checkstyle)
```bash
mvn checkstyle:check
```

---

## 6. Danh sách chức năng đã hoàn thành

Hệ thống đã triển khai đầy đủ các nhóm chức năng cốt lõi sau:

- [x] **Xác thực & Quản lý người dùng**:
  - Đăng ký tài khoản mới (Mã hóa mật khẩu bằng BCrypt).
  - Đăng nhập bảo mật, đăng xuất và quản lý phiên làm việc (Session).
  - Xem và cập nhật thông tin hồ sơ cá nhân (Profile).
- [x] **Quản lý sản phẩm**:
  - Đăng bán sản phẩm mới kèm các thông tin mô tả chi tiết.
  - Cập nhật thông tin hoặc xóa sản phẩm (chỉ đối với sản phẩm chưa được đưa vào phiên đấu giá).
  - Tìm kiếm và lọc danh sách sản phẩm.
- [x] **Quản lý phiên đấu giá**:
  - Tạo phiên đấu giá mới liên kết với sản phẩm, đặt thời gian bắt đầu và kết thúc.
  - Kích hoạt bắt đầu đấu giá, đóng phiên đấu giá khi hết thời gian và cập nhật kết quả người thắng cuộc.
- [x] **Tham gia đặt giá (Bidding)**:
  - Đặt giá thầu trực tiếp theo thời gian thực (giá thầu mới phải lớn hơn giá hiện tại cộng với bước giá tối thiểu).
  - Xem lịch sử đặt giá của từng phiên đấu giá.
- [x] **Đấu giá tự động (Auto-Bidding)**:
  - Cho phép người dùng thiết lập ngân sách tối đa và bước tăng giá tự động.
  - Hệ thống tự động đặt giá thầu thay thế người dùng khi có người khác trả giá cao hơn, cho đến khi đạt giới hạn ngân sách.
- [x] **Quản trị hệ thống (Admin)**:
  - Xem danh sách toàn bộ người dùng trong hệ thống.
  - Khóa (Ban) hoặc mở khóa tài khoản vi phạm.
- [x] **Đồng bộ thời gian thực**:
  - Sử dụng cơ chế Subscribe để tự động đẩy thông tin cập nhật (giá thầu mới, đếm ngược thời gian...) tới các Client đang xem phiên đấu giá mà không cần reload.

---

## 7. Phân công nhóm & Mẫu thiết kế sử dụng

### Phân công thành viên
| Thành viên | Nhiệm vụ chính |
|------------|----------------|
| Nguyễn Viết Ngọc Duy | Thiết kế & phát triển kiến trúc Socket Client-Server, các lớp DAO & Services cốt lõi, cơ chế đồng bộ thời gian thực (Observer), Auto-bidding, Anti-sniping và giao diện Admin |
| Đặng Quốc Khánh | Thiết kế CSDL MySQL, cấu trúc MVC ban đầu, giao diện & logic Đăng nhập/Đăng ký, biểu mẫu thêm sản phẩm và chi tiết đấu giá |
| Đặng Tiến Đạt | Thiết lập in-memory H2 database test, viết 94 Unit & Integration Tests, kiểm thử Concurrency, cấu hình Checkstyle & CI/CD GitHub Actions |

### Design Patterns áp dụng
- **Singleton Pattern**: Áp dụng tại `AuctionManager` (phía Server), `ServerConnection` (phía Client), `SessionManager` để quản lý tập trung và duy nhất các luồng dữ liệu chính.
- **Factory Method Pattern**: Áp dụng tại `ItemFactory` để tạo đối tượng sản phẩm dựa trên các tiêu chí cụ thể.
- **Observer Pattern**: Triển khai thông qua kết nối Socket để cập nhật thời gian thực sự thay đổi giá thầu phiên đấu giá từ Server đến toàn bộ Client đang theo dõi.

---
## 8. Link báo cáo & video demo
- Link báo cáo: [Bao_Cao_Bai_Tap_Lon_OOP_Auction_System.pdf](https://drive.google.com/file/d/1ClKH3G5__VLyf6K4NPxGqRk-N15X1x11/view?usp=sharing)
- Link video demo: [Video Demo](https://drive.google.com/file/d/1CaGWrnCBO1_5jM8wYC85RuQTHslnK9uT/view?usp=sharing)
