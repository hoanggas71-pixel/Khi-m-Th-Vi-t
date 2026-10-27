# Bộ nhớ Kiến trúc & Ngữ cảnh Dự án: Khiếm Thị Việt (Spa Management)

## 1. Kiến trúc tổng thể
- **AndroidManifest.xml**:
  - `android:windowSoftInputMode="adjustResize"` cho `MainActivity`.
- **MainActivity.kt**:
  - `SpaHeaderBar`: Loại bỏ huy hiệu `Admin` rời cạnh tên quán `KHIẾM THỊ VIỆT` (nguyên nhân gây ép chữ dọc `A-d-m-i-n` trên điện thoại thật) và khóa `softWrap = false, maxLines = 1` cho tất cả chữ & nút trên thanh tiêu đề.
  - `TabSoQuy` & `TabDoanhThuBaoCao`:
    - Đơn giản hóa Sổ quỹ: Bỏ tổng kết 3 ô thu chi trên Sổ quỹ, chỉ hiển thị bảng Thu ngoài và Chi ngoài riêng biệt cùng form nhập ngoài lề.
    - Báo cáo tài chính: Bảng tổng kết hiển thị ô Doanh thu (Chủ quán thực nhận sau khi trừ hoa hồng & chi phí) và ô Tổng chi phí. Bấm vào ô mở hộp thoại chi tiết.
    - Ô Lịch trực quan: Nhấn vào ô ngày bất kỳ mở hộp thoại chi tiết 3 mục: Số vé, Tiền thu, Tiền chi.
    - Bảng lương KTV: Thống kê chi tiết số vé, số phút và thu nhập hoa hồng theo từng mốc thời gian tùy chọn để dễ dàng gửi phiếu lương.
- **Trợ lý Trí tuệ Nhân tạo gehihi (SpaStore.kt)**:
  - **Gemini làm trung tâm xử lý kết hợp Fallback thông minh**: Toàn bộ câu lệnh tự nhiên được chuyển cho Gemini cùng đầy đủ ngữ cảnh. Khi mạng/AppCheck có sự cố, bộ xử lý nội bộ tự động phân tích hành động trực tiếp thay vì rơi về lời chào mặc định.
  - **Tự động hủy thẻ chờ xác nhận cũ khi có lệnh mới**: Khi Admin gõ câu lệnh mới mà không phải "xác nhận"/"hủy", hệ thống lập tức dọn dẹp `pendingAiConfirmation = null` để tránh tình trạng thẻ cũ treo vĩnh viễn trên màn hình chat.
  - **Hỗ trợ lệnh xóa dữ liệu nhạy cảm an toàn**: Nhận diện chuẩn xác các lệnh `"xoá hết dữ liệu gốc"`, `"xóa toàn bộ dữ liệu"`, `"reset app"` và luôn yêu cầu xác nhận 2 lớp bằng popup cảnh báo đỏ trước khi thực thi.
  - **AppCheck**: Chuẩn hóa `DebugAppCheckProviderFactory.getInstance()` trên bản Debug theo đặc tả Firebase.
  - File cài đặt APK mới nhất: `KhiemThiViet-CaiDat.apk` và `CAI_DAT_APK/KhiemThiViet-CaiDat.apk`.

