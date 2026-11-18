# CẨM NANG HƯỚNG DẪN QUẢN TRỊ & VẬN HÀNH TOÀN DIỆN DÀNH CHO CHỦ CƠ SỞ SPA KHIẾM THỊ VIỆT

**Dự án**: Hệ Thống Quản Lý Spa Khiếm Thị Việt  
**Đối tượng sử dụng**: Chủ cơ sở Spa, Quản lý tiệm Massage Người Khiếm Thị  
**Phiên bản**: M2 Iteration 3 (Cập nhật Tháng 11/2026)  
**Hotline & Zalo Kỹ thuật**: `0397772963`  

---

## MỤC LỤC
1. [Phần 1: Giới thiệu Tổng quan về Hệ sinh thái Khiếm Thị Việt](#phần-1-giới-thiệu-tổng-quan-về-hệ-sinh-thái-khiếm-thị-việt)
2. [Phần 2: Hướng dẫn Cài đặt File APK trên Điện thoại Android](#phần-2-hướng-dẫn-cài-đặt-file-apk-trên-điện-thoại-android)
3. [Phần 3: Đăng nhập & Thiết lập Tài khoản Chủ Spa (Admin Gốc)](#phần-3-đăng-nhập--thiết-lập-tài-khoản-chủ-spa-admin-gốc)
4. [Phần 4: Quản lý Nhân sự & Phân quyền Kỹ thuật viên (KTV)](#phần-4-quản-lý-nhân-sự--phân-quyền-kỹ-thuật-viên-ktv)
5. [Phần 5: Cài đặt Bảng giá Dịch vụ & Gói Liệu trình Combo](#phần-5-cài-đặt-bảng-giá-dịch-vụ--gói-liệu-trình-combo)
6. [Phần 6: Điều phối Vận hành Vé & Phân ca Tua Thợ Công bằng](#phần-6-điều-phối-vận-hành-vé--phân-ca-tua-thợ-công-bằng)
7. [Phần 7: Quản lý Sổ Quỹ Thu - Chi Ngoài lề (Cân đối 2 Cột)](#phần-7-quản-lý-sổ-quỹ-thu---chi-ngoài-lề-cân-đối-2-cột)
8. [Phần 8: Báo cáo Doanh thu, Bảng Ô Lịch & Bảng Lương Thợ](#phần-8-báo-cáo-doanh-thu-bảng-ô-lịch--bảng-lương-thợ)
9. [Phần 9: Khai thác Sức mạnh Trợ lý Trí tuệ Nhân tạo Gehihi](#phần-9-khai-thác-sức-mạnh-trợ-lý-trí-tuệ-nhân-tạo-gehihi)
10. [Phần 10: Quy trình Sao lưu Định kỳ Documents & Chia sẻ Zalo An toàn](#phần-10-quy-trình-sao-lưu-định-kỳ-documents--chia-sẻ-zalo-an-toàn)

---

## PHẦN 1: GIỚI THIỆU TỔNG QUAN VỀ HỆ SINH THÁI KHIẾM THỊ VIỆT

Hệ sinh thái **Khiếm Thị Việt** là nền tảng quản trị công nghệ chuyên sâu đầu tiên tại Việt Nam được thiết kế và may đo riêng cho các cơ sở Xoa bóp, Bấm huyệt, Spa do người khiếm thị làm chủ và vận hành.

### Các Tính Năng Nổi Bật:
- **Tối ưu 100% cho Người Khiếm Thị**: Hỗ trợ toàn diện trình đọc màn hình Google TalkBack, độ tương phản cao đạt chuẩn AAA, các vùng chạm cảm ứng lớn (tối thiểu 48x48dp), nhịp rung phản hồi xúc giác độc quyền (`450ms rung - 200ms nghỉ - 450ms rung`) báo hiệu có vé mới.
- **Phân Tua Thợ Tự Động & Công Bằng**: Minh bạch tuyệt đối các lượt phục vụ, đảm bảo mọi kỹ thuật viên đều có thu nhập đồng đều và không xảy ra khiếu nại nội bộ.
- **Tính Hoa Hồng & Tiền Lương Tức Thì**: Trích hoa hồng tự động theo từng vé ngay khi khách thanh toán, hỗ trợ gửi phiếu lương chi tiết cho thợ qua Zalo chỉ với một lần chạm.
- **Sổ Quỹ Thu - Chi 2 Cột Song Song**: Quản lý rạch ròi giữa tiền thu từ dịch vụ và các khoản chi vật tư, điện nước, ăn uống ngoài lề.
- **Thanh Toán Không Tiền Mặt VietQR Napas 247**: Tự động sinh mã QR động chuẩn compact2 có sẵn số tiền và nội dung theo ngân hàng của Chủ Spa, khách quét mã chuyển tiền trực tiếp về tài khoản Chủ tiệm.
- **Trợ Lý Trí Tuệ Nhân Tạo Gehihi**: Tích hợp mô hình AI tiên tiến, nhận lệnh bằng giọng nói tự nhiên tiếng Việt, hỗ trợ tra cứu báo cáo và ra lệnh tạo vé, thu chi với cơ chế xác nhận an toàn 2 bước.

---

## PHẦN 2: HƯỚNG DẪN CÀI ĐẶT FILE APK TRÊN ĐIỆN THOẠI ANDROID

Ứng dụng Khiếm Thị Việt được phân phối dưới dạng file cài đặt độc lập `KhiemThiViet-CaiDat.apk`. Bạn có thể tải file trực tiếp từ Web Portal của hệ thống hoặc nhận file qua tin nhắn Zalo từ nhà phát triển (`0397772963`).

### 2.1. Bật Quyền Cài Đặt Nguồn Không Xác Định (Cài đặt ngoài CH Play)
Tùy thuộc vào dòng điện thoại Android bạn đang sử dụng, hãy thao tác theo hướng dẫn sau:
- **Điện thoại Samsung (One UI)**:
  1. Vào **Cài đặt** -> Chọn **Bảo mật và quyền riêng tư** -> Chọn **Cài đặt ứng dụng không xác định**.
  2. Tìm ứng dụng bạn đã dùng để tải file (ví dụ: **Zalo**, **Chrome**, hoặc **File của bạn**).
  3. Gạt công tắc sang trạng thái **BẬT** (Cho phép từ nguồn này).
- **Điện thoại Xiaomi, Redmi, POCO (MIUI / HyperOS)**:
  1. Vào **Cài đặt** -> Chọn **Bảo vệ quyền riêng tư** -> Chọn **Quyền đặc biệt** -> **Cài đặt ứng dụng không xác định**.
  2. Chọn **Zalo** hoặc **Trình duyệt** -> Bật cho phép và tích vào ô xác nhận "Tôi nhận thức được các rủi ro có thể xảy ra" sau 10 giây đếm ngược.
- **Điện thoại OPPO, Realme (ColorOS)**:
  1. Vào **Cài đặt** -> Chọn **Bảo mật** -> Chọn **Cài đặt từ nguồn không xác định** và bật cho phép.

### 2.2. Xử Lý Cảnh Báo Google Play Protect
Khi mở file APK để cài đặt, màn hình Google Play Protect có thể xuất hiện cảnh báo *"Ứng dụng bị chặn bởi Play Protect"* (do ứng dụng phát triển riêng cho cộng đồng khiếm thị chưa đưa lên Google Play Store công cộng):
1. Chạm vào chữ **"Chi tiết"** (More details) có mũi tên nhỏ chỉ xuống.
2. Chạm vào nút **"Vẫn cài đặt"** (Install anyway).
3. Ứng dụng sẽ hoàn tất cài đặt trong vòng 5 giây và biểu tượng **Khiếm Thị Việt** sẽ xuất hiện trên màn hình chính của bạn.

---

## PHẦN 3: ĐĂNG NHẬP & THIẾT LẬP TÀI KHOẢN CHỦ SPA (ADMIN GỐC)

### 3.1. Đăng Nhập Lần Đầu Tiên
1. Mở ứng dụng Khiếm Thị Việt. Màn hình **Đăng nhập một lần duy nhất** (`OneTimeLoginScreen`) sẽ xuất hiện.
2. Ô **"Tên đăng nhập"**: Nhập số điện thoại của Chủ Spa đã đăng ký (placeholder mẫu hiển thị `VD: 0912345678`).
3. Ô **"Mật khẩu"**: Nhập mã PIN bảo mật do nhà phát triển cấp (placeholder mẫu `VD: 123456`).
4. **Tùy chọn "Ghi nhớ tài khoản / mật khẩu"**:
   - Ô Checkbox này được mặc định **ĐÃ TÍCH CHỌN**.
   - TalkBack sẽ đọc: *"Ghi nhớ tài khoản và mật khẩu, đã chọn. Tự động đăng nhập vào các lần mở ứng dụng tiếp theo."*
   - Giữ nguyên tích chọn để các lần mở app sau không cần gõ lại tài khoản.
   - Nếu bạn dùng máy lạ hoặc cho mượn máy, hãy chạm đúp để bỏ tích; khi đó đóng app mở lại sẽ bắt buộc đăng nhập lại.
5. Chạm đúp nút **"ĐĂNG NHẬP (GHI NHỚ 1 LẦN)"**: Ứng dụng sẽ kết nối đám mây, tải dữ liệu tiệm của bạn và mở thẳng vào màn hình làm việc.
6. *Nếu chưa có tài khoản*: Chạm vào nút **"MUA / GIA HẠN GÓI DỊCH VỤ (ZALO)"** để được điều hướng sang Zalo gặp nhà phát triển đăng ký mới.

### 3.2. Cấu Hình Thông Tin Ngân Hàng Thụ Hưởng VietQR
Để khách hàng có thể quét mã chuyển khoản trực tiếp vào tài khoản ngân hàng của Chủ Spa:
1. Vào **Tab 5 (Quyền hạn Admin)** -> Chọn Subtab **"TK Admin gốc"**.
2. Cuộn xuống phần **"Cấu hình Tài khoản Ngân hàng nhận tiền VietQR"**.
3. **Mã ngân hàng**: Chọn ngân hàng của bạn từ danh sách (MBBank, Vietcombank, Techcombank, ACB, VPBank, BIDV, VietinBank...).
4. **Số tài khoản**: Nhập chính xác số tài khoản ngân hàng của bạn (chỉ nhập chữ số, không nhập dấu cách).
5. **Tên chủ tài khoản**: Nhập họ tên in hoa không dấu của bạn (ví dụ: `NGUYEN VAN A`).
6. Bấm **"Lưu Cấu Hình Ngân Hàng"**. Từ thời điểm này, mọi vé dịch vụ tạo ra sẽ có mã VietQR Napas 247 chuyển tiền chuẩn xác về tài khoản này.

---

## PHẦN 4: QUẢN LÝ NHÂN SỰ & PHÂN QUYỀN KỸ THUẬT VIÊN (KTV)

Chủ Spa có toàn quyền quản lý đội ngũ kỹ thuật viên xoa bóp bấm huyệt và cấp tài khoản riêng biệt cho từng người.

### 4.1. Thêm Nhân Viên Mới
1. Vào **Tab 1 (Khách & Thợ)** -> Chọn thẻ **"Nhân viên"**, hoặc vào **Tab 5 (Quyền hạn Admin)**.
2. Bấm nút **"Thêm KTV Mới"**.
3. Nhập các thông tin: Họ và tên thợ, Số điện thoại, Giới tính, Chuyên môn (ví dụ: Xoa bóp bấm huyệt, Giác hơi, Chườm ngải cứu...).
4. Bấm **"Lưu thông tin"**: Nhân viên sẽ xuất hiện trong danh sách xoay vòng nhận tua.

### 4.2. Cấp Tài Khoản Đăng Nhập & Gửi Thông Tin Cho Thợ
1. Tại Tab 5, tìm tên nhân viên trong danh sách tài khoản.
2. Nhập số điện thoại đăng nhập và mã PIN (thường đặt mặc định 6 số dễ nhớ như `123456`).
3. Chạm vào nút **"Gửi TK / MK"**: Hệ thống sẽ tự động tạo một tin nhắn mẫu chuẩn và mở Zalo hoặc SMS để bạn gửi thẳng cho thợ:
   *`"Tài khoản làm việc Khiếm Thị Việt của [Tên thợ]: SĐT đăng nhập: [SĐT], Mật khẩu PIN: 123456. Hãy mở app và đăng nhập để nhận ca làm."`*

### 4.3. Thiết Lập Phân Quyền Chi Tiết (6 Chức Năng Cốt Lõi)
Để bảo vệ số liệu kinh doanh của quán, Chủ tiệm có thể trao quyền hạn chế cho thợ:
- **Công tắc "Cấp quyền ngang bằng Admin"**: Chỉ bật cho người quản lý tiệm hoặc người thân tin cậy (cho phép xem toàn bộ thu chi, doanh thu và cấu hình tiệm).
- Nếu không bật quyền Admin, bạn có thể phân quyền độc lập 6 tính năng:
  1. *Được quyền sửa vé đã chốt*: Cho phép sửa lại thông tin khi lỡ chốt nhầm.
  2. *Được quyền sửa giờ làm vé*: Cho phép điều chỉnh giờ bắt đầu/kết thúc khi khách vào muộn.
  3. *Được quyền quản lý danh bạ khách*: Cho phép thêm mới hoặc sửa thông tin khách quen.
  4. *Được quyền sửa bảng giá & dịch vụ*: Cho phép đổi giá hoặc thêm menu.
  5. *Được quyền ghi chép sổ quỹ*: Cho phép thợ ghi nhận các khoản mua sắm nhỏ trong ca.
  6. *Được quyền xem báo cáo doanh thu*: Cho phép xem tổng kết tiền của cả tiệm.

---

## PHẦN 5: CÀI ĐẶT BẢNG GIÁ DỊCH VỤ & GÓI LIỆU TRÌNH COMBO

### 5.1. Thiết Lập Dịch Vụ Đơn Lẻ
1. Mở **Tab 2 (Dịch vụ & Gói)**.
2. Bấm **"Thêm Dịch Vụ Mới"** (hoặc chạm vào dịch vụ có sẵn để sửa).
3. Nhập:
   - **Tên dịch vụ**: Ví dụ: *Massage Bấm Huyệt Toàn Thân 60 Phút*, *Chườm Ngải Cứu Thuốc Bắc*, *Xông Hơi Thảo Dược*.
   - **Thời lượng**: Số phút làm việc (ví dụ: 60, 90, 120 phút).
   - **Giá tiền thu của khách**: Ví dụ: `150.000 đ`.
   - **Tiền hoa hồng trả cho thợ**: Ví dụ: `60.000 đ`. Hệ thống sẽ tự động trừ hoa hồng này để tính lương cho thợ khi hoàn thành vé.
4. Bấm **"Lưu Dịch Vụ"**.

### 5.2. Thiết Lập Gói Liệu Trình (Combo Nhiều Buổi)
Gói liệu trình giúp cơ sở thu tiền trước một khoản lớn và giữ chân khách hàng:
1. Trong Tab 2, chuyển sang mục **"Gói liệu trình"**.
2. Nhập: Tên gói (ví dụ: *Gói 10 Buổi Xoa Bóp Toàn Thân Tặng 1 Buổi Xông Hơi*).
3. Số buổi: `10`.
4. Giá bán gói: Ví dụ: `1.300.000 đ` (rẻ hơn mua lẻ).
5. **Dịch vụ tặng kèm (🎁)**: Chọn dịch vụ tặng miễn phí để kích cầu khách mua.
6. **Bảo vệ quyền lợi của thợ**: Hệ thống Khiếm Thị Việt tự động ghi nhận trích đủ hoa hồng cho kỹ thuật viên khi làm vé trừ buổi từ gói hoặc làm vé quà tặng miễn phí của khách, giúp thợ luôn vui vẻ, nhiệt tình phục vụ.

---

## PHẦN 6: ĐIỀU PHỐI VẬN HÀNH VÉ & PHÂN CA TUA THỢ CÔNG BẰNG

Hệ thống quản lý quy trình làm việc theo 3 giai đoạn trực quan:

```
[MỞ VÉ PHỤC VỤ] ---> [1. ĐANG LÀM] ---> [2. CHỜ CHỐT] ---> [3. ĐÃ CHỐT HÔM NAY]
```

### 6.1. Quy Trình Mở Vé Mới (Tab 0 - Vận Hành Vé)
1. Bấm nút nổi **"MỞ VÉ PHỤC VỤ MỚI"**.
2. **Chọn Khách hàng**: Chọn khách có sẵn trong danh bạ hoặc bấm "Thêm khách nhanh" (chỉ cần nhập Tên và SĐT).
3. **Chọn Thợ KTV**: Hệ thống hiển thị thợ theo thứ tự tua vòng tròn. Thợ nào chờ lâu nhất sẽ đứng đầu danh sách gợi ý.
4. **Chọn Hình thức Vé**:
   - *Vé dịch vụ lẻ*: Chọn dịch vụ trong menu.
   - *Trừ buổi từ gói có sẵn*: Áp dụng khi khách đã mua combo trước đó.
   - *Khách mua gói mới*: Vừa mở vé làm buổi đầu tiên vừa ghi nhận bán gói combo.
5. Bấm **"Tạo Vé Ngay"**: Vé lập tức chuyển vào danh sách **"Đang làm"**. Điện thoại của thợ sẽ rung 2 nhịp báo hiệu có ca làm mới.

### 6.2. 3 Trạng Thái Quản Lý Vé
1. **Đang làm**: Khách và thợ đang trong phòng trị liệu. Có đồng hồ đếm giờ hiển thị trực quan số phút đã làm.
2. **Chờ chốt**: Hết giờ làm việc, thợ bấm "Hoàn thành vé". Vé chuyển sang mục Chờ chốt để Lễ tân hoặc Chủ tiệm kiểm tra số tiền trước khi thu.
3. **Đã chốt hôm nay**: Sau khi khách trả tiền mặt hoặc chuyển khoản xong, Chủ tiệm bấm "Xác nhận chốt vé". Tiền vào doanh thu tiệm và hoa hồng được cộng ngay vào bảng lương của thợ.

### 6.3. Thu Tiền Vé
- **Thanh toán Tiền mặt**: Nhập số tiền khách đưa, ứng dụng tự động hiển thị số tiền thừa cần thối lại và phát âm thanh đọc to cho người khiếm thị nghe.
- **Thanh toán Chuyển khoản VietQR**: Chạm đúp nút **"Thanh toán VietQR Napas 247"**. Màn hình hiển thị mã QR có sẵn số tiền và nội dung chuyển khoản. Bạn chỉ cần đưa màn hình cho khách quét bằng ứng dụng ngân hàng bất kỳ.

---

## PHẦN 7: QUẢN LÝ SỔ QUỸ THU - CHI NGOÀI LỀ (CÂN ĐỐI 2 CỘT)

Tại **Tab 3 (Sổ Quỹ)**, hệ thống áp dụng nguyên tắc quản lý tài chính 2 cột song song cân đối:
- **Cột Bên Trái: Thu Ngoài Lề**: Dành cho các khoản thu không xuất phát từ vé lẻ (ví dụ: Bán thêm tinh dầu, kem xoa bóp mang về, tiền khách ủng hộ tiệm, thanh lý tài sản cũ...).
- **Cột Bên Phải: Chi Ngoài Lề**: Ghi nhận các chi phí vận hành tiệm (mua cồn xoa bóp, tinh dầu, bột ngải cứu, tiền giặt sấy khăn massage, tiền điện, nước, internet, tiền ăn trưa của thợ...).

### Nguyên Tắc Bất Biến Chống Trùng Lặp Số Liệu:
- Doanh thu từ tiền bán vé và tiền hoa hồng trả cho thợ được hệ thống hạch toán tự động từ Tab Vé.
- **TUYỆT ĐỐI KHÔNG** nhập lại tiền vé hoặc tiền lương hoa hồng thợ vào sổ quỹ thu chi này, tránh làm sai lệch số liệu kế toán cuối tháng.

---

## PHẦN 8: BÁO CÁO DOANH THU, BẢNG Ô LỊCH & BẢNG LƯƠNG THỢ

Tại **Tab 4 (Báo Cáo)**, Chủ Spa có bức tranh tài chính chính xác tuyệt đối:

### 8.1. Bộ Lọc Thời Gian & 2 Thẻ Tài Chính Chuẩn Mực
- Bộ lọc linh hoạt: **Hôm nay**, **Tháng này**, **Năm nay**, hoặc **Từ trước đến nay**.
- **Thẻ 1 - Doanh Thu Thực Nhận (Màu Xanh Thảo Dược)**:
  $$\text{Doanh Thu Thực Nhận} = (\text{Tiền Vé} + \text{Thu Ngoài Lề}) - (\text{Hoa Hồng Trả Thợ} + \text{Chi Ngoài Lề})$$
  Đây chính là số tiền lãi ròng thực tế đút túi của Chủ tiệm sau khi đã thanh toán hết mọi nghĩa vụ.
- **Thẻ 2 - Tổng Chi Phí Tiệm (Màu Đỏ Nâu)**:
  $$\text{Tổng Chi} = \text{Tổng Hoa Hồng KTV} + \text{Tổng Chi Phí Vật Tư Ngoài Lề}$$

### 8.2. Bảng Ô Lịch Doanh Thu Độc Đáo
- Bảng lịch hiển thị từng ngày trong tháng. Chạm vào bất kỳ ngày nào, TalkBack sẽ đọc chi tiết 3 chỉ số:
  1. *Số vé đã hoàn thành trong ngày.*
  2. *Tổng số tiền thu vào.*
  3. *Tổng số tiền chi ra.*

### 8.3. Bảng Lương Thợ & Tính Năng "Gửi Phiếu Lương Zalo"
- Dưới bảng báo cáo là danh sách lương của từng kỹ thuật viên trong kỳ:
  - Hiển thị rõ: Tổng số vé đã làm, tổng số phút làm việc, tổng tiền hoa hồng kiếm được, và tiền thưởng tip thêm của khách.
- **Nút "Gửi Phiếu Lương Zalo"**: Khi chạm vào tên một kỹ thuật viên và bấm nút này, ứng dụng tự động tổng hợp toàn bộ danh sách từng ca làm việc (ngày giờ, tên khách, dịch vụ, tiền hoa hồng) và mở Zalo gửi trực tiếp cho thợ đó đối soát minh bạch, không tốn công chép sổ tay.

---

## PHẦN 9: KHAI THÁC SỨC MẠNH TRỢ LÝ TRÍ TUỆ NHÂN TẠO GEHIHI

Trợ lý **Gehihi** (ứng dụng mô hình ngôn ngữ lớn thế hệ mới Gemini 3.8 Flash) là người trợ lý ảo đắc lực, luôn lắng nghe và hiểu tiếng Việt tự nhiên của người khiếm thị.

### 9.1. Cách Mở và Tương Tác
- Chạm vào biểu tượng **Trợ lý AI** ở góc trên thanh tiêu đề của ứng dụng.
- Bấm nút micro và nói tự nhiên (hoặc gõ văn bản):
  - *"Hôm nay tiệm làm được bao nhiêu vé rồi?"*
  - *"Thợ nào đang có thu nhập cao nhất tháng này?"*
  - *"Ghi sổ quỹ chi 200 nghìn mua khăn lau phòng."*
  - *"Dịch vụ nào của tiệm đang được khách làm nhiều nhất?"*

### 9.2. Cơ Chế Thẻ Xác Nhận An Toàn 2 Bước (Safety Cards)
Để tránh trường hợp người dùng nói nhầm hoặc AI nhận diện sai gây ảnh hưởng số liệu tài chính:
- Mọi câu lệnh liên quan đến **tạo vé mới**, **ghi chi sổ quỹ**, **đổi bảng giá**, hoặc **xóa dữ liệu** sẽ **KHÔNG BAO GIỜ** được thực thi ngay lập tức.
- Gehihi sẽ hiển thị một **Thẻ Xác Nhận Màu Cam/Đỏ** nêu rõ: *"Bạn có chắc chắn muốn chi 200.000 đ cho nội dung Mua khăn không?"*.
- Chỉ khi Chủ tiệm bấm nút **"XÁC NHẬN DUYỆT"**, lệnh đó mới được ghi vào hệ thống.

---

## PHẦN 10: QUY TRÌNH SAO LƯU ĐỊNH KỲ DOCUMENTS & CHIA SẺ ZALO AN TOÀN

Bảo đảm dữ liệu của quán trường tồn qua nhiều năm hoạt động:

### 10.1. Cơ Chế Tự Động Định Kỳ 1 Tuần
- Mỗi 7 ngày, hệ thống sẽ tự động xuất một bản sao lưu toàn bộ dữ liệu ra thư mục `Documents` trên máy điện thoại và hiển thị hộp thoại gợi ý gửi sang Zalo.
- Khi hộp thoại xuất hiện, Chủ tiệm chỉ cần chọn gửi vào mục **"Cloud của tôi"** trên Zalo để lưu trữ vĩnh viễn trên đám mây an toàn.

### 10.2. Sao Lưu Thủ Công Bất Cứ Lúc Nào
- Vào **Tab 5 (Quyền hạn)** -> Chọn **"Sao lưu & Khôi phục"**.
- Chạm đúp nút **"XUẤT TOÀN BỘ DỮ LIỆU RA FILE BACKUP (.JSON)"** hoặc nút **"Chia sẻ Zalo / Lưu"**.
- Bấm nghe nút **"📢 HƯỚNG DẪN TALKBACK: SAO LƯU HÀNG TUẦN & ZALO"** để được trình đọc màn hình hướng dẫn chi tiết từng bước.

---

**KHIẾM THỊ VIỆT — CÔNG NGHỆ CHUYÊN BIỆT, NÂNG TẦM YÊU THƯƠNG**  
Mọi thắc mắc kỹ thuật, hỗ trợ cài đặt hoặc gia hạn gói dịch vụ, vui lòng liên hệ:  
- **Zalo Hỗ trợ**: `https://zalo.me/0397772963` (Hotline: `0397772963`)
