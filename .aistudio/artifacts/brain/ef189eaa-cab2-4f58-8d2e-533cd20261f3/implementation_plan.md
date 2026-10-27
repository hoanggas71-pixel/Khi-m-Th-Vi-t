# Nâng Cấp gehihi Thành Trí Tuệ Gemini Thực Thụ & Loại Bỏ Hoàn Toàn Bẫy Từ Khóa Thủ Công

Kế hoạch chuyển đổi toàn diện kiến trúc trợ lý ảo **gehihi** từ việc quét từ khóa cục bộ thô sơ (regex/substring) sang xử lý ngôn ngữ tự nhiên bằng trí tuệ thực thụ của **Gemini 3.8 Flash**, giải quyết triệt để lỗi hiểu nhầm câu lệnh và mang lại khả năng tư vấn, hỗ trợ quản lý spa toàn diện.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Đã xác nhận từ người dùng**: gehihi phải hoạt động như một Gemini thực thụ, tự do hiểu và giải quyết mọi vấn đề tự nhiên mà không bị trói buộc hay bóp méo bởi các từ khóa vụn vặt và file json cứng nhắc.

- **Quyết định cốt lõi 1 - Xóa bỏ tầng chặn bắt từ khóa thủ công trước LLM**: Trước đây, hàm `thucThiLenhTrucTiepTrenApp` chạy trước và dùng `norm.contains(...)` bắt chữ "cập nhật" trong "mail chưa cập nhật", chữ "nam" trong "giới tính nam" để tự chế ra lệnh sửa nhân viên Lê Hoàng Nam mà Gemini chưa từng được đọc câu hỏi. Chúng ta sẽ dẹp bỏ tầng chặn bắt nguy hiểm này, ưu tiên gửi trực tiếp ngữ cảnh và câu lệnh người dùng cho Gemini 3.8 Flash xử lý.
- **Quyết định cốt lõi 2 - Gemini làm não bộ trung tâm (Context-Aware Intent & Multi-Turn Intelligence)**: Đưa toàn bộ bức tranh dữ liệu (danh sách nhân viên hiện tại, dịch vụ, lượt vé, doanh thu, vai trò admin/nhân viên) vào System Prompt thông minh của Gemini. Gemini sẽ tự phân biệt rõ:
  - Khi nào người dùng đang **ra lệnh thêm/sửa/xóa** (và trích xuất chính xác họ tên mới, SĐT, giới tính, email mà không bị nhầm lẫn).
  - Khi nào người dùng đang **hỏi đáp, tra cứu, tính toán hoặc xin tư vấn kinh doanh**.
- **Quyết định cốt lõi 3 - Cơ chế xác nhận an toàn tuyệt đối (Safety Guardrail)**: Đối với các thao tác thay đổi dữ liệu (Thêm KTV mới, Sửa thông tin tài khoản, Xóa vé...), gehihi sẽ tạo thẻ xác nhận trực quan rõ ràng, minh bạch thông tin trước khi thực thi, không bao giờ tự ý sửa đè dữ liệu của nhân viên khác.

---

## 1. Tổng Quan & Trải Nghiệm Mục Tiêu

- **Khả năng mới của gehihi**:
  - Hiểu trọn vẹn câu tiếng Việt khẩu ngữ, viết tắt, không dấu hoặc có dấu (ví dụ: *"thêm nhân viên mai văn hoài, giới tính nam, số điện thoại 0968608795, mail chưa cập nhật"*).
  - Trích xuất chuẩn xác: Họ tên: **Mai Văn Hoài**, Giới tính: **Nam**, SĐT: **0968608795**, Email: **Chưa có / Trống**.
  - Không bao giờ bị nhầm từ *"nam"* (giới tính) thành nhân viên *"Lê Hoàng Nam"*, không bị nhầm cụm *"chưa cập nhật"* thành lệnh *"đang yêu cầu cập nhật"*.
  - Hỗ trợ trò chuyện đa năng: hỏi doanh thu, tư vấn giữ chân khách, tính toán phần trăm hoa hồng, giải thích quy chế spa như một cố vấn chuyên nghiệp.

---

## 2. Luồng Người Dùng & Giao Diện Tương Tác

```
┌─────────────────────────────────────────────────────────┐
│ User nhập câu lệnh tự nhiên trong khung chat gehihi     │
│ "thêm nhân viên mai văn hoài ,giới tính nam, sđt ... " │
└────────────────────────────┬────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────┐
│ Bơm ngữ cảnh Spa + Gửi trực tiếp tới Gemini 3.8 Flash   │
│ (Trí tuệ nhân tạo đọc, hiểu ngữ nghĩa, phân loại ý định)│
└────────────────────────────┬────────────────────────────┘
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
   [Ý ĐỊNH THAY ĐỔI DỮ LIỆU]           [CÂU HỎI / TƯ VẤN]
            │                                 │
            ▼                                 ▼
┌──────────────────────────────┐    ┌──────────────────────────────┐
│ Hiển thị Hộp thoại / Thẻ     │    │ Phản hồi bằng ngôn ngữ tự    │
│ xác nhận an toàn với thông   │    │ nhiên, mượt mà, phân tích    │
│ tin trích xuất chính xác 100%│    │ sâu sắc không cụt lủn        │
│ • Tên: Mai Văn Hoài          │    └──────────────────────────────┘
│ • SĐT: 0968608795            │
│ • Giới tính: Nam             │
│ [Xác nhận & Thêm]  [Hủy]     │
└──────────────────────────────┘
```

- **Thẻ xác nhận hành động trực quan**:
  - Thể hiện rõ các trường thông tin mà Gemini đã trích xuất được.
  - Người dùng bấm **"Xác nhận & Thêm ngay"** thì hệ thống tạo nhân viên và cấp tài khoản tương ứng, sau đó mở thẳng sang mục Nhân viên để người dùng xem kết quả.
  - Người dùng bấm **"Hủy"** thì lệnh bị hủy bỏ an toàn, không có bất kỳ dữ liệu nào bị thay đổi ngoài ý muốn.

---

## 3. Quyết Định Kỹ Thuật & Đánh Đổi

| Vấn đề | Giải pháp trước đây (Gây lỗi) | Giải pháp mới với Gemini thực thụ |
| :--- | :--- | :--- |
| **Nhận diện ý định** | Dùng chuỗi `if (norm.contains("cap nhat"))` thô thiển | Gemini 3.8 Flash đọc toàn câu và tự hiểu ngữ nghĩa sâu |
| **Xử lý từ ngữ phủ định** | Thấy chữ "cập nhật" trong "chưa cập nhật" là tưởng lệnh sửa | Gemini hiểu nghĩa phủ định: "chưa cập nhật" nghĩa là email để trống |
| **Xung đột danh từ riêng** | Thấy chữ "nam" trong "giới tính nam" là gán cho Lê Hoàng Nam | Gemini phân tách rõ ràng: "nam" là Gender, "Mai Văn Hoài" là Full Name |
| **Khả năng tư vấn** | Trả lời cụt lủn theo các kịch bản cứng | Trả lời tự nhiên, sáng tạo, giải quyết mọi thắc mắc chuyên sâu |

---

## 4. Kiến Trúc Kỹ Thuật Chi Tiết

### A. Sơ Đồ Kiến Trúc Xử Lý Lệnh

```
┌──────────────────────────────────────────────────────────────┐
│                       SpaStore.kt                            │
│                                                              │
│  guiYeuCauAi(prompt)                                         │
│       │                                                      │
│       ├─► 1. Chuẩn bị Context dữ liệu Spa (NV, Vé, Dịch vụ)  │
│       ├─► 2. buildGehihiPrompt (Chỉ dẫn Gemini làm não bộ)    │
│       ├─► 3. goiGeminiVoiDuPhong (Gemini 3.8 Flash)          │
│       │                                                      │
│       └─► 4. phanTichPhanHoiGemini (Đọc kết quả từ Gemini)   │
│                 │                                            │
│                 ├─► Nếu là LỆNH: Tạo Action an toàn (Thêm/   │
│                 │   Sửa/Xóa đúng người, đúng dữ liệu)        │
│                 │                                            │
│                 └─► Nếu là TƯ VẤN: Hiển thị câu trả lời      │
│                     thông minh, tự nhiên cho người dùng      │
└──────────────────────────────────────────────────────────────┘
```

### B. Các Bước Thực Hiện
1. **Lược bỏ cơ chế chặn trước (`thucThiLenhTrucTiepTrenApp`)**:
   - Không cho bộ quét từ khóa thủ công tự tiện can thiệp và bóp méo câu lệnh của người dùng trước khi gửi cho Gemini.
   - Giữ lại bộ xử lý dự phòng offline nhẹ nhàng chỉ khi mất kết nối mạng hoàn toàn.
2. **Nâng cấp `buildGehihiPrompt` & `phanTichPhanHoiGemini`**:
   - Cung cấp hướng dẫn hành động rõ ràng cho Gemini:
     - Khi người dùng muốn thêm KTV: Trích xuất `[ACTION:THEM_NV|ten=...|sdt=...|gioitinh=...|email=...]`.
     - Khi người dùng muốn sửa KTV: Trích xuất `[ACTION:SUA_NV|id=...|ten=...|sdt=...]`.
     - Khi người dùng muốn mở vé/tính tiền: Trích xuất action tương ứng.
     - Khi người dùng hỏi đáp/tư vấn: Trả lời tự nhiên, giàu thông tin và thân thiện.
   - Nhận diện linh hoạt, kể cả khi người dùng viết tự nhiên, không cần theo cú pháp cố định nào.
3. **Kiểm tra và biên dịch**:
   - Đảm bảo toàn bộ quy trình biên dịch không lỗi, chạy thử nghiệm kịch bản *"thêm nhân viên mai văn hoài ,giới tính nam,số điện thoại 0968608795, mail chưa cập nhật"*.
