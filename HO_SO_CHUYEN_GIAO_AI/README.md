# 🌟 HỒ SƠ CHUYỂN GIAO DỰ ÁN DÀNH CHO AI TIẾP THEO (AI HANDOVER DOSSIER)
**Dự án:** KHIẾM THỊ VIỆT (Nền tảng Quản lý Spa & Massage Trợ năng TalkBack)  
**Thời điểm lập:** 21/11/2026  
**Thư mục làm việc (Workspace):** `C:\Users\Admin\.gemini\antigravity\scratch\Khi-m-Th-Vi-t`  
**GitHub Repository:** `https://github.com/hoanggas71-pixel/Khi-m-Th-Vi-t`  
**Bản Web Render:** `https://khienthiviet.onrender.com/`  
**Cơ sở dữ liệu đám mây:** Firebase / Cloud Firestore (Hỗ trợ Offline-first)

---

## 📂 MỤC LỤC TÀI LIỆU CHUYỂN GIAO
1. [01_THOI_QUEN_VA_PHONG_CACH_USER.md](./01_THOI_QUEN_VA_PHONG_CACH_USER.md): Thói quen làm việc, phong cách giao tiếp, các nguyên tắc cấm kỵ và yêu cầu cốt lõi của người dùng.
2. [02_KIEN_TRUC_DU_AN_VA_CONG_NGHE.md](./02_KIEN_TRUC_DU_AN_VA_CONG_NGHE.md): Toàn cảnh kiến trúc Android Compose, Node.js Web Portal, Firebase, Cloudflare, cơ chế đồng bộ và phân quyền.
3. [03_LICH_SU_TINH_NANG_VA_SUA_LOI.md](./03_LICH_SU_TINH_NANG_VA_SUA_LOI.md): Toàn bộ các tính năng đã hoàn thiện, các lỗi lịch sử đã xử lý triệt để và kinh nghiệm kỹ thuật.
4. [04_HUONG_DAN_VAN_HANH_CHO_AI_TIEP_THEO.md](./04_HUONG_DAN_VAN_HANH_CHO_AI_TIEP_THEO.md): Hướng dẫn lệnh chạy test, build APK, chạy web server, push git và các bộ unit test bắt buộc phải đạt 100%.

---

## ⚡ TÓM TẮT NHANH 1 PHÚT CHO AI MỚI TIẾP QUẢN:
1. **Ứng dụng phục vụ người khiếm thị:** Luôn giữ 100% khả năng tiếp cận TalkBack (`contentDescription`, `semantics`, kích thước bấm tối thiểu 48dp). Không bao giờ tạo icon/nút bấm câm không đọc được.
2. **Không tự ý tạo dữ liệu giả (No Dummy Data):** Không nạp tài khoản, khách hàng, vé rác vào cơ sở dữ liệu nếu user không yêu cầu.
3. **Không tự ý build APK:** Chỉ build APK khi user yêu cầu cụ thể hoặc kết thúc cột mốc quan trọng mà user chỉ thị đóng gói. Luôn chạy `.\gradlew.bat testDebugUnitTest` để xác nhận test PASS trước khi báo xong.
4. **Trợ lý AI gehihi:** Hiện tại chỉ dành riêng cho Chủ cơ sở và nhân viên có quyền Admin (`currentUser.isAdmin() == true`). Lịch sử chat được cô lập theo tài khoản (`aiChatVault`), giới hạn 100 tin nhắn cuốn chiếu (Rolling Window).
5. **Cấp quyền Admin cho nhân viên:** Khi bật công tắc Admin, nhân viên **phải giữ nguyên trong danh sách nhân viên**, gắn tem "Admin" màu vàng Champagne, không được lọc ẩn làm mất nhân viên.
