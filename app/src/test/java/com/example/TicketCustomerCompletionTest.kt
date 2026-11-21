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

/**
 * Robolectric unit tests for Milestone 1:
 * Ticket Customer Saving Options & Customer Service History
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TicketCustomerCompletionTest {

    private lateinit var context: Context
    private lateinit var store: SpaStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        store = SpaStore(context)

        // Set current user as owner/admin with full privileges
        store.currentUser = store.taiKhoans.firstOrNull { it.isOwner() } ?: store.taiKhoans.firstOrNull()

        // Ensure at least one staff member exists
        if (store.nvs.isEmpty()) {
            store.themNhanVien(ten = "KTV Thử Nghiệm", gt = "Nam", sdt = "0911223344", gc = "")
        }
        // Ensure at least one service exists
        if (store.dvs.isEmpty()) {
            store.luuDichVu(id = null, ten = "Massage Body 60p", phut = 60, gia = 150000L, moTa = "Thư giãn toàn thân", chietKhauVnd = 60000L)
        }
    }

    @Test
    fun testTicketClosedWithoutSavingCustomerLeavesCustomersUntouched() {
        val initialKhCount = store.khs.size
        val nv = store.nvs.first()
        val dv = store.dvs.first()

        // 1. Open ticket for a walk-in customer without creating a record in store.khs upfront
        val ve = store.moVeMoi(
            khId = "",
            nvId = nv.id,
            selectedDvIds = listOf(dv.id),
            tgbd = "09:00",
            thuTuyChinh = 150000L,
            khTen = "Khách Vãng Lai Không Lưu",
            khSdt = "0981999888"
        )
        assertNotNull("Vé phải được tạo thành công", ve)
        assertEquals("Khách Vãng Lai Không Lưu", ve!!.khTen)
        assertEquals("0981999888", ve.khSdt)
        assertTrue(ve.dangLam)
        assertFalse(ve.daXacNhan)
        assertEquals(initialKhCount, store.khs.size)

        // 2. Complete ticket with luuKhach = false
        store.hoanThanhVe(
            luotId = ve.id,
            tgktInput = "10:00",
            thucThu = 150000L,
            luuKhach = false
        )
        val veChoChot = store.luots.first { it.id == ve.id }
        assertTrue(veChoChot.choChot)
        assertEquals(initialKhCount, store.khs.size)

        // 3. Confirm / Close ticket with luuKhach = false
        val resultKh = store.xacNhanChotVe(
            luotId = ve.id,
            luuKhach = false
        )

        // Verify:
        // - Result customer is null
        assertEquals(null, resultKh)
        // - store.khs remains completely untouched
        assertEquals(initialKhCount, store.khs.size)
        assertFalse(store.khs.any { it.sdt == "0981999888" || it.ten == "Khách Vãng Lai Không Lưu" })

        // - Ticket status is confirmed and revenue/commissions recorded
        val veDaChot = store.luots.first { it.id == ve.id }
        assertTrue(veDaChot.daXacNhan)
        assertTrue(veDaChot.daHoanTat)
        assertEquals(150000L, veDaChot.thu)
        assertNotNull(veDaChot.hoaHongChot)
        assertTrue(veDaChot.hoaHongChot!! > 0L)

        // - Revenue transaction recorded in cashbook
        assertTrue(store.tcs.any { it.id == veDaChot.phieuThuVeId && it.tien == 150000L })
    }

    @Test
    fun testTicketClosedWithSavingCustomerAddsAndLinksCustomer() {
        val initialKhCount = store.khs.size
        val nv = store.nvs.first()
        val dv = store.dvs.first()

        // 1. Open ticket for a walk-in guest who later wants to be saved
        val ve = store.moVeMoi(
            khId = "",
            nvId = nv.id,
            selectedDvIds = listOf(dv.id),
            tgbd = "14:00",
            thuTuyChinh = 200000L,
            khTen = "Chị Lan Hương",
            khSdt = "0912345678"
        )
        assertNotNull(ve)
        assertEquals(initialKhCount, store.khs.size)

        // 2. Confirm and close ticket with luuKhach = true
        val savedKh = store.xacNhanChotVe(
            luotId = ve!!.id,
            luuKhach = true,
            tenKhachMoi = "Chị Lan Hương",
            sdtKhachMoi = "0912345678"
        )

        // Verify:
        // - Customer record created in store.khs
        assertNotNull(savedKh)
        assertEquals("Chị Lan Hương", savedKh!!.ten)
        assertEquals("0912345678", savedKh.sdt)
        assertEquals(initialKhCount + 1, store.khs.size)
        assertTrue(store.khs.any { it.id == savedKh.id && it.ten == "Chị Lan Hương" })

        // - Ticket is confirmed and linked to savedKh.id
        val veDaChot = store.luots.first { it.id == ve.id }
        assertTrue(veDaChot.daXacNhan)
        assertEquals(savedKh.id, veDaChot.khId)
        assertEquals("Chị Lan Hương", veDaChot.khTen)
        assertEquals("0912345678", veDaChot.khSdt)
    }

    @Test
    fun testHoanThanhVeWithLuuKhachOptionCreatesCustomerDuringCompletion() {
        val initialKhCount = store.khs.size
        val nv = store.nvs.first()
        val dv = store.dvs.first()

        val ve = store.moVeMoi(
            khId = "",
            nvId = nv.id,
            selectedDvIds = listOf(dv.id),
            tgbd = "11:00",
            thuTuyChinh = 180000L,
            khTen = "Anh Đức VIP",
            khSdt = "0988776655"
        )
        assertNotNull(ve)

        // Complete ticket with luuKhach = true
        store.hoanThanhVe(
            luotId = ve!!.id,
            tgktInput = "12:00",
            thucThu = 180000L,
            luuKhach = true,
            tenKhachMoi = "Anh Đức VIP",
            sdtKhachMoi = "0988776655"
        )

        // Verify customer was created and ticket linked
        assertEquals(initialKhCount + 1, store.khs.size)
        val createdKh = store.khs.first { it.sdt == "0988776655" }
        assertEquals("Anh Đức VIP", createdKh.ten)

        val updatedVe = store.luots.first { it.id == ve.id }
        assertEquals(createdKh.id, updatedVe.khId)
        assertTrue(updatedVe.choChot)
    }

    @Test
    fun testCustomerServiceHistoryQueryAccurateAndSortedDescending() {
        val nv = store.nvs.first()
        val dv = store.dvs.first()

        // Create a customer directly
        val kh = store.themKhachHang("Bác Hoàng Lịch Sử", "0905123456")
        assertNotNull(kh)

        // Add 2 tickets on different dates/times for this customer
        val ve1 = store.moVeMoi(
            khId = kh!!.id,
            nvId = nv.id,
            selectedDvIds = listOf(dv.id),
            tgbd = "08:30",
            thuTuyChinh = 150000L
        )
        assertNotNull(ve1)
        val idx1 = store.luots.indexOfFirst { it.id == ve1!!.id }
        store.luots[idx1] = store.luots[idx1].copy(ngay = "2026-11-18", tgkt = "09:30", daXacNhan = true)

        val ve2 = store.moVeMoi(
            khId = kh.id,
            nvId = nv.id,
            selectedDvIds = listOf(dv.id),
            tgbd = "15:00",
            thuTuyChinh = 150000L
        )
        assertNotNull(ve2)
        val idx2 = store.luots.indexOfFirst { it.id == ve2!!.id }
        store.luots[idx2] = store.luots[idx2].copy(ngay = "2026-11-20", tgkt = "16:00", daXacNhan = true)

        // Query customer history
        val lichSu = store.layLichSuDichVuCuaKhach(kh.id, kh.sdt)

        // Verify accurate count and sorting: newest (2026-11-20) first, oldest (2026-11-18) second
        assertEquals(2, lichSu.size)
        assertEquals(ve2!!.id, lichSu[0].id)
        assertEquals("2026-11-20", lichSu[0].ngay)
        assertEquals("15:00", lichSu[0].tgbd)
        assertEquals("16:00", lichSu[0].tgkt)

        assertEquals(ve1!!.id, lichSu[1].id)
        assertEquals("2026-11-18", lichSu[1].ngay)

        // Verify service name display helper
        val tenDv = store.tenDichVuCuaVe(lichSu[0])
        assertTrue("Tên dịch vụ phải chứa tên dịch vụ đã chọn", tenDv.contains(dv.ten))
    }

    @Test
    fun testDataPersistenceSurvivesJsonBackupAndRestore() {
        val nv = store.nvs.first()
        val dv = store.dvs.first()

        // 1. Create a ticket and confirm with luuKhach = true
        val ve = store.moVeMoi(
            khId = "",
            nvId = nv.id,
            selectedDvIds = listOf(dv.id),
            tgbd = "13:15",
            thuTuyChinh = 170000L,
            khTen = "Anh Vũ JSON Test",
            khSdt = "0933445566"
        )
        assertNotNull(ve)

        val savedKh = store.xacNhanChotVe(
            luotId = ve!!.id,
            luuKhach = true,
            tenKhachMoi = "Anh Vũ JSON Test",
            sdtKhachMoi = "0933445566"
        )
        assertNotNull(savedKh)

        // 2. Export to JSON backup
        val backupObj = store.buildBackupJsonObject()
        val backupString = backupObj.toString()

        assertTrue(backupString.contains("Anh Vũ JSON Test"))
        assertTrue(backupString.contains("0933445566"))
        assertTrue(backupString.contains("khTen"))
        assertTrue(backupString.contains("khSdt"))

        // 3. Restore into a new SpaStore instance
        val storeRestored = SpaStore(context)
        storeRestored.parseDataFromJson(backupString)

        // 4. Verify customer exists in restored store
        val restoredKh = storeRestored.khs.firstOrNull { it.id == savedKh!!.id }
        assertNotNull(restoredKh)
        assertEquals("Anh Vũ JSON Test", restoredKh?.ten)
        assertEquals("0933445566", restoredKh?.sdt)

        // Verify ticket exists with khTen, khSdt and is confirmed
        val restoredVe = storeRestored.luots.firstOrNull { it.id == ve.id }
        assertNotNull(restoredVe)
        assertTrue(restoredVe!!.daXacNhan)
        assertEquals(savedKh!!.id, restoredVe.khId)
        assertEquals("Anh Vũ JSON Test", restoredVe.khTen)
        assertEquals("0933445566", restoredVe.khSdt)

        // Verify history query works on restored store
        val lichSuRestored = storeRestored.layLichSuDichVuCuaKhach(restoredKh!!.id, restoredKh.sdt)
        assertEquals(1, lichSuRestored.size)
        assertEquals(ve.id, lichSuRestored[0].id)
    }
}
