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

Xem file `PROJECT_STRUCTURE.md` để biết chi tiết từng file.

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
