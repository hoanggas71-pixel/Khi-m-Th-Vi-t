# Hướng Dẫn Triển Khai Hoàn Chỉnh: Tên Miền `.id.vn` + Cloudflare + Render + Firebase + UptimeRobot (0đ)

Tài liệu này đi kèm bản nâng cấp toàn diện cho **Khiếm Thị Việt SaaS** (`server.js`, `public/index.html`, `render.yaml`, `SpaStore.kt`), giúp hệ thống chạy 24/7 không ngủ đông, lưu tài khoản Chủ Spa bền vững 100% không bao giờ bị reset, tự khóa khi hết hạn gói và chống đăng nhập lại tài khoản đã xóa trên App Android.

---

## Bước 1: Đưa Tên Miền `.id.vn` Lên Cloudflare (DNS & SSL Miễn Phí)
1. Đăng nhập [https://dash.cloudflare.com](https://dash.cloudflare.com) → bấm **Add a Site** → nhập tên miền `.id.vn` của bạn (ví dụ `tenban.id.vn`) → chọn gói **Free ($0)**.
2. Copy **2 địa chỉ Nameserver (NS)** Cloudflare cấp.
3. Vào trang quản trị nhà cung cấp tên miền `.id.vn` (Nhân Hòa / Mắt Bão / ...) → mục **Đổi Nameserver** → dán 2 NS của Cloudflare và lưu lại (chờ 5–15 phút).
4. Trong Cloudflare vào **SSL/TLS → Overview** → chọn **Full (strict)** *(tránh lỗi vòng lặp `ERR_TOO_MANY_REDIRECTS`)*.

---

## Bước 2: Gắn Tên Miền `.id.vn` Vào Render Dashboard
1. Mở **Render Dashboard** → chọn Web Service `khienthiviet` → **Settings → Custom Domains → Add Custom Domain**.
2. Nhập tên miền chính `tenban.id.vn` (và tùy chọn thêm `admin.tenban.id.vn` hoặc `portal-rieng.tenban.id.vn` để mở thẳng Cổng Dev Admin).
3. Quay lại **Cloudflare → DNS → Add record**:
   - Tên miền gốc (`@`): Type `A`, Name `@`, IPv4 theo địa chỉ Render hiển thị.
   - Tên miền phụ (`admin` hoặc `portal-rieng` hoặc `api`): Type `CNAME`, Name `admin`, Target `khienthiviet.onrender.com`.
   - **Lưu ý:** Lúc mới trỏ hãy để **DNS only (mây xám)** để Render cấp SSL (~2–5 phút). Khi Render báo **Verified** mới bật lại **Proxied (mây cam)**.

---

## Bước 3: Bật Lưu Trữ Vĩnh Viễn (Chống Reset Khi Render Ngủ / Redeploy)
Vào **Render Dashboard → dịch vụ `khienthiviet` → Environment → Add Environment Variable**:

### Cách A — Dùng Firebase Firestore (Khuyên dùng — 0đ, tức thì, không làm Render build lại)
1. Vào [https://console.firebase.google.com](https://console.firebase.google.com) → chọn hoặc tạo 1 Project mới miễn phí → bật **Firestore Database (Production mode)**.
2. Vào **Project Settings (⚙️) → Service accounts → Generate new private key** để tải file `.json`.
3. Mở file `.json` đó, copy toàn bộ nội dung (1 dòng) dán vào biến môi trường trên Render:
   - `FIREBASE_SERVICE_ACCOUNT_JSON` = *(toàn bộ nội dung JSON)*
   - `FIREBASE_DATABASE_ID` = `(default)` *(hoặc ID database riêng nếu có)*

### Cách B — Hoặc dùng GitHub Data Branch (Nếu chưa muốn tạo Firebase)
- Tạo GitHub Personal Access Token (quyền `Contents: Read & Write` cho repo `Khi-m-Th-Vi-t`) và dán vào Render:
  - `GITHUB_TOKEN` = *(token của bạn)*
  - `GITHUB_DATA_BRANCH` = `render-data` *(lưu sang nhánh riêng nên KHÔNG kích hoạt Render rebuild)*

### Các biến tùy chọn khác trên Render Environment:
- `DEV_ADMIN_PIN`: Mã PIN mở Cổng Dev Super Admin (mặc định nếu không đặt là `247208`).
- `CUSTOM_DOMAIN`: Nhập tên miền của bạn (ví dụ `tenban.id.vn`).
- `GEMINI_API_KEY`: Khóa Gemini AI hệ thống (giữ ổn định qua mọi lần Render restart).

Sau khi bấm **Save Changes**, Render sẽ khởi động lại và góc phải Cổng Dev sẽ chuyển sang đèn xanh **`Cloud Firestore: Bền Vững 100%`**.

---

## Bước 4: Chống Ngủ Đông 24/7 Bằng UptimeRobot (Miễn Phí)
1. Đăng ký tài khoản miễn phí tại [https://uptimerobot.com](https://uptimerobot.com).
2. Bấm **Add New Monitor**:
   - Monitor Type: **HTTP(s)**
   - URL: `https://tenban.id.vn/health` *(hoặc `https://khienthiviet.onrender.com/health`)*
   - Monitoring Interval: **5 minutes**
3. Bấm **Create Monitor** → Render sẽ luôn thức 24/7, không còn độ trễ ngủ đông 30–50 giây nữa.
