# Hướng Dẫn Triển Khai Hoàn Chỉnh: `hoangdev08.id.vn` + Cloudflare + Render + Firebase + UptimeRobot (0đ)

Tài liệu này đi kèm bản nâng cấp cho tên miền chính thức **`https://hoangdev08.id.vn`**, giúp hệ thống chạy 24/7 không ngủ đông, lưu tài khoản Chủ Spa bền vững 100% trên Firebase Firestore (không bao giờ bị reset), tự khóa khi hết hạn gói và chặn đăng nhập lại tài khoản đã xóa trên App Android.

---

## Bước 1: Đưa `hoangdev08.id.vn` Lên Cloudflare (DNS & SSL Miễn Phí)
1. Đăng nhập [https://dash.cloudflare.com](https://dash.cloudflare.com) → bấm **Add a Site** → nhập **`hoangdev08.id.vn`** → chọn gói **Free ($0)**.
2. Copy **2 địa chỉ Nameserver (NS)** Cloudflare cấp.
3. Vào trang quản trị nhà cung cấp tên miền `.id.vn` (Nhân Hòa / Mắt Bão / ...) → mục **Đổi Nameserver** của `hoangdev08.id.vn` → dán 2 NS của Cloudflare và lưu lại (chờ 5–15 phút).
4. Trong Cloudflare vào **SSL/TLS → Overview** → chọn **Full (strict)** *(tránh lỗi vòng lặp `ERR_TOO_MANY_REDIRECTS`)*.

---

## Bước 2: Gắn `hoangdev08.id.vn` Vào Render Dashboard
1. Mở **Render Dashboard** → chọn Web Service `khienthiviet` → **Settings → Custom Domains → Add Custom Domain** → nhập **`hoangdev08.id.vn`**.
2. Quay lại **Cloudflare → DNS → Add record** theo đúng giá trị Render hiển thị:
   - Tên miền gốc (`@`): Type `A`, Name `@`, IPv4 theo địa chỉ Render hiển thị (hoặc CNAME flattening `khienthiviet.onrender.com`).
   - **Lưu ý quan trọng:** Lúc mới trỏ hãy để **DNS only (mây xám)** để Render cấp chứng chỉ SSL (~2–5 phút). Khi Render báo **Verified** mới bật lại **Proxied (mây cam)**.
3. Sau khi hoàn tất:
   - Trang chủ & Cổng Chủ Spa: `https://hoangdev08.id.vn`
   - Cổng Quản Trị Dev Super Admin: nút 🔒 góc phải hoặc `https://hoangdev08.id.vn/portal-admin` (mã PIN mặc định `247208`)
   - Link tải APK chính thức: `https://hoangdev08.id.vn/KhiemThiViet-CaiDat.apk`

---

## Bước 3: Bật Lưu Trữ Vĩnh Viễn Firebase Firestore Trên Render (Chống Reset 100%)
1. Vào [https://console.firebase.google.com](https://console.firebase.google.com) → chọn hoặc tạo 1 Project mới miễn phí → bật **Firestore Database (Production mode)**.
2. Vào **Project Settings (⚙️) → Service accounts → Generate new private key** để tải file `.json`.
3. Vào **Render Dashboard → dịch vụ `khienthiviet` → Environment → Add Environment Variable**:
   - `FIREBASE_SERVICE_ACCOUNT_JSON` = *(copy toàn bộ nội dung file JSON vừa tải)*
   - `FIREBASE_DATABASE_ID` = `(default)`
   - `CUSTOM_DOMAIN` = `hoangdev08.id.vn`
   - `DEV_ADMIN_PIN` = `247208` *(hoặc đổi mã PIN riêng của bạn)*
   - `GEMINI_API_KEY` = *(tùy chọn, khóa Gemini AI hệ thống)*
4. Bấm **Save Changes**. Khi Render khởi động lại, mở `https://hoangdev08.id.vn/portal-admin` sẽ thấy đèn xanh **`Cloud Firestore: Bền Vững 100%`**.

---

## Bước 4: Chống Ngủ Đông 24/7 Bằng UptimeRobot (Miễn Phí)
1. Đăng ký tài khoản miễn phí tại [https://uptimerobot.com](https://uptimerobot.com).
2. Bấm **Add New Monitor**:
   - Monitor Type: **HTTP(s)**
   - URL: **`https://hoangdev08.id.vn/health`**
   - Monitoring Interval: **5 minutes**
3. Bấm **Create Monitor** → Render luôn thức 24/7.
