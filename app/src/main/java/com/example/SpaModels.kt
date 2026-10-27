package com.example

import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * 1. HỆ THỐNG PHÂN QUYỀN (CHỈ DÙNG MÁC ADMIN, KHÔNG HIỂN THỊ OWNER/STAFF)
 */
enum class VaiTro(val badgeLabel: String) {
    OWNER("Admin"),
    STAFF("")
}

enum class QuyenHan(val title: String, val desc: String) {
    CAN_EDIT_COMPLETED_TICKET(
        "Sửa vé đã chốt",
        "Đổi tiền hoặc xóa vé đã hoàn thành"
    ),
    CAN_EDIT_TIME(
        "Sửa giờ vé",
        "Chỉnh giờ bắt đầu và kết thúc"
    ),
    CAN_MANAGE_CUSTOMERS(
        "Quản lý khách hàng",
        "Thêm, sửa, xóa thông tin khách"
    ),
    CAN_MANAGE_SERVICES(
        "Sửa bảng giá",
        "Thêm, sửa, xóa dịch vụ và gói"
    ),
    CAN_MANAGE_FINANCE(
        "Ghi sổ quỹ",
        "Xem và nhập thu chi ngoài lề"
    ),
    CAN_VIEW_REVENUE(
        "Xem báo cáo",
        "Xem doanh thu và bảng lương KTV"
    )
}

enum class LoaiTC(val label: String) {
    THU("Thu"),
    CHI("Chi")
}

enum class BoLocThoiGian(val label: String) {
    NGAY("1 Ngày"),
    THANG("1 Tháng"),
    NAM("1 Năm"),
    TAT_CA("Từ trước đến giờ")
}

/**
 * 2. MÔ HÌNH DỮ LIỆU CỐT LÕI (JSON SCHEMA)
 * Tương thích cả chuẩn mới và chuẩn backup cũ (menu_mới_nhất_2.json)
 */
data class TaiKhoan(
    val id: String,
    val username: String,
    val matKhauHash: String,
    val quyen: VaiTro,
    val nvId: String = "",
    val hoTen: String,
    val cacQuyenDuocCap: Set<QuyenHan> = emptySet(),
    val matKhauHienThi: String = "",
    val email: String = ""
)

fun normalizePhoneDigits(raw: String): String {
    val trimmed = raw.trim().replace(" ", "").replace(".", "").replace("-", "")
    val digitsOnly = trimmed.filter { it.isDigit() }
    if (digitsOnly.startsWith("84") && digitsOnly.length >= 10) {
        return "0" + digitsOnly.removePrefix("84")
    }
    return digitsOnly.ifBlank { trimmed.lowercase() }
}

data class NV(
    val id: String,
    val ten: String,
    val gt: String,
    val sdt: String,
    val gc: String = ""
)

data class DV(
    val id: String,
    val ten: String,
    val phut: Int,
    val gia: Long,
    val moTa: String = "",
    val chietKhauVnd: Long
)

data class Goi(
    val id: String,
    val ten: String,
    val dvIds: List<String>,
    val soBuoi: Int,
    val gia: Long,
    val chiTietLuot: String = "",
    val dvTangKem: String = ""
)

data class KH(
    val id: String,
    val ten: String,
    val sdt: String
)

data class DK(
    val id: String,
    val khId: String,
    val goiId: String,
    val ngay: String,
    val daDung: Boolean = false,
    val luotDaDung: Int = 0
)

data class Luot(
    val id: String,
    val khId: String,
    val nvId: String,
    val dvIds: List<String>,
    val dkId: String = "",
    val tgbd: String,
    val tgkt: String = "",
    val thu: Long = 0L,
    val daXacNhan: Boolean = false,
    val ngay: String = currentShiftDateStr(),
    val dvNgoai: String = "",
    val hoaHongChot: Long? = null,
    val phieuChiHoaHongId: String = "",
    val phieuThuVeId: String = ""
) {
    val dangLam: Boolean get() = tgkt.isBlank()
    val choChot: Boolean get() = tgkt.isNotBlank() && !daXacNhan
    val daHoanTat: Boolean get() = tgkt.isNotBlank() && daXacNhan
    val dungGoi: Boolean get() = dkId.isNotBlank()
}

data class TC(
    val id: String,
    val loai: LoaiTC,
    val tien: Long,
    val lyDo: String,
    val ngay: String
)

/**
 * 3. MÔ HÌNH Ô LỊCH BÁO CÁO, THU NHẬP KTV & BẢNG CHI TIÊU KHÁCH HÀNG
 */
data class OLichBaoCaoItem(
    val keyDate: String,
    val tieuDeO: String,
    val moTaDayDu: String,
    val tongThu: Long,
    val tongPhiChi: Long,
    val doanhThuSauTruPhi: Long,
    val soVeDaChot: Int,
    val dangChon: Boolean
)

data class ChiTietLuotKtv(
    val luotId: String,
    val ngay: String,
    val khungGio: String,
    val soPhut: Int,
    val tenKhach: String,
    val sdtKhach: String,
    val tenDichVu: String,
    val loaiVe: String,
    val tienThuVe: Long,
    val hoaHongKtv: Long
)

data class BangLuongKtvSummary(
    val nv: NV,
    val tongSoLuot: Int,
    val tongHoaHong: Long,
    val tongPhutLam: Int,
    val danhSachLuot: List<ChiTietLuotKtv>
)

data class ThongKeKhachHangItem(
    val kh: KH,
    val sdtDinhDanh: String,
    val soLuotDichVuLe: Int,
    val soLuotDungGoi: Int,
    val soGoiDangMua: Int,
    val tongTienDaChi: Long,
    val chiTietDichVuDaDung: List<String>,
    val diemKhopTimKiem: Int = 100
)

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val actionNote: String = "",
    val thoiGian: String = "${todayDateStr()} ${nowTimeStr()}"
)

enum class LoaiLenhChoXacNhan(val tenHienThi: String) {
    THEM("THÊM DỮ LIỆU"),
    SUA("SỬA DỮ LIỆU"),
    XOA("XÓA DỮ LIỆU")
}

data class LenhChoXacNhanGehihi(
    val id: String,
    val loai: LoaiLenhChoXacNhan,
    val tieuDeTomTat: String,
    val chiTietXacNhan: String,
    val thucThi: () -> KetQuaAiBot
)

/**
 * 4. HÀM TIỆN ÍCH PHÂN QUYỀN, TIỀN TỆ, THỜI GIAN & TÌM KIẾM GẦN ĐÚNG GEHIHI
 */
const val DEFAULT_ADMIN_PHONE = "0397772963"
const val DEFAULT_ADMIN_PASS = "247208"

fun TaiKhoan?.isAdmin(): Boolean = this?.quyen == VaiTro.OWNER
fun TaiKhoan?.isOwner(): Boolean = isAdmin()

fun TaiKhoan?.hasPermission(permission: QuyenHan): Boolean {
    if (this == null) return false
    if (this.quyen == VaiTro.OWNER) return true
    return this.cacQuyenDuocCap.contains(permission)
}

fun TaiKhoan?.canDeleteTicket(ve: Luot): Boolean {
    if (this == null) return false
    if (this.isAdmin()) return true
    if (ve.dangLam) return true
    return this.hasPermission(QuyenHan.CAN_EDIT_COMPLETED_TICKET)
}

fun TaiKhoan?.canModifyCompletedTicket(ve: Luot): Boolean {
    if (this == null) return false
    if (this.isAdmin()) return true
    if (ve.dangLam) return true
    return this.hasPermission(QuyenHan.CAN_EDIT_COMPLETED_TICKET)
}

fun hashPassword(raw: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(raw.trim().toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun formatVnd(amount: Long): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)}đ"
}

fun formatVndCompact(amount: Long): String {
    val absVal = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else if (amount > 0) "+" else ""
    return when {
        absVal == 0L -> "0đ"
        absVal >= 1_000_000L -> {
            val tr = absVal / 1_000_000.0
            val formatted = if (absVal % 1_000_000L == 0L) "${absVal / 1_000_000L}tr"
            else String.format(Locale.US, "%.1ftr", tr)
            "$sign$formatted"
        }
        absVal >= 1_000L -> "$sign${absVal / 1_000L}k"
        else -> "$sign${absVal}đ"
    }
}

fun formatVndSpeakable(amount: Long): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)} đồng"
}

/**
 * Ca làm việc tính từ 06:00 sáng hôm nay đến 05:59 sáng hôm sau.
 * Nếu thời điểm hiện tại từ 00:00 đến 05:59 sáng -> vẫn thuộc ca của ngày hôm trước.
 */
fun currentShiftDateStr(now: Date = Date()): String {
    val cal = Calendar.getInstance()
    cal.time = now
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    if (hour < 6) {
        cal.add(Calendar.DAY_OF_MONTH, -1)
    }
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
}

fun todayDateStr(): String {
    return currentShiftDateStr()
}

fun nowTimeStr(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}

fun calcDurationMinutes(tgbd: String, tgkt: String, fallbackMinutes: Int = 60): Int {
    if (tgbd.isBlank() || tgkt.isBlank()) return fallbackMinutes
    return try {
        val p1 = tgbd.trim().split(":")
        val p2 = tgkt.trim().split(":")
        val m1 = p1[0].toInt() * 60 + p1[1].toInt()
        val m2 = p2[0].toInt() * 60 + p2[1].toInt()
        val diff = if (m2 >= m1) m2 - m1 else (m2 + 24 * 60) - m1
        max(1, diff)
    } catch (_: Exception) {
        fallbackMinutes
    }
}

fun matchesTimeFilter(dateStr: String, filter: BoLocThoiGian, referenceDate: String = todayDateStr()): Boolean {
    if (filter == BoLocThoiGian.TAT_CA) return true
    val cleanDate = dateStr.trim()
    val cleanRef = referenceDate.trim()
    if (cleanDate.length < 4 || cleanRef.length < 4) return true
    return when (filter) {
        BoLocThoiGian.NGAY -> cleanDate.take(10) == cleanRef.take(10)
        BoLocThoiGian.THANG -> cleanDate.take(7) == cleanRef.take(7)
        BoLocThoiGian.NAM -> cleanDate.take(4) == cleanRef.take(4)
        BoLocThoiGian.TAT_CA -> true
    }
}

fun shiftReferenceDate(refDate: String, filter: BoLocThoiGian, delta: Int): String {
    if (filter == BoLocThoiGian.TAT_CA) return todayDateStr()
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsed = sdf.parse(refDate) ?: Date()
        val cal = Calendar.getInstance()
        cal.time = parsed
        when (filter) {
            BoLocThoiGian.NGAY -> cal.add(Calendar.DAY_OF_MONTH, delta)
            BoLocThoiGian.THANG -> cal.add(Calendar.MONTH, delta)
            BoLocThoiGian.NAM -> cal.add(Calendar.YEAR, delta)
            BoLocThoiGian.TAT_CA -> {}
        }
        sdf.format(cal.time)
    } catch (_: Exception) {
        todayDateStr()
    }
}

fun formatPeriodLabel(refDate: String, filter: BoLocThoiGian): String {
    if (filter == BoLocThoiGian.TAT_CA) return "Từ trước đến giờ (Toàn bộ)"
    val clean = refDate.trim()
    if (clean.length < 10) return clean
    val year = clean.substring(0, 4)
    val month = clean.substring(5, 7)
    val day = clean.substring(8, 10)
    return when (filter) {
        BoLocThoiGian.NGAY -> "Ngày $day/$month/$year"
        BoLocThoiGian.THANG -> "Tháng $month/$year"
        BoLocThoiGian.NAM -> "Năm $year"
        BoLocThoiGian.TAT_CA -> "Từ trước đến giờ"
    }
}

fun getDaysInMonth(yearMonthDateStr: String): Int {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsed = sdf.parse(yearMonthDateStr) ?: Date()
        val cal = Calendar.getInstance()
        cal.time = parsed
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    } catch (_: Exception) {
        30
    }
}

fun normalizeVietnameseText(input: String): String {
    val mapDia = mapOf(
        'à' to 'a', 'á' to 'a', 'ả' to 'a', 'ã' to 'a', 'ạ' to 'a',
        'ă' to 'a', 'ằ' to 'a', 'ắ' to 'a', 'ẳ' to 'a', 'ẵ' to 'a', 'ặ' to 'a',
        'â' to 'a', 'ầ' to 'a', 'ấ' to 'a', 'ẩ' to 'a', 'ẫ' to 'a', 'ậ' to 'a',
        'đ' to 'd',
        'è' to 'e', 'é' to 'e', 'ẻ' to 'e', 'ẽ' to 'e', 'ẹ' to 'e',
        'ê' to 'e', 'ề' to 'e', 'ế' to 'e', 'ể' to 'e', 'ễ' to 'e', 'ệ' to 'e',
        'ì' to 'i', 'í' to 'i', 'ỉ' to 'i', 'ĩ' to 'i', 'ị' to 'i',
        'ò' to 'o', 'ó' to 'o', 'ỏ' to 'o', 'õ' to 'o', 'ọ' to 'o',
        'ô' to 'o', 'ồ' to 'o', 'ố' to 'o', 'ổ' to 'o', 'ỗ' to 'o', 'ộ' to 'o',
        'ơ' to 'o', 'ờ' to 'o', 'ớ' to 'o', 'ở' to 'o', 'ỡ' to 'o', 'ợ' to 'o',
        'ù' to 'u', 'ú' to 'u', 'ủ' to 'u', 'ũ' to 'u', 'ụ' to 'u',
        'ư' to 'u', 'ừ' to 'u', 'ứ' to 'u', 'ử' to 'u', 'ữ' to 'u', 'ự' to 'u',
        'ỳ' to 'y', 'ý' to 'y', 'ỷ' to 'y', 'ỹ' to 'y', 'ỵ' to 'y'
    )
    return input.lowercase(Locale.getDefault())
        .map { mapDia[it] ?: it }
        .joinToString("")
}

fun normalizePhoneOrUsername(sdt: String, fallbackName: String, fallbackIndex: Int): String {
    val digitsOnly = sdt.filter { it.isDigit() }
    if (digitsOnly.length >= 6) return digitsOnly
    val slug = normalizeVietnameseText(fallbackName).replace(Regex("[^a-z0-9]"), "")
    return if (slug.isNotBlank()) slug else "090000000$fallbackIndex"
}

/**
 * Khoảng cách chỉnh sửa Levenshtein để gehihi gợi ý tên khách hàng khi gõ sai chính tả hoặc gần đúng
 */
fun levenshteinDistance(s1: String, s2: String): Int {
    if (s1 == s2) return 0
    if (s1.isEmpty()) return s2.length
    if (s2.isEmpty()) return s1.length

    val prev = IntArray(s2.length + 1) { it }
    val curr = IntArray(s2.length + 1)

    for (i in 1..s1.length) {
        curr[0] = i
        for (j in 1..s2.length) {
            val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
            curr[j] = min(
                min(curr[j - 1] + 1, prev[j] + 1),
                prev[j - 1] + cost
            )
        }
        for (j in 0..s2.length) prev[j] = curr[j]
    }
    return prev[s2.length]
}

/**
 * Tính điểm khớp tìm kiếm khách hàng (0..100) theo Số điện thoại hoặc Tên gần đúng (hỗ trợ viết sai chữ)
 */
fun tinhDiemTimKiemKhachHang(query: String, tenKhach: String, sdtKhach: String): Int {
    val cleanQuery = query.trim()
    if (cleanQuery.isBlank()) return 100

    val queryDigits = cleanQuery.filter { it.isDigit() }
    val phoneDigits = sdtKhach.filter { it.isDigit() }

    // 1. Tìm theo Số điện thoại (Định danh chính của khách)
    if (queryDigits.isNotEmpty() && phoneDigits.isNotEmpty()) {
        if (phoneDigits == queryDigits) return 100
        if (phoneDigits.contains(queryDigits)) return 95
    }

    // 2. Tìm theo Tên khách hàng (hỗ trợ không dấu, viết tắt, viết sai chính tả)
    val qNorm = normalizeVietnameseText(cleanQuery).replace(Regex("\\s+"), " ").trim()
    val nameNorm = normalizeVietnameseText(tenKhach).replace(Regex("\\s+"), " ").trim()
    if (qNorm.isEmpty()) return 0

    if (nameNorm == qNorm) return 100
    if (nameNorm.contains(qNorm)) return 92

    // So sánh từng từ trong tên (VD: gõ "tuan" hoặc "tuấn", hoặc gõ sai "tuanh", "huog", "hug")
    val qTokens = qNorm.split(" ").filter { it.isNotBlank() }
    val nameTokens = nameNorm.split(" ").filter { it.isNotBlank() }

    var bestTokenScore = 0
    for (qt in qTokens) {
        for (nt in nameTokens) {
            if (nt == qt) {
                bestTokenScore = max(bestTokenScore, 90)
            } else if (nt.startsWith(qt) || qt.startsWith(nt)) {
                bestTokenScore = max(bestTokenScore, 82)
            } else {
                val dist = levenshteinDistance(qt, nt)
                val maxLen = max(qt.length, nt.length)
                if (maxLen >= 3 && dist <= 1) {
                    bestTokenScore = max(bestTokenScore, 75)
                } else if (maxLen >= 4 && dist <= 2) {
                    bestTokenScore = max(bestTokenScore, 62)
                }
            }
        }
    }

    // So sánh toàn chuỗi nếu gõ liền hoặc sai 1-2 ký tự
    val fullDist = levenshteinDistance(qNorm, nameNorm)
    val fullMax = max(qNorm.length, nameNorm.length)
    val fullScore = if (fullMax > 0) {
        ((fullMax - fullDist) * 100) / fullMax
    } else 0

    return max(bestTokenScore, if (fullScore >= 55) fullScore else 0)
}
