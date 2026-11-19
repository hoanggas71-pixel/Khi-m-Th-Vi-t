# HƯỚNG DẪN CHUYỂN GIAO & NẠP DỮ LIỆU CƠ SỞ SPA KHIẾM THỊ VIỆT
> **Tài liệu bàn giao chuyển giao dữ liệu chuẩn cho Chủ Spa & Kỹ thuật viên**

---

## 1. Cấu Trúc File Chuyển Giao Dữ Liệu Chuẩn (`.json`)

Tệp dữ liệu mẫu chuẩn được lưu tại:
👉 `C:\Users\Admin\Desktop\HO_SO_BAN_GIAO_KHIEM_THI_VIET\06_MAU_CHUYEN_GIAO_DU_LIEU\MAU_CHUYEN_GIAO_DU_LIEU_SPA_CHUAN.json`

File chứa đầy đủ 6 phân hệ quản lý độc lập:
1. **`dvs` (Dịch vụ)**: 10 dịch vụ massage khiếm thị chuẩn (thời gian làm, giá tiền, hoa hồng trích cho thợ KTV).
2. **`gois` (Gói liệu trình)**: 3 gói thẻ liệu trình 10 buổi (tiết kiệm, chuyên sâu, VIP).
3. **`nvs` (Nhân sự KTV)**: Danh sách kỹ thuật viên gồm Họ tên, Giới tính, SĐT (dùng làm tên đăng nhập), tay nghề chuyên môn.
4. **`khs` (Khách hàng)**: Danh bạ khách quen, số điện thoại, ghi chú sở thích trị liệu.
5. **`luots` (Vé làm)**: Lịch sử các ca vé (nếu muốn chuyển giao cả lịch sử hoạt động).
6. **`tcs` (Sổ quỹ thu chi)**: Phiếu thu tiền vé, chi hoa hồng, chi phí điện nước, mặt bằng, vật tư quán.

---

## 2. Cách Nạp File Chuyển Giao Vào Ứng Dụng (2 Cách)

### 👉 Cách 1: Nạp siêu tốc qua Trợ lý Gehihi (Khuyên Dùng)
1. Trong App, bấm vào biểu tượng **`✨ gehihi`** ở góc trên.
2. Bấm vào nút **`+`** cạnh ô nhập tin nhắn.
3. Chọn tệp `MAU_CHUYEN_GIAO_DU_LIEU_SPA_CHUAN.json`.
4. Gehihi sẽ tự động đọc file và thông báo tóm tắt:
   > *"gehihi đã đọc file: Gồm 10 dịch vụ, 3 gói, 2 khách hàng, 2 nhân viên KTV..."*
5. Bấm nút **`Xác nhận`** (hoặc nhắn *"ok" / "đồng ý"*) $\rightarrow$ Toàn bộ dữ liệu được nạp vào máy ngay lập tức trong 0.1 giây!

### 👉 Cách 2: Nạp qua Mục Cài Đặt / Sao Lưu & Khôi Phục
1. Vào tab **Cài đặt** (Tab cuối cùng bên phải).
2. Chọn mục **Sao lưu & Khôi phục dữ liệu**.
3. Bấm **Khôi phục từ file JSON**.
4. Chọn file cần nạp $\rightarrow$ Chọn **Gộp thông minh** hoặc **Ghi đè** tùy nhu cầu.

---

## 3. Cách Xuất File Dữ Liệu Để Chuyển Giao Cho Máy Khác
1. Vào tab **Cài đặt** $\rightarrow$ **Sao lưu & Khôi phục**.
2. Bấm **Xuất file sao lưu (JSON)**.
3. File sẽ được lưu vào bộ nhớ máy, bạn có thể gửi file này qua Zalo, Gmail, Drive để nạp sang máy phụ hoặc chuyển giao cho cơ sở khác.
