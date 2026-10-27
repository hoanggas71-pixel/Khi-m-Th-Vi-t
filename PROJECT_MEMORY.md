# PROJECT MEMORY & TECHNICAL CONTEXT — KHIẾM THỊ VIỆT (Spa & Massage Management)

## 1. Kiến trúc cốt lõi & Quy ước bất biến
- **Cấu trúc 3 file Kotlin duy nhất** trong `/app/src/main/java/com/example/`:
  1. `SpaModels.kt`: Data Model (`TaiKhoan`, `QuyenHan`, `NV`, `DV`, `Goi`, `KH`, `DK`, `Luot`, `TC`, `BoLocThoiGian`, `AiChatMessage`, `LoaiLenhChoXacNhan`, `LenhChoXacNhanGehihi`, `OLichDoanhThu`, `ThongKeKhachHangChiTieu`), chuẩn hóa tiếng Việt & fuzzy search.
  2. `SpaStore.kt`: State holder & Persistence (`SharedPreferences` `"tamquat_fresh_v2"` + File nội bộ `"gehihi_memory_vault.json"`), phân quyền tài khoản, Cloud Sync Firestore, báo cáo ô lịch, bảng lương KTV, và Trợ lý AI **`gehihi`** tích hợp sâu mô hình Gemini 3.8 Flash.
  3. `MainActivity.kt`: Giao diện Jetpack Compose chuẩn Material 3 và TalkBack Accessibility.

## 2. Nâng cấp Tích hợp sâu Trí tuệ Nhân tạo Gemini vào Gehihi
- **Tư vấn Kinh doanh & Phân tích Tài chính Chuyên sâu**:
  - `buildGehihiPrompt` được nạp bức tranh tài chính thời gian thực: Doanh thu thực nhận hôm nay và tháng này, tỷ lệ hoa hồng trả KTV so với tổng doanh thu, cơ cấu chi phí ngoài lề, xếp hạng KTV làm nhiều vé nhất, top dịch vụ bán chạy nhất, số gói liệu trình còn hạn.
  - Khi Admin đặt câu hỏi tư vấn trong khung chat (*"Tình hình tháng này thế nào?", "Làm sao tăng doanh thu?", "Chi phí có gì bất thường?"*), Gemini đóng vai trò Cố vấn Quản trị Spa cấp cao, phân tích dựa trên số liệu thực tế của cơ sở và đưa ra 2-3 giải pháp chiến lược khả thi.
- **Bóc tách Khẩu lệnh Tự nhiên (Không cần đúng cú pháp)**:
  - Gemini tự động nhận diện ý định (Intent) từ mọi câu nói tự do, tiếng lóng, viết tắt (*"thêm nv...", "mở vé cho...", "chốt ca...", "chi 150k...", "sửa giá..."*) và gắn thẻ `[ACTION:...]` kèm đầy đủ tham số đã được chuẩn hóa.
  - Hỗ trợ đầy đủ các hành động: `ADD_STAFF` (nhận tên, SĐT, giới tính Nam/Nữ, Gmail), `OPEN_TICKET`, `CONFIRM_TICKETS` (chốt theo ca/thợ), `COMPLETE_TICKET`, `ADD_FINANCE_CHI`, `ADD_FINANCE_THU`, `EDIT_SERVICE`, `EDIT_STAFF`, `DELETE_STAFF` (chỉ xóa đúng người chỉ định).
- **Cơ chế Bảo vệ & Xác nhận An toàn Bất biến**:
  - Dù câu lệnh được Gemini nhận diện tự động, mọi lệnh thay đổi dữ liệu (thêm/sửa/xóa/chốt) đều được bọc trong thẻ xác nhận màu cam/đỏ yêu cầu Admin bấm duyệt trước khi thực thi.

## 3. Tái cấu trúc Sổ quỹ & Báo cáo Chuyên nghiệp
- **Sổ quỹ (`TabSoQuy`)**: Bỏ tính tổng thu/chi/lãi ròng trùng lặp. Quản lý 2 cột song song cân đối: Thu ngoài lề và Chi ngoài lề kèm ghi chú.
- **Báo cáo (`TabDoanhThuBaoCao`)**:
  - **Số tiền nguyên bản**: Hiển thị chính xác 100% không làm tròn (`+1.590.002đ`, `-330.000đ`). Bỏ dòng chữ nhỏ dưới 2 ô.
  - **Hộp thoại chi tiết khi chạm vào 2 ô**: Chạm ô Doanh thu (Chủ nhận) hoặc ô Tổng chi phí sẽ hiển thị Dialog chi tiết từng khoản cấu thành.
  - **Ô lịch chi tiết 3 mục**: Chạm vào ngày bất kỳ sẽ hiện popup 3 mục: 1. Số vé hoàn thành, 2. Số tiền thu, 3. Số tiền chi.
  - **Dưới bảng lịch**: Bảng lương KTV là mục duy nhất, có nút xem ca làm và gửi phiếu lương.
