# HƯỚNG DẪN KỸ THUẬT: CƠ CHẾ TỰ ĐỘNG SAO LƯU HÀNG TUẦN RA DOCUMENTS & CHIA SẺ ZALO

**Dự án**: Khiếm Thị Việt (Hệ Thống Quản Lý Spa Chuyên Biệt Cho Người Khiếm Thị)  
**Tài liệu tham chiếu**: R2 - Hướng dẫn Sao lưu & Trợ năng TalkBack  
**Phiên bản**: 2.0 (Milestone M2)  
**Ngày phát hành**: Tháng 11/2026  

---

## 1. TỔNG QUAN VỀ KIẾN TRÚC BẢO VỆ DỮ LIỆU KÉP (HYBRID DATA PROTECTION)

Hệ thống quản lý Spa Khiếm Thị Việt được thiết kế theo triết lý **"Bảo vệ dữ liệu toàn diện - Không phụ thuộc đơn lẻ"**, đảm bảo cơ sở vận hành trơn tru ngay cả khi mất mạng internet hoặc gặp sự cố phần cứng. Hệ thống ứng dụng mô hình bảo vệ kép:

1. **Lớp 1: Đồng bộ Đám mây Google Cloud Firestore (Thời gian thực)**:
   - Mọi giao dịch mở vé, chốt vé, thu chi sổ quỹ đều được đồng bộ tự động lên cụm máy chủ đám mây Google Firestore.
   - Khi chuyển sang máy mới, chỉ cần đăng nhập tài khoản Google Cloud của cơ sở, toàn bộ dữ liệu sẽ tự động nạp về máy trong vài giây.
   - Tích hợp bộ nén và lọc thông minh giữ gói dữ liệu luôn dưới ngưỡng 750KB (chống tràn giới hạn 1MB Firestore), bảo toàn 100% dữ liệu danh mục vé, khách hàng, nhân viên, sổ quỹ và 20.000 mẩu ký ức trợ lý Gehihi.

2. **Lớp 2: Sao lưu Cục bộ Định kỳ Hàng tuần ra Thư mục Documents & Chia sẻ Zalo (Độc lập)**:
   - Đây là chốt chặn an toàn cuối cùng độc lập với tài khoản Google.
   - Phòng ngừa các rủi ro: Chủ Spa quên mật khẩu Gmail, tài khoản Google bị tạm khóa do vi phạm chính sách của bên thứ ba, hoặc cơ sở hoạt động hoàn toàn ở chế độ mạng nội bộ (Offline-First).
   - Tệp sao lưu là một tập tin chuẩn văn bản JSON độc lập, lưu trên bộ nhớ trong điện thoại và được đưa lên kho lưu trữ đám mây cá nhân Zalo ("Cloud của tôi") thông qua Intent hệ thống Android.

---

## 2. QUY TRÌNH KỸ THUẬT TỰ ĐỘNG SAO LƯU HÀNG TUẦN (WEEKLY BACKUP)

### 2.1. Chu kỳ và Điều kiện Kích hoạt
- **Chu kỳ**: 7 ngày một lần (168 giờ liên tục).
- **Cơ chế theo dõi**:
  - Hệ thống lưu trữ dấu thời gian (timestamp) của lần sao lưu thành công gần nhất vào `SharedPreferences` với khóa `last_weekly_backup_timestamp`.
  - Mỗi khi ứng dụng được khởi chạy hoặc quay lại tiền trình (OnResume), hệ thống thực hiện phép so sánh:
    $$\Delta t = \text{System.currentTimeMillis}() - \text{lastWeeklyBackupTimestamp}$$
  - Nếu $\Delta t \ge 7 \times 86.400 \times 1.000\text{ ms}$ (đủ 7 ngày), tiến trình tạo bản sao lưu định kỳ sẽ tự động được kích hoạt trong nền.

### 2.2. Trích xuất Cấu trúc Dữ liệu JSON Toàn diện
Tiến trình gọi hàm `store.xuatDuLieuBackupJson()` để tuần tự hóa toàn bộ các bảng thực thể của cơ sở thành chuỗi JSON tiêu chuẩn UTF-8, bao gồm:
- **Thông tin cơ sở & tài khoản**: Danh sách tài khoản đăng nhập (`taiKhoans`), quyền hạn chi tiết (`cacQuyenDuocCap`), mã định danh cơ sở (`tenantId`).
- **Danh mục nhân sự**: Danh sách kỹ thuật viên (`nvs`), số điện thoại, ngày vào làm, ca kíp.
- **Bảng giá & Gói liệu trình**: Dịch vụ đơn lẻ (`dvs`), mức hoa hồng thợ, các gói combo nhiều buổi (`gois`), dịch vụ tặng kèm (`dvTangKem`).
- **Hồ sơ khách hàng**: Danh bạ khách (`khs`), lịch sử dịch vụ, số buổi còn lại trong gói (`dks`).
- **Nhật ký vận hành vé**: Lịch sử ca làm việc (`luots`), trạng thái phục vụ (Đang làm, Chờ chốt, Đã chốt), tiền tip, tiền hoa hồng chi tiết từng ca.
- **Sổ quỹ thu chi ngoài lề**: 2 cột thu - chi song song (`tcs`), mua vật tư, tiền điện nước tiệm.
- **Kho ký ức Trợ lý AI Gehihi**: Lịch sử hội thoại âm thanh, ghi chú giọng nói, chỉ thị vận hành đã qua phê duyệt.

### 2.3. Vị trí Lưu trữ Trên Hệ điều hành Android (Scoped Storage Compliant)
- Tương thích 100% từ Android 10 (API level 29) đến Android 15+:
  - **Đường dẫn chuẩn**: Thư mục công khai `Environment.DIRECTORY_DOCUMENTS/KhiemThiViet_Backups/` (hoặc `/storage/emulated/0/Documents/KhiemThiViet/`).
  - **Quy tắc đặt tên file**: `backup_khiem_thi_viet_[tenantId]_[YYYY-MM-DD].json`
    *(Ví dụ: `backup_khiem_thi_viet_spa_0912345678_2026-11-18.json`)*.
  - Sử dụng `MediaStore.Files` API hoặc `FileProvider` để tạo tệp an toàn mà không cần yêu cầu quyền nguy hiểm `MANAGE_EXTERNAL_STORAGE`.

---

## 3. CƠ CHẾ INTENT CHIA SẺ SANG ZALO CHO CHỦ SPA XÁC NHẬN

### 3.1. Tại sao lựa chọn Zalo làm kênh lưu trữ thứ cấp?
- Hầu hết 100% Chủ Spa và thợ khiếm thị tại Việt Nam đều sử dụng Zalo hàng ngày làm kênh liên lạc chính.
- Mục **"Cloud của tôi" (My Cloud)** trên Zalo cung cấp dung lượng lưu trữ đám mây trọn đời hoàn toàn miễn phí, hỗ trợ gửi và tải mọi định dạng tệp (bao gồm cả file `.json`).
- Thao tác gửi tệp vào Cloud của tôi trên Zalo cực kỳ đơn giản, chỉ cần chạm đúp 2 lần trên trình đọc TalkBack, không đòi hỏi kiến thức kỹ thuật phức tạp như Google Drive hay OneDrive.

### 3.2. Luồng Kích hoạt Intent Kỹ thuật trên Android
Khi tệp `.json` được tạo thành công trong thư mục Documents:
1. Hệ điều hành tạo một URI nội bộ thông qua `FileProvider` đã cấu hình trong `AndroidManifest.xml`:
   ```kotlin
   val fileUri = FileProvider.getUriForFile(
       context,
       "${context.packageName}.fileprovider",
       backupFile
   )
   ```
2. Khởi tạo Android `Intent.ACTION_SEND`:
   ```kotlin
   val shareIntent = Intent(Intent.ACTION_SEND).apply {
       type = "application/json"
       putExtra(Intent.EXTRA_STREAM, fileUri)
       putExtra(Intent.EXTRA_SUBJECT, "Bản Sao Lưu Khiếm Thị Việt - Tuần ${getWeekNumber()}")
       putExtra(Intent.EXTRA_TEXT, "File sao lưu dữ liệu tự động định kỳ 1 tuần của cơ sở Spa Khiếm Thị Việt. Hãy lưu vào Cloud của tôi để phòng ngừa sự cố.")
       addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
   }
   ```
3. Màn hình hiển thị hộp thoại xác nhận cho Chủ Spa:
   - *"Đã đến lịch sao lưu hàng tuần. Hệ thống đã tạo bản sao lưu mới nhất vào thư mục Documents. Bạn có muốn gửi file sang Zalo để cất giữ an toàn không?"*
4. Khi Chủ Spa chọn "Gửi Zalo": Hệ thống mở bộ chọn ứng dụng (Chooser), ưu tiên ứng dụng Zalo (`com.zing.zalo`). Chủ Spa chỉ cần chọn người nhận là **"Cloud của tôi"** hoặc gửi cho người đồng quản lý.

---

## 4. KỊCH BẢN GIỌNG ĐỌC TALKBACK CHUẨN HÓA CHO NGƯỜI KHIẾM THỊ

Văn bản dưới đây đã được tối ưu hóa ngữ điệu, ngắt câu rõ ràng bằng dấu chấm và phẩy, sử dụng các cụm từ ngắn gọn để Google Speech Engine và Samsung TTS đọc to, rành mạch và dễ hiểu nhất:

> **"Tính năng Sao lưu dữ liệu hàng tuần.  
> Hệ thống Khiếm Thị Việt tự động tạo một bản sao lưu toàn bộ dữ liệu của cơ sở vào thư mục Documents trên điện thoại của bạn, định kỳ mỗi tuần một lần.  
> Dữ liệu sao lưu bao gồm: danh sách khách hàng, nhân viên, bảng giá dịch vụ, các vé phục vụ, sổ quỹ thu chi và ký ức trợ lý Gehihi.  
> Sau khi tạo file sao lưu, ứng dụng sẽ tự động mở hộp thoại chia sẻ để bạn gửi file trực tiếp sang Zalo. Bạn nên chọn gửi vào mục 'Cloud của tôi' trên Zalo để lưu trữ an toàn vĩnh viễn trên mạng.  
> Bất cứ lúc nào, bạn cũng có thể bấm nút 'Xuất file Backup' hoặc 'Chia sẻ Zalo' trong màn hình này để sao lưu ngay lập tức.  
> Khi bạn đổi sang điện thoại mới hoặc lỡ xóa ứng dụng, bạn chỉ cần chọn lại file sao lưu từ Zalo để khôi phục lại toàn bộ dữ liệu một trăm phần trăm, không lo mất mát thông tin của tiệm."**

### Tích hợp trong Giao diện Android:
- **Vị trí**: Nằm tại đầu Subtab 2 ("Sao lưu & Khôi phục") trong Tab 5 ("Quyền hạn Admin").
- **Tên nút**: `📢 HƯỚNG DẪN TALKBACK: SAO LƯU HÀNG TUẦN & ZALO` (testTag: `btn_talkback_backup_help`).
- **Thuộc tính trợ năng**: Gắn `Modifier.semantics { contentDescription = "..." }` bao gồm toàn bộ nội dung kịch bản trên, giúp người khiếm thị chỉ cần chạm vào nút là TalkBack sẽ đọc trọn vẹn hướng dẫn.

---

## 5. QUY TRÌNH KHÔI PHỤC DỮ LIỆU TỪ ZALO KHI ĐỔI MÁY MỚI HOẶC CÀI LẠI APP

Khi Chủ Spa mua điện thoại mới hoặc cài đặt lại ứng dụng sau khi đặt lại máy:

1. **Bước 1 - Tải file sao lưu từ Zalo**:
   - Mở ứng dụng Zalo trên máy mới -> Vào mục **"Cloud của tôi"**.
   - Tìm file tin nhắn có tên `backup_khiem_thi_viet_...json`.
   - Chạm đúp vào file để tải về máy (file sẽ được lưu tại thư mục `Download` hoặc `Zalo`).
2. **Bước 2 - Mở chức năng Nhập dữ liệu trong App**:
   - Mở ứng dụng Khiếm Thị Việt -> Đăng nhập tài khoản Chủ Spa.
   - Chuyển sang Tab 5 (**"Quyền hạn"**) -> Chọn mục (**"Sao lưu & Khôi phục"**).
   - Vuốt đến thẻ **"Card 2: Nhập dữ liệu từ File Backup (.json) cũ hoặc mới"**.
3. **Bước 3 - Chọn File và Hoàn tất**:
   - Chạm đúp nút **"CHỌN FILE BACKUP (.JSON) ĐỂ NHẬP"**.
   - Bộ chọn tệp của Android mở ra -> Chọn file sao lưu vừa tải từ Zalo.
   - Hệ thống hiển thị hộp thoại xác nhận số lượng dữ liệu sẽ nạp (Khách hàng, Dịch vụ, Sổ quỹ...).
   - Bấm **"Xác nhận nhập"**: Toàn bộ hệ thống được khôi phục 100% trong vòng chưa đầy 3 giây, kèm thông báo giọng đọc thành công.
