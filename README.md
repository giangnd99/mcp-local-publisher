# MCP Local Publisher

Máy chủ MCP cục bộ để agent như Codex, OpenCode hoặc Claude Code chuẩn bị, kiểm tra, xếp lịch và đăng bài Facebook qua Chrome trên máy người dùng.

## Yêu cầu

- Java 21
- Maven 3.8 trở lên
- Docker với Docker Compose để chạy PostgreSQL
- Chrome đã đăng nhập Facebook và MCP bridge cho Chrome nếu cần đăng bài tự động

## Chạy ứng dụng

```sh
docker compose up -d postgres
mvn test package
java -jar posting-container/target/posting-container-0.0.1-SNAPSHOT.jar
```

Mở trang thiết lập tại <http://127.0.0.1:8085>. Máy chủ MCP dùng địa chỉ <http://127.0.0.1:8085/mcp>. Trang thiết lập hướng dẫn cách kết nối với từng agent.

## Chính sách nội dung và nhóm

- Mục tiêu nội dung gồm `BRAND_AWARENESS`, `EDUCATIONAL`, `SOFT_SELL` và `DIRECT_SALE`; mặc định là xây dựng nhận diện thương hiệu.
- Chỉ cho phép nhóm chuyên laptop, PC, máy tính hoặc công nghệ. Nhóm chợ, rao vặt, thanh lý và sang nhượng bị loại, kể cả khi tên nhóm có chữ PC/laptop.
- Ứng dụng yêu cầu nội dung riêng theo nhóm và không tự gửi lại bài khi chưa xác minh được kết quả đăng.

Chi tiết các công cụ MCP nằm trong [mcp-server/README.md](mcp-server/README.md).

## Cấu hình riêng

Các cài đặt trên từng máy được lưu trong `config/posting-settings.properties`. Tệp này bị loại khỏi Git vì có thể chứa thông tin đăng nhập hoặc khóa API. Không đưa khóa API, mật khẩu hay dữ liệu cá nhân vào repo.
