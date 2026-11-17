package com.example.vietqr

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class NganHangInfo(
    val code: String,      // Mã viết tắt VietQR (vd: MB, VCB, TCB)
    val ten: String,       // Tên đầy đủ
    val bin: String        // Mã BIN Napas
)

data class ThongTinNganHang(
    val bankCode: String = "MB",
    val soTaiKhoan: String = "",
    val tenChuTaiKhoan: String = "",
    val qrTemplate: String = "compact2"
)

object VietQrHelper {

    val DANH_SACH_NGAN_HANG = listOf(
        NganHangInfo("MB", "MBBank (Quân Đội)", "970422"),
        NganHangInfo("VCB", "Vietcombank (Ngoại Thương)", "970436"),
        NganHangInfo("TCB", "Techcombank (Kỹ Thương)", "970407"),
        NganHangInfo("VPB", "VPBank (Việt Nam Thịnh Vượng)", "970432"),
        NganHangInfo("ACB", "ACB (Á Châu)", "970416"),
        NganHangInfo("BIDV", "BIDV (Đầu Tư & Phát Triển)", "970418"),
        NganHangInfo("CTG", "VietinBank (Công Thương)", "970415"),
        NganHangInfo("TPB", "TPBank (Tiên Phong)", "970423"),
        NganHangInfo("VBA", "Agribank (Nông Nghiệp)", "970405"),
        NganHangInfo("STB", "Sacombank (Sài Gòn Thương Tín)", "970403"),
        NganHangInfo("VIB", "VIB (Quốc Tế)", "970441"),
        NganHangInfo("MSB", "MSB (Hàng Hải)", "970426"),
        NganHangInfo("HDB", "HDBank (Phát Triển TP.HCM)", "970437"),
        NganHangInfo("OCB", "OCB (Phương Đông)", "970448")
    )

    fun timNganHangTheoCode(code: String): NganHangInfo {
        return DANH_SACH_NGAN_HANG.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: DANH_SACH_NGAN_HANG.first()
    }

    /**
     * Tạo đường link tải ảnh mã VietQR chuẩn Napas 247 thời gian thực
     * @param bankCode Mã ngân hàng (vd: "MB")
     * @param soTaiKhoan Số tài khoản thụ hưởng
     * @param tenChuTaiKhoan Tên chủ tài khoản
     * @param soTien Số tiền cần thanh toán
     * @param noiDung Nội dung chuyển khoản (tự động encode URL)
     * @param template Giao diện mã QR: compact2 (đầy đủ logo & khung), qr_only (chỉ mã vuông)
     */
    fun taoLinkVietQrDong(
        bankCode: String,
        soTaiKhoan: String,
        tenChuTaiKhoan: String = "",
        soTien: Long = 0L,
        noiDung: String = "",
        template: String = "compact2"
    ): String {
        val cleanAcc = soTaiKhoan.filter { it.isLetterOrDigit() }
        if (cleanAcc.isBlank()) return ""
        val cleanBank = bankCode.trim().uppercase()
        val encMemo = URLEncoder.encode(noiDung.trim(), StandardCharsets.UTF_8.toString()).replace("+", "%20")
        val encName = URLEncoder.encode(tenChuTaiKhoan.trim().uppercase(), StandardCharsets.UTF_8.toString()).replace("+", "%20")

        val builder = StringBuilder("https://img.vietqr.io/image/$cleanBank-$cleanAcc-$template.png")
        val params = mutableListOf<String>()
        if (soTien > 0) {
            params.add("amount=$soTien")
        }
        if (encMemo.isNotBlank()) {
            params.add("addInfo=$encMemo")
        }
        if (encName.isNotBlank()) {
            params.add("accountName=$encName")
        }
        if (params.isNotEmpty()) {
            builder.append("?").append(params.joinToString("&"))
        }
        return builder.toString()
    }

    /**
     * Sinh nội dung chuyển khoản tự động cho vé dịch vụ: VE_<6_ký_tự_cuối_id>
     */
    fun taoNoiDungChuyenKhoanVe(ticketId: String): String {
        val slug = ticketId.replace("ve_", "").replace("-", "").takeLast(6).uppercase()
        return "VE $slug"
    }

    /**
     * Sinh nội dung chuyển khoản gia hạn gói dịch vụ cơ sở: GIAHAN_<tenantId>
     */
    fun taoNoiDungGiaHanSaaS(tenantId: String): String {
        val slug = tenantId.replace("spa_", "").replace("-", "_").take(15).trimEnd('_').uppercase()
        return "GIAHAN $slug"
    }
}
