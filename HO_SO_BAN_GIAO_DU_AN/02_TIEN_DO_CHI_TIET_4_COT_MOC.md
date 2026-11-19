# BÁO CÁO TIẾN ĐỘ VÀ CHI TIẾT KỸ THUẬT 4 CỘT MỐC

Tài liệu này ghi chép chi tiết từng dòng mã nguồn, chức năng đã lập trình, và kết quả nghiệm thu của 4 Cột mốc trong dự án.

---

## CỘT MỐC 1: SERVER AI CONFIG, WEB DEV PORTAL & EXTERNAL CSS
- **Trạng thái**: ĐÃ HOÀN THÀNH 100% (GATE PASSED)
- **Tệp sửa đổi**:
  1. `server.js`:
     - Endpoint `GET /api/system/ai-config`: Trả về JSON `{ success: true, apiKey: "...", model: "gemini-1.5-flash", updatedAt: "..." }`.
     - Endpoint `POST /api/system/ai-config`: Nhận body `{ apiKey: "..." }`, ghi vào file `public/ai-config.json` và trả về kết quả thành công.
     - Fallback: Nếu file `ai-config.json` chưa tồn tại, tự động fallback sang biến môi trường `process.env.GEMINI_API_KEY`.
  2. `public/index.html`:
     - Thêm thẻ quản trị System Gemini API Key trong phân hệ `#dev-view` (Super Admin).
     - Tích hợp ô nhập key (ẩn/hiện), nút "⚡ Test Kết Nối" gọi trực tiếp Gemini API trả về HTTP 200 OK, và nút "💾 Lưu Khóa Hệ Thống".
     - Tích hợp nút in báo phí và hàm `printInvoice()` kích hoạt in hóa đơn VietQR.
  3. `public/style.css`:
     - Viết mới 434 dòng stylesheet ngoài cho portal, bảng quản trị cơ sở, card VietQR, định dạng chia sẻ Zalo.
     - Tích hợp kỹ thuật cách ly in ấn (`Print Isolation CSS`): Sử dụng selector `:has(#customer-renew-modal:not(.hidden))` và class `.printing-invoice` trên `document.body` để ẩn toàn bộ layout nền, rút gọn bản in hóa đơn VietQR về đúng **1 trang A4 duy nhất**, không rò rỉ dữ liệu tenant.
- **Nghiệm thu**: Reviewer 1 (APPROVE), Reviewer 2 (APPROVE), Challenger 1 (APPROVE), Challenger 2 (APPROVE sau bản vá class printing-invoice), Auditor (CLEAN).

---

## CỘT MỐC 2: ON-DEVICE SPA INTELLIGENCE & QUERY ENGINE
- **Trạng thái**: ĐÃ HOÀN THÀNH 100% (GATE PASSED)
- **Tệp tạo mới / sửa đổi**:
  1. `app/src/main/java/com/example/OnDeviceSpaQueryEngine.kt` (Tạo mới hoàn toàn):
     - Hàm `parseVietnameseDateRange(prompt, refDate)`: Phân tích cú pháp ngày tháng tiếng Việt có dấu/không dấu, nói tắt, đảo ngữ (*"10 ngày đầu tháng 10"*, *"10 ngay dau t10"*, *"tháng 9"*, *"đầu t9"*, *"tuần trước"*, *"tuần rồi"*, *"từ 01/10 đến 15/10"*, *"hôm qua"*, *"hôm nay"*).
     - 5 phân hệ dữ liệu chạy offline siêu tốc (<5ms) trên các danh sách:
       + *Doanh thu, Sổ quỹ, Lãi ròng*: Tính từ vé đã chốt `luots` và thu chi `tcs`. Áp dụng **Zero-Data Honesty** (khi không có vé/thu chi trong khoảng hỏi, báo 0 vé, 0đ rõ ràng, tuyệt đối không nhảy sang hôm nay).
       + *Khách hàng & Gói*: Tra cứu số buổi còn lại, lịch sử dùng gói, tần suất đến quán, xếp hạng khách VIP.
       + *KTV & Phân bổ ca*: Phân loại KTV đang bận (`dangLam == true`) vs KTV đang rảnh; tổng phút trị liệu và hoa hồng theo mốc thời gian bất kỳ; KTV làm nhiều vé nhất.
       + *Dịch vụ bán chạy*: Thống kê dịch vụ bán chạy nhất, bảng giá, hoa hồng.
       + *So sánh đối chiếu*: So sánh số liệu giữa 2 kỳ (*"so sánh tuần này với tuần trước"*).
  2. `app/src/main/java/com/example/SpaStore.kt`:
     - Tích hợp đường truyền siêu tốc `OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen()` vào hàm `guiYeuCauAiBot` (dòng 3327) và `traLoiThongMinhNoiBo` (dòng 5780).
     - Thay thế khối Block 6 cũ (vốn hardcode `veHomNay`) bằng động cơ ngoại tuyến mới.
  3. `app/src/test/java/com/example/SpaOnDeviceQueryEngineTest.kt`:
     - 19 kịch bản test kiểm thử đơn vị tự động.
- **Nghiệm thu**: Toàn bộ 19/19 test case vượt qua 100% (`BUILD SUCCESSFUL in 1m 40s`). Đã được 5 đặc vụ kiểm định độc lập phê duyệt đồng thuận (Reviewer 1&2, Challenger 1&2, Auditor).

---

## CỘT MỐC 3: GIAO DIỆN GEHIHI ROSEWOOD & AI KEY CONFIG DIALOG
- **Trạng thái**: ĐÃ VIẾT XONG MÃ NGUỒN, ĐANG HOÀN THIỆN BIÊN
- **Tệp sửa đổi**:
  1. `app/src/main/java/com/example/SpaStore.kt`:
     - Bổ sung biến trạng thái phản ứng: `var isGeminiConnected by mutableStateOf(false)`.
     - Hàm `layGeminiApiKey(): String`: Kiểm tra SharedPreferences `gemini_api_key` (khóa người dùng tự nhập) -> fallback sang khóa hệ thống đồng bộ từ server `system_gemini_api_key` -> `BuildConfig.GEMINI_API_KEY` -> biến môi trường.
     - Hàm `luuGeminiApiKey(key: String)`, `xoaGeminiApiKey()`.
     - Hàm `suspend fun kiemTraGeminiApiKey(key: String): Pair<Boolean, String>`: Gọi trực tiếp Google Gemini API (`https://generativelanguage.googleapis.com/v1beta/models?key=...`) kiểm tra phản hồi HTTP 200 OK thời gian thực.
     - Hàm `xoaLichSuTroChuyenGehihi()`: Xóa sạch tin nhắn lịch sử và bộ nhớ ngắn hạn.
     - Hàm `dongBoGeminiApiKeyTuServer()`: Tự động gọi endpoint `/api/system/ai-config` khi khởi động app để lấy key dùng chung nếu app chưa có key riêng.
  2. `app/src/main/java/com/example/MainActivity.kt`:
     - `AiKeyConfigDialog`: Hộp thoại cấu hình key có ô nhập, nút Dán clipboard, icon ẩn/hiện mật khẩu, nút Test kết nối 200 OK kèm spinner, nút Lưu khóa, nút Gỡ khóa, và hướng dẫn 3 bước kèm link mở Google AI Studio miễn phí trong 1 phút.
     - `SpaHeaderBar`: Thanh tiêu đề phong cách Spa Rosewood/Champagne, huy hiệu `🟢 Ngoại tuyến` / `✨ Trực tuyến`, nút 🔑/⚙️ mở nhanh cấu hình key.
     - `AdminAiAssistantBotDialog`:
       + Hỗ trợ chế độ mở rộng / toàn màn hình linh hoạt.
       + `RichReportCardComposable`: Hiển thị 3 khối KPI thu/chi/lãi bằng mã màu xanh lá (`#81C995`), đỏ (`#FF8A80`), vàng kim (`#FFD54F`).
       + 4 nút Quick Action Pills: `[🎫 Mở vé]`, `[📖 Xem sổ quỹ]`, `[👥 Gọi KTV]`, `[📊 Xem báo cáo]`.
       + Nút Sao chép câu trả lời vào Clipboard kèm Toast thông báo.
       + Nút Đọc giọng nói (TalkBack / TextToSpeech).
       + Nút Xóa lịch sử trò chuyện kèm dialog xác nhận.
     - 3 điểm kích hoạt mở Dialog cấu hình key: Nút trên Header Bar, nút trong Tab Quyền hạn Admin, và nhận diện lệnh chat/thoại.
- **Các điểm biên cần hoàn tất rà soát**:
  - Bảo mật RBAC: Nhân viên KTV không được mở dialog cấu hình hay xem/sửa key của Chủ Spa; chỉ tài khoản `VaiTro.OWNER` hoặc Admin mới có quyền.
  - Vùng chạm tối thiểu 48dp cho người khiếm thị sử dụng TalkBack.
  - Regex bóc tách lãi ròng khi có dấu ngoặc hoặc số âm `(Lãi ròng): -260.000 đồng`.
  - Khớp chỉ số subtab con khi nhấn phím tắt `[👥 Gọi KTV]`.

---

## CỘT MỐC 4: BIÊN DỊCH GRADLE & ĐÓNG GÓI 3 FILE APK
- **Trạng thái**: CHỜ THỰC THI (Ngay sau khi Cột mốc 3 hoàn tất)
- **Quy trình thực hiện**:
  1. Chạy lệnh: `.\gradlew.bat assembleDebug`
  2. Sao chép đè APK vào 3 đường dẫn:
     - `public/KhiemThiViet-CaiDat.apk`
     - `CAI_DAT_APK/KhiemThiViet-CaiDat.apk`
     - `KhiemThiViet-CaiDat.apk`
  3. Kiểm tra mã SHA-256 trùng khớp giữa 3 tệp.
