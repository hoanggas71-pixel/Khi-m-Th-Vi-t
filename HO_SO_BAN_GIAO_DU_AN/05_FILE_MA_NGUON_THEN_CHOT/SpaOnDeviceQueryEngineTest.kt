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
class SpaOnDeviceQueryEngineTest {

    private lateinit var store: SpaStore
    private val refDate = "2026-11-19" // Thứ Năm

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        store = SpaStore(context)
        // Cấu hình tài khoản Admin cho store
        store.currentUser = TaiKhoan(
            id = "tk_admin",
            username = DEFAULT_ADMIN_PHONE,
            matKhauHash = "hash_admin",
            quyen = VaiTro.OWNER,
            nvId = "",
            hoTen = "Admin Quản trị",
            cacQuyenDuocCap = QuyenHan.entries.toSet(),
            matKhauHienThi = DEFAULT_ADMIN_PASS
        )
    }

    // =========================================================================
    // 1. KIỂM THỬ BÓC TÁCH KHOẢNG THỜI GIAN TIẾNG VIỆT (DATE RANGE PARSER)
    // =========================================================================

    @Test
    fun testParseVietnameseDateRangeAccented() {
        // "10 ngày đầu tháng 10"
        val r1 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("10 ngày đầu tháng 10", refDate)
        assertNotNull(r1)
        assertEquals("2026-10-01", r1!!.startDate)
        assertEquals("2026-10-10", r1.endDate)

        // "5 ngày đầu tháng 9"
        val r2 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("5 ngày đầu tháng 9", refDate)
        assertNotNull(r2)
        assertEquals("2026-09-01", r2!!.startDate)
        assertEquals("2026-09-05", r2.endDate)

        // "giữa tháng 10" (11..20)
        val r3 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("giữa tháng 10", refDate)
        assertNotNull(r3)
        assertEquals("2026-10-11", r3!!.startDate)
        assertEquals("2026-10-20", r3.endDate)

        // "cuối tháng 10" (21..31)
        val r4 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("cuối tháng 10", refDate)
        assertNotNull(r4)
        assertEquals("2026-10-21", r4!!.startDate)
        assertEquals("2026-10-31", r4.endDate)

        // "tháng 9"
        val r5 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("tháng 9", refDate)
        assertNotNull(r5)
        assertEquals("2026-09-01", r5!!.startDate)
        assertEquals("2026-09-30", r5.endDate)

        // "từ 01/10 đến 15/10"
        val r6 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("từ 01/10 đến 15/10", refDate)
        assertNotNull(r6)
        assertEquals("2026-10-01", r6!!.startDate)
        assertEquals("2026-10-15", r6.endDate)

        // "hôm nay"
        val r7 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("hôm nay", refDate)
        assertNotNull(r7)
        assertEquals("2026-11-19", r7!!.startDate)
        assertEquals("2026-11-19", r7.endDate)
        assertTrue(r7.isSingleDay)

        // "hôm qua"
        val r8 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("hôm qua", refDate)
        assertNotNull(r8)
        assertEquals("2026-11-18", r8!!.startDate)
        assertEquals("2026-11-18", r8.endDate)
        assertTrue(r8.isSingleDay)

        // "tuần này" (2026-11-19 là thứ Năm -> tuần từ T2 2026-11-16 đến CN 2026-11-22)
        val r9 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("tuần này", refDate)
        assertNotNull(r9)
        assertEquals("2026-11-16", r9!!.startDate)
        assertEquals("2026-11-22", r9.endDate)

        // "tuần trước" (tuần từ T2 2026-11-09 đến CN 2026-11-15)
        val r10 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("tuần trước", refDate)
        assertNotNull(r10)
        assertEquals("2026-11-09", r10!!.startDate)
        assertEquals("2026-11-15", r10.endDate)

        // "tháng này"
        val r11 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("tháng này", refDate)
        assertNotNull(r11)
        assertEquals("2026-11-01", r11!!.startDate)
        assertEquals("2026-11-30", r11.endDate)
    }

    @Test
    fun testParseVietnameseDateRangeUnaccentedAndShorthand() {
        // "10 ngay dau thang 10"
        val r1 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("10 ngay dau thang 10", refDate)
        assertNotNull(r1)
        assertEquals("2026-10-01", r1!!.startDate)
        assertEquals("2026-10-10", r1.endDate)

        // "thang 9"
        val r2 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("thang 9", refDate)
        assertNotNull(r2)
        assertEquals("2026-09-01", r2!!.startDate)
        assertEquals("2026-09-30", r2.endDate)

        // "tuan truoc"
        val r3 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("tuan truoc", refDate)
        assertNotNull(r3)
        assertEquals("2026-11-09", r3!!.startDate)
        assertEquals("2026-11-15", r3.endDate)

        // "tu 01/10 den 15/10"
        val r4 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("tu 01/10 den 15/10", refDate)
        assertNotNull(r4)
        assertEquals("2026-10-01", r4!!.startDate)
        assertEquals("2026-10-15", r4.endDate)

        // "hom qua"
        val r5 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("hom qua", refDate)
        assertNotNull(r5)
        assertEquals("2026-11-18", r5!!.startDate)

        // Shorthand "10 ngay dau t10"
        val r6 = OnDeviceSpaQueryEngine.parseVietnameseDateRange("10 ngay dau t10", refDate)
        assertNotNull(r6)
        assertEquals("2026-10-01", r6!!.startDate)
        assertEquals("2026-10-10", r6.endDate)
    }

    // =========================================================================
    // 2. KIỂM THỬ NGUYÊN TẮC TRUNG THỰC DỮ LIỆU (ZERO-DATA HONESTY)
    // =========================================================================

    @Test
    fun testZeroDataHonestyDoesNotFallBackToToday() {
        // Chuẩn bị dữ liệu hôm nay có vé và có doanh thu
        store.luots.clear()
        store.tcs.clear()
        store.luots.add(
            Luot(
                id = "ve_today",
                khId = "kh_1",
                nvId = "nv_1",
                dvIds = listOf("dv_1"),
                tgbd = "08:00",
                tgkt = "09:00",
                thu = 150000L,
                daXacNhan = true,
                ngay = refDate // 2026-11-19
            )
        )

        // Truy vấn thời gian tháng 9 (hoàn toàn không có vé nào)
        val res = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("Doanh thu tháng 9", store, refDate)
        assertNotNull(res)

        val reply = res!!.cauTraLoi
        // Phải báo trung thực 0 vé, 0 giao dịch, 0đ
        assertTrue(reply.contains("0 vé"))
        assertTrue(reply.contains("0 giao dịch"))
        assertTrue(reply.contains("Tổng Thu: 0đ"))
        assertTrue(reply.contains("Lãi ròng): 0đ"))

        // TUYỆT ĐỐI KHÔNG ĐƯỢC CHỨA ngày hôm nay hoặc doanh thu hôm nay!
        assertFalse(reply.contains(refDate))
        assertFalse(reply.contains("150.000"))
        assertFalse(reply.contains("Báo cáo ca hôm nay"))
    }

    // =========================================================================
    // 3. KIỂM THỬ MODULE 1: DOANH THU, SỔ QUỸ, CHI PHÍ & LÃI RÒNG
    // =========================================================================

    @Test
    fun testRevenueExpensesAndNetProfitComputation() {
        store.luots.clear()
        store.tcs.clear()
        store.dvs.clear()

        // 2 dịch vụ
        store.dvs.add(DV("dv_body", "Massage Body", 60, 200000L, "", 50000L))
        store.dvs.add(DV("dv_foot", "Massage Chân", 60, 150000L, "", 40000L))

        // 2 vé trong khoảng 10 ngày đầu tháng 10
        store.luots.add(
            Luot(
                id = "ve_1",
                khId = "kh_1",
                nvId = "nv_1",
                dvIds = listOf("dv_body"),
                tgbd = "09:00",
                tgkt = "10:00",
                thu = 200000L,
                daXacNhan = true,
                ngay = "2026-10-02"
            )
        )
        store.luots.add(
            Luot(
                id = "ve_2",
                khId = "kh_2",
                nvId = "nv_2",
                dvIds = listOf("dv_foot"),
                tgbd = "14:00",
                tgkt = "15:00",
                thu = 150000L,
                daXacNhan = true,
                ngay = "2026-10-05"
            )
        )

        // 1 khoản thu ngoài lề (bán gói) trong khoảng
        store.tcs.add(
            TC(
                id = "tc_thu_goi",
                loai = LoaiTC.THU,
                tien = 1000000L,
                lyDo = "Khách mua gói 10 buổi",
                ngay = "2026-10-03"
            )
        )

        // 1 khoản chi vận hành ngoài lề (mua khăn, tinh dầu)
        store.tcs.add(
            TC(
                id = "tc_chi_mua",
                loai = LoaiTC.CHI,
                tien = 100000L,
                lyDo = "Mua khăn và tinh dầu",
                ngay = "2026-10-04"
            )
        )

        // Tổng thu = 200k + 150k + 1000k = 1.350.000đ
        // Hoa hồng vé = 50k + 40k = 90k
        // Tổng chi = 90k + 100k = 190.000đ
        // Lãi ròng = 1.350.000 - 190.000 = 1.160.000đ

        val res = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("10 ngày đầu tháng 10 doanh thu thế nào", store, refDate)
        assertNotNull(res)

        val reply = res!!.cauTraLoi
        assertTrue(reply.contains("1.350.000") || reply.contains("1.35tr") || reply.contains("1.350.000đ"))
        assertTrue(reply.contains("190.000") || reply.contains("190k"))
        assertTrue(reply.contains("1.160.000") || reply.contains("1.16tr"))
        assertTrue(reply.contains("2 vé"))
    }

    // =========================================================================
    // 4. KIỂM THỬ MODULE 2: KHÁCH HÀNG & GÓI LIỆU TRÌNH & KHÁCH VIP
    // =========================================================================

    @Test
    fun testCustomerRemainingSessionsAndVisitFrequency() {
        store.khs.clear()
        store.gois.clear()
        store.dks.clear()
        store.luots.clear()

        val khTuan = KH("kh_tuan", "Nguyễn Văn Tuấn", "0912345678")
        store.khs.add(khTuan)

        val goiVg = Goi("goi_vg", "Gói Trị Liệu Vai Gáy", listOf("dv_1"), 10, 1500000L)
        store.gois.add(goiVg)

        // Đăng ký gói 10 buổi, đã dùng 4 buổi -> còn 6 buổi
        store.dks.add(
            DK(
                id = "dk_tuan_1",
                khId = "kh_tuan",
                goiId = "goi_vg",
                ngay = "2026-10-01",
                luotDaDung = 4
            )
        )

        // 3 vé đã chốt trong tháng 11 của khách Tuấn
        repeat(3) { i ->
            store.luots.add(
                Luot(
                    id = "ve_tuan_$i",
                    khId = "kh_tuan",
                    nvId = "nv_1",
                    dvIds = listOf("dv_1"),
                    tgbd = "09:00",
                    tgkt = "10:00",
                    thu = 0L,
                    daXacNhan = true,
                    ngay = "2026-11-0${i + 1}"
                )
            )
        }

        // Truy vấn "Khách Tuấn còn mấy buổi"
        val resBuoi = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("Khách Tuấn còn mấy buổi", store, refDate)
        assertNotNull(resBuoi)
        val replyBuoi = resBuoi!!.cauTraLoi
        assertTrue(replyBuoi.contains("còn 6 buổi"))
        assertTrue(replyBuoi.contains("đã dùng 4/10 buổi"))

        // Truy vấn "Khách Hương đăng ký gói gì" khi chưa có khách Hương
        val resHuong = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("Khách Hương đăng ký gói gì", store, refDate)
        // Chưa có khách Hương trong danh sách -> trả về null hoặc báo không tìm thấy
        assertTrue(resHuong == null || resHuong.cauTraLoi.contains("chưa đăng ký") || resHuong.cauTraLoi.contains("trống"))

        // Thêm khách Hương
        val khHuong = KH("kh_huong", "Trần Thị Hương", "0987654321")
        store.khs.add(khHuong)
        store.dks.add(
            DK(
                id = "dk_huong_1",
                khId = "kh_huong",
                goiId = "goi_vg",
                ngay = "2026-11-01",
                luotDaDung = 1
            )
        )

        val resHuongFound = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("Khách Hương đăng ký gói gì", store, refDate)
        assertNotNull(resHuongFound)
        assertTrue(resHuongFound!!.cauTraLoi.contains("Gói Trị Liệu Vai Gáy"))
        assertTrue(resHuongFound.cauTraLoi.contains("còn 9 buổi"))
    }

    @Test
    fun testVipCustomerRanking() {
        store.khs.clear()
        store.luots.clear()
        store.dks.clear()
        store.gois.clear()

        val kh1 = KH("kh_1", "Anh Minh", "0911111111")
        val kh2 = KH("kh_2", "Chị Lan", "0922222222")
        store.khs.addAll(listOf(kh1, kh2))

        val goiVip = Goi("goi_vip", "Gói VIP Toàn Diện", emptyList(), 20, 5000000L)
        store.gois.add(goiVip)

        // Chị Lan mua gói 5tr + vé 200k = 5.2tr
        store.dks.add(DK("dk_lan", "kh_2", "goi_vip", "2026-10-01", luotDaDung = 2))
        store.luots.add(Luot("ve_lan", "kh_2", "nv_1", emptyList(), tgbd = "10:00", tgkt = "11:00", thu = 200000L, daXacNhan = true, ngay = "2026-10-05"))

        // Anh Minh chỉ mua 2 vé lẻ = 400k
        store.luots.add(Luot("ve_minh", "kh_1", "nv_1", emptyList(), tgbd = "10:00", tgkt = "11:00", thu = 400000L, daXacNhan = true, ngay = "2026-10-06"))

        val resVip = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("Ai là khách VIP nhất", store, refDate)
        assertNotNull(resVip)
        val reply = resVip!!.cauTraLoi
        assertTrue(reply.contains("Top 1: Chị Lan"))
        assertTrue(reply.contains("5.200.000") || reply.contains("5.2tr"))
    }

    // =========================================================================
    // 5. KIỂM THỬ MODULE 3: NHÂN VIÊN KTV, BẬN / RẢNH & HOA HỒNG
    // =========================================================================

    @Test
    fun testKtvBusyAndIdleDetermination() {
        store.nvs.clear()
        store.luots.clear()
        store.khs.clear()

        val nvLan = NV("nv_lan", "Lan", "Nữ", "0901")
        val nvHung = NV("nv_hung", "Hùng", "Nam", "0902")
        store.nvs.addAll(listOf(nvLan, nvHung))

        val khA = KH("kh_a", "Bác Nam", "0933")
        store.khs.add(khA)

        // Lan đang có vé làm dở (dangLam == true vì tgkt rỗng)
        store.luots.add(
            Luot(
                id = "ve_active",
                khId = "kh_a",
                nvId = "nv_lan",
                dvIds = emptyList(),
                tgbd = "10:00",
                tgkt = "", // ĐANG LÀM
                daXacNhan = false,
                ngay = refDate
            )
        )

        // Hỏi "ai đang phục vụ khách"
        val resBusy = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("ai đang phục vụ khách", store, refDate)
        assertNotNull(resBusy)
        assertTrue(resBusy!!.cauTraLoi.contains("KTV Lan"))
        assertFalse(resBusy.cauTraLoi.contains("KTV Hùng: đang phục vụ"))

        // Hỏi "ai đang rảnh"
        val resFree = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("ai đang rảnh", store, refDate)
        assertNotNull(resFree)
        assertTrue(resFree!!.cauTraLoi.contains("Hùng"))
        assertFalse(resFree.cauTraLoi.contains("Lan (Nữ"))
    }

    @Test
    fun testKtvPerformanceAndRanking() {
        store.nvs.clear()
        store.luots.clear()
        store.dvs.clear()

        val nvLan = NV("nv_lan", "Lan", "Nữ", "0901")
        val nvHung = NV("nv_hung", "Hùng", "Nam", "0902")
        store.nvs.addAll(listOf(nvLan, nvHung))

        val dv = DV("dv_1", "Trị Liệu Cổ Vai Gáy", 60, 200000L, "", 50000L)
        store.dvs.add(dv)

        // Lan làm 2 vé trong tháng 10 (mỗi vé 60p, hoa hồng 50k -> 120p, 100k)
        repeat(2) { i ->
            store.luots.add(
                Luot(
                    id = "ve_lan_$i",
                    khId = "kh_1",
                    nvId = "nv_lan",
                    dvIds = listOf("dv_1"),
                    tgbd = "08:00",
                    tgkt = "09:00",
                    thu = 200000L,
                    daXacNhan = true,
                    ngay = "2026-10-0${i + 1}"
                )
            )
        }

        // Hùng làm 5 vé trong tháng 10
        repeat(5) { i ->
            store.luots.add(
                Luot(
                    id = "ve_hung_$i",
                    khId = "kh_1",
                    nvId = "nv_hung",
                    dvIds = listOf("dv_1"),
                    tgbd = "10:00",
                    tgkt = "11:00",
                    thu = 200000L,
                    daXacNhan = true,
                    ngay = "2026-10-0${i + 1}"
                )
            )
        }

        // Truy vấn hiệu suất KTV Lan
        val resLan = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("KTV Lan tháng 10 làm mấy vé hoa hồng bao nhiêu", store, refDate)
        assertNotNull(resLan)
        val replyLan = resLan!!.cauTraLoi
        assertTrue(replyLan.contains("2 vé"))
        assertTrue(replyLan.contains("120 phút"))
        assertTrue(replyLan.contains("100.000"))

        // Truy vấn "ai làm nhiều vé nhất tháng 10"
        val resTop = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("ai làm nhiều vé nhất tháng 10", store, refDate)
        assertNotNull(resTop)
        val replyTop = resTop!!.cauTraLoi
        assertTrue(replyTop.contains("Top 1: Hùng — 5 vé"))
        assertTrue(replyTop.contains("Top 2: Lan — 2 vé"))
    }

    // =========================================================================
    // 6. KIỂM THỬ MODULE 4: DỊCH VỤ & XẾP HẠNG BÁN CHẠY
    // =========================================================================

    @Test
    fun testTopSellingServicesAndPriceLookup() {
        store.dvs.clear()
        store.luots.clear()

        val dvXongHoi = DV("dv_xong", "Xông Hơi Đá Muối", 30, 100000L, "", 30000L)
        val dvBody = DV("dv_body", "Massage Body Tinh Dầu", 90, 300000L, "", 80000L)
        store.dvs.addAll(listOf(dvXongHoi, dvBody))

        // Body bán 4 lượt, Xông hơi bán 1 lượt
        repeat(4) { i ->
            store.luots.add(Luot("ve_b_$i", "kh_1", "nv_1", listOf("dv_body"), tgbd = "08:00", tgkt = "09:30", thu = 300000L, daXacNhan = true, ngay = "2026-10-01"))
        }
        store.luots.add(Luot("ve_x_1", "kh_1", "nv_1", listOf("dv_xong"), tgbd = "10:00", tgkt = "10:30", thu = 100000L, daXacNhan = true, ngay = "2026-10-02"))

        // Truy vấn dịch vụ nào chạy nhất
        val resTop = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("Dịch vụ nào chạy nhất", store, refDate)
        assertNotNull(resTop)
        val replyTop = resTop!!.cauTraLoi
        assertTrue(replyTop.contains("Top 1: Massage Body Tinh Dầu — 4 lượt"))

        // Tra cứu giá & hoa hồng dịch vụ cụ thể
        val resGia = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("xông hơi giá bao nhiêu hoa hồng bao nhiêu", store, refDate)
        assertNotNull(resGia)
        val replyGia = resGia!!.cauTraLoi
        assertTrue(replyGia.contains("100.000"))
        assertTrue(replyGia.contains("30.000"))
        assertTrue(replyGia.contains("70.000")) // Chủ thực nhận
    }

    // =========================================================================
    // 7. KIỂM THỬ MODULE 5: PHÂN TÍCH SO SÁNH ĐA KỲ
    // =========================================================================

    @Test
    fun testMultiPeriodComparison() {
        store.luots.clear()
        store.tcs.clear()

        // Tuần này (2026-11-16..2026-11-22): 2 vé, tổng thu 500k
        store.luots.add(Luot("ve_w1", "kh_1", "nv_1", emptyList(), tgbd = "09:00", tgkt = "10:00", thu = 300000L, daXacNhan = true, ngay = "2026-11-17"))
        store.luots.add(Luot("ve_w2", "kh_1", "nv_1", emptyList(), tgbd = "10:00", tgkt = "11:00", thu = 200000L, daXacNhan = true, ngay = "2026-11-18"))

        // Tuần trước (2026-11-09..2026-11-15): 1 vé, tổng thu 200k
        store.luots.add(Luot("ve_pw1", "kh_1", "nv_1", emptyList(), tgbd = "09:00", tgkt = "10:00", thu = 200000L, daXacNhan = true, ngay = "2026-11-10"))

        val resCmp = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("so sánh doanh thu tuần này với tuần trước", store, refDate)
        assertNotNull(resCmp)
        val replyCmp = resCmp!!.cauTraLoi
        assertTrue(replyCmp.contains("So sánh: Tuần này"))
        assertTrue(replyCmp.contains("500.000"))
        assertTrue(replyCmp.contains("200.000"))
        assertTrue(replyCmp.contains("Tăng +300.000"))
    }

    // =========================================================================
    // 8. KIỂM THỬ PHÂN QUYỀN BẢO MẬT NHÂN SỰ (KTV SECURITY PERMISSIONS)
    // =========================================================================

    @Test
    fun testStaffSecurityRestrictions() {
        store.currentUser = TaiKhoan(
            id = "tk_ktv",
            username = "0912345678",
            matKhauHash = "hash_ktv",
            quyen = VaiTro.STAFF,
            nvId = "nv_lan",
            hoTen = "KTV Lan",
            cacQuyenDuocCap = emptySet()
        )

        // KTV hỏi doanh thu cơ sở -> Phải bị từ chối bảo mật
        val resRev = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("Doanh thu tháng 10 của quán", store, refDate)
        assertNotNull(resRev)
        assertTrue(resRev!!.cauTraLoi.contains("bảo mật bởi Chủ Spa"))

        // KTV hỏi so sánh doanh thu -> Bị từ chối bảo mật
        val resCmp = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("So sánh doanh thu tuần này với tuần trước", store, refDate)
        assertNotNull(resCmp)
        assertTrue(resCmp!!.cauTraLoi.contains("thuộc quyền bảo mật"))

        // Nhưng KTV được phép hỏi "Ai đang rảnh" để đón khách
        val resFree = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("Ai đang rảnh", store, refDate)
        assertNotNull(resFree)
        assertFalse(resFree!!.cauTraLoi.contains("bảo mật"))

        // KTV được phép tra cứu bảng giá dịch vụ của quán
        val resGia = OnDeviceSpaQueryEngine.xuLyTruyVanNgoaiTuyen("Bảng giá dịch vụ", store, refDate)
        assertNotNull(resGia)
        assertFalse(resGia!!.cauTraLoi.contains("bảo mật"))
    }

    // =========================================================================
    // 9. KIỂM THỬ TÍCH HỢP TRALOI THONG MINH NOI BO TRONG SPASTORE
    // =========================================================================

    @Test
    fun testSpaStoreTraLoiThongMinhNoiBoIntegration() {
        store.luots.clear()
        store.tcs.clear()

        // Gọi qua store.traLoiThongMinhNoiBo
        val res = store.traLoiThongMinhNoiBo("10 ngày đầu tháng 10 thu chi thế nào")
        assertNotNull(res)
        // Vì dữ liệu trống, phải trả về trung thực 0 vé, 0đ chứ không phải hôm nay
        assertTrue(res.cauTraLoi.contains("0 vé"))
        assertTrue(res.cauTraLoi.contains("0đ"))
        assertFalse(res.cauTraLoi.contains("Báo cáo ca hôm nay"))
    }
}
