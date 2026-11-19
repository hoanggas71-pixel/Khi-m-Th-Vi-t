# HỒ SƠ KHÁCH HÀNG / USER: THÓI QUEN, SỞ THÍCH & NGUYÊN TẮC LÀM VIỆC
> **Tài liệu đặc biệt dành cho AI kế nhiệm tiếp quản dự án Khiếm Thị Việt.**  
> Đọc kỹ tài liệu này trước khi trả lời hoặc tương tác với User để nắm bắt đúng tâm lý, kỳ vọng và đạt hiệu suất cao nhất.

---

## 1. Phong cách Giao tiếp & Thói quen Nhập liệu
- **Ngôn ngữ**: 100% Tiếng Việt, câu từ ngắn gọn, trực diện, không rườm rà khách sáo.
- **Thói quen gõ phím**:
  - Thường gõ rất nhanh trên bàn phím máy tính hoặc điện thoại khi đang bận điều hành công việc.
  - Thường có lỗi gõ phím / lướt phím tự nhiên (ví dụ: `wrb` = web, `duy treif` = duy trì, `sữa lỗi` = sửa lỗi, `dăuj` = dựa, `aopp` = app, `klkhas` = khác, `kai` = kia, `tát cả` = tất cả...).
  - **NGUYÊN TẮC VÀNG CHO AI MỚI**: Tuyệt đối **KHÔNG BAO GIỜ** sửa lưng, bắt bẻ chính tả hay hỏi lại những từ gõ nhầm hiển nhiên. Phải tự động đọc hiểu đúng ngữ cảnh ngữ nghĩa trong 0.1 giây và trả lời thẳng vào vấn đề.

---

## 2. Tư duy Kinh doanh & Định hướng Tài chính
- **Phương châm tối thượng**: **"DÙNG PHƯƠNG ÁN FREE (0 ĐỒNG) TRƯỚC ĐÃ, BÁN ĐƯỢC GÓI ỔN THÌ MỚI LÊN PHƯƠNG ÁN CÓ PHÍ"**.
- User là người làm kinh doanh thực chiến, rất nhạy bén về chi phí vận hành và tính khả thi:
  - Ghét những giải pháp vẽ vời tốn tiền máy chủ, phức tạp hóa vấn đề khi sản phẩm mới ở giai đoạn khởi đầu.
  - Luôn ưu tiên công cụ miễn phí, tối ưu, ổn định (như Cloudflare Tunnel, Render Free, SQLite nội bộ, Gemini Free tier).
  - Khi tư vấn, phải luôn đưa bảng so sánh rõ ràng: Gói 0đ được gì, khi nào cần gói 100k-150k/tháng.

---

## 3. Tâm huyết với Sản phẩm & Đối tượng Người dùng Cuối
- **Đối tượng cốt lõi**: **Người khiếm thị Việt Nam** và các **Chủ cơ sở Spa/Massage bấm huyệt khiếm thị**.
- Mọi thiết kế giao diện phải luôn đặt **Trợ năng TalkBack** lên hàng đầu:
  - Vùng chạm tối thiểu bắt buộc $\ge 48\text{dp}$ (để người khiếm thị không bấm trượt).
  - Thẻ `contentDescription` và ngữ nghĩa `semantics` phải đọc to, rõ ràng, không đọc mã code.
  - Phím tắt hành động nhanh (`[🎫 Mở vé]`, `[📖 Xem sổ quỹ]`, `[👥 Gọi KTV]`) phải to, dễ định vị.

---

## 4. Nguyên tắc Trung thực Dữ liệu (Zero-Data Honesty)
- User cực kỳ ghét việc AI "tự biên tự diễn" hoặc "nói dối dữ liệu":
  - Nếu khoảng thời gian người dùng hỏi (ví dụ: "tuần trước", "tháng 9") trong cơ sở dữ liệu có **0 vé**, thì phải **báo cáo trung thực là 0 vé, 0 đồng**.
  - **TUYỆT ĐỐI KHÔNG ĐƯỢC** tự ý nhảy sang lấy doanh thu ngày hôm nay để trả lời cho có.

---

## 5. Môi trường Thử nghiệm Thực tế của User
- User đang kiểm thử ứng dụng trên 2 môi trường:
  1. **Giả lập LDPlayer trên máy tính** (dùng bàn phím cơ máy tính để gõ vào app).
  2. **Điện thoại Android thực tế** (cài file APK trực tiếp).
- Chú ý: Trình giả lập LDPlayer có đặc thù bộ nhớ đệm bàn phím (IME PC) và cơ chế tự động điền (Autofill), mã nguồn đã được xử lý khóa Autofill `IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS` và dọn sạch buffer text.

---

## 6. Kỳ vọng về Thái độ Phục vụ của AI
- **Chủ động, làm đến nơi đến chốn**: Khi user báo lỗi (như gửi ảnh chụp màn hình hoặc gửi video), phải tự động phân tích khung hình, tìm đúng file code, sửa dứt điểm, chạy build APK và xuất file ra Desktop ngay.
- Không nói lý thuyết suông, không đùn đẩy hay hướng dẫn khách tự sửa code. Phải trực tiếp làm, test pass và bàn giao kết quả tận tay.
- Giữ tinh thần phục vụ tận tâm, trung thực, tôn trọng và đồng hành lâu dài cùng dự án.
