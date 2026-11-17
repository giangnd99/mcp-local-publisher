# mcp-server

Module inbound adapter cung cấp công cụ Posting Management qua MCP Streamable HTTP.
Module phụ thuộc vào application use case và không gọi DAO hay mã giao diện.

## Địa chỉ

Khi ứng dụng chạy cục bộ, MCP endpoint mặc định là `http://127.0.0.1:8085/mcp`.
Địa chỉ chỉ lắng nghe trên loopback vì MCP HTTP không tự bật xác thực.

## Công cụ

- `list_groups`: đọc các nhóm đã lưu; nhóm ngoài chủ đề hoặc bị khóa sẽ có `postingAllowed=false`.
- `find_groups`: chỉ tìm trong nhóm laptop, PC, máy tính hoặc công nghệ đang bật quyền đăng.
- `list_campaigns`, `get_campaign`: xem chiến dịch đã có.
- `create_campaign`: tạo chiến dịch với nội dung riêng cho từng nhóm; nếu `text` để rỗng, ứng dụng soạn theo mục tiêu bằng AI hoặc mẫu cục bộ; chưa đăng.
- `validate_campaign`: kiểm tra trước khi xếp lịch.
- `queue_campaign`: xếp bài theo nhịp đăng; chưa đăng.
- `dispatch_next_post`: thực hiện đăng thật một bài đến hạn qua Chrome.
- `verify_post`: tìm bài trong feed đúng nhóm khi kết quả đăng chưa xác định hoặc đang chờ duyệt.

Các tool chỉ gọi use case trong `posting-application-service`. Trước khi tạo chiến dịch,
agent dùng `find_groups`/`list_groups` để chọn nhóm đã lưu. Khi đăng, ứng dụng đối chiếu
mã nhóm trên URL và tên tiêu đề Facebook trước khi mở composer. Sau khi gửi, chỉ đánh
dấu LIVE nếu tìm thấy câu riêng của bài trong một thẻ bài thuộc feed; nếu chưa thấy thì
giữ trạng thái SUBMITTING để gọi `verify_post`, tránh tự đăng lại và tạo bài trùng.

Tùy chọn soạn bằng OpenAI nằm trong trang thiết lập. Khi bật, nội dung còn trống sẽ được
soạn riêng theo từng nhóm và được rà soát trước khi lưu; khi tắt, ứng dụng dùng mẫu cục
bộ. Sau khi đổi tùy chọn hoặc API key cần khởi động lại ứng dụng. `find_groups` chỉ tra
các nhóm đã lưu trong ứng dụng; nó không tự tìm kiếm trên toàn bộ Facebook.

### Mục tiêu nội dung

`create_campaign` nhận `contentObjective` tùy chọn; mặc định là `BRAND_AWARENESS`:

- `BRAND_AWARENESS`: giúp người đọc nhớ thương hiệu và cách thương hiệu hỗ trợ họ; không ép giá hoặc số liên hệ vào bài.
- `EDUCATIONAL`: chia sẻ kiến thức chọn, sử dụng hoặc chăm sóc máy tính; gắn tự nhiên với định vị thương hiệu.
- `SOFT_SELL`: nêu tình huống sử dụng, giới thiệu sản phẩm như lựa chọn tham khảo và mời trao đổi nhẹ nhàng.
- `DIRECT_SALE`: giới thiệu giá, bảo hành và cách liên hệ để người đọc cân nhắc mua.

Có thể truyền `brandName`, `brandPromise` và `productFactsJson` để cung cấp định vị cùng dữ kiện đã xác nhận. Với Laptop GTH, nên dùng thông điệp “Công nghệ đúng với bạn”, tư vấn theo nhu cầu/ngân sách và thông tin rõ trước khi quyết định. Chỉ bài `DIRECT_SALE` mới bắt buộc giá; bảo hành/liên hệ đã cung cấp phải khớp. Mọi chiến dịch mới đều phải nhắc đúng tên thương hiệu. Các chiến dịch đã có trước khi nâng cấp giữ quy tắc cũ để không làm hỏng nội dung đang chờ.

Định vị này lấy từ [website Laptop GTH](https://www.laptopgth.com.vn/). Meta cho biết các caption dài gây xao nhãng, không liên quan nội dung hoặc hành vi phát tán lặp lại có thể bị giảm phân phối; vì vậy ứng dụng yêu cầu bài riêng theo nhóm, đúng chủ đề và tránh lời giật gân ([Meta: hạn chế nội dung spam trên Facebook](https://about.fb.com/news/2025/04/cracking-down-spammy-content-facebook/)).

### Chính sách nhóm được phép đăng

Ứng dụng chỉ cho tạo, xếp lịch và đăng vào nhóm có từ khóa chuyên môn như `laptop`, `PC`, `máy tính`, `công nghệ`, `linh kiện` hoặc `tin học`. Tên nhóm có `chợ`, `rao vặt`, `thanh lý`, `sang nhượng` hoặc `marketplace` bị loại dù tên có PC/laptop. Khi khởi động, app khóa nhóm không phù hợp và hủy các bài `DRAFT`, `VALIDATED`, `QUEUED` hoặc `FAILED` chưa gửi; bài đang gửi hoặc đã chờ duyệt được giữ nguyên vì đã có thể xuất hiện trên Facebook.

## Kết nối agent

```sh
codex mcp add posting-management --url http://127.0.0.1:8085/mcp
opencode mcp add posting-management --global --url http://127.0.0.1:8085/mcp
claude mcp add --transport http posting-management http://127.0.0.1:8085/mcp --scope user
```
