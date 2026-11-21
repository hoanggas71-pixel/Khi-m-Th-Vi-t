# 👤 HỒ SƠ THÓI QUEN & PHONG CÁCH LÀM VIỆC CỦA USER

Tài liệu này ghi lại chi tiết các đặc điểm tâm lý, thói quen giao tiếp và các nguyên tắc làm việc với User của dự án **Khiếm Thị Việt**. Bất kỳ AI nào tiếp quản **BẮT BUỘC** phải đọc kỹ để phối hợp hiệu quả.

---

## 1. Phong cách Giao tiếp & Thói quen Đặt câu hỏi
- **Ngắn gọn, súc tích, thực tế:** User không thích văn phong lý thuyết dài dòng hay giải thích lan man. Luôn tóm tắt kết quả theo dạng gạch đầu dòng rõ ràng, trực diện vào vấn đề.
- **Các câu hỏi ngắn thường gặp của User:**
  - *"tiến độ"*, *"tiến dộ"*: User đang muốn biết công việc đang chạy tới bước nào. Hãy báo cáo ngay bước hiện tại (đang code, đang chạy test, hay đang đóng gói) và kết quả đạt được.
  - *"xong chưa"*: Báo cáo ngay trạng thái hoàn thành hoặc ước lượng ngắn gọn thời gian/bước còn lại.
- **Cách xưng hô & Thái độ:**
  - User gọi AI là *"nhóm gehihi"*, xưng *"tôi"*.
  - AI xưng *"tôi"* hoặc *"nhóm gehihi"*, gọi user là *"bạn"*, giữ thái độ lễ phép, tận tâm, trách nhiệm cao.

---

## 2. Thói quen sử dụng Slash Command `/teamwork`
- User rất hay sử dụng lệnh:
  ```text
  /teamwork [skip-interview] <nội dung yêu cầu>
  ```
- **Ý nghĩa:** User muốn hệ thống tự động chạy quy trình làm việc đa tác nhân (teamwork) bỏ qua bước phỏng vấn hỏi lại (`[skip-interview]`), tập trung giải quyết kỹ thuật ngay và tự chạy kiểm thử kiểm định.
- **Xử lý:** Khi user gọi lệnh này, lập tức chuyển giao cho `teamwork` subagent hoặc triển khai tuần tự theo đúng quy trình kiểm thử nghiêm ngặt.

---

## 3. Các Nguyên Tắc "Bất Khả Xâm Phạm" (Do's & Don'ts)

### ❌ NHỮNG ĐIỀU TUYỆT ĐỐI KHÔNG LÀM:
1. **KHÔNG BAO GIỜ nạp dữ liệu giả / rác (No Dummy Data):**
   - Không được tự ý nhồi các khách hàng mẫu, vé mẫu, tài khoản mẫu vào hệ thống cơ sở dữ liệu thật của cơ sở.
   - User coi trọng tính trung thực của dữ liệu cơ sở rỗng mới khởi tạo (`zero-data honesty`).
2. **KHÔNG tự ý đóng gói file APK khi chưa yêu cầu:**
   - Việc build APK mất tài nguyên và thời gian. Chỉ build APK khi user yêu cầu cụ thể (ví dụ: *"đóng gói APK mới"*, *"làm 2 cái này: Đóng gói bản APK... và Đẩy lên Web Sing"*).
   - Trong quá trình phát triển thông thường, chỉ cần chạy kiểm thử tự động: `.\gradlew.bat testDebugUnitTest`.
3. **KHÔNG BAO GIỜ phá hỏng trợ năng TalkBack (Accessibility):**
   - Ứng dụng được thiết kế cho **Người Khiếm Thị** (kỹ thuật viên khiếm thị, chủ spa khiếm thị).
   - Mọi nút bấm, icon, thẻ dữ liệu, văn bản trạng thái phải có `contentDescription`, `semantics`, `mergeDescendants` đọc to bằng tiếng Việt rõ ràng.
   - Kích thước vùng chạm (touch target) tối thiểu luôn phải đạt **48x48dp**.
4. **KHÔNG tự ý sửa đổi hoặc ghi đè thông tin tài khoản người dùng nhập:**
   - Không được dùng hàm ép kiểu số điện thoại hay tên không dấu để ghi đè username/mật khẩu mà user hoặc AI tạo. Phải giữ nguyên 100% dữ liệu gốc (kể cả chứa dấu `.`, `-`, `@`, `!`).

---

### ✅ NHỮNG ĐIỀU PHẢI LUÔN THỰC HIỆN:
1. **Luôn chạy Unit Test trước khi báo hoàn thành:**
   - Mỗi khi chỉnh sửa mã nguồn Java/Kotlin, phải chạy:
     ```powershell
     cd "C:\Users\Admin\.gemini\antigravity\scratch\Khi-m-Th-Vi-t"
     .\gradlew.bat testDebugUnitTest
     ```
   - Đảm bảo **100% các bài test (85+ bài test) đạt PASS**.
2. **Đồng bộ song song Web và Git khi bàn giao:**
   - Khi user yêu cầu *"Đẩy lên Web Sing / Render"*: Phải chạy `git push origin main` lên repo GitHub `https://github.com/hoanggas71-pixel/Khi-m-Th-Vi-t`.
3. **Cập nhật cả 2 file APK khi đóng gói:**
   - Sau khi chạy `.\gradlew.bat assembleDebug`, copy file output vào cả 2 vị trí:
     * `Khi-m-Th-Vi-t\KhiemThiViet-CaiDat.apk`
     * `Khi-m-Th-Vi-t\CAI_DAT_APK\KhiemThiViet.apk`
   - Cung cấp link file clickable dạng `file:///C:/Users/Admin/...` cho user.
