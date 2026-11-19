package com.example

import org.junit.Assert.*
import org.junit.Test

class ReportKpiExtractionTest {

    @Test
    fun testExtractReportKpi_negativeNetProfitWithParenthesesAndDong() {
        val report = "• (Lãi ròng): -260.000 đồng"
        val kpi = extractReportKpi(report)
        assertNotNull("KPI extraction should not be null for (Lãi ròng): -260.000 đồng", kpi)
        assertEquals("-260.000 đồng", kpi?.laiRong)
    }

    @Test
    fun testExtractReportKpi_negativeNetProfitLongLabelWithParenthesesAndTrailingDot() {
        val report = "• CHỦ QUÁN THỰC NHẬN THÁNG NÀY (LÃI RÒNG): -260.000 đồng."
        val kpi = extractReportKpi(report)
        assertNotNull("KPI extraction should match long label with parentheses and trailing dot", kpi)
        assertEquals("-260.000 đồng", kpi?.laiRong)
    }

    @Test
    fun testExtractReportKpi_negativeNetProfitWithShortD() {
        val report = "• Lãi ròng: -260.000đ"
        val kpi = extractReportKpi(report)
        assertNotNull("KPI extraction should match negative net profit with đ", kpi)
        assertEquals("-260.000đ", kpi?.laiRong)
    }

    @Test
    fun testExtractReportKpi_positiveNetProfitWithPlusSignAndDong() {
        val report = "• Lãi ròng: +1.500.000 đồng"
        val kpi = extractReportKpi(report)
        assertNotNull("KPI extraction should match positive net profit with + and đồng", kpi)
        assertEquals("+1.500.000 đồng", kpi?.laiRong)
    }

    @Test
    fun testExtractReportKpi_fullGehihiDailyReport() {
        val fullReport = """
            Báo cáo doanh thu & chi phí hôm nay:
            • Tổng Thu: +5.200.000 đồng (Thu vé lẻ: 3.200.000đ, Thu ngoài: 2.000.000đ)
            • Chi phí: 2.100.000 đồng
            • Chủ quán thực nhận (Lãi ròng): +3.100.000 đồng.
            • Số vé phục vụ: 12 vé
            • KTV: Ngọc Anh
        """.trimIndent()

        val kpi = extractReportKpi(fullReport)
        assertNotNull("Full report should be extracted successfully", kpi)
        assertEquals("+5.200.000 đồng", kpi?.tongThu)
        assertEquals("2.100.000 đồng", kpi?.chiPhi)
        assertEquals("+3.100.000 đồng", kpi?.laiRong)
        assertEquals("12 vé", kpi?.soVe)
        assertEquals("Ngọc Anh", kpi?.ktvSummary)
    }

    @Test
    fun testExtractReportKpi_fullLossReportNegativeNetProfit() {
        val lossReport = """
            Báo cáo tài chính ngày 19/11/2026:
            • Doanh thu: 1.000.000đ
            • Chi phí: 1.260.000đ
            • Lãi ròng: -260.000đ
            • 5 vé
            • Nhân viên: Trần Minh
        """.trimIndent()

        val kpi = extractReportKpi(lossReport)
        assertNotNull("Loss report should be parsed correctly", kpi)
        assertEquals("1.000.000đ", kpi?.tongThu)
        assertEquals("1.260.000đ", kpi?.chiPhi)
        assertEquals("-260.000đ", kpi?.laiRong)
        assertEquals("5 vé", kpi?.soVe)
        assertEquals("Trần Minh", kpi?.ktvSummary)
    }

    @Test
    fun testExtractReportKpi_markdownBoldAndVariousCurrencyUnits() {
        val report = """
            **Tổng thu**: 10.000.000 VND
            **Tổng chi**: 4.000.000 vnđ
            **Thực nhận**: 6.000.000 dong
        """.trimIndent()

        val kpi = extractReportKpi(report)
        assertNotNull("Markdown bold report should be extracted correctly", kpi)
        assertEquals("10.000.000 VND", kpi?.tongThu)
        assertEquals("4.000.000 vnđ", kpi?.chiPhi)
        assertEquals("6.000.000 dong", kpi?.laiRong)
    }

    @Test
    fun testExtractReportKpi_kAndTrUnits() {
        val report = """
            • Doanh thu: 15tr
            • Trả KTV: 5tr
            • Lợi nhuận: 10tr
        """.trimIndent()

        val kpi = extractReportKpi(report)
        assertNotNull("Millions (tr) unit should be preserved", kpi)
        assertEquals("15tr", kpi?.tongThu)
        assertEquals("5tr", kpi?.chiPhi)
        assertEquals("10tr", kpi?.laiRong)
    }

    @Test
    fun testExtractReportKpi_unitAppendedWhenMissing() {
        val report = """
            • Doanh thu: 500000
            • Chi phí: 200000
            • Lãi ròng: 300000
        """.trimIndent()

        val kpi = extractReportKpi(report)
        assertNotNull("Should append đ when currency unit is missing", kpi)
        assertEquals("500000đ", kpi?.tongThu)
        assertEquals("200000đ", kpi?.chiPhi)
        assertEquals("300000đ", kpi?.laiRong)
    }

    @Test
    fun testExtractReportKpi_missingKpiKeywords_returnsNull() {
        val randomText = "Xin chào bạn, hôm nay quán có bao nhiêu khách hàng mới đến trải nghiệm?"
        val kpi = extractReportKpi(randomText)
        assertNull("Non-KPI text should return null", kpi)
    }
}
