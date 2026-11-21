# 🛠️ HƯỚNG DẪN VẬN HÀNH & BẢO TRÌ CHO AI TIẾP THEO

Tài liệu này tổng hợp toàn bộ các câu lệnh thực thi, quy trình kiểm thử và các bước triển khai bàn giao bắt buộc phải tuân thủ khi làm việc trên dự án **Khiếm Thị Việt**.

---

## 1. Môi Trường Làm Việc (Environment)
- **Hệ điều hành:** Windows (PowerShell / CMD).
- **Thư mục dự án:** `C:\Users\Admin\.gemini\antigravity\scratch\Khi-m-Th-Vi-t`
- **Bộ SDK & Công cụ:**
  - Java 17 / JDK 17
  - Android Gradle Plugin 8.x + Gradle Wrapper (`gradlew.bat`)
  - Node.js (Express server trên cổng 3000)
  - Git CLI
  - Cloudflare Tunnel (`C:\Users\Admin\.gemini\antigravity\scratch\cloudflared.exe`)

---

## 2. Các Câu Lệnh Vận Hành Thiết Yếu

### 🧪 Chạy Kiểm Thử Tự Động (Bắt buộc chạy trước mọi commit):
```powershell
cd "C:\Users\Admin\.gemini\antigravity\scratch\Khi-m-Th-Vi-t"
.\gradlew.bat testDebugUnitTest
```
*Để chạy toàn bộ các bài test không dùng cache:*
```powershell
.\gradlew.bat testDebugUnitTest --rerun-tasks
```

### 📱 Đóng Gói File Cài Đặt APK (Chỉ khi User yêu cầu):
```powershell
cd "C:\Users\Admin\.gemini\antigravity\scratch\Khi-m-Th-Vi-t"
.\gradlew.bat assembleDebug
Copy-Item 'app\build\outputs\apk\debug\app-debug.apk' 'KhiemThiViet-CaiDat.apk' -Force
Copy-Item 'app\build\outputs\apk\debug\app-debug.apk' 'CAI_DAT_APK\KhiemThiViet.apk' -Force
```

### 🌐 Khởi Động Máy Chủ Web Portal Cục Bộ & Cloudflare Tunnel:
```powershell
# Chạy Node.js Server (Port 3000)
cd "C:\Users\Admin\.gemini\antigravity\scratch\Khi-m-Th-Vi-t"
node server.js

# Mở Public Tunnel qua Cloudflare (nếu cần truy cập từ xa)
C:\Users\Admin\.gemini\antigravity\scratch\cloudflared.exe tunnel --url http://localhost:3000
```

### 🚀 Đồng Bộ Mã Nguồn Lên GitHub & Render:
```powershell
cd "C:\Users\Admin\.gemini\antigravity\scratch\Khi-m-Th-Vi-t"
git add -A
git commit -m "feat/fix: mô tả nội dung thay đổi rõ ràng"
git push origin main
```
*(Render tự động theo dõi nhánh `main` để triển khai bản web mới).*

---

## 3. Danh Mục Các Bộ Unit Test Bắt Buộc Phải Đạt 100% PASS
Hiện tại dự án có **85+ bài test tự động** bao phủ toàn diện:
1. `StaffAccountAdminManagementTest.kt`: 14 bài test (cấp quyền Admin, giữ nhân viên, tem Admin, chuẩn hóa tài khoản).
2. `GehihiAiChatLimitsAndIsolationTest.kt`: 13 bài test (phân quyền AI, cô lập chat theo tài khoản, rolling window 100 tin, hủy pending action).
3. `TicketCustomerCompletionTest.kt`: 5 bài test (tùy chọn lưu khách khi chốt vé, truy vấn lịch sử dịch vụ).
4. `ImeComposingDeduplicationTest.kt`: 19 bài test (chống dồn chữ ô mật khẩu trên LDPlayer).
5. `SpaOnDeviceQueryEngineTest.kt`: 12 bài test (tra cứu offline doanh thu, nhân viên bận/rảnh, xếp hạng dịch vụ).
6. `NewAccountFacilityEmptyDataTest.kt`: 4 bài test (tính trung thực của dữ liệu cơ sở mới).
7. `ReportKpiExtractionTest.kt`: 11 bài test (bóc tách chỉ số KPI báo cáo tài chính).
8. `SpaUpgradeFeaturesTest.kt`: 5 bài test (chế độ bảo trì, khóa Gemini API key).

---

## 4. Checklist Nghiệm Thu Bắt Buộc Trước Khi Phản Hồi Cho User:
- [ ] Mã nguồn biên dịch thành công (`compileDebugKotlin`).
- [ ] Chạy `.\gradlew.bat testDebugUnitTest` đạt **100% PASS (0 failures)**.
- [ ] Mọi UI mới hoặc sửa đổi đều có nhãn trợ năng TalkBack (`contentDescription`).
- [ ] Không có dữ liệu mẫu rác được tạo trong cơ sở dữ liệu.
- [ ] Nếu có yêu cầu build APK: File APK được copy vào `KhiemThiViet-CaiDat.apk` và cung cấp link tải cho user.
- [ ] Nếu có yêu cầu đồng bộ web: Đã `git push origin main` lên GitHub.
