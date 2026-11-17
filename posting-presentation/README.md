# posting-presentation

Module này chứa giao diện Thymeleaf và bộ điều khiển HTTP nhận thao tác từ người dùng.

## Ranh giới kiến trúc

- `controller` là inbound adapter: nhận yêu cầu web, gọi các use case và chọn trang hiển thị.
- Nghiệp vụ đọc, kiểm tra và lưu cấu hình nằm trong `posting-application-service`.
- `SetupSettingsRepository` và `SetupRuntimeValidator` là port do tầng ứng dụng sở hữu.
- `posting-data-access` lưu cấu hình ra tệp; `posting-automation` kiểm tra Node.js và MCP bridge.
- `posting-container` chỉ lắp ráp các module và cung cấp cấu hình lúc chạy.

Module giao diện không gọi DAO, không tự ghi tệp cấu hình và không phụ thuộc vào chi tiết MCP.
Các hàm Java mới có chú thích tiếng Việt để hỗ trợ bảo trì.
