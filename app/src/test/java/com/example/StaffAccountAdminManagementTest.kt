package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StaffAccountAdminManagementTest {

    private lateinit var context: Context
    private lateinit var store: SpaStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        store = SpaStore(context)
        // Đăng nhập tài khoản Admin gốc để có toàn quyền quản trị
        store.currentUser = store.taiKhoans.firstOrNull { it.id == "tk_admin" }
            ?: TaiKhoan(
                id = "tk_admin",
                username = DEFAULT_ADMIN_PHONE,
                matKhauHash = hashPassword(DEFAULT_ADMIN_PASS),
                quyen = VaiTro.OWNER,
                nvId = "",
                hoTen = "Admin Quản trị",
                cacQuyenDuocCap = QuyenHan.entries.toSet(),
                matKhauHienThi = DEFAULT_ADMIN_PASS
            )
    }

    /**
     * Vấn đề 1: Khi gạt công tắc cấp quyền ngang bằng Admin cho nhân viên,
     * nhân viên phải tiếp tục hiển thị trong danh sách staffAccounts của SubTab 0,
     * không bị ẩn/biến mất khỏi danh sách quản lý.
     */
    @Test
    fun testEmployeeRemainsInStaffListWhenAdminToggledOn() {
        val nv = store.themNhanVien(
            ten = "Hoàng Văn Nam",
            gt = "Nam",
            sdt = "0944556677",
            gc = "KTV Trị liệu",
            matKhauKhoiTao = "pass123",
            laAdmin = false
        )
        assertNotNull(nv)
        val tk = store.taiKhoans.firstOrNull { it.nvId == nv!!.id }
        assertNotNull(tk)
        assertFalse("Ban đầu nhân viên chưa có quyền Admin", tk!!.isAdmin())

        // 1. Gạt công tắc cấp quyền ngang bằng Admin cho nhân viên
        val toggleOk = store.toggleMacAdminChoTaiKhoan(tk.id, true)
        assertTrue(toggleOk)

        // Kiểm tra tài khoản đã mang quyền OWNER / Admin
        val updatedTk = store.taiKhoans.first { it.id == tk.id }
        assertTrue(updatedTk.isAdmin())
        assertTrue(updatedTk.isOwner())

        // 2. Logic lọc danh sách staffAccounts trong SubTab 0 của MainActivity:
        val rootAdminTk = store.taiKhoans.firstOrNull {
            it.id == "tk_admin" || it.id.startsWith("tk_owner_") || (it.quyen == VaiTro.OWNER && it.nvId.isBlank())
        } ?: store.taiKhoans.firstOrNull { it.id == "tk_admin" } ?: store.currentUser

        val staffAccounts = store.taiKhoans.filter {
            it.id != rootAdminTk?.id && it.id != "tk_admin" && !it.id.startsWith("tk_owner_")
        }

        // Nhân viên được cấp Admin VẪN PHẢI NẰM trong danh sách staffAccounts!
        assertTrue(
            "Nhân viên được cấp quyền Admin vẫn phải giữ nguyên trong danh sách staffAccounts",
            staffAccounts.any { it.id == tk.id }
        )

        // 3. Gạt công tắc thu hồi quyền Admin
        val revokeOk = store.toggleMacAdminChoTaiKhoan(tk.id, false)
        assertTrue(revokeOk)
        val revokedTk = store.taiKhoans.first { it.id == tk.id }
        assertFalse(revokedTk.isAdmin())

        val staffAccountsAfterRevoke = store.taiKhoans.filter {
            it.id != rootAdminTk?.id && it.id != "tk_admin" && !it.id.startsWith("tk_owner_")
        }
        assertTrue(staffAccountsAfterRevoke.any { it.id == tk.id })
    }

    /**
     * Vấn đề 1: Tài khoản Admin gốc (SubTab 1) luôn độc lập và không bị chiếm đoạt
     * khi có nhân viên được thăng quyền Admin.
     */
    @Test
    fun testRootAdminRemainsDistinctInSubTab1WhenStaffPromoted() {
        val rootAdminBefore = store.taiKhoans.first { it.id == "tk_admin" }

        val nv = store.themNhanVien("Trần Thị Hoa", "Nữ", "0933221100", "", "hoa@123", laAdmin = true)
        assertNotNull(nv)

        val rootAdminAfter = store.taiKhoans.first { it.id == "tk_admin" }
        assertEquals(DEFAULT_ADMIN_PHONE, rootAdminAfter.username)
        assertEquals(rootAdminBefore.id, rootAdminAfter.id)

        // SubTab 1 tìm tài khoản Admin gốc
        val resolvedRootAdmin = store.taiKhoans.firstOrNull {
            it.id == "tk_admin" || it.id.startsWith("tk_owner_") || (it.quyen == VaiTro.OWNER && it.nvId.isBlank())
        } ?: store.taiKhoans.firstOrNull { it.id == "tk_admin" } ?: store.currentUser

        assertEquals("tk_admin", resolvedRootAdmin?.id)
        assertTrue("Admin gốc không có nvId gắn kèm", resolvedRootAdmin?.nvId.isNullOrBlank())
    }

    /**
     * Vấn đề 2: Tạo nhân viên / tài khoản thủ công với tên đăng nhập tùy biến
     * chứa chữ cái (alphanumeric) và mật khẩu tùy biến có ký tự đặc biệt.
     */
    @Test
    fun testManualStaffCreationPreservesAlphanumericUsernameAndCustomPassword() {
        val customUser = "thaospa"
        val customPass = "Thao@Spa2026"

        val nv = store.themNhanVien(
            ten = "Lê Thị Thảo",
            gt = "Nữ",
            sdt = customUser,
            gc = "KTV Chăm sóc da",
            matKhauKhoiTao = customPass,
            laAdmin = false
        )
        assertNotNull(nv)

        val tk = store.taiKhoans.firstOrNull { it.nvId == nv!!.id }
        assertNotNull("Tài khoản phải được tạo thành công", tk)
        assertEquals("Tên đăng nhập phải giữ nguyên chính xác chuỗi nhập vào", customUser, tk!!.username)
        assertEquals("Mật khẩu hiển thị phải giữ nguyên cả chữ hoa và ký tự đặc biệt", customPass, tk.matKhauHienThi)
        assertEquals(hashPassword(customPass), tk.matKhauHash)

        // Kiểm tra đăng nhập với tên đăng nhập chữ và mật khẩu đặc biệt này
        val loginSuccess = store.login(customUser, customPass)
        assertTrue("Đăng nhập thành công với username và password tùy biến", loginSuccess)
    }

    /**
     * Vấn đề 2: Hàm themTaiKhoanMoi bảo toàn đầy đủ username và password.
     */
    @Test
    fun testThemTaiKhoanMoiHelperPreservesCustomCredentials() {
        val customUser = "letan01"
        val customPass = "Letan@Pass#2026"

        val nv = store.themTaiKhoanMoi(
            hoTen = "Trần Lễ Tân",
            tenDangNhap = customUser,
            matKhau = customPass,
            laAdmin = false,
            email = "letan@spa.com",
            gioiTinh = "Nam",
            ghiChu = "Lễ tân ca sáng"
        )
        assertNotNull(nv)

        val tk = store.taiKhoans.firstOrNull { it.nvId == nv!!.id }
        assertNotNull(tk)
        assertEquals(customUser, tk!!.username)
        assertEquals(customPass, tk.matKhauHienThi)
        assertEquals(hashPassword(customPass), tk.matKhauHash)
        assertEquals("letan@spa.com", tk.email)
    }

    /**
     * Vấn đề 2: Lệnh AI gehihi ngoại tuyến (offline) phân tích khẩu lệnh tự nhiên
     * bóc tách chính xác tên đăng nhập tùy biến và mật khẩu có từ nối (là, thành) và ký tự đặc biệt.
     */
    @Test
    fun testAiGehihiDirectExecutionPreservesCustomUsernameAndPassword() {
        val prompt = "Thêm nhân viên Hoa tên đăng nhập hoaktv mật khẩu là hoa@2024"
        val res = store.traLoiThongMinhNoiBo(prompt)

        assertNotNull(res)
        assertTrue("Lệnh tạo nhân viên cần yêu cầu xác nhận", store.pendingAiConfirmation != null)

        // Xác nhận thực thi
        store.xacNhanThucThiLenhGehihi()

        val tk = store.taiKhoans.firstOrNull { it.username == "hoaktv" }
        assertNotNull("Tài khoản 'hoaktv' phải được tạo thành công từ lệnh offline", tk)
        assertEquals("hoaktv", tk!!.username)
        assertEquals("hoa@2024", tk.matKhauHienThi)
        assertEquals(hashPassword("hoa@2024"), tk.matKhauHash)
    }

    /**
     * Vấn đề 2: Thẻ lệnh [ACTION:ADD_STAFF] của AI gehihi (online parser)
     * bóc tách trường tk và mk, không lọc bỏ chữ cái của username.
     */
    @Test
    fun testAiGehihiAddStaffActionPreservesCustomUsernameAndPassword() {
        val actionMessage = "Dạ tôi đã lên đơn thêm nhân viên. [ACTION:ADD_STAFF|ten=Vũ Văn Nam|tk=nam_ktv|sdt=0988776655|gt=Nam|mk=Nam#2026|email=nam@gmail.com|admin=false]"
        val res = store.phanTichPhanHoiGeminiVaThucThi(actionMessage)

        assertNotNull(res)
        assertTrue("Lệnh tạo nhân viên từ thẻ hành động cần xác nhận", store.pendingAiConfirmation != null)

        store.xacNhanThucThiLenhGehihi()

        val tk = store.taiKhoans.firstOrNull { it.username == "nam_ktv" }
        assertNotNull("Tài khoản 'nam_ktv' phải được tạo từ thẻ ACTION", tk)
        assertEquals("nam_ktv", tk!!.username)
        assertEquals("Nam#2026", tk.matKhauHienThi)
        assertEquals(hashPassword("Nam#2026"), tk.matKhauHash)
    }

    /**
     * Vấn đề 2: Kiểm tra normalizePhoneOrUsername chỉ fallback khi đầu vào rỗng,
     * giữ nguyên chuỗi khi người dùng có nhập username tùy biến.
     */
    @Test
    fun testNormalizePhoneOrUsernamePreservesInputAndOnlyFallbacksWhenEmpty() {
        assertEquals("letan1", normalizePhoneOrUsername("letan1", "Lê Tân", 1))
        assertEquals("thaospa", normalizePhoneOrUsername("thaospa", "Thảo Spa", 2))
        assertEquals("ktv_01", normalizePhoneOrUsername("ktv_01", "KTV 01", 3))
        assertEquals("0912345678", normalizePhoneOrUsername("0912345678", "Văn A", 4))
        assertEquals("0912345678", normalizePhoneOrUsername("84912345678", "Văn A", 4))

        // Chỉ fallback sang tên không dấu khi input rỗng
        assertEquals("nguyenvanb", normalizePhoneOrUsername("", "Nguyễn Văn B", 5))
        assertEquals("tranthic", normalizePhoneOrUsername("   ", "Trần Thị C", 6))
    }

    /**
     * Vấn đề 2: Kiểm tra normalizePhoneDigits không tước đoạt chữ cái trong username chữ.
     */
    @Test
    fun testNormalizePhoneDigitsPreservesAlphanumericStrings() {
        assertEquals("0912345678", normalizePhoneDigits("0912 345 678"))
        assertEquals("0912345678", normalizePhoneDigits("0912.345.678"))
        assertEquals("0912345678", normalizePhoneDigits("84912345678"))
        assertEquals("0912345678", normalizePhoneDigits("+84912345678"))

        assertEquals("letan1", normalizePhoneDigits("letan1"))
        assertEquals("thaospa", normalizePhoneDigits("thaospa"))
        assertEquals("ktv_01", normalizePhoneDigits("ktv_01"))
        assertEquals("spa.thaole", normalizePhoneDigits("spa.thaole"))
        assertEquals("user-123", normalizePhoneDigits("user-123"))
        assertEquals("spa.thaole", normalizePhoneDigits("  spa.thaole  "))
        assertEquals("user-123", normalizePhoneDigits("  user-123  "))
    }

    /**
     * Vấn đề 2: Kiểm tra migrateLegacyAccountsAndMenuIfNeeded không tự động
     * ghi đè username tùy biến của nhân viên bằng số điện thoại khi khởi động lại app.
     */
    @Test
    fun testLegacyMigrationDoesNotOverwriteCustomAlphanumericUsername() {
        val nv = NV(id = "nv_custom_test", ten = "Nhân viên Test", gt = "Nữ", sdt = "0988776655")
        store.nvs.add(nv)

        val tk = TaiKhoan(
            id = "tk_custom_test",
            username = "thaospa",
            matKhauHash = hashPassword("Pass@123"),
            quyen = VaiTro.STAFF,
            nvId = nv.id,
            hoTen = nv.ten,
            cacQuyenDuocCap = emptySet(),
            matKhauHienThi = "Pass@123"
        )
        store.taiKhoans.add(tk)

        // Chạy migration
        store.migrateLegacyAccountsAndMenuIfNeeded()

        val tkAfter = store.taiKhoans.first { it.id == "tk_custom_test" }
        assertEquals("Username tùy biến 'thaospa' không được bị ghi đè thành phoneDigits", "thaospa", tkAfter.username)
        assertEquals("Pass@123", tkAfter.matKhauHienThi)
    }

    /**
     * Vấn đề 3: Kiểm tra ngữ nghĩa trợ năng TalkBack cho Badge và Switch Admin.
     */
    @Test
    fun testTalkBackSemanticsForAdminBadgeAndSwitch() {
        val staffUser = TaiKhoan("tk_1", "0912000000", "hash", VaiTro.STAFF, "nv_1", "Lê Văn An", emptySet(), "123")
        val adminUser = TaiKhoan("tk_2", "0912111111", "hash", VaiTro.OWNER, "nv_2", "Lê Văn Bình", emptySet(), "123")

        fun getBadgeContentDescription(isAdmin: Boolean): String {
            return if (isAdmin) "Được cấp quyền Admin" else ""
        }

        fun getSwitchStateDescription(isAdmin: Boolean): String {
            return if (isAdmin) "Đang có quyền ngang bằng Admin" else "Nhân viên thường"
        }

        fun getCardContentDescription(tk: TaiKhoan): String {
            val passText = tk.matKhauHienThi.ifBlank { "123456" }
            return "Nhân viên ${tk.hoTen}. Số điện thoại đăng nhập: ${tk.username}. Mật khẩu hiện tại: $passText. Trạng thái: ${if (tk.isAdmin()) "Được cấp quyền Admin" else "Nhân viên thường, đã mở ${tk.cacQuyenDuocCap.size} quyền phụ"}."
        }

        assertEquals("Được cấp quyền Admin", getBadgeContentDescription(adminUser.isAdmin()))
        assertEquals("", getBadgeContentDescription(staffUser.isAdmin()))

        assertEquals("Đang có quyền ngang bằng Admin", getSwitchStateDescription(adminUser.isAdmin()))
        assertEquals("Nhân viên thường", getSwitchStateDescription(staffUser.isAdmin()))

        assertTrue(getCardContentDescription(adminUser).contains("Được cấp quyền Admin"))
        assertTrue(getCardContentDescription(staffUser).contains("Nhân viên thường"))
    }

    /**
     * Kiểm thử khắc phục lỗi nuốt tiền tố tên đăng nhập (Finding 1 từ Challenger):
     * Khẩu lệnh "Thêm nhân viên Lan tài khoản lan_ktv mật khẩu là Lan@2026!"
     * phải trích xuất chính xác username = "lan_ktv" (không bị cắt thành "n_ktv")
     * và password = "Lan@2026!".
     */
    @Test
    fun testAiGehihiNlpExtractsLanKtvWithoutPrefixLoss() {
        val prompt = "Thêm nhân viên Lan tài khoản lan_ktv mật khẩu là Lan@2026!"
        val res = store.traLoiThongMinhNoiBo(prompt)
        assertNotNull(res)
        assertTrue("Lệnh tạo nhân viên cần yêu cầu xác nhận", store.pendingAiConfirmation != null)

        store.xacNhanThucThiLenhGehihi()

        val tk = store.taiKhoans.firstOrNull { it.username == "lan_ktv" }
        assertNotNull("Tài khoản 'lan_ktv' phải được tạo thành công, không bị nuốt tiền tố thành n_ktv", tk)
        assertEquals("lan_ktv", tk!!.username)
        assertEquals("Lan@2026!", tk.matKhauHienThi)
        assertEquals(hashPassword("Lan@2026!"), tk.matKhauHash)
    }

    /**
     * Kiểm thử khắc phục lỗi va chạm từ khóa password (Finding 2A từ Challenger):
     * Khẩu lệnh chứa từ khóa tiếng Anh "password":
     * "Thêm nhân viên Phương username phuong_spa password là Phuong!2026"
     * phải trích xuất chính xác password = "Phuong!2026" (không bị cắt thành "word")
     * và username = "phuong_spa".
     */
    @Test
    fun testAiGehihiNlpExtractsPasswordKeywordCorrectly() {
        val prompt = "Thêm nhân viên Phương username phuong_spa password là Phuong!2026"
        val res = store.traLoiThongMinhNoiBo(prompt)
        assertNotNull(res)
        assertTrue("Lệnh tạo nhân viên cần yêu cầu xác nhận", store.pendingAiConfirmation != null)

        store.xacNhanThucThiLenhGehihi()

        val tk = store.taiKhoans.firstOrNull { it.username == "phuong_spa" }
        assertNotNull("Tài khoản 'phuong_spa' phải được tạo thành công", tk)
        assertEquals("phuong_spa", tk!!.username)
        assertEquals("Phuong!2026", tk.matKhauHienThi)
        assertEquals(hashPassword("Phuong!2026"), tk.matKhauHash)
    }

    /**
     * Kiểm thử khắc phục lỗi nuốt tiền tố mật khẩu bắt đầu bằng lam, thanh (Finding 2B từ Challenger):
     * Khẩu lệnh: "Thêm nhân viên Tú tài khoản tu_ktv mật khẩu lam123"
     * phải trích xuất chính xác password = "lam123" (không bị cắt thành "m123")
     * và username = "tu_ktv".
     */
    @Test
    fun testAiGehihiNlpExtractsPasswordPrefixWithoutLoss() {
        val prompt = "Thêm nhân viên Tú tài khoản tu_ktv mật khẩu lam123"
        val res = store.traLoiThongMinhNoiBo(prompt)
        assertNotNull(res)
        assertTrue("Lệnh tạo nhân viên cần yêu cầu xác nhận", store.pendingAiConfirmation != null)

        store.xacNhanThucThiLenhGehihi()

        val tk = store.taiKhoans.firstOrNull { it.username == "tu_ktv" }
        assertNotNull("Tài khoản 'tu_ktv' phải được tạo thành công", tk)
        assertEquals("tu_ktv", tk!!.username)
        assertEquals("lam123", tk.matKhauHienThi)
        assertEquals(hashPassword("lam123"), tk.matKhauHash)
    }

    /**
     * Kiểm thử bảo toàn dấu chấm (.) và dấu gạch ngang (-) trong tên đăng nhập (Finding 3 từ Challenger):
     * normalizePhoneDigits và store.themNhanVien phải giữ nguyên các chuỗi username dạng "spa.thaole" và "user-123".
     */
    @Test
    fun testThemNhanVienPreservesDotAndHyphenUsernames() {
        assertEquals("spa.thaole", normalizePhoneDigits("spa.thaole"))
        assertEquals("user-123", normalizePhoneDigits("user-123"))

        val nv1 = store.themNhanVien(
            ten = "Thảo Lê",
            gt = "Nữ",
            sdt = "spa.thaole",
            gc = "KTV Trị liệu Da",
            matKhauKhoiTao = "Thao@Pass1"
        )
        assertNotNull("Nhân viên Thảo Lê phải được tạo", nv1)
        val tk1 = store.taiKhoans.firstOrNull { it.nvId == nv1!!.id }
        assertNotNull("Tài khoản cho nv1 phải được tạo", tk1)
        assertEquals("Tên đăng nhập phải giữ nguyên dấu chấm", "spa.thaole", tk1!!.username)
        assertEquals("Thao@Pass1", tk1.matKhauHienThi)

        val nv2 = store.themNhanVien(
            ten = "Tú Trần",
            gt = "Nam",
            sdt = "user-123",
            gc = "KTV Body",
            matKhauKhoiTao = "Tu#Pass2"
        )
        assertNotNull("Nhân viên Tú Trần phải được tạo", nv2)
        val tk2 = store.taiKhoans.firstOrNull { it.nvId == nv2!!.id }
        assertNotNull("Tài khoản cho nv2 phải được tạo", tk2)
        assertEquals("Tên đăng nhập phải giữ nguyên dấu gạch ngang", "user-123", tk2!!.username)
        assertEquals("Tu#Pass2", tk2.matKhauHienThi)
    }
}
