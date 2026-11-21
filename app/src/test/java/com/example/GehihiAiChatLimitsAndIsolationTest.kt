package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GehihiAiChatLimitsAndIsolationTest {

    private lateinit var context: Context
    private lateinit var store: SpaStore

    private val rootAdmin = TaiKhoan(
        id = "tk_admin",
        username = DEFAULT_ADMIN_PHONE,
        matKhauHash = hashPassword(DEFAULT_ADMIN_PASS),
        quyen = VaiTro.OWNER,
        nvId = "",
        hoTen = "Chủ Cơ Sở (Admin Gốc)",
        cacQuyenDuocCap = QuyenHan.entries.toSet(),
        matKhauHienThi = DEFAULT_ADMIN_PASS
    )

    private val staffUser = TaiKhoan(
        id = "tk_staff_1",
        username = "0911223344",
        matKhauHash = hashPassword("123456"),
        quyen = VaiTro.STAFF,
        nvId = "nv_01",
        hoTen = "Nguyễn Văn KTV",
        cacQuyenDuocCap = emptySet(),
        matKhauHienThi = "123456"
    )

    private val secondAdmin = TaiKhoan(
        id = "tk_staff_promoted",
        username = "0988776655",
        matKhauHash = hashPassword("654321"),
        quyen = VaiTro.OWNER,
        nvId = "nv_02",
        hoTen = "Trần Thị Quản Lý",
        cacQuyenDuocCap = QuyenHan.entries.toSet(),
        matKhauHienThi = "654321"
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences(SpaStore.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        context.filesDir.listFiles()?.forEach { it.delete() }
        store = SpaStore(context)
        store.setCurrentUserAndReloadChat(rootAdmin)
    }

    // =========================================================================
    // 1. KIỂM THỬ PHÂN QUYỀN TRUY CẬP AI GEHIHI (ADMIN VS NON-ADMIN)
    // =========================================================================

    @Test
    fun testAdminPermissionChecks_onlyAdminsHaveAccess() {
        // Admin gốc có quyền Admin
        assertTrue("Admin gốc phải có quyền Admin", rootAdmin.isAdmin())

        // Nhân viên thường KHÔNG có quyền Admin
        assertFalse("KTV thường không được có quyền Admin", staffUser.isAdmin())

        // Nhân viên được cấp ngang quyền Admin có quyền Admin
        assertTrue("Nhân viên được gạt công tắc Admin phải có quyền Admin", secondAdmin.isAdmin())
    }

    @Test
    fun testNonAdminStaff_aiExecutionBlockedInStore() {
        // Đăng nhập với tư cách nhân viên thường
        store.setCurrentUserAndReloadChat(staffUser)
        assertFalse(store.currentUser.isAdmin())

        // Thử gửi lệnh phân tích hành động quản trị qua gehihi
        val result = store.phanTichPhanHoiGeminiVaThucThi(
            rawText = "Tôi sẽ giúp bạn. [ACTION:ADD_STAFF|ten=Test|sdt=0912345678|tk=0912345678|gt=Nam|mk=123456|email=]",
            originalPrompt = "Thêm nhân viên"
        )

        // Không được phép tạo lệnh chờ xác nhận thực thi
        assertNotNull(result)
        assertTrue(
            "Nhân viên thường không được kích hoạt thẻ ACTION",
            store.pendingAiConfirmation == null
        )
    }

    @Test
    fun testNonAdminStaff_guiYeuCauAiBotRejected() = kotlinx.coroutines.runBlocking {
        store.setCurrentUserAndReloadChat(staffUser)
        assertFalse(store.currentUser.isAdmin())

        store.guiYeuCauAiBot(userPrompt = "Xóa toàn bộ vé đang làm")

        // Store phải từ chối ngay và không tạo lệnh chờ xác nhận
        assertTrue(store.pendingAiConfirmation == null)
        assertTrue(store.aiMessages.any { it.actionNote == "Từ chối quyền truy cập AI" })
    }

    // =========================================================================
    // 2. KIỂM THỬ CÔ LẬP LỊCH SỬ CHAT THEO TÀI KHOẢN (ACCOUNT ISOLATION)
    // =========================================================================

    @Test
    fun testAccountIsolation_messagesNotSharedBetweenDifferentAccounts() {
        // 1. Admin 1 đăng nhập và gửi tin nhắn
        store.setCurrentUserAndReloadChat(rootAdmin)
        store.themTinNhanAiKiemThu(isUser = true, text = "Tin nhắn mật của Admin 1: Doanh thu tháng này thế nào?")
        store.themTinNhanAiKiemThu(isUser = false, text = "Doanh thu tháng này đạt 45 triệu đồng.")

        assertTrue(store.aiMessages.any { it.text.contains("Tin nhắn mật của Admin 1") })

        // 2. Chuyển sang Admin 2
        store.setCurrentUserAndReloadChat(secondAdmin)

        // Admin 2 TUYỆT ĐỐI KHÔNG được nhìn thấy tin nhắn của Admin 1
        assertFalse(
            "Admin 2 không được nhìn thấy tin nhắn của Admin 1",
            store.aiMessages.any { it.text.contains("Tin nhắn mật của Admin 1") }
        )

        // 3. Admin 2 gửi tin nhắn riêng
        store.themTinNhanAiKiemThu(isUser = true, text = "Admin 2 hỏi: Lịch làm việc ngày mai?")
        assertTrue(store.aiMessages.any { it.text.contains("Admin 2 hỏi: Lịch làm việc ngày mai?") })

        // 4. Quay trở lại Admin 1
        store.setCurrentUserAndReloadChat(rootAdmin)

        // Admin 1 vẫn thấy tin nhắn của mình và KHÔNG thấy tin nhắn của Admin 2
        assertTrue(
            "Admin 1 phải thấy lại tin nhắn cũ của mình",
            store.aiMessages.any { it.text.contains("Tin nhắn mật của Admin 1") }
        )
        assertFalse(
            "Admin 1 không được nhìn thấy tin nhắn của Admin 2",
            store.aiMessages.any { it.text.contains("Admin 2 hỏi: Lịch làm việc ngày mai?") }
        )
    }

    @Test
    fun testAccountIsolation_persistedVaultRestoresPerAccountCorrectly() {
        // 1. Tạo tin nhắn cho Admin 1
        store.setCurrentUserAndReloadChat(rootAdmin)
        store.themTinNhanAiKiemThu(isUser = true, text = "Admin 1 lưu kế hoạch tháng 12")
        store.saveGehihiMemoryVault()

        // 2. Tạo tin nhắn cho Admin 2
        store.setCurrentUserAndReloadChat(secondAdmin)
        store.themTinNhanAiKiemThu(isUser = true, text = "Admin 2 lưu kế hoạch khuyến mãi")
        store.saveGehihiMemoryVault()

        // 3. Giả lập khởi động lại Store mới từ SharedPreferences/File
        val newStoreInstance = SpaStore(context)
        newStoreInstance.setCurrentUserAndReloadChat(rootAdmin)

        // Phải khôi phục đúng tin nhắn của Admin 1
        assertTrue(newStoreInstance.aiMessages.any { it.text.contains("Admin 1 lưu kế hoạch tháng 12") })
        assertFalse(newStoreInstance.aiMessages.any { it.text.contains("Admin 2 lưu kế hoạch khuyến mãi") })

        // Đổi sang Admin 2 trên Store mới
        newStoreInstance.setCurrentUserAndReloadChat(secondAdmin)
        assertTrue(newStoreInstance.aiMessages.any { it.text.contains("Admin 2 lưu kế hoạch khuyến mãi") })
        assertFalse(newStoreInstance.aiMessages.any { it.text.contains("Admin 1 lưu kế hoạch tháng 12") })
    }

    // =========================================================================
    // 3. KIỂM THỬ GIỚI HẠN TIN NHẮN & CƠ CHẾ XOAY VÒNG (ROLLING WINDOW TRIMMING)
    // =========================================================================

    @Test
    fun testChatLimit_rollingWindowTrimsOldestWhenLimitExceeded() {
        store.setCurrentUserAndReloadChat(rootAdmin)
        store.aiMessages.clear()

        val maxLimit = SpaStore.MAX_AI_MESSAGES_PER_ACCOUNT

        // 1. Thêm đúng số lượng tối đa
        for (i in 1..maxLimit) {
            store.themTinNhanAiKiemThu(isUser = (i % 2 == 1), text = "Tin nhắn thứ $i")
        }
        assertEquals(maxLimit, store.aiMessages.size)
        assertEquals("Tin nhắn thứ 1", store.aiMessages.first().text)
        assertEquals("Tin nhắn thứ $maxLimit", store.aiMessages.last().text)

        // 2. Thêm tin nhắn thứ N+1 -> Tin cũ nhất phải bị cắt tỉa (Rolling Window)
        val overflowMsg = "Tin nhắn vượt ngưỡng ${maxLimit + 1}"
        store.themTinNhanAiKiemThu(isUser = true, text = overflowMsg)

        // Kích thước danh sách không được vượt quá maxLimit
        assertEquals(maxLimit, store.aiMessages.size)

        // Tin nhắn thứ 1 đã bị xóa, tin đầu tiên giờ là tin thứ 2
        assertFalse("Tin nhắn cũ nhất (thứ 1) phải bị xóa", store.aiMessages.any { it.text == "Tin nhắn thứ 1" })
        assertEquals("Tin nhắn thứ 2", store.aiMessages.first().text)
        assertEquals(overflowMsg, store.aiMessages.last().text)
    }

    @Test
    fun testChatLimit_noCrashOnRapidMessageBurst() {
        store.setCurrentUserAndReloadChat(rootAdmin)
        store.aiMessages.clear()

        val maxLimit = SpaStore.MAX_AI_MESSAGES_PER_ACCOUNT

        // Bắn dồn dập 250 tin nhắn liên tục
        for (i in 1..250) {
            store.themTinNhanAiKiemThu(isUser = true, text = "Burst message #$i")
        }

        // Đảm bảo không văng app và luôn giữ đúng maxLimit tin nhắn mới nhất
        assertEquals(maxLimit, store.aiMessages.size)
        assertEquals("Burst message #${250 - maxLimit + 1}", store.aiMessages.first().text)
        assertEquals("Burst message #250", store.aiMessages.last().text)
    }

    // =========================================================================
    // 4. KIỂM THỬ NÚT LÀM MỚI / BẮT ĐẦU ĐOẠN CHAT MỚI (CLEAR / NEW CHAT)
    // =========================================================================

    @Test
    fun testClearNewChat_clearsOnlyActiveAccountAndPreservesWelcome() {
        // Thiết lập tin nhắn cho cả 2 tài khoản
        store.setCurrentUserAndReloadChat(rootAdmin)
        store.themTinNhanAiKiemThu(isUser = true, text = "Nội dung cần xóa của Admin 1")

        store.setCurrentUserAndReloadChat(secondAdmin)
        store.themTinNhanAiKiemThu(isUser = true, text = "Nội dung quan trọng của Admin 2")

        // Quay lại Admin 1 và bấm Làm mới / Bắt đầu đoạn chat mới
        store.setCurrentUserAndReloadChat(rootAdmin)
        store.xoaLichSuTroChuyenGehihi()

        // Admin 1 đã sạch tin nhắn cũ, chỉ còn tin chào mừng mới
        assertFalse(store.aiMessages.any { it.text.contains("Nội dung cần xóa của Admin 1") })
        assertTrue("Phải có tin chào mừng sau khi làm mới", store.aiMessages.any { !it.isUser })

        // Admin 2 hoàn toàn không bị ảnh hưởng!
        store.setCurrentUserAndReloadChat(secondAdmin)
        assertTrue(
            "Tin nhắn của Admin 2 vẫn phải còn nguyên vẹn",
            store.aiMessages.any { it.text.contains("Nội dung quan trọng của Admin 2") }
        )
    }

    // =========================================================================
    // 5. KIỂM THỬ NÂNG CẤP QUYỀN NHÂN VIÊN LÊN ADMIN & DI CHUYỂN DỮ LIỆU CŨ (MIGRATION)
    // =========================================================================

    @Test
    fun testPromotingStaffToAdmin_enablesAiChatAccess() {
        // 1. Tạo nhân viên thường
        val nv = store.themNhanVien(
            ten = "Lê Thị Thảo",
            gt = "Nữ",
            sdt = "0977889900",
            gc = "KTV Trị liệu",
            matKhauKhoiTao = "thao123",
            laAdmin = false
        )
        assertNotNull(nv)
        val tk = store.taiKhoans.firstOrNull { it.nvId == nv!!.id }
        assertNotNull(tk)
        assertFalse("Ban đầu là nhân viên thường", tk!!.isAdmin())

        // Đăng nhập tài khoản nhân viên thường -> Không có quyền AI, không có tin nhắn
        store.setCurrentUserAndReloadChat(tk)
        assertFalse(store.currentUser.isAdmin())
        assertEquals(0, store.aiMessages.size)

        // 2. Chủ Spa gạt công tắc cấp quyền Admin
        store.currentUser = rootAdmin
        val toggleSuccess = store.toggleMacAdminChoTaiKhoan(tk.id, true)
        assertTrue(toggleSuccess)

        val promotedTk = store.taiKhoans.first { it.id == tk.id }
        assertTrue("Sau khi cấp quyền phải là Admin", promotedTk.isAdmin())

        // 3. Đăng nhập tài khoản vừa được cấp quyền Admin -> Kích hoạt AI gehihi với tin nhắn chào mừng riêng
        store.setCurrentUserAndReloadChat(promotedTk)
        assertTrue(store.currentUser.isAdmin())
        assertTrue("Admin mới phải có tin nhắn chào mừng cá nhân", store.aiMessages.isNotEmpty())
        assertTrue(store.aiMessages.first().text.contains("Lê Thị Thảo"))

        // Gửi lệnh AI thành công
        store.themTinNhanAiKiemThu(isUser = true, text = "Tôi đã được cấp quyền Admin!")
        assertTrue(store.aiMessages.any { it.text == "Tôi đã được cấp quyền Admin!" })
    }

    @Test
    fun testLegacyChatHistoryMigration_migratesToRootAdmin() {
        val prefs = context.getSharedPreferences(SpaStore.PREFS_NAME, Context.MODE_PRIVATE)

        // 1. Giả lập file gehihi_memory_vault.json cũ có lịch sử chat dùng chung
        val legacyVaultFile = File(context.filesDir, SpaStore.GEHIHI_VAULT_FILE_NAME)
        val legacyJson = JSONObject().apply {
            put("updatedAt", "2026-01-01 10:00:00")
            put("aiMessages", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "legacy_01")
                    put("isUser", true)
                    put("text", "Lệnh cũ của Admin trước khi nâng cấp hệ thống")
                    put("actionNote", "Lệnh legacy")
                    put("thoiGian", "2026-01-01 10:00:00")
                })
            })
            put("aiLongTermMemories", JSONArray().apply {
                put("Ghi nhớ tri thức dài hạn cũ")
            })
        }.toString()

        legacyVaultFile.writeText(legacyJson, Charsets.UTF_8)
        prefs.edit().putString(SpaStore.KEY_GEHIHI_MEMORY_JSON, legacyJson).apply()

        // Xóa cờ migration để kiểm thử
        prefs.edit().putBoolean(SpaStore.PREF_KEY_GEHIHI_MIGRATED, false).apply()

        // Xóa file chat riêng của rootAdmin nếu có từ trước
        val rootKey = store.getAccountChatKey(rootAdmin)
        File(context.filesDir, "gehihi_vault_${rootKey}.json").delete()
        prefs.edit().remove("${SpaStore.KEY_GEHIHI_CHAT_ACCOUNT_PREFIX}$rootKey").apply()
        store.accountChatCache.remove(rootKey)

        // 2. Chạy migration
        store.migrateGehihiChatHistoryIfNeeded()

        // 3. Tải chat cho rootAdmin
        store.setCurrentUserAndReloadChat(rootAdmin)

        // Phải tìm thấy tin nhắn legacy được di chuyển thành công
        assertTrue(
            "Tin nhắn cũ phải được chuyển giao vào tài khoản của Root Admin",
            store.aiMessages.any { it.text.contains("Lệnh cũ của Admin trước khi nâng cấp hệ thống") }
        )
        assertEquals(
            rootAdmin.id,
            store.aiMessages.first { it.text.contains("Lệnh cũ của Admin trước khi nâng cấp hệ thống") }.accountId
        )
    }

    // =========================================================================
    // 6. KIỂM THỬ BẢO MẬT LỆNH CHỜ XÁC NHẬN & PHÂN QUYỀN GỌI LOAD CHAT
    // =========================================================================

    @Test
    fun testPendingAiConfirmation_invalidatedOnAccountSwitchAndLogout() {
        // 1. Admin đăng nhập và kích hoạt lệnh chờ xác nhận xóa vé
        store.setCurrentUserAndReloadChat(rootAdmin)
        store.phanTichPhanHoiGeminiVaThucThi(
            rawText = "Đang chuẩn bị xóa vé [ACTION:DELETE_TICKETS|scope=ALL]",
            originalPrompt = "Xóa toàn bộ vé"
        )
        assertNotNull("Admin phải tạo được lệnh chờ xác nhận", store.pendingAiConfirmation)

        // 2. Chuyển sang tài khoản KTV thường -> pendingAiConfirmation phải bị vô hiệu hóa (null)
        store.switchChatAccount(staffUser)
        assertFalse(store.currentUser.isAdmin())
        assertTrue("Lệnh chờ phải bị xóa khi đổi tài khoản sang KTV", store.pendingAiConfirmation == null)

        // 3. Admin đăng nhập lại và tạo lệnh chờ xác nhận mới
        store.setCurrentUserAndReloadChat(rootAdmin)
        store.phanTichPhanHoiGeminiVaThucThi(
            rawText = "Đang chuẩn bị xóa nhân viên [ACTION:DELETE_STAFF|target=ALL]",
            originalPrompt = "Xóa toàn bộ nhân viên"
        )
        assertNotNull("Admin tạo lệnh chờ mới", store.pendingAiConfirmation)

        // 4. Admin đăng xuất -> pendingAiConfirmation phải bị vô hiệu hóa (null)
        store.logout()
        assertTrue("Lệnh chờ phải bị xóa khi đăng xuất", store.pendingAiConfirmation == null)
    }

    @Test
    fun testNonAdminCannotExecutePendingAiConfirmation() {
        // 1. Admin tạo một lệnh chờ xác nhận hành động nguy hiểm
        store.setCurrentUserAndReloadChat(rootAdmin)
        var actionExecuted = false
        store.pendingAiConfirmation = LenhChoXacNhanGehihi(
            id = "pending_test_01",
            loai = LoaiLenhChoXacNhan.XOA,
            tieuDeTomTat = "Xóa toàn bộ vé",
            chiTietXacNhan = "Kiểm tra quyền thực thi lệnh xác nhận",
            thucThi = {
                actionExecuted = true
                KetQuaAiBot(cauTraLoi = "Đã thực thi trái phép", hanhDongThucThi = "Nguy hiểm")
            }
        )
        assertNotNull(store.pendingAiConfirmation)

        // 2. Giả lập một tài khoản non-admin hoặc currentUser = staffUser cố gọi xacNhanThucThiLenhGehihi
        store.currentUser = staffUser
        val result = store.xacNhanThucThiLenhGehihi()

        // 3. Kết quả phải là null, block không được chạy, pendingAiConfirmation bị hủy
        assertTrue("Non-admin xác nhận phải trả về null", result == null)
        assertFalse("Block thực thi tuyệt đối không được kích hoạt", actionExecuted)
        assertTrue("pendingAiConfirmation phải bị dọn dẹp", store.pendingAiConfirmation == null)
    }

    @Test
    fun testNonAdminCallingLoadChatForAccount_rejectedAndCleared() {
        // 1. Admin nạp chat và có tin nhắn riêng tư
        store.setCurrentUserAndReloadChat(rootAdmin)
        store.themTinNhanAiKiemThu(isUser = true, text = "Báo cáo tài chính tuyệt mật của Chủ cơ sở")
        assertTrue(store.aiMessages.any { it.text.contains("tuyệt mật") })

        // 2. Chuyển sang tài khoản KTV thường
        store.setCurrentUserAndReloadChat(staffUser)
        assertEquals(0, store.aiMessages.size)

        // 3. KTV hoặc mã độc cố tình gọi trực tiếp loadChatForAccount(rootAdmin)
        store.loadChatForAccount(rootAdmin)

        // Phải bị từ chối và aiMessages bị dọn sạch (clear)
        assertEquals("Tin nhắn của Admin không được nạp khi caller là non-admin", 0, store.aiMessages.size)
        assertTrue("activeChatAccountKey phải là null khi bị từ chối", store.activeChatAccountKey == null)
    }
}
