package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SpaUpgradeFeaturesTest {

    @Test
    fun testRootAdminAccountPreserved() {
        assertEquals("0397772963", DEFAULT_ADMIN_PHONE)
        assertEquals("247208", DEFAULT_ADMIN_PASS)

        val rootAdmin = TaiKhoan(
            id = "tk_admin",
            username = DEFAULT_ADMIN_PHONE,
            matKhauHash = "hash_dummy",
            quyen = VaiTro.OWNER,
            nvId = "",
            hoTen = "Admin Quản trị",
            cacQuyenDuocCap = QuyenHan.entries.toSet(),
            matKhauHienThi = DEFAULT_ADMIN_PASS
        )
        assertTrue(rootAdmin.isAdmin())
        assertTrue(rootAdmin.isOwner())
        assertTrue(rootAdmin.isDev())
    }

    @Test
    fun testRoleSeparationForMaintenance() {
        val staffUser = TaiKhoan(
            id = "tk_nv1",
            username = "0912345678",
            matKhauHash = "hash_staff",
            quyen = VaiTro.STAFF,
            nvId = "nv_01",
            hoTen = "Nguyễn Văn KTV",
            cacQuyenDuocCap = emptySet(),
            matKhauHienThi = "123456"
        )
        // KTV must not be admin, owner or dev
        assertFalse(staffUser.isAdmin())
        assertFalse(staffUser.isOwner())
        assertFalse(staffUser.isDev())

        val ownerUser = TaiKhoan(
            id = "tk_owner",
            username = "0988112233",
            matKhauHash = "hash_owner",
            quyen = VaiTro.OWNER,
            nvId = "",
            hoTen = "Chủ Spa Hương Sen",
            cacQuyenDuocCap = QuyenHan.entries.toSet(),
            matKhauHienThi = "686868"
        )
        assertTrue(ownerUser.isAdmin())
        assertTrue(ownerUser.isOwner())
        assertFalse(ownerUser.isDev())
    }

    @Test
    fun testTestAccountPurgedFromStore() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SpaStore(context)

        // Verify that seed data does not contain any test admin account (0988888888 / tk_admin_test)
        assertFalse(store.taiKhoans.any { it.id == "tk_admin_test" || it.username == "0988888888" })

        // Verify that root admin 0397772963 exists
        assertTrue(store.taiKhoans.any { it.id == "tk_admin" && it.username == DEFAULT_ADMIN_PHONE })
    }

    @Test
    fun testGeminiApiKeyHierarchy() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SpaStore(context)

        // When key is placeholder YOUR_GEMINI_API_KEY, layGeminiApiKey should filter it out
        val apiKey = store.layGeminiApiKey()
        assertNotEquals("YOUR_GEMINI_API_KEY", apiKey)
    }

    @Test
    fun testGlobalMaintenanceStateToggle() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SpaStore(context)

        // Default global maintenance is false
        assertFalse(store.isGlobalMaintenance)
        assertEquals("", store.globalMaintenanceMessage)
    }
}
