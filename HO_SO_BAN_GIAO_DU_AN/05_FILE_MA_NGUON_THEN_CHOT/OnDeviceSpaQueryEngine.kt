package com.example

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

data class DateRange(
    val startDate: String, // yyyy-MM-dd
    val endDate: String,   // yyyy-MM-dd
    val label: String,
    val isSingleDay: Boolean = false
)

object OnDeviceSpaQueryEngine {

    /**
     * Bóc tách khoảng thời gian tiếng Việt (có dấu, không dấu, viết tắt, tương đối & cố định)
     */
    fun parseVietnameseDateRange(prompt: String, refDate: String = todayDateStr()): DateRange? {
        val norm = normalizeVietnameseText(prompt).trim()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val refCal = Calendar.getInstance().apply {
            time = runCatching { sdf.parse(refDate) }.getOrNull() ?: Date()
        }
        val currentYear = refCal.get(Calendar.YEAR)
        val currentMonth = refCal.get(Calendar.MONTH) + 1 // 1..12

        // 1. Khoảng ngày cụ thể: "từ dd/MM đến dd/MM", "tu dd/MM den dd/MM", "dd/MM - dd/MM"
        val tuDenRegex = Regex("(?:tu|từ|from)?\\s*(?:ngay|ngày)?\\s*(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{4}))?\\s*(?:den|đến|to|-)\\s*(?:ngay|ngày)?\\s*(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{4}))?")
        val tdMatch = tuDenRegex.find(norm)
        if (tdMatch != null) {
            val d1 = tdMatch.groupValues[1].toInt()
            val m1 = tdMatch.groupValues[2].toInt().coerceIn(1, 12)
            val y1 = tdMatch.groupValues[3].toIntOrNull() ?: currentYear
            val d2 = tdMatch.groupValues[4].toInt()
            val m2 = tdMatch.groupValues[5].toInt().coerceIn(1, 12)
            val y2 = tdMatch.groupValues[6].toIntOrNull() ?: y1
            val start = "%04d-%02d-%02d".format(y1, m1, d1)
            val end = "%04d-%02d-%02d".format(y2, m2, d2)
            return DateRange(start, end, "Từ %02d/%02d/%04d đến %02d/%02d/%04d".format(d1, m1, y1, d2, m2, y2))
        }

        // 2. "N ngày đầu tháng M" hoặc "đầu tháng M" (VD: "10 ngày đầu tháng 10", "5 ngày đầu tháng 9", "dau thang 10", "10 ngay dau t10")
        val dauThangRegex = Regex("(?:(\\d+)\\s*(?:ngay|ngày)?\\s*)?(?:dau|đầu)\\s*(?:thang|tháng|t)\\s*(\\d{1,2})(?:\\s*(?:nam|năm|/)\\s*(\\d{4}))?")
        val dauMatch = dauThangRegex.find(norm)
        if (dauMatch != null) {
            val days = dauMatch.groupValues[1].toIntOrNull() ?: 10
            val m = dauMatch.groupValues[2].toInt().coerceIn(1, 12)
            val y = dauMatch.groupValues[3].toIntOrNull() ?: currentYear
            val maxD = getDaysInMonth("%04d-%02d-01".format(y, m))
            val actualDays = days.coerceIn(1, maxD)
            val start = "%04d-%02d-01".format(y, m)
            val end = "%04d-%02d-%02d".format(y, m, actualDays)
            return DateRange(start, end, "$actualDays ngày đầu tháng $m/$y")
        }

        // 2B. "N ngày đầu tháng này" hoặc "đầu tháng này"
        if (norm.contains("dau thang nay") || norm.contains("đầu tháng này")) {
            val daysMatch = Regex("(\\d+)\\s*(?:ngay|ngày)?\\s*(?:dau|đầu)\\s*thang\\s*nay").find(norm)
            val days = daysMatch?.groupValues?.get(1)?.toIntOrNull() ?: 10
            val maxD = getDaysInMonth("%04d-%02d-01".format(currentYear, currentMonth))
            val actualDays = days.coerceIn(1, maxD)
            val start = "%04d-%02d-01".format(currentYear, currentMonth)
            val end = "%04d-%02d-%02d".format(currentYear, currentMonth, actualDays)
            return DateRange(start, end, "$actualDays ngày đầu tháng $currentMonth/$currentYear")
        }

        // 3. "Giữa tháng M" hoặc "giữa tháng này" (ngày 11..20)
        if (norm.contains("giua thang") || norm.contains("giữa tháng")) {
            val mMatch = Regex("(?:giua|giữa)\\s*(?:thang|tháng|t)\\s*(\\d{1,2})(?:\\s*(?:nam|năm|/)\\s*(\\d{4}))?").find(norm)
            val m = mMatch?.groupValues?.get(1)?.toIntOrNull() ?: currentMonth
            val y = mMatch?.groupValues?.get(2)?.toIntOrNull() ?: currentYear
            val start = "%04d-%02d-11".format(y, m)
            val end = "%04d-%02d-20".format(y, m)
            return DateRange(start, end, "Giữa tháng $m/$y")
        }

        // 4. "Cuối tháng M" hoặc "cuối tháng này" (ngày 21..hết tháng)
        if (norm.contains("cuoi thang") || norm.contains("cuối tháng")) {
            val mMatch = Regex("(?:cuoi|cuối)\\s*(?:thang|tháng|t)\\s*(\\d{1,2})(?:\\s*(?:nam|năm|/)\\s*(\\d{4}))?").find(norm)
            val m = mMatch?.groupValues?.get(1)?.toIntOrNull() ?: currentMonth
            val y = mMatch?.groupValues?.get(2)?.toIntOrNull() ?: currentYear
            val maxD = getDaysInMonth("%04d-%02d-01".format(y, m))
            val start = "%04d-%02d-21".format(y, m)
            val end = "%04d-%02d-%02d".format(y, m, maxD)
            return DateRange(start, end, "Cuối tháng $m/$y")
        }

        // 5. Hôm qua / hôm trước
        if (norm.contains("hom qua") || norm.contains("hôm qua") || norm.contains("ngay qua") || norm.contains("hom trc") || norm.contains("hôm trước")) {
            val yCal = (refCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
            val yStr = sdf.format(yCal.time)
            return DateRange(yStr, yStr, "Hôm qua ($yStr)", isSingleDay = true)
        }

        // 6. Hôm kia
        if (norm.contains("hom kia") || norm.contains("hôm kia")) {
            val yCal = (refCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -2) }
            val yStr = sdf.format(yCal.time)
            return DateRange(yStr, yStr, "Hôm kia ($yStr)", isSingleDay = true)
        }

        // 7. Hôm nay / ca này / ngày này
        if (norm.contains("hom nay") || norm.contains("hôm nay") || norm.contains("ca nay") || norm.contains("ca này") || norm.contains("ngay nay") || norm.contains("ngày này") || norm.contains("hien tai") || norm.contains("hiện tại")) {
            return DateRange(refDate, refDate, "Hôm nay ($refDate)", isSingleDay = true)
        }

        // 8. Ngày cụ thể: "ngày 15/10", "ngay 15/10/2026"
        val ngayRegex = Regex("(?:ngay|ngày|vao ngay|vào ngày)\\s*(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{4}))?")
        val ngayMatch = ngayRegex.find(norm)
        if (ngayMatch != null) {
            val d = ngayMatch.groupValues[1].toInt()
            val m = ngayMatch.groupValues[2].toInt().coerceIn(1, 12)
            val y = ngayMatch.groupValues[3].toIntOrNull() ?: currentYear
            val dStr = "%04d-%02d-%02d".format(y, m, d)
            return DateRange(dStr, dStr, "Ngày %02d/%02d/%04d".format(d, m, y), isSingleDay = true)
        }

        // 9. Tuần trước
        if (norm.contains("tuan truoc") || norm.contains("tuần trước") || norm.contains("tuan roi") || norm.contains("tuần rồi")) {
            val dow = refCal.get(Calendar.DAY_OF_WEEK)
            val daysSinceMonday = (dow + 5) % 7
            val monCal = (refCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, -daysSinceMonday - 7)
            }
            val sunCal = (monCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, 6)
            }
            val startStr = sdf.format(monCal.time)
            val endStr = sdf.format(sunCal.time)
            return DateRange(startStr, endStr, "Tuần trước ($startStr đến $endStr)")
        }

        // 10. Tuần này
        if (norm.contains("tuan nay") || norm.contains("tuần này")) {
            val dow = refCal.get(Calendar.DAY_OF_WEEK)
            val daysSinceMonday = (dow + 5) % 7
            val monCal = (refCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, -daysSinceMonday)
            }
            val sunCal = (monCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, 6)
            }
            val startStr = sdf.format(monCal.time)
            val endStr = sdf.format(sunCal.time)
            return DateRange(startStr, endStr, "Tuần này ($startStr đến $endStr)")
        }

        // 11. Tháng trước
        if (norm.contains("thang truoc") || norm.contains("tháng trước") || norm.contains("thang roi") || norm.contains("tháng rồi")) {
            val prevCal = (refCal.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, -1)
            }
            val py = prevCal.get(Calendar.YEAR)
            val pm = prevCal.get(Calendar.MONTH) + 1
            val maxD = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val start = "%04d-%02d-01".format(py, pm)
            val end = "%04d-%02d-%02d".format(py, pm, maxD)
            return DateRange(start, end, "Tháng trước ($pm/$py)")
        }

        // 12. Tháng này
        if (norm.contains("thang nay") || norm.contains("tháng này")) {
            val maxD = getDaysInMonth("%04d-%02d-01".format(currentYear, currentMonth))
            val start = "%04d-%02d-01".format(currentYear, currentMonth)
            val end = "%04d-%02d-%02d".format(currentYear, currentMonth, maxD)
            return DateRange(start, end, "Tháng này ($currentMonth/$currentYear)")
        }

        // 13. Tháng M cụ thể: "tháng 9", "tháng 10", "thang 9", "thang 10", "t9", "t10", "tháng 10 năm 2026"
        val thangRegex = Regex("(?:\\bthang|\\btháng|\\bt)\\s*(\\d{1,2})(?:\\s*(?:nam|năm|/)\\s*(\\d{4}))?\\b")
        val thangMatch = thangRegex.find(norm)
        if (thangMatch != null) {
            val m = thangMatch.groupValues[1].toInt().coerceIn(1, 12)
            val y = thangMatch.groupValues[2].toIntOrNull() ?: currentYear
            val maxD = getDaysInMonth("%04d-%02d-01".format(y, m))
            val start = "%04d-%02d-01".format(y, m)
            val end = "%04d-%02d-%02d".format(y, m, maxD)
            return DateRange(start, end, "Tháng $m/$y")
        }

        // 14. Năm nay / Năm ngoái / Năm YYYY
        if (norm.contains("nam nay") || norm.contains("năm nay")) {
            return DateRange("%04d-01-01".format(currentYear), "%04d-12-31".format(currentYear), "Năm $currentYear")
        }
        if (norm.contains("nam ngoai") || norm.contains("năm ngoái") || norm.contains("nam truoc") || norm.contains("năm trước")) {
            val py = currentYear - 1
            return DateRange("%04d-01-01".format(py), "%04d-12-31".format(py), "Năm $py")
        }
        val namRegex = Regex("\\b(?:nam|năm)\\s*(\\d{4})\\b")
        val namMatch = namRegex.find(norm)
        if (namMatch != null) {
            val y = namMatch.groupValues[1].toInt()
            return DateRange("%04d-01-01".format(y), "%04d-12-31".format(y), "Năm $y")
        }

        return null
    }

    /**
     * Điểm vào xử lý truy vấn ngoại tuyến toàn diện trên thiết bị.
     * Trả về KetQuaAiBot nếu thuộc nghiệp vụ Spa, hoặc null nếu là câu hỏi kiến thức mở ngoài hệ thống.
     */
    fun xuLyTruyVanNgoaiTuyen(
        prompt: String,
        store: SpaStore,
        refDate: String = todayDateStr()
    ): KetQuaAiBot? {
        val norm = normalizeVietnameseText(prompt).trim()
        val user = store.currentUser

        // 1. Module 5: So sánh đối chiếu đa kỳ ("so sánh doanh thu tuần này với tuần trước"...)
        if (norm.contains("so sanh") || norm.contains("doi chieu") || norm.contains("so voi")) {
            val cmpRes = xuLyTruyVanSoSanhDaKy(prompt, norm, store, refDate, user)
            if (cmpRes != null) return cmpRes
        }

        // 2. Module 3: KTV & Phân bổ công việc (ai đang phục vụ khách, ai đang rảnh, ai làm nhiều vé nhất)
        val ktvRes = xuLyTruyVanKtvVaPhanBo(prompt, norm, store, refDate, user)
        if (ktvRes != null) return ktvRes

        // 3. Module 2: Khách hàng & Gói liệu trình (còn mấy buổi, khách VIP, tần suất ghé quán)
        val khRes = xuLyTruyVanKhachHangVaGoi(prompt, norm, store, refDate, user)
        if (khRes != null) return khRes

        // 4. Module 4: Dịch vụ & Xếp hạng bán chạy (bán chạy nhất, bảng giá, tra cứu giá/hoa hồng)
        val dvRes = xuLyTruyVanDichVuVaMenu(prompt, norm, store, refDate, user)
        if (dvRes != null) return dvRes

        // 5. Module 1: Doanh thu, Sổ quỹ, Chi phí & Lợi nhuận (Chủ quán thực nhận)
        val financeRes = xuLyTruyVanDoanhThuVaSoQuy(prompt, norm, store, refDate, user)
        if (financeRes != null) return financeRes

        return null
    }

    // =========================================================================
    // MODULE 1: DOANH THU, SỔ QUỸ, CHI PHÍ & LỢI NHUẬN (CHỦ QUÁN THỰC NHẬN)
    // =========================================================================
    private fun xuLyTruyVanDoanhThuVaSoQuy(
        prompt: String,
        norm: String,
        store: SpaStore,
        refDate: String,
        user: TaiKhoan?
    ): KetQuaAiBot? {
        val parsedRange = parseVietnameseDateRange(prompt, refDate)

        val isExplicitFinanceAsk = norm.contains("doanh thu") || norm.contains("thu chi") ||
            norm.contains("loi nhuan") || norm.contains("lai rong") || norm.contains("thuc nhan") ||
            norm.contains("chu quan") || norm.contains("so quy") || norm.contains("dong tien") ||
            norm.contains("bao cao") || norm.contains("tong thu") || norm.contains("tong chi") ||
            norm.contains("lai bao nhieu") || norm.contains("thu bao nhieu") || norm.contains("chi bao nhieu") ||
            norm.contains("tinh hinh") || norm.contains("tien ve")

        // Nếu câu hỏi có mốc thời gian rõ ràng (VD: "10 ngày đầu tháng 10", "tháng 9", "tuần trước") hoặc có từ khóa tài chính
        if (parsedRange == null && !isExplicitFinanceAsk) return null

        val range = parsedRange ?: DateRange(refDate, refDate, "Hôm nay ($refDate)", isSingleDay = true)

        // BẢO MẬT: Kỹ thuật viên không được xem doanh thu tổng hay sổ quỹ của cơ sở
        if (user?.isAdmin() != true) {
            return KetQuaAiBot(
                cauTraLoi = "🔒 Doanh thu tổng, chi phí và lợi nhuận của cơ sở được bảo mật bởi Chủ Spa (Admin).\n" +
                    "Bạn có thể tra cứu hoa hồng và số vé cá nhân bằng cách hỏi: \"Hôm nay tôi làm mấy vé\" hoặc \"Hoa hồng của tôi\".",
                hanhDongThucThi = "Từ chối quyền xem doanh thu cơ sở"
            )
        }

        // Lọc vé đã hoàn tất / chốt trong kỳ
        val veTrongKy = store.luots.filter {
            (it.daHoanTat || it.daXacNhan) && it.ngay >= range.startDate && it.ngay <= range.endDate
        }
        val tcTrongKy = store.tcs.filter {
            it.ngay >= range.startDate && it.ngay <= range.endDate
        }

        // NGUYÊN TẮC TRUNG THỰC TUYỆT ĐỐI (ZERO-DATA HONESTY):
        // Khi 0 vé và 0 giao dịch trong kỳ, thông báo rõ ràng 0 vé / 0đ thay vì hiển thị số liệu hôm nay!
        if (veTrongKy.isEmpty() && tcTrongKy.isEmpty()) {
            return KetQuaAiBot(
                cauTraLoi = "📊 Báo cáo ${range.label}:\n" +
                    "• Cơ sở ghi nhận: 0 vé phục vụ và 0 giao dịch phát sinh trong khoảng thời gian này.\n" +
                    "• Tổng Thu: 0đ\n" +
                    "• Tổng Chi phí & Hoa hồng: 0đ\n" +
                    "• Chủ quán thực nhận (Lãi ròng): 0đ.\n" +
                    "(Số liệu được đối soát trực tiếp 100% từ cơ sở dữ liệu trên máy).",
                hanhDongThucThi = "Báo cáo ${range.label}",
                chuyenTab = 4
            )
        }

        val thuVe = veTrongKy.sumOf { it.thu }
        val thuNgoai = tcTrongKy.filter { it.loai == LoaiTC.THU && !store.laPhieuThuTuVe(it) }.sumOf { it.tien }
        val tongThu = thuVe + thuNgoai

        val hoaHongVe = veTrongKy.sumOf { store.tinhHoaHongVe(it) }
        val chiNgoai = tcTrongKy.filter { it.loai == LoaiTC.CHI && !store.laPhieuChiHoaHongKtv(it) }.sumOf { it.tien }
        val chiKtvTrongTc = tcTrongKy.filter { store.laPhieuChiHoaHongKtv(it) }.sumOf { it.tien }
        val tongChi = max(hoaHongVe, chiKtvTrongTc) + chiNgoai
        val laiRong = tongThu - tongChi

        val soVe = veTrongKy.size
        val veDungGoi = veTrongKy.count { it.dungGoi }
        val veLe = soVe - veDungGoi

        val chiTietToday = if (range.isSingleDay && range.startDate == refDate) {
            val dangLam = store.luots.count { it.dangLam && it.ngay == refDate }
            val choChot = store.luots.count { it.choChot && it.ngay == refDate }
            "• Trạng thái hiện tại: $dangLam vé đang làm • $choChot vé chờ chốt • $soVe vé đã chốt.\n"
        } else {
            "• Tổng vé hoàn thành: $soVe vé ($veLe vé lẻ, $veDungGoi vé dùng gói liệu trình).\n"
        }

        val chiTietChi = if (chiNgoai > 0L) {
            "• Chi phí & Hoa hồng: -${formatVndSpeakable(tongChi)} (trong đó hoa hồng KTV: ${formatVnd(hoaHongVe)}, chi vận hành khác: ${formatVnd(chiNgoai)})\n"
        } else {
            "• Chi trả hoa hồng KTV: -${formatVndSpeakable(tongChi)}\n"
        }

        val sign = if (laiRong >= 0) "+" else ""
        return KetQuaAiBot(
            cauTraLoi = "📊 Báo cáo ${range.label}:\n" +
                chiTietToday +
                "• Tổng Thu: +${formatVndSpeakable(tongThu)} (Thu vé lẻ: ${formatVnd(thuVe)}${if (thuNgoai > 0L) ", Thu ngoài/Gói: ${formatVnd(thuNgoai)}" else ""})\n" +
                chiTietChi +
                "• Chủ quán thực nhận (Lãi ròng): $sign${formatVndSpeakable(laiRong)}.\n" +
                "(Dữ liệu trích xuất trực tiếp từ vé phục vụ và sổ quỹ cơ sở).",
            hanhDongThucThi = "Báo cáo ${range.label}",
            chuyenTab = 4
        )
    }

    // =========================================================================
    // MODULE 2: KHÁCH HÀNG & GÓI LIỆU TRÌNH
    // =========================================================================
    private fun xuLyTruyVanKhachHangVaGoi(
        prompt: String,
        norm: String,
        store: SpaStore,
        refDate: String,
        user: TaiKhoan?
    ): KetQuaAiBot? {
        // 1. Xếp hạng khách VIP ("ai là khách vip nhất", "khach vip", "top khach hang")
        if (norm.contains("khach vip") || norm.contains("ai la khach vip") || norm.contains("top khach") || norm.contains("khach quen")) {
            return layXepHangKhachVip(store)
        }

        val isKhAsk = norm.contains("khach") || norm.contains("khách") || norm.contains("goi") ||
            norm.contains("gói") || norm.contains("buoi") || norm.contains("buổi") || norm.contains("lieu trinh")

        if (!isKhAsk) return null

        val matchedKh = timKhachHangPhuHop(prompt, store.khs) ?: return null

        // Truy vấn số buổi còn lại hoặc thông tin gói của khách
        val dsDk = store.dks.filter { it.khId == matchedKh.id }
        val goiInfo = if (dsDk.isEmpty()) {
            "chưa đăng ký gói liệu trình nào (hoặc đã dùng hết toàn bộ)"
        } else {
            dsDk.joinToString("\n• ") { dk ->
                val goi = store.gois.firstOrNull { it.id == dk.goiId }
                val tong = goi?.soBuoi ?: 10
                val conLai = max(0, tong - dk.luotDaDung)
                "${goi?.ten ?: "Gói Liệu trình"}: đã dùng ${dk.luotDaDung}/$tong buổi (còn $conLai buổi)"
            }
        }

        // Đếm tần suất ghé quán
        val veCuaKh = store.luots.filter { it.khId == matchedKh.id && (it.daHoanTat || it.daXacNhan) }
        val parsedRange = parseVietnameseDateRange(prompt, refDate)
        val veTrongKy = if (parsedRange != null) {
            veCuaKh.filter { it.ngay >= parsedRange.startDate && it.ngay <= parsedRange.endDate }
        } else {
            // Mặc định lấy theo tháng hiện tại nếu hỏi "tháng này" hoặc tổng thể
            val currentMonthPrefix = refDate.take(7)
            veCuaKh.filter { it.ngay.startsWith(currentMonthPrefix) }
        }

        val rangeLabel = parsedRange?.label ?: "tháng này (${refDate.take(7)})"
        val tanSuatText = "• Tần suất ghé quán: ${veTrongKy.size} lượt trong $rangeLabel (tổng cộng ${veCuaKh.size} lượt từ trước đến nay)."

        val tienVe = veCuaKh.sumOf { it.thu }
        val tienGoi = dsDk.sumOf { dk -> store.gois.firstOrNull { it.id == dk.goiId }?.gia ?: 0L }
        val tongChiTieu = tienVe + tienGoi
        val chiTieuText = if (user?.isAdmin() == true) {
            "\n• Tổng chi tiêu tích lũy: ${formatVnd(tongChiTieu)} (Mua vé lẻ: ${formatVnd(tienVe)}, Mua gói: ${formatVnd(tienGoi)})."
        } else ""

        val cauTraLoi = "👤 Thông tin Khách hàng ${matchedKh.ten} (SĐT: ${matchedKh.sdt}):\n" +
            "• Tình trạng gói liệu trình:\n  $goiInfo\n" +
            tanSuatText +
            chiTieuText

        return KetQuaAiBot(
            cauTraLoi = cauTraLoi,
            hanhDongThucThi = "Xem hồ sơ khách ${matchedKh.ten}",
            chuyenTab = 1,
            chuyenMucNguoi = 0
        )
    }

    private fun layXepHangKhachVip(store: SpaStore): KetQuaAiBot {
        if (store.khs.isEmpty()) {
            return KetQuaAiBot(
                cauTraLoi = "Hiện cơ sở chưa có dữ liệu khách hàng nào được ghi nhận.",
                chuyenTab = 1,
                chuyenMucNguoi = 0
            )
        }

        val vipList = store.khs.map { kh ->
            val veCuaKh = store.luots.filter { it.khId == kh.id && (it.daHoanTat || it.daXacNhan) }
            val tienVe = veCuaKh.sumOf { it.thu }
            val tienGoi = store.dks.filter { it.khId == kh.id }.sumOf { dk ->
                store.gois.firstOrNull { it.id == dk.goiId }?.gia ?: 0L
            }
            val tongChiTieu = tienVe + tienGoi
            val soLuot = veCuaKh.size
            val soGoi = store.dks.count { it.khId == kh.id }
            Triple(kh, Pair(tongChiTieu, soLuot), soGoi)
        }.sortedWith(
            compareByDescending<Triple<KH, Pair<Long, Int>, Int>> { it.second.first }
                .thenByDescending { it.second.second }
        ).take(5)

        val topText = vipList.mapIndexed { idx, (kh, stats, soGoi) ->
            val (spend, visits) = stats
            "Top ${idx + 1}: ${kh.ten} (SĐT: ${kh.sdt}) — Chi tiêu: ${formatVnd(spend)} (${visits} lượt đến, $soGoi gói)"
        }.joinToString("\n• ", prefix = "• ")

        return KetQuaAiBot(
            cauTraLoi = "👑 Bảng xếp hạng Khách hàng VIP của cơ sở:\n$topText",
            hanhDongThucThi = "Mở danh sách Khách hàng",
            chuyenTab = 1,
            chuyenMucNguoi = 0
        )
    }

    // =========================================================================
    // MODULE 3: NHÂN VIÊN KTV & PHÂN BỔ CÔNG VIỆC
    // =========================================================================
    private fun xuLyTruyVanKtvVaPhanBo(
        prompt: String,
        norm: String,
        store: SpaStore,
        refDate: String,
        user: TaiKhoan?
    ): KetQuaAiBot? {
        // 1. "Ai đang phục vụ khách" / "KTV nào đang bận"
        if (norm.contains("dang phuc vu") || norm.contains("dang lam") || norm.contains("ktv nao dang ban") ||
            (norm.contains("ai") && (norm.contains("phuc vu") || norm.contains("ban")) && !norm.contains("ranh"))
        ) {
            val veDangLam = store.luots.filter { it.dangLam }
            if (veDangLam.isEmpty()) {
                return KetQuaAiBot(
                    cauTraLoi = "🟢 Hiện tại không có KTV nào đang phục vụ khách (toàn bộ ${store.nvs.size} nhân viên KTV đều đang rảnh sẵn sàng đón khách).",
                    hanhDongThucThi = "Xem vé đang làm",
                    chuyenTab = 0,
                    chuyenMucVe = 0
                )
            }

            val busyLines = veDangLam.map { ve ->
                val nv = store.nvs.firstOrNull { it.id == ve.nvId }
                val kh = store.khs.firstOrNull { it.id == ve.khId }
                val tenDv = store.tenDichVuCuaVe(ve)
                "KTV ${nv?.ten ?: "KTV"}: đang phục vụ khách ${kh?.ten ?: "Khách lẻ"} — Dịch vụ: $tenDv (Bắt đầu lúc ${ve.tgbd})"
            }.joinToString("\n• ", prefix = "• ")

            return KetQuaAiBot(
                cauTraLoi = "🔴 Danh sách KTV đang bận phục vụ khách (${veDangLam.size} người):\n$busyLines",
                hanhDongThucThi = "Xem vé đang làm",
                chuyenTab = 0,
                chuyenMucVe = 0
            )
        }

        // 2. "Ai đang rảnh" / "KTV nào đang rảnh"
        if (norm.contains("dang ranh") || norm.contains("ktv nao dang ranh") || norm.contains("ai ranh") || norm.contains("ai dang ranh")) {
            val busyNvIds = store.luots.filter { it.dangLam }.map { it.nvId }.toSet()
            val freeNvs = store.nvs.filter { it.id !in busyNvIds }
            if (freeNvs.isEmpty()) {
                return KetQuaAiBot(
                    cauTraLoi = "🔴 Hiện tại toàn bộ nhân viên KTV của quán đều đang bận phục vụ khách, không có KTV nào đang rảnh.",
                    hanhDongThucThi = "Xem trạng thái nhân viên",
                    chuyenTab = 0
                )
            }
            val freeLines = freeNvs.joinToString("\n• ", prefix = "• ") {
                "${it.ten} (${it.gt}, SĐT: ${it.sdt})"
            }
            return KetQuaAiBot(
                cauTraLoi = "🟢 Danh sách KTV đang rảnh sẵn sàng nhận khách (${freeNvs.size}/${store.nvs.size} người):\n$freeLines",
                hanhDongThucThi = "Xem nhân viên rảnh",
                chuyenTab = 0
            )
        }

        // 3. "Ai làm nhiều vé nhất [kỳ]" / "Top KTV"
        if (norm.contains("nhieu ve nhat") || norm.contains("nhieu luot nhat") || norm.contains("top ktv") || norm.contains("ktv xuat sac")) {
            val range = parseVietnameseDateRange(prompt, refDate) ?: DateRange(refDate.take(7) + "-01", refDate, "Tháng này (${refDate.take(7)})")
            return layXepHangKtvNhieuVe(range, store)
        }

        // 4. Hiệu suất & hoa hồng của từng KTV cụ thể
        val matchedNv = timNvPhuHop(prompt, store.nvs)
        val isSelfAsk = (user?.isAdmin() != true) && (norm.contains("toi") || norm.contains("minh") || norm.contains("ca nhan"))
        val targetNv = matchedNv ?: if (isSelfAsk) store.nvs.firstOrNull { it.id == user?.nvId || it.id == user?.id } else null

        if (targetNv != null) {
            val isPerformanceAsk = norm.contains("ve") || norm.contains("luot") || norm.contains("hoa hong") ||
                norm.contains("chiet khau") || norm.contains("phut") || norm.contains("luong") || norm.contains("thu nhap")

            if (isPerformanceAsk) {
                // Kiểm tra phân quyền: KTV chỉ được tra cứu vé và hoa hồng của chính mình
                if (user?.isAdmin() != true && targetNv.id != user?.nvId && targetNv.id != user?.id) {
                    return KetQuaAiBot(
                        cauTraLoi = "🔒 Kỹ thuật viên chỉ có quyền tra cứu vé và hoa hồng cá nhân của chính mình. Dữ liệu của nhân sự khác được bảo mật.",
                        hanhDongThucThi = "Từ chối quyền tra cứu KTV khác"
                    )
                }

                val range = parseVietnameseDateRange(prompt, refDate) ?: DateRange(refDate, refDate, "Hôm nay ($refDate)", isSingleDay = true)
                val veNv = store.luots.filter {
                    it.nvId == targetNv.id && (it.daHoanTat || it.daXacNhan) && it.ngay >= range.startDate && it.ngay <= range.endDate
                }
                val soVe = veNv.size
                val tongPhut = veNv.sumOf { calcDurationMinutes(it.tgbd, it.tgkt, 60) }
                val tongHoaHong = veNv.sumOf { store.tinhHoaHongVe(it) }

                if (soVe == 0) {
                    return KetQuaAiBot(
                        cauTraLoi = "👨‍⚕️ Báo cáo KTV ${targetNv.ten} (${range.label}):\n" +
                            "• Ghi nhận: 0 vé phục vụ, 0 phút trị liệu và 0đ hoa hồng tích lũy.",
                        hanhDongThucThi = "Xem vé KTV ${targetNv.ten}",
                        chuyenTab = 4
                    )
                }

                val dangLam = store.luots.count { it.nvId == targetNv.id && it.dangLam }
                val dangLamText = if (dangLam > 0) " (hiện đang làm $dangLam vé)" else ""

                return KetQuaAiBot(
                    cauTraLoi = "👨‍⚕️ Hiệu suất KTV ${targetNv.ten} trong ${range.label}:\n" +
                        "• Số vé đã chốt: $soVe vé$dangLamText\n" +
                        "• Tổng thời gian trị liệu: $tongPhut phút\n" +
                        "• Hoa hồng tích lũy: ${formatVndSpeakable(tongHoaHong)}.",
                    hanhDongThucThi = "Xem chi tiết KTV ${targetNv.ten}",
                    chuyenTab = 4
                )
            }
        }

        return null
    }

    private fun layXepHangKtvNhieuVe(range: DateRange, store: SpaStore): KetQuaAiBot {
        if (store.nvs.isEmpty()) {
            return KetQuaAiBot("Hiện cơ sở chưa có dữ liệu nhân viên KTV nào.")
        }

        val ranking = store.nvs.map { nv ->
            val veNv = store.luots.filter {
                it.nvId == nv.id && (it.daHoanTat || it.daXacNhan) && it.ngay >= range.startDate && it.ngay <= range.endDate
            }
            val soVe = veNv.size
            val tongPhut = veNv.sumOf { calcDurationMinutes(it.tgbd, it.tgkt, 60) }
            val tongHh = veNv.sumOf { store.tinhHoaHongVe(it) }
            Triple(nv, soVe, Pair(tongPhut, tongHh))
        }.sortedWith(
            compareByDescending<Triple<NV, Int, Pair<Int, Long>>> { it.second }
                .thenByDescending { it.third.first }
        )

        val topLines = ranking.mapIndexed { idx, (nv, soVe, extra) ->
            val (phut, hh) = extra
            "Top ${idx + 1}: ${nv.ten} — $soVe vé ($phut phút trị liệu, hoa hồng: ${formatVnd(hh)})"
        }.joinToString("\n• ", prefix = "• ")

        return KetQuaAiBot(
            cauTraLoi = "🏆 Xếp hạng KTV làm nhiều vé nhất trong ${range.label}:\n$topLines",
            hanhDongThucThi = "Xem xếp hạng KTV",
            chuyenTab = 4
        )
    }

    // =========================================================================
    // MODULE 4: DỊCH VỤ & XẾP HẠNG BÁN CHẠY
    // =========================================================================
    private fun xuLyTruyVanDichVuVaMenu(
        prompt: String,
        norm: String,
        store: SpaStore,
        refDate: String,
        @Suppress("UNUSED_PARAMETER") user: TaiKhoan?
    ): KetQuaAiBot? {
        // 1. Dịch vụ bán chạy nhất ("dịch vụ nào chạy nhất", "ban chay nhat", "dich vu hot")
        if (norm.contains("chay nhat") || norm.contains("bán chạy nhất") || norm.contains("ban chay") ||
            norm.contains("top dich vu") || norm.contains("dich vu hot")
        ) {
            val range = parseVietnameseDateRange(prompt, refDate)
            return layXepHangDichVuBanChay(range, store)
        }

        // 2. Tra cứu giá và hoa hồng của dịch vụ cụ thể ("xông hơi giá bao nhiêu hoa hồng bao nhiêu"...)
        val matchedDv = timDichVuPhuHop(prompt, store.dvs)
        if (matchedDv != null && (norm.contains("gia") || norm.contains("hoa hong") || norm.contains("bao nhieu") || norm.contains("chiet khau"))) {
            val chuThucNhan = matchedDv.gia - matchedDv.chietKhauVnd
            return KetQuaAiBot(
                cauTraLoi = "💆 Dịch vụ \"${matchedDv.ten}\" (${matchedDv.phut} phút):\n" +
                    "• Giá thu khách: ${formatVndSpeakable(matchedDv.gia)}\n" +
                    "• Hoa hồng KTV: ${formatVndSpeakable(matchedDv.chietKhauVnd)}\n" +
                    "• Chủ quán thực nhận: ${formatVndSpeakable(chuThucNhan)}/vé.",
                hanhDongThucThi = "Xem dịch vụ ${matchedDv.ten}",
                chuyenTab = 2
            )
        }

        // 3. Tra cứu bảng giá chung
        if (norm.contains("bang gia") || norm.contains("bảng giá") || (norm.contains("dich vu") && norm.contains("menu"))) {
            val dsDv = store.dvs.joinToString("\n• ", prefix = "• ") {
                "${it.ten} (${it.phut}p): ${formatVnd(it.gia)} — Hoa hồng KTV: ${formatVnd(it.chietKhauVnd)}"
            }
            return KetQuaAiBot(
                cauTraLoi = "📋 Bảng giá dịch vụ hiện tại của cơ sở (${store.dvs.size} dịch vụ, ${store.gois.size} gói):\n$dsDv",
                hanhDongThucThi = "Mở bảng giá dịch vụ",
                chuyenTab = 2
            )
        }

        return null
    }

    private fun layXepHangDichVuBanChay(range: DateRange?, store: SpaStore): KetQuaAiBot {
        val veChot = if (range != null) {
            store.luots.filter { (it.daHoanTat || it.daXacNhan) && it.ngay >= range.startDate && it.ngay <= range.endDate }
        } else {
            store.luots.filter { it.daHoanTat || it.daXacNhan }
        }

        if (veChot.isEmpty() || store.dvs.isEmpty()) {
            val kỳ = range?.label ?: "từ trước đến giờ"
            return KetQuaAiBot(
                cauTraLoi = "Hiện chưa có vé hoàn tất nào trong $kỳ để xếp hạng dịch vụ bán chạy.",
                chuyenTab = 2
            )
        }

        val countMap = veChot.flatMap { it.dvIds }.groupingBy { it }.eachCount()
        val ranking = store.dvs.map { dv ->
            val c = countMap[dv.id] ?: 0
            val rev = c * dv.gia
            val hh = c * dv.chietKhauVnd
            Triple(dv, c, Pair(rev, hh))
        }.sortedWith(
            compareByDescending<Triple<DV, Int, Pair<Long, Long>>> { it.second }
                .thenByDescending { it.third.first }
        ).take(5)

        val kỳ = range?.label ?: "từ trước đến giờ"
        val topLines = ranking.mapIndexed { idx, (dv, count, extra) ->
            val (rev, hh) = extra
            "Top ${idx + 1}: ${dv.ten} — $count lượt (Doanh thu: ${formatVnd(rev)}, Hoa hồng KTV: ${formatVnd(hh)})"
        }.joinToString("\n• ", prefix = "• ")

        return KetQuaAiBot(
            cauTraLoi = "🔥 Xếp hạng dịch vụ bán chạy nhất ($kỳ):\n$topLines",
            hanhDongThucThi = "Xem xếp hạng dịch vụ",
            chuyenTab = 2
        )
    }

    // =========================================================================
    // MODULE 5: PHÂN TÍCH SO SÁNH ĐA KỲ
    // =========================================================================
    private fun xuLyTruyVanSoSanhDaKy(
        prompt: String,
        norm: String,
        store: SpaStore,
        refDate: String,
        user: TaiKhoan?
    ): KetQuaAiBot? {
        if (user?.isAdmin() != true) {
            return KetQuaAiBot(
                cauTraLoi = "🔒 Tính năng so sánh hiệu quả kinh doanh đa kỳ thuộc quyền bảo mật của Chủ Spa (Admin).",
                hanhDongThucThi = "Từ chối quyền so sánh"
            )
        }

        val (range1, range2) = tachHaiKhoangSoSanh(prompt, norm, refDate) ?: return null

        val (thu1, chi1, lai1, ve1) = tinhKpiKhoangNgay(range1, store)
        val (thu2, chi2, lai2, ve2) = tinhKpiKhoangNgay(range2, store)

        val deltaThu = thu1 - thu2
        val deltaLai = lai1 - lai2
        val deltaVe = ve1 - ve2

        val pctThuText = if (thu2 > 0L) {
            val pct = ((deltaThu.toDouble() / thu2) * 100).toInt()
            " (${if (pct >= 0) "+$pct" else "$pct"}%)"
        } else ""

        val signThu = if (deltaThu >= 0) "Tăng +${formatVnd(deltaThu)}" else "Giảm -${formatVnd(abs(deltaThu))}"
        val signLai = if (deltaLai >= 0) "Tăng +${formatVnd(deltaLai)}" else "Giảm -${formatVnd(abs(deltaLai))}"
        val signVe = if (deltaVe >= 0) "+$deltaVe" else "$deltaVe"

        val cauTraLoi = "📈 Báo cáo So sánh Đối chiếu Doanh thu:\n" +
            "• So sánh: ${range1.label}  VỚI  ${range2.label}\n\n" +
            "1. Doanh thu:\n" +
            "  - ${range1.label}: ${formatVnd(thu1)}\n" +
            "  - ${range2.label}: ${formatVnd(thu2)}\n" +
            "  => Kết quả: $signThu$pctThuText\n\n" +
            "2. Số vé hoàn thành:\n" +
            "  - ${range1.label}: $ve1 vé  vs  ${range2.label}: $ve2 vé (Chênh lệch: $signVe vé)\n\n" +
            "3. Lãi ròng (Chủ quán thực nhận):\n" +
            "  - ${range1.label}: ${formatVnd(lai1)} (Chi phí: ${formatVnd(chi1)})\n" +
            "  - ${range2.label}: ${formatVnd(lai2)} (Chi phí: ${formatVnd(chi2)})\n" +
            "  => Kết quả: $signLai."

        return KetQuaAiBot(
            cauTraLoi = cauTraLoi,
            hanhDongThucThi = "So sánh đối chiếu kinh doanh",
            chuyenTab = 4
        )
    }

    private fun tinhKpiKhoangNgay(range: DateRange, store: SpaStore): KpiKy {
        val ve = store.luots.filter {
            (it.daHoanTat || it.daXacNhan) && it.ngay >= range.startDate && it.ngay <= range.endDate
        }
        val tc = store.tcs.filter {
            it.ngay >= range.startDate && it.ngay <= range.endDate
        }
        val thuVe = ve.sumOf { it.thu }
        val thuNgoai = tc.filter { it.loai == LoaiTC.THU && !store.laPhieuThuTuVe(it) }.sumOf { it.tien }
        val tongThu = thuVe + thuNgoai

        val hoaHongVe = ve.sumOf { store.tinhHoaHongVe(it) }
        val chiNgoai = tc.filter { it.loai == LoaiTC.CHI && !store.laPhieuChiHoaHongKtv(it) }.sumOf { it.tien }
        val chiKtvTc = tc.filter { store.laPhieuChiHoaHongKtv(it) }.sumOf { it.tien }
        val tongChi = max(hoaHongVe, chiKtvTc) + chiNgoai
        val laiRong = tongThu - tongChi

        return KpiKy(tongThu, tongChi, laiRong, ve.size)
    }

    private data class KpiKy(val thu: Long, val chi: Long, val lai: Long, val soVe: Int)

    private fun tachHaiKhoangSoSanh(prompt: String, norm: String, refDate: String): Pair<DateRange, DateRange>? {
        // Tuần này vs Tuần trước
        if (norm.contains("tuan nay") && (norm.contains("tuan truoc") || norm.contains("tuan roi"))) {
            val r1 = parseVietnameseDateRange("tuần này", refDate)
            val r2 = parseVietnameseDateRange("tuần trước", refDate)
            if (r1 != null && r2 != null) return Pair(r1, r2)
        }

        // Tháng này vs Tháng trước
        if (norm.contains("thang nay") && (norm.contains("thang truoc") || norm.contains("thang roi"))) {
            val r1 = parseVietnameseDateRange("tháng này", refDate)
            val r2 = parseVietnameseDateRange("tháng trước", refDate)
            if (r1 != null && r2 != null) return Pair(r1, r2)
        }

        // Hôm nay vs Hôm qua
        if (norm.contains("hom nay") && (norm.contains("hom qua") || norm.contains("ngay qua"))) {
            val r1 = parseVietnameseDateRange("hôm nay", refDate)
            val r2 = parseVietnameseDateRange("hôm qua", refDate)
            if (r1 != null && r2 != null) return Pair(r1, r2)
        }

        // Cố gắng tách theo từ khóa phân cách "voi" hoặc "va"
        val splitParts = prompt.split(Regex("(?i)\\b(?:với|voi|và|va|so với|so voi)\\b"))
        if (splitParts.size >= 2) {
            val r1 = parseVietnameseDateRange(splitParts[0], refDate)
            val r2 = parseVietnameseDateRange(splitParts[1], refDate)
            if (r1 != null && r2 != null) return Pair(r1, r2)
        }

        return null
    }

    // =========================================================================
    // HÀM TIỆN ÍCH TRỢ GIÚP TÌM KIẾM
    // =========================================================================
    fun timKhachHangPhuHop(prompt: String, khs: List<KH>): KH? {
        val normPrompt = normalizeVietnameseText(prompt).trim()

        // 1. Kiểm tra trực tiếp tên đầy đủ hoặc tên gọi (từ cuối)
        for (kh in khs) {
            val khNorm = normalizeVietnameseText(kh.ten).trim()
            if (khNorm.isNotBlank() && normPrompt.contains(khNorm)) {
                return kh
            }
            val shortName = khNorm.split(" ").lastOrNull().orEmpty()
            if (shortName.length >= 2 && Regex("\\b$shortName\\b").containsMatchIn(normPrompt)) {
                return kh
            }
        }

        // 2. Tìm theo số điện thoại nếu trong câu hỏi có chứa số điện thoại
        val promptDigits = normPrompt.filter { it.isDigit() }
        if (promptDigits.length >= 4) {
            for (kh in khs) {
                val phoneDigits = kh.sdt.filter { it.isDigit() }
                if (phoneDigits.isNotBlank() && phoneDigits.contains(promptDigits)) {
                    return kh
                }
            }
        }

        // 3. Tìm gần đúng qua tinhDiemTimKiemKhachHang (yêu cầu điểm khớp cao >= 80)
        var bestKh: KH? = null
        var bestScore = 0
        for (kh in khs) {
            val score = tinhDiemTimKiemKhachHang(prompt, kh.ten, kh.sdt)
            if (score > bestScore && score >= 80) {
                bestScore = score
                bestKh = kh
            }
        }
        return bestKh
    }

    fun timNvPhuHop(prompt: String, nvs: List<NV>): NV? {
        val normPrompt = normalizeVietnameseText(prompt).trim()
        for (nv in nvs) {
            val nvNorm = normalizeVietnameseText(nv.ten).trim()
            if (nvNorm.isNotBlank() && normPrompt.contains(nvNorm)) {
                return nv
            }
            val shortName = nvNorm.split(" ").lastOrNull().orEmpty()
            if (shortName.length >= 2 && Regex("\\b$shortName\\b").containsMatchIn(normPrompt)) {
                return nv
            }
        }
        return null
    }

    fun timDichVuPhuHop(prompt: String, dvs: List<DV>): DV? {
        val normPrompt = normalizeVietnameseText(prompt).trim()
        // 1. Khớp chính xác toàn bộ tên dịch vụ
        for (dv in dvs) {
            val dvNorm = normalizeVietnameseText(dv.ten).trim()
            if (dvNorm.isNotBlank() && normPrompt.contains(dvNorm)) {
                return dv
            }
        }
        // 2. Khớp cụm từ khóa chính của dịch vụ (VD: "xông hơi", "cổ vai gáy", "body", "đá muối", "bấm huyet")
        for (dv in dvs) {
            val dvNorm = normalizeVietnameseText(dv.ten).trim()
            val cleanName = dvNorm.removePrefix("dich vu ").removePrefix("dv ").removePrefix("massage ").trim()
            if (cleanName.length >= 3 && normPrompt.contains(cleanName)) {
                return dv
            }
            val tokens = cleanName.split(" ").filter { it.length >= 2 }
            if (tokens.size >= 2) {
                val bigram1 = "${tokens[0]} ${tokens[1]}"
                if (normPrompt.contains(bigram1)) {
                    return dv
                }
            }
            if (tokens.size >= 3) {
                val bigram2 = "${tokens[1]} ${tokens[2]}"
                if (normPrompt.contains(bigram2)) {
                    return dv
                }
            }
            for (t in tokens) {
                if (t.length >= 4 && Regex("\\b$t\\b").containsMatchIn(normPrompt)) {
                    return dv
                }
            }
        }
        return null
    }
}
