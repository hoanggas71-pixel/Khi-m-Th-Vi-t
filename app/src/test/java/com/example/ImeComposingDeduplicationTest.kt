package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Kiểm thử toàn diện cơ chế khử dồn chuỗi IME từ bàn phím máy tính trên giả lập LDPlayer
 * cho màn hình OneTimeLoginScreen (MainActivity.kt).
 */
class ImeComposingDeduplicationTest {

    // =========================================================================
    // 1. MÔ PHỎNG SỰ KIỆN NHẬP DỒN COMPOSING TRÊN LDPLAYER (PROGRESSIVE COMPOSING)
    // =========================================================================

    @Test
    fun testSequentialDigitsUnderLdPlayerImeAccumulation() {
        // Mô phỏng chính xác chuỗi sự kiện nhập số 1, 2, 3, 4, 5 trên giả lập LDPlayer
        // Kết quả đầu ra từng bước và kết thúc phải là "12345", không bị dồn chuỗi.
        var state = ""

        // Bước 1: Gõ '1' -> IME gửi "1"
        state = sanitizePasswordInput(current = state, incoming = "1")
        assertEquals("1", state)

        // Bước 2: Gõ '2' -> LDPlayer dồn composing "12" vào buffer "1" thành "112"
        state = sanitizePasswordInput(current = state, incoming = "112")
        assertEquals("12", state)

        // Bước 3: Gõ '3' -> LDPlayer dồn composing "123" vào buffer "12" thành "12123"
        state = sanitizePasswordInput(current = state, incoming = "12123")
        assertEquals("123", state)

        // Bước 4: Gõ '4' -> LDPlayer dồn composing "1234" vào buffer "123" thành "1231234"
        state = sanitizePasswordInput(current = state, incoming = "1231234")
        assertEquals("1234", state)

        // Bước 5: Gõ '5' -> LDPlayer dồn composing "12345" vào buffer "1234" thành "123412345"
        state = sanitizePasswordInput(current = state, incoming = "123412345")
        assertEquals("12345", state)
    }

    @Test
    fun testProgressiveComposing_rawCumulativeSequence() {
        // Mô phỏng chuỗi lũy tích toàn phần khi các sự kiện dồn trễ gửi dồn dập
        val seq1 = sanitizePasswordInput("", "1")
        assertEquals("1", seq1)

        val seq2 = sanitizePasswordInput(seq1, "112")
        assertEquals("12", seq2)

        val seq3 = sanitizePasswordInput(seq2, "112123")
        assertEquals("123", seq3)

        val seq4 = sanitizePasswordInput(seq3, "1121231234")
        assertEquals("1234", seq4)

        val seq5 = sanitizePasswordInput(seq4, "112123123412345")
        assertEquals("12345", seq5)

        val seq6 = sanitizePasswordInput(seq5, "112123123412345123456")
        assertEquals("123456", seq6)
    }

    @Test
    fun testProgressiveComposing_defaultAdminPassword() {
        // Mô phỏng gõ mật khẩu quản trị mặc định "247208" trên bàn phím PC LDPlayer
        var text = ""
        val keystrokes = listOf(
            "2" to "2",
            "224" to "24",
            "24247" to "247",
            "2472472" to "2472",
            "247224720" to "24720",
            "24720247208" to "247208"
        )
        for ((incoming, expected) in keystrokes) {
            text = sanitizePasswordInput(text, incoming)
            assertEquals(expected, text)
        }
        assertEquals("247208", text)
    }

    // =========================================================================
    // 2. GIẢI NÉN CHUỖI TAM GIÁC COMPOSING (TRIANGULAR COMPOSING UNPACKING)
    // =========================================================================

    @Test
    fun testFullyAccumulatedTriangularUnpacking() {
        // Kiểm tra giải nén chuỗi dồn toàn phần theo công thức Tn = n*(n+1)/2
        assertEquals("12", unpackTriangularComposing("112"))
        assertEquals("123", unpackTriangularComposing("112123"))
        assertEquals("1234", unpackTriangularComposing("1121231234"))
        assertEquals("12345", unpackTriangularComposing("112123123412345"))
        assertEquals("123456", unpackTriangularComposing("112123123412345123456"))

        // Thử nghiệm với chuỗi chữ cái
        assertEquals("abc", unpackTriangularComposing("aababc"))

        // Chuỗi không phải dạng dồn composing tam giác phải được giữ nguyên bản
        assertEquals("12345", unpackTriangularComposing("12345"))
        assertEquals("1111", unpackTriangularComposing("1111"))
        assertEquals("", unpackTriangularComposing(""))
        assertEquals("12", unpackTriangularComposing("12"))
        assertEquals("123123", unpackTriangularComposing("123123"))
    }

    // =========================================================================
    // 3. THAO TÁC XÓA KÝ TỰ (BACKSPACE & DELETION RESILIENCE)
    // =========================================================================

    @Test
    fun testBackspaceAndDeletion() {
        // Xóa lùi từng ký tự phải hoạt động mượt mà, không bao giờ kích hoạt lại bộ đệm dồn
        var state = "12345"
        state = sanitizePasswordInput(state, "1234")
        assertEquals("1234", state)

        state = sanitizePasswordInput(state, "123")
        assertEquals("123", state)

        state = sanitizePasswordInput(state, "12")
        assertEquals("12", state)

        state = sanitizePasswordInput(state, "1")
        assertEquals("1", state)

        state = sanitizePasswordInput(state, "")
        assertEquals("", state)
    }

    @Test
    fun testBackspace_clearAllAndMiddleDeletion() {
        // Chọn tất cả và xóa
        val cleared = sanitizePasswordInput("123456", "")
        assertEquals("", cleared)

        // Xóa ký tự ở giữa chuỗi
        val midDelete = sanitizePasswordInput("123456", "1256")
        assertEquals("1256", midDelete)
    }

    // =========================================================================
    // 4. BÀN PHÍM CHUẨN: GÕ TUẦN TỰ TỪNG KÝ TỰ (SINGLE CHARACTER INVARIANT)
    // =========================================================================

    @Test
    fun testNormalKeyboardTypingPreserved() {
        // Gõ trên bàn phím thông thường (mỗi bước tăng 1 ký tự) tuyệt đối không bị biến dạng
        var state = ""
        val chars = "admin123"
        for (i in chars.indices) {
            val nextText = chars.substring(0, i + 1)
            state = sanitizePasswordInput(state, nextText)
            assertEquals(nextText, state)
        }
        assertEquals("admin123", state)
    }

    // =========================================================================
    // 5. MẬT KHẨU CÓ CÁC KÝ TỰ TRÙNG LẶP LIÊN TIẾP ("888888", "1111")
    // =========================================================================

    @Test
    fun testConsecutiveDuplicateCharactersPreserved_normalTyping() {
        // Người dùng gõ mật khẩu chứa nhiều số trùng lặp như "8888" hoặc "1111"
        var state = ""
        state = sanitizePasswordInput(state, "8")
        assertEquals("8", state)
        state = sanitizePasswordInput(state, "88")
        assertEquals("88", state)
        state = sanitizePasswordInput(state, "888")
        assertEquals("888", state)
        state = sanitizePasswordInput(state, "8888")
        assertEquals("8888", state)

        var ones = ""
        ones = sanitizePasswordInput(ones, "1")
        assertEquals("1", ones)
        ones = sanitizePasswordInput(ones, "11")
        assertEquals("11", ones)
        ones = sanitizePasswordInput(ones, "111")
        assertEquals("111", ones)
        ones = sanitizePasswordInput(ones, "1111")
        assertEquals("1111", ones)
    }

    @Test
    fun testConsecutiveDuplicateCharacters_imeComposing() {
        // Gõ số trùng lặp trên bàn phím LDPlayer có dồn IME
        var state = ""
        state = sanitizePasswordInput(state, "8")
        assertEquals("8", state)

        state = sanitizePasswordInput(state, "888") // "8" + "88"
        assertEquals("88", state)

        state = sanitizePasswordInput(state, "88888") // "88" + "888"
        assertEquals("888", state)

        state = sanitizePasswordInput(state, "8888888") // "888" + "8888"
        assertEquals("8888", state)
    }

    // =========================================================================
    // 6. DÁN MẬT KHẨU TỪ CLIPBOARD (PASTING)
    // =========================================================================

    @Test
    fun testPastingPasswords() {
        // Dán mật khẩu thông thường không bị cắt xén hay biến dạng
        assertEquals("MyP@ssw0rd!", sanitizePasswordInput("", "MyP@ssw0rd!"))
        assertEquals("123456", sanitizePasswordInput("", "123456"))
        assertEquals("admin123", sanitizePasswordInput("", "admin123"))
        assertEquals("Password@2026", sanitizePasswordInput("", "Password@2026"))

        // Thay thế chuỗi đã bôi đen
        val replaced = sanitizePasswordInput("old_password", "new_secret_99")
        assertEquals("new_secret_99", replaced)

        // Dán chuỗi chứa đoạn lặp hợp lệ
        val repeated = sanitizePasswordInput("", "123123")
        assertEquals("123123", repeated)
    }

    // =========================================================================
    // 7. LÀM SẠCH BỘ ĐỆM SỐ ĐIỆN THOẠI TRÔI XUỐNG (PHONE COLLISION REMOVAL)
    // =========================================================================

    @Test
    fun testPhoneInputCollisionRemoval() {
        // Khắc phục triệt để lỗi trôi SĐT từ ô trên xuống ô dưới (Vấn đề 1)
        val phone = "0912345678"
        val state = sanitizePasswordInput(
            current = "",
            incoming = "091234567812345",
            phoneInput = phone
        )
        assertEquals("12345", state)

        // Chỉ có mỗi số điện thoại bị trôi xuống
        val phoneOnly = sanitizePasswordInput(
            current = "",
            incoming = "0397772963",
            phoneInput = "0397772963"
        )
        assertEquals("", phoneOnly)

        // SĐT bị trôi kèm theo chuỗi IME dồn trên LDPlayer
        val phoneWithIme = sanitizePasswordInput(
            current = "1",
            incoming = "0397772963112",
            phoneInput = "0397772963"
        )
        assertEquals("12", phoneWithIme)
    }

    @Test
    fun testPasswordNotWipedWhenMatchingPhonePrefix() {
        // R1: Tài khoản/SĐT là "12345", người dùng gõ mật khẩu từng ký tự "1" -> "12" -> "123" -> "1234" -> "12345" -> "123456"
        // Tuyệt đối không bị xóa trắng tại ký tự thứ 5 ("12345") hoặc bất kỳ ký tự nào
        val phone = "12345"
        var pass = ""
        val keystrokes = listOf("1", "12", "123", "1234", "12345", "123456")
        for (incoming in keystrokes) {
            pass = sanitizePasswordInput(current = pass, incoming = incoming, phoneInput = phone)
        }
        assertEquals("123456", pass)

        // Kiểm tra xóa lùi (backspace): "123456" -> "12345" -> "1234"
        pass = sanitizePasswordInput(current = pass, incoming = "12345", phoneInput = phone)
        assertEquals("12345", pass)
        pass = sanitizePasswordInput(current = pass, incoming = "1234", phoneInput = phone)
        assertEquals("1234", pass)
    }

    // =========================================================================
    // 8. TƯƠNG THÍCH VỚI HELPER ImeInputHelper
    // =========================================================================

    @Test
    fun testImeInputHelperCompatibility() {
        assertEquals("12345", ImeInputHelper.extractProgressiveComposing("112123123412345"))
        assertNull(ImeInputHelper.extractProgressiveComposing("12345"))
        assertNull(ImeInputHelper.extractProgressiveComposing(""))

        val res = ImeInputHelper.deduplicateImeInput("12", "12123")
        assertEquals("123", res)
    }

    // =========================================================================
    // 9. PHÁT HIỆN VÀ KHỬ CHUỖI NHÂN ĐÔI NGUYÊN XI (DOUBLING BUG - LDPlayer)
    // Lỗi thực tế: Người dùng nhập mật khẩu "12345", LDPlayer trả về "1234512345"
    // Nguyên nhân: LDPlayer gửi lại nội dung remember-password cũ + lần nhập mới
    // =========================================================================

    @Test
    fun testDoublingBug_passwordDoubledWhenEmpty() {
        // Trường hợp điển hình từ bug report:
        // Ô mật khẩu rỗng, LDPlayer gửi "1234512345" = "12345" + "12345"
        // Phải trả về "12345" (nửa đầu), loại bỏ phần nhân đôi
        val result = sanitizePasswordInput(current = "", incoming = "1234512345")
        assertEquals("12345", result)
    }

    @Test
    fun testDoublingBug_shortPasswordDoubled() {
        // "123123": half = 3 < 4 -> KHÔNG phát hiện nhân đôi (tránh false positive mật khẩu hợp lệ "123123")
        // Chỉ phát hiện nhân đôi khi half >= 4 (ví dụ "1234512345" half=5)
        val result = sanitizePasswordInput(current = "", incoming = "123123")
        assertEquals("123123", result)
    }

    @Test
    fun testDoublingBug_currentEqualsHalf_keepCurrent() {
        // Current đã có "12345", LDPlayer gửi "1234512345" (gửi lại cả current + current)
        // Phải giữ nguyên current "12345", không cộng thêm
        val result = sanitizePasswordInput(current = "12345", incoming = "1234512345")
        assertEquals("12345", result)
    }

    @Test
    fun testDoublingBug_normalInputNotAffected() {
        // Gõ bình thường "1234" rồi thêm "5" -> phải ra "12345", không bị ảnh hưởng
        val result = sanitizePasswordInput(current = "1234", incoming = "12345")
        assertEquals("12345", result)
    }

    @Test
    fun testDoublingBug_repeatedCharPasswordNotAffected() {
        // Mật khẩu hợp lệ có ký tự lặp đôi "aabbcc" không phải nhân đôi ("abc" + "abc")
        // "aabbcc" != "aab" + "aab" -> giữ nguyên
        val result = sanitizePasswordInput(current = "", incoming = "aabbcc")
        assertEquals("aabbcc", result)
    }

    @Test
    fun testDoublingBug_singleCharDoubled() {
        // "11" có thể là: (a) gõ 2 lần phím '1' hợp lệ, hoặc (b) nhân đôi "1"+"1"
        // half = 1 < 3 -> KHÔNG áp dụng doubling detection -> giữ nguyên "11"
        // Bảo toàn tuyệt đối mật khẩu hợp lệ có ký tự trùng
        val result = sanitizePasswordInput(current = "", incoming = "11")
        assertEquals("11", result)
    }

    @Test
    fun testDoublingBug_fourCharDoubled() {
        // "12121212" = "1212" + "1212" (half = 4 >= 3) -> phát hiện nhân đôi -> trả về "1212"
        val result = sanitizePasswordInput(current = "", incoming = "12121212")
        assertEquals("1212", result)
    }
}
