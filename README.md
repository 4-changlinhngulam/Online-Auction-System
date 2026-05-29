# 🏷️ Online Auction System – LTNC 2026

Hệ thống đấu giá trực tuyến xây dựng theo kiến trúc **Client–Server**, sử dụng **Java**, **JavaFX**, **MVC**, và **Java Sockets**.

## 📐 Kiến trúc tổng quan

```
Client (JavaFX) ←──Socket──→ Server (Multi-threaded)
     │                              │
  Controller                   ClientHandler
     │                              │
   Model ←── DAO              AuctionManager (Singleton)
     │                              │
  FXML (View)               UserService / ItemService
```

## 📁 Cấu trúc dự án

```
Online-Auction-System
├── src
│   ├── main
│   │   ├── java/com/auction
│   │   │   ├── client          # Client application & JavaFX Controllers
│   │   │   ├── server          # Server implementation, DAO, Handlers, Services
│   │   │   └── shared          # Shared entities, enums, exceptions, protocols
│   │   └── resources
│   │       ├── application.properties
│   │       └── com/auction/fxml    # FXML Layouts for Client GUI
│   └── test                    # Unit & Integration Tests (JUnit 5)
├── checkstyle.xml              # Checkstyle rules configuration
└── pom.xml                     # Maven project definition
```

## 🚀 Cách chạy

### Yêu cầu
- Java 17+
- Maven 3.8+
- JavaFX 21

### Chạy Server
```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.auction.server.AuctionServer"
```

### Chạy Client
```bash
mvn exec:java -Dexec.mainClass="com.auction.client.AuctionClientApp"
```

## 🧪 Kiểm thử (Testing)

Dự án sử dụng JUnit 5 để thực hiện các bài kiểm tra tự động (Unit Test & Integration Test) và Checkstyle để đảm bảo tiêu chuẩn định dạng mã nguồn.

### Chạy toàn bộ Test Suite
```bash
mvn test
```

### Chạy kiểm tra định dạng code (Checkstyle)
```bash
mvn checkstyle:check
```

## 👥 Phân công nhóm

| Thành viên | Module |
|------------|--------|
| ...        | Server / Network |
| ...        | Client GUI (JavaFX) |
| ...        | Business Logic / Service |
| ...        | Test / CI/CD |

## 📌 Design Patterns sử dụng
- **Singleton**: `AuctionManager`, `ServerConnection`, `SessionManager`
- **Factory Method**: `ItemFactory`
- **Observer**: Realtime bid update qua Socket
