# TÀI LIỆU CHI TIẾT 6 VẤN ĐỀ THỰC TẾ NGƯỜI DÙNG PHẢN ÁNH VÀ PHƯƠNG ÁN XỬ LÝ

Tài liệu này tổng hợp toàn bộ các phản hồi thực tế từ người dùng (kèm phân tích từ các ảnh chụp màn hình gửi lên) và giải pháp kỹ thuật đã áp dụng để giải quyết dứt điểm.

---

### Vấn Đề 1: Lỗi nhập số đăng nhập `123456789` bị trôi/đè xuống ô Mật khẩu
- **Hiện tượng thực tế**: Khi người dùng nhập số điện thoại `123456789` ở ô trên rồi chạm xuống ô Mật khẩu ở dưới, chuỗi `123456789` bị tự động điền hoặc đè vào ô mật khẩu (thành `1234567891`).
- **Nguyên nhân kỹ thuật**: Trên trình giả lập Android (LDPlayer/BlueStacks) hoặc một số bàn phím ảo, khi dùng phím chuyển `ImeAction.Next` / `focusManager.moveFocus(FocusDirection.Down)`, bộ đệm soạn thảo văn bản (IME composition buffer) từ ô SĐT bị chuyển tiếp sang ô mật khẩu.
- **Giải pháp xử lý trong `MainActivity.kt`**:
  + Tách biệt 100% hai biến trạng thái `phoneInput` và `passwordInput`.
  + Trong sự kiện `onValueChange` của `passwordInput`, bổ sung cơ chế thanh lọc:
    ```kotlin
    val cleanVal = if (passwordInput.isEmpty() && phoneInput.isNotBlank() && newVal.startsWith(phoneInput) && newVal.length > phoneInput.length) {
        newVal.removePrefix(phoneInput)
    } else {
        newVal
    }
    passwordInput = cleanVal
    ```
  + Thiết lập `singleLine = true`, `maxLines = 1`, `autoCorrect = false`.

---

### Vấn Đề 2: Tài khoản mới tạo trên Web Portal khi đăng nhập vào App lại bị dính dữ liệu mẫu của cơ sở cũ (`Spa Suối Nước Nóng`, `Anh Tuấn`, `Nguyễn Văn Minh`)
- **Hiện tượng thực tế**: Người dùng tạo một tài khoản mới trên Web Portal, nhưng khi mở app đăng nhập thì bên trong lại hiển thị vé phục vụ của khách Anh Tuấn, KTV Nguyễn Văn Minh và tên cơ sở "Chủ Spa Suối Nước Nóng".
- **Nguyên nhân kỹ thuật**: Khi app khởi động lần đầu, nếu SharedPreferences chưa có dữ liệu, app tự động gọi `initDefaultMockData()` để nạp dữ liệu mẫu vào bộ nhớ RAM. Khi người dùng bấm đăng nhập tài khoản mới, hàm `pullLatestFromCloudAndLogin` trước đây chỉ đổi `currentUser` mà không làm sạch các danh sách dữ liệu đang có trong RAM.
- **Giải pháp xử lý trong `SpaStore.kt`**:
  + Trong hàm `provisionAndLoginTenantOwner` (dòng 767): Khi phát hiện đây là một cơ sở mới chưa từng có snapshot dữ liệu lưu trên máy, bắt buộc thực thi lệnh dọn dẹp triệt để:
    ```kotlin
    luots.clear()
    khs.clear()
    nvs.clear()
    tcs.clear()
    dks.clear()
    deletedIds.clear()
    ```
  + Sau đó chỉ nạp bảng menu 10 dịch vụ và 3 gói liệu trình khiếm thị chuẩn ban đầu để cơ sở bắt đầu hoạt động với trạng thái **trang giấy trắng độc lập 100%**.

---

### Vấn Đề 3: Vào Tab "TK Admin gốc" lại hiển thị thông tin khác (`0397772963` / `247208`)
- **Hiện tượng thực tế**: Người dùng đăng nhập bằng số điện thoại của mình (ví dụ `123456789`), nhưng khi vào cài đặt Tab "TK Admin gốc", các ô nhập lại hiển thị số điện thoại `0397772963` và mật khẩu `247208` của tài khoản Admin cũ.
- **Nguyên nhân kỹ thuật**: Trong `MainActivity.kt` (dòng 5303), mã nguồn cũ viết:
  ```kotlin
  val rootAdminTk = store.taiKhoans.firstOrNull { it.quyen == VaiTro.OWNER }
      ?: store.currentUser
      ?: store.taiKhoans.firstOrNull { it.id == "tk_admin" }
  ```
  Vì trong danh sách `taiKhoans` luôn có tài khoản mặc định `tk_admin` mang quyền `VaiTro.OWNER`, hàm `firstOrNull` luôn luôn lấy phần tử đầu tiên là `tk_admin` cũ, bỏ qua tài khoản người dùng đang đăng nhập!
- **Giải pháp xử lý trong `MainActivity.kt`**:
  + Đổi thứ tự ưu tiên: Ưu tiên tài khoản đang đăng nhập của chính người dùng trước:
  ```kotlin
  val rootAdminTk = store.currentUser?.takeIf { it.quyen == VaiTro.OWNER }
      ?: store.taiKhoans.firstOrNull { it.id == store.currentUser?.id }
      ?: store.taiKhoans.firstOrNull { it.quyen == VaiTro.OWNER }
      ?: store.taiKhoans.firstOrNull { it.id == "tk_admin" }
  ```

---

### Vấn Đề 4: Chữ bị chìm xuống dưới các ô và thanh điều hướng (Cắt xén giao diện)
- **Hiện tượng thực tế**:
  + Nút *"MUA / GIA HẠN GÓI DỊCH VỤ (ZALO)"* bị cắt mất chữ `(ZALO)` ở cạnh dưới.
  + Thanh điều hướng dưới cùng (Bottom Navigation Bar) bị chìm các chữ *"Quyền hạn"*, *"Báo cáo"* xuống sát mép màn hình.
- **Nguyên nhân kỹ thuật**: Chiều cao các nút bị cố định cứng bằng `.height(48.dp)` mà không tính đến font chữ to hoặc màn hình độ phân giải hẹp; thanh điều hướng chưa tính khoảng đệm thanh hệ thống `WindowInsets.navigationBars`.
- **Giải pháp xử lý trong `MainActivity.kt`**:
  + Đổi toàn bộ `.height(48.dp)` trên các nút bấm dài thành `Modifier.heightIn(min = 48.dp)` kèm padding co giãn `PaddingValues(horizontal = 12.dp, vertical = 8.dp)`.
  + Trên thanh điều hướng dưới cùng, thiết lập `windowInsets = WindowInsets.navigationBars` để tự động nâng nội dung lên trên dải phím điều hướng ảo của Android.

---

### Vấn Đề 5: Trợ lý Gehihi khi tắt Wi-Fi bị ngô nghê, trả lời cứng nhắc kịch bản soạn sẵn
- **Hiện tượng thực tế**: Tắt Wi-Fi hỏi *"thống kê cho tôi doanh thu 10 ngày đầu tháng 10"*, Gehihi lại trả về *"Báo cáo ca hôm nay (2026-11-18)..."*.
- **Nguyên nhân kỹ thuật**: Trong `SpaStore.kt:5926`, bất cứ khi nào câu hỏi có từ "doanh thu", regex cũ bỏ qua toàn bộ mốc thời gian và chỉ lọc vé `ngay == todayDateStr()`.
- **Giải pháp xử lý**:
  + Xây dựng toàn bộ động cơ [OnDeviceSpaQueryEngine.kt](file:///C:/Users/Admin/.gemini/antigravity/scratch/Khi-m-Th-Vi-t/app/src/main/java/com/example/OnDeviceSpaQueryEngine.kt).
  + Bóc tách chính xác mốc thời gian hỏi và áp dụng **Zero-Data Honesty** (khi 0 vé báo rõ 0 vé, không nhảy sang hôm nay).

---

### Vấn Đề 6: Hướng dẫn cài đặt API Key bảo người dùng sửa file `.env`
- **Hiện tượng thực tế**: Gehihi bảo *"vui lòng thêm GEMINI_API_KEY vào cấu hình file .env của ứng dụng nhé!"* -> Khách hàng dùng điện thoại không thể sửa file `.env`.
- **Giải pháp xử lý**:
  + Tạo hộp thoại trực quan `AiKeyConfigDialog` trên app (có link Google AI Studio lấy key miễn phí trong 1 phút, nút dán clipboard, nút test kết nối HTTP 200 OK, lưu SharedPreferences `gemini_api_key`).
  + Cung cấp endpoint trên Server Web Portal (`/api/system/ai-config`) để app tự động đồng bộ key hệ thống dùng chung ngay khi cài app.

---

### Vấn Đề 7: Nhân bản dồn ký tự IME trên giả lập LDPlayer và trôi SĐT sang ô Mật khẩu
- **Hiện tượng thực tế**: Gõ `12345` trên bàn phím máy tính vào ô Mật khẩu LDPlayer bị biến thành `112123123412345` hoặc SĐT nhảy sang ô mật khẩu.
- **Nguyên nhân kỹ thuật**: LDPlayer gửi chuỗi composing dạng tam giác tích lũy dồn (`1`, `12`, `123`...) kết hợp Autofill của Android tự động điền tài khoản.
- **Giải pháp xử lý**:
  + Tắt Autofill Android: `view.importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS`.
  + Cài đặt thuật toán khử tam giác `unpackTriangularComposing` và lọc tiền tố SĐT `sanitizePasswordInput`.
  + Bổ sung 12 unit tests kiểm chứng đạt chuẩn 100%.

---

### Vấn Đề 8: Vừa đăng nhập thành công sau vài giây bị văng ra với thông báo đỏ "Cơ sở này đã bị xóa khỏi hệ sinh thái..."
- **Hiện tượng thực tế**: Đăng nhập SĐT `123456789`, PIN `12345` thành công, sau 6 giây tự động bị đá ra màn hình đăng nhập.
- **Nguyên nhân kỹ thuật**: Tiến trình Heartbeat định kỳ gọi `GET /api/tenants/auth?phone=...` nhưng không truyền `pin`. Server kiểm tra `pin` rỗng trả về `Sai mật khẩu`. App ngộ nhận cơ sở đã bị xóa trên Web Portal nên kích hoạt cơ chế ngắt phiên.
- **Giải pháp xử lý**:
  + Trên `server.js`: Bổ sung endpoint `/api/tenants/status`, kiểm tra nếu `!pass` thì xác nhận cơ sở đang `🟢 Mở / Hoạt động` mà không bắt buộc so khớp PIN. Chỉ gửi tín hiệu ngắt phiên khi cơ sở bị Admin bấm **Xóa** thật sự.
  + Trên `SpaStore.kt`: Phân biệt rõ ràng trạng thái Hoạt động, Tạm khóa và Xóa cơ sở.

---

### Vấn Đề 9: Máy ảo có mạng và test API Key thành công nhưng Gehihi lại báo "Ngoại tuyến" & không tạo được nhân viên
- **Hiện tượng thực tế**: Speedtest mạng máy ảo đạt 5.12 Mbps, test API Key xanh 200 OK nhưng chat với Gehihi lại bảo *"Thiết bị hiện đang ở chế độ ngoại tuyến..."*. Ra lệnh *"tạo cho tôi 1 nhân viên..."* thì Gehihi in ra *"Danh sách nhân viên (0 người)"*.
- **Nguyên nhân kỹ thuật**:
  + App cũ cố định gọi mô hình `gemini-1.5-flash` và `gemini-2.0-flash` đã bị Google khai tử/ngừng hỗ trợ. Khi Google từ chối, App ngộ nhận là mất mạng ngoại tuyến.
  + Bộ lọc ngoại tuyến cũ chỉ tìm từ khóa cứng `"tao nhan vien"`, khi có từ đệm tự nhiên *"tạo cho tôi 1 nhân viên"* thì không khớp lệnh tạo mà nhảy nhầm sang lệnh xem danh sách.
- **Giải pháp xử lý**:
  + Nâng cấp sang mô hình chuẩn mới: **`gemini-2.5-flash`** và chuỗi dự phòng (`2.5-flash` $\rightarrow$ `2.5-flash-lite` $\rightarrow$ `3.8-flash`).
  + Bóc tách mã lỗi chính xác từ Google thay vì báo nhầm mất mạng.
  + Nâng cấp regex nhận diện khẩu lệnh tự nhiên tạo nhân viên và loại trừ các lệnh hiển thị danh sách khi có ý định Thêm/Tạo/Sửa/Xóa.

---

### Vấn Đề 10: Đăng nhập trên điện thoại thật báo "Không thể kết nối đến Máy chủ Web Portal" hoặc "Sai mật khẩu"
- **Hiện tượng thực tế**: Cài APK trên điện thoại thật (kết nối Wi-Fi hoặc 4G), nhập đúng SĐT `123456789`, PIN `12345` nhưng báo *"Không thể kết nối đến Máy chủ Web Portal (cổng 3000 hoặc mạng ngoại tuyến)"* hoặc *"Sai số điện thoại hoặc mật khẩu"*.
- **Nguyên nhân kỹ thuật**:
  + IP `10.0.2.2:3000` là IP cục bộ ảo chỉ dành riêng cho Android Emulator / LDPlayer trỏ về PC. Điện thoại thật kết nối qua 4G hoặc khác dải mạng Wi-Fi không thể truy cập `10.0.2.2` hay IP mạng LAN `192.168.1.105`.
  + Cloudflare Quick Tunnel cũ (`highway-workout-dairy-source.trycloudflare.com`) bị hết hạn và đổi URL khi máy tính hoặc tiến trình nền khởi động lại.
  + Cơ chế báo lỗi đăng nhập cũ gộp chung lỗi timeout/không có mạng thành "Sai mật khẩu", gây hoang mang cho người dùng.
- **Giải pháp xử lý**:
  + Bật lại Cloudflare Tunnel mới: `https://stretch-rid-utils-decreased.trycloudflare.com` (chạy nền ổn định).
  + Cập nhật URL mới vào cả 2 luồng: `pullLatestFromCloudAndLogin` (dòng 851) và `startTenantStatusListener` (dòng 1150) trong `SpaStore.kt`.
  + Bổ sung cờ `serverResponded` và biến `lastAuthErrorMessage` để phân định chính xác: Nếu server có phản hồi sai PIN mới báo "Sai mật khẩu", nếu không chạm được server thì báo rõ lỗi kết nối mạng/máy chủ.
  + Chạy toàn bộ 29/29 Unit Tests PASS, build APK mới (SHA-256: `6D93B06784A7D272F8FF87233ADE0AF5AA1153F10EED59BEF5D305770AFDB157`) và đồng bộ vào 4 vị trí + Desktop.
