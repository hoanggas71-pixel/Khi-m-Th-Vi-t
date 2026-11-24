# Quy Tắc Vận Hành & Bảo Mật — Khiếm Thị Việt SaaS

## 0. Nguyên Tắc Tư Duy Gốc Rễ (Không Vá Theo Ví Dụ Đơn Lẻ)
- **Luôn nhận diện lớp lỗi hệ thống, không nhắm vào 1 tài khoản/dữ liệu ví dụ:** Khi người dùng nêu một trường hợp cụ thể (ví dụ: một tài khoản cũ vẫn đăng nhập được trên App dù trên Web sau 1 đêm mở lại không còn cơ sở nào), phải xem đó là **biểu hiện của lỗi kiến trúc chung áp dụng cho TẤT CẢ các cơ sở / tài khoản**, tuyệt đối không hardcode xử lý riêng theo ID/SĐT mẫu và không báo cáo theo kiểu chỉ sửa cho 1 ví dụ đó.
- **Đảm bảo nhất quán 2 chiều Web ↔ App cho mọi cơ sở:**
  1. Nguồn dữ liệu trên Web/Server (Firebase Firestore) phải bền vững qua mọi lần ngủ đông/khởi động lại để không bao giờ tự mất danh sách cơ sở đã tạo.
  2. Mọi cơ sở bất kỳ khi bị xóa trên Web đều phải bị vô hiệu hóa & xóa sạch cache đăng nhập cục bộ trên App Android (áp dụng tổng quát cho mọi `tenantId` / `ownerPhone`).

## 1. Hạ Tầng & Tên Miền Chính Thức
- **Tên miền chính:** `https://hoangdev08.id.vn` (qua Cloudflare DNS + SSL `Full (strict)` trỏ về Render).
- **Tên miền dự phòng:** `https://khienthiviet.onrender.com`.
- **Cổng quản trị Dev Super Admin:** mở bằng nút khóa 🔒 trên trang chủ hoặc `https://hoangdev08.id.vn/portal-admin`.
- **Health Check (UptimeRobot 5 phút/lần):** `GET /health` và `GET /api/health` phải luôn trả `200 OK` cực nhẹ, không truy vấn DB hay đọc ổ đĩa.
- **Lưu trữ bền vững trên Render Free:** Tuyệt đối không coi ổ đĩa cục bộ của Render là nơi lưu vĩnh viễn. Dữ liệu cơ sở (`tenants`), bảng giá và ngân hàng phải đồng bộ qua **Firebase Firestore** (`FIREBASE_SERVICE_ACCOUNT_JSON`, chỉ đọc 1 lần khi boot vào RAM và ghi khi có thay đổi).

## 2. Quy Tắc Dữ Liệu & Chống Hồi Sinh Dữ Liệu Rác
- File `public/tenants.json` trong Git phải luôn giữ sạch (`[]`), tuyệt đối không commit mọi cơ sở thử nghiệm hoặc dữ liệu rác lên Git.
- Không tự động seed lại `DEFAULT_TENANTS` trong `server.js` hay `public/index.html` khi danh sách cơ sở trống `[]`.
- Không tự ý chỉnh sửa `public/ai-config.json` hay API key Gemini khi người dùng chưa yêu cầu rõ ràng.

## 3. Quy Tắc Bảo Mật Backend, Web & Android
- **Không lộ `tempPin` và khóa bí mật:** `GET /api/tenants` công khai phải ẩn `tempPin`; chặn truy cập tĩnh (`403`) tới `/tenants.json`, `/portal-config.json`, `/ai-config.json`.
- **Xác thực mọi API ghi:** `POST/DELETE /api/tenants`, `POST /api/tenants/extend`, `POST /api/system/portal-config`, `POST /api/system/ai-config` bắt buộc xác thực Dev Bearer Token (`/api/dev/login`) hoặc `X-Dev-Pin`.
- **Cấm PIN cửa hậu cho khách:** Không cho phép dùng `"247208"`, `"1234"` hoặc PIN rỗng để đăng nhập vào tài khoản Chủ Spa khách hàng; chỉ giữ `DEV_ADMIN_PIN` cho Cổng Dev Super Admin và tài khoản Admin gốc trên Android (`0397772963` / `247208`).
- **Xóa sạch cache tổng quát cho mọi cơ sở bị xóa trên Android (`SpaStore.kt`):** Khi server báo bất kỳ cơ sở nào `notFound` (và không phải `coldStartEmpty`), bắt buộc gọi `clearDeletedTenantCache(tenantId, ownerPhone)` trước khi `logout()` để đảm bảo tính nhất quán Web ↔ App cho mọi tài khoản, không thể đăng nhập lại kể cả khi offline.
- **Tài khoản Admin gốc Android (`0397772963` / `247208`, `tk_admin`):** Luôn giữ nguyên khả năng đăng nhập offline và miễn bị đá phiên bởi heartbeat (trừ khi người dùng yêu cầu gỡ bỏ).

## 4. Đóng Gói APK & Kiểm Thử Trước Khi Push
- Luôn chạy `node test_facility_deletion_permanent.js` và `node test_gemini_key_validation.js` trước khi push thay đổi Web/Backend.
- Khi có thay đổi Kotlin/Android, chạy `.\gradlew.bat testDebugUnitTest assembleDebug` và đồng bộ file APK mới tới cả `KhiemThiViet-CaiDat.apk`, `public/KhiemThiViet-CaiDat.apk`, `CAI_DAT_APK/KhiemThiViet.apk`, `CAI_DAT_APK/KhiemThiViet-CaiDat.apk`.
