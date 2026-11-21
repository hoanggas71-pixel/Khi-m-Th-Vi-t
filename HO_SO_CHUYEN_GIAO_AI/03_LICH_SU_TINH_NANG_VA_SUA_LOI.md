# 📜 LỊCH SỬ TÍNH NĂNG & CÁC LỖI ĐÃ XỬ LÝ TRIỆT ĐỂ (CHANGELOG & LESSONS LEARNED)

Tài liệu này ghi lại chi tiết các vấn đề lịch sử mà người dùng đã từng phản ánh, nguyên nhân gốc rễ và giải pháp đã thực hiện để AI tiếp theo không bao giờ lặp lại lỗi cũ.

---

### 1. Lỗi Dồn Chữ & Nhân Đôi Ký Tự Ô Mật Khẩu (LDPlayer / Gboard IME)
- **Hiện tượng:** Khi gõ mật khẩu trên giả lập LDPlayer hoặc bàn phím tiếng Việt (như gõ `123456`), ô mật khẩu bị dồn thành `112123123412345123456` hoặc tự động xóa sạch.
- **Nguyên nhân:** Giao thức soạn thảo `ComposingText` của bàn phím IME gửi các chuỗi tích lũy liên tục (`1` -> `12` -> `123`), nếu xử lý sự kiện `onValueChange` không đúng cách sẽ gây xung đột.
- **Giải pháp:** Xây dựng `ImeInputHelper.kt` bóc tách chuỗi gõ tích lũy hình tam giác (triangular composing unpacking), bảo vệ ký tự trùng lặp liên tiếp (`11`, `22`), chống xóa nhầm khi trùng đầu số điện thoại.

---

### 2. Thu Hồi Phiên Đăng Nhập Khi Xóa Cơ Sở Trên Web Portal
- **Hiện tượng:** Khi Super Admin xóa vĩnh viễn tài khoản cơ sở trên Web Portal, app Android vẫn còn lưu token đăng nhập cũ, chỉ bị lỗi ngắt kết nối với AI gehihi mà không tự động đăng xuất và không có thông báo cho người dùng.
- **Giải pháp:** 
  - Trong `SpaStore.kt`, tích hợp bộ lắng nghe trạng thái cơ sở thời gian thực (`startTenantStatusListener()` và `localTenantPollJob`).
  - Khi cơ sở bị xóa trên Firestore hoặc máy chủ trả về `deleted: true / 404`, app lập tức:
    1. Ngắt phiên làm việc trong vòng tối đa **3.5 giây**.
    2. Đăng xuất tài khoản an toàn, dọn dẹp dữ liệu nhạy cảm trong RAM.
    3. Hiển thị thông báo thoại TalkBack và thông báo màn hình: *"Cơ sở này đã bị xóa hoặc ngừng hoạt động. Phiên làm việc đã kết thúc."*

---

### 3. Tùy Chọn Lưu Khách Khi Chốt Vé & Xem Lịch Sử Dịch Vụ
- **Yêu cầu:** Khi xong vé / chốt vé ở mục Vé, cung cấp tùy chọn có lưu khách hàng hay không. Nếu lưu thì khách xuất hiện ở mục Khách, bấm vào xem được chi tiết lịch sử (làm dịch vụ gì, vào lúc nào, ai làm). Nếu không lưu thì vẫn chốt vé, tính doanh thu và hoa hồng bình thường nhưng không tạo khách rác.
- **Giải pháp:**
  - Bổ sung trường `luuKhachHang: Boolean` trong `Luot` entity và Room database.
  - Hộp thoại `HoanThanhVeDialog` và `XacNhanChotVeDialog` có checkbox tùy chọn lưu khách.
  - Tại `TabNguoi` (mục Khách): Bấm vào thẻ khách hàng sẽ mở `CustomerServiceHistoryDialog` hiển thị danh sách các lần làm dịch vụ sắp xếp giảm dần theo thời gian.

---

### 4. Nâng Cấp Giao Diện Spa Chuẩn Quốc Tế (Fresha & Boulevard Style)
- **Yêu cầu:** Học hỏi các mẫu app spa được đánh giá cao trên thế giới (Fresha, Boulevard, Mangomint, Vagaro) để nâng cấp giao diện app Khiếm Thị Việt, đảm bảo 100% TalkBack.
- **Giải pháp:**
  - **Thanh Cockpit Bar (Spa Dashboard):** Hiển thị 3 chỉ số vận hành tức thời: 🟢 Số khách đang làm, 🟡 Số vé chờ chốt tiền, 💰 Doanh thu hôm nay (chạm vào thẻ để chuyển tab lọc nhanh).
  - **Thẻ Vé Sang Trọng:** Bo góc 16dp, viền sang trọng, avatar chữ cái đầu của khách, huy hiệu trạng thái sàn nổi bật (Đang làm, Chờ chốt, Đã chốt), hiển thị hoa hồng KTV và giá tiền in đậm.
  - **Live Floor KTV:** Trong danh sách nhân viên hiển thị trạng thái sàn thời gian thực (🟢 Đang làm cho ai / ⚪ Đang rảnh chờ khách).
  - **Huy hiệu Tab Vé:** Tab Vé dưới thanh điều hướng có số đỏ hiển thị tổng số vé đang cần xử lý.

---

### 5. Giữ Nguyên Nhân Viên Khi Cấp Quyền Admin
- **Hiện tượng:** Khi gạt công tắc cấp quyền Admin cho nhân viên ở Tab Quyền hạn, nhân viên liền bị biến mất khỏi danh sách quản lý nhân viên.
- **Nguyên nhân:** Bộ lọc `staffAccounts` trước đây loại bỏ các tài khoản có `quyen == OWNER`. Khi bật công tắc Admin cho nhân viên, quyền chuyển thành `OWNER` làm bộ lọc loại nhân viên ra khỏi danh sách.
- **Giải pháp:**
  - Phân biệt rõ tài khoản chủ cơ sở gốc (`nvId.isBlank()`) với nhân viên được cấp quyền (`nvId.isNotBlank()`).
  - Toàn bộ nhân viên luôn được giữ nguyên vẹn trong danh sách quản lý.
  - Gắn thêm tem nhãn **"Admin"** màu vàng Champagne nổi bật ngay cạnh tên nhân viên và đọc rõ ràng trên TalkBack.

---

### 6. Chuẩn Hóa Tài Khoản & Mật Khẩu (AI gehihi & Thủ Công)
- **Hiện tượng:** Khi tạo tài khoản qua AI gehihi hoặc nhập tay, tên đăng nhập hoặc mật khẩu bị tự ý đổi sang số điện thoại hoặc tên không dấu của chế độ offline; một số trường hợp mật khẩu có tiền tố `la...`, `lam...`, `password` bị nuốt mất ký tự.
- **Giải pháp:**
  - Cập nhật `SpaModels.kt` và `SpaStore.kt`: Giữ nguyên vẹn 100% dữ liệu người dùng nhập, hỗ trợ đầy đủ các ký tự đặc biệt (`.`, `-`, `@`, `!`).
  - Sửa regex NLP của AI gehihi với ranh giới từ `\b(?:là|la|thành|thanh)\b` và ưu tiên từ khóa `password` trước `pass`.
  - Đổi bàn phím nhập thủ công từ `KeyboardType.Phone` sang `KeyboardType.Text`.

---

### 7. Phân Quyền AI gehihi & Cô Lập Đoạn Chat Theo Tài Khoản
- **Yêu cầu:** 
  1. Chỉ Chủ cơ sở (Admin) và nhân viên có quyền Admin mới được dùng AI gehihi; nhân viên thường bị ẩn hoàn toàn.
  2. Mỗi tài khoản có một đoạn chat riêng biệt, không dùng chung, không xem được tin nhắn của nhau.
  3. Giới hạn tối đa 100 tin nhắn cuốn chiếu (Rolling Window FIFO) để chống tràn bộ nhớ, có nút xóa/làm mới đoạn chat.
- **Giải pháp:**
  - Ẩn nút AI gehihi trên `SpaHeaderBar` đối với tài khoản `currentUser.isAdmin() == false`.
  - Phân tách lưu trữ `aiChatVault: Map<String, List<AiChatMessage>>` theo `accountId`.
  - Tự động hủy lệnh AI đang chờ xác nhận (`pendingAiConfirmation`) khi chuyển tài khoản hoặc đăng xuất.
  - Hàm `nenTinNhanAiCuonChieu()` tự động giữ tối đa 100 tin nhắn gần nhất.
  - Bổ sung nút "Bắt đầu đoạn chat mới" trong hộp thoại AI bot.
