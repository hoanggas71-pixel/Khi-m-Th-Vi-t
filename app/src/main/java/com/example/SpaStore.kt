package com.example

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class OperationType {
    GET, LIST, CREATE, UPDATE, DELETE, WRITE
}

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String?
) {
    val currentUser = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
    val providerInfo = JSONArray().apply {
        currentUser?.providerData?.forEach { profile ->
            put(JSONObject().apply {
                put("providerId", profile.providerId)
                put("displayName", profile.displayName)
                put("email", profile.email)
                put(" federatedId", profile.uid)
            })
        }
    }
    val authInfo = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("displayName", currentUser?.displayName)
        put("emailVerified", currentUser?.isEmailVerified)
        put("isAnonymous", currentUser?.isAnonymous)
        put("providerInfo", providerInfo)
    }
    val errorInfo = JSONObject().apply {
        put("error", exception.message ?: "Unknown Firestore error")
        put("operationType", operationType.name.lowercase())
        put("path", path)
        put("authInfo", authInfo)
    }
    Log.e("FirestoreError", errorInfo.toString(), exception)
    throw exception
}

object SpaCloudRepository {
    const val WORKSPACE_COLLECTION = "spa_workspaces"
    const val USERS_COLLECTION = "users"
    const val SHARED_WORKSPACE_ID = "khiem_thi_viet_shared"

    suspend fun saveWorkspaceSnapshot(
        db: FirebaseFirestore,
        auth: FirebaseAuth,
        workspaceId: String = SHARED_WORKSPACE_ID,
        shopName: String = "KHIẾM THỊ VIỆT",
        payloadJson: String,
        accountsCount: Int,
        customersCount: Int,
        ticketsCount: Int,
        updatedByName: String,
        overrideUpdatedByUid: String? = null
    ) {
        val uid = overrideUpdatedByUid ?: auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in to sync workspace")
        val path = "$WORKSPACE_COLLECTION/$workspaceId"
        val data = mapOf(
            "workspaceId" to workspaceId,
            "shopName" to shopName.take(200).ifBlank { "KHIẾM THỊ VIỆT" },
            "payloadJson" to payloadJson,
            "accountsCount" to accountsCount.coerceIn(1, 500),
            "customersCount" to customersCount.coerceIn(0, 10000),
            "ticketsCount" to ticketsCount.coerceIn(0, 50000),
            "updatedByUid" to uid,
            "updatedByName" to updatedByName.take(200).ifBlank { "Khiếm Thị Việt" },
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection(WORKSPACE_COLLECTION).document(workspaceId).set(data).await()
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreError(e, OperationType.WRITE, path)
        }
    }

    suspend fun getWorkspaceSnapshot(
        db: FirebaseFirestore,
        workspaceId: String = SHARED_WORKSPACE_ID
    ): Map<String, Any?>? {
        val path = "$WORKSPACE_COLLECTION/$workspaceId"
        return try {
            val doc = db.collection(WORKSPACE_COLLECTION).document(workspaceId).get().await()
            if (doc.exists()) doc.data else null
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    suspend fun deleteWorkspaceSnapshot(
        db: FirebaseFirestore,
        workspaceId: String = SHARED_WORKSPACE_ID
    ) {
        val path = "$WORKSPACE_COLLECTION/$workspaceId"
        try {
            db.collection(WORKSPACE_COLLECTION).document(workspaceId).delete().await()
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreError(e, OperationType.DELETE, path)
        }
    }

    suspend fun saveUserProfile(
        db: FirebaseFirestore,
        auth: FirebaseAuth,
        targetUserId: String? = null,
        displayName: String,
        email: String,
        spaPhone: String,
        spaRole: String
    ) {
        val uid = targetUserId ?: auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in")
        val path = "$USERS_COLLECTION/$uid"
        val data = mapOf(
            "uid" to uid,
            "displayName" to displayName.take(150).ifBlank { "Nhân sự Khiếm Thị Việt" },
            "email" to email.take(254).ifBlank { "user@khiemthiviet.vn" },
            "spaPhone" to spaPhone.take(30),
            "spaRole" to if (spaRole == "ADMIN") "ADMIN" else "STAFF",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection(USERS_COLLECTION).document(uid).set(data).await()
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreError(e, OperationType.WRITE, path)
        }
    }

    suspend fun getUserProfile(
        db: FirebaseFirestore,
        userId: String
    ): Map<String, Any?>? {
        val path = "$USERS_COLLECTION/$userId"
        return try {
            val doc = db.collection(USERS_COLLECTION).document(userId).get().await()
            if (doc.exists()) doc.data else null
        } catch (e: FirebaseFirestoreException) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }
}

data class KetQuaAiBot(
    val cauTraLoi: String,
    val hanhDongThucThi: String = "",
    val chuyenTab: Int? = null,
    val chuyenMucVe: Int? = null,
    val chuyenMucNguoi: Int? = null,
    val chuyenMucAdmin: Int? = null,
    val moChiTietLuongNvId: String? = null,
    val tuDongDongChat: Boolean = false
)

class SpaStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val memoryVaultFile = File(appContext.filesDir, GEHIHI_VAULT_FILE_NAME)

    val taiKhoans = mutableStateListOf<TaiKhoan>()
    val nvs = mutableStateListOf<NV>()
    val dvs = mutableStateListOf<DV>()
    val gois = mutableStateListOf<Goi>()
    val khs = mutableStateListOf<KH>()
    val dks = mutableStateListOf<DK>()
    val luots = mutableStateListOf<Luot>()
    val tcs = mutableStateListOf<TC>()
    val aiMessages = mutableStateListOf<AiChatMessage>()
    val aiLongTermMemories = mutableStateListOf<String>()
    val deletedIds = mutableStateListOf<String>()

    var currentUser by mutableStateOf<TaiKhoan?>(null)
        private set

    var aiIsLoading by mutableStateOf(false)
        private set

    // Điều khiển Tab con trong mục Quyền hạn (0: Cấp quyền & TK NV, 1: Sửa TK Admin gốc, 2: Backup Xuất / Nhập)
    var requestedAdminSubTab by mutableIntStateOf(0)
        private set

    fun selectAdminSubTab(index: Int) {
        requestedAdminSubTab = index.coerceIn(0, 2)
    }

    // Lệnh THÊM / SỬA / XÓA đang chờ Admin xác nhận lại trước khi thực thi thật
    var pendingAiConfirmation by mutableStateOf<LenhChoXacNhanGehihi?>(null)
        private set

    // Trợ lý AI gehihi: Khởi tạo lazy theo chuẩn Firebase AI Logic
    private val geminiFlashModel by lazy {
        Firebase.ai.generativeModel(modelName = "gemini-3.8-flash")
    }

    private val geminiLiteModel by lazy {
        Firebase.ai.generativeModel(modelName = "gemini-3.5-flash-lite")
    }

    // Trạng thái Đồng bộ Đám mây Thời gian thực (Firebase Cloud Firestore)
    var isCloudConnected by mutableStateOf(false)
        private set
    var cloudUserEmail by mutableStateOf("")
        private set
    var cloudUserName by mutableStateOf("")
        private set
    var cloudSyncStatus by mutableStateOf("Chưa kết nối đám mây Google")
        private set
    var lastCloudSyncTime by mutableStateOf("")
        private set

    private var isApplyingCloudSnapshot = false
    private var initialCloudCheckComplete = false
    private var workspaceListener: ListenerRegistration? = null

    private val firestoreDb: FirebaseFirestore? by lazy {
        runCatching {
            val dbId = appContext.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        }.onFailure { e ->
            Log.e(TAG, "Failed to get named FirebaseFirestore instance", e)
        }.getOrNull()
    }

    private val firebaseAuth: FirebaseAuth? by lazy {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }

    init {
        loadFromPrefs()
        loadGehihiMemoryVault()
        if (taiKhoans.isEmpty()) {
            seedDefaultData()
            dongBoTatCaPhieuThuChiTuVeDaChot(luuNgay = false)
            saveToPrefs()
        } else {
            migrateLegacyAccountsAndMenuIfNeeded()
            dongBoTatCaPhieuThuChiTuVeDaChot(luuNgay = true)
        }

        // Khôi phục tài khoản đã đăng nhập 1 lần duy nhất trên thiết bị
        val savedAccountId = prefs.getString(KEY_LOGGED_IN_ACCOUNT_ID, null)
        currentUser = if (!savedAccountId.isNullOrBlank()) {
            taiKhoans.firstOrNull { it.id == savedAccountId }
        } else {
            null
        }

        if (aiLongTermMemories.isEmpty()) {
            ghiNhoSuKienDaiHan(
                "Khởi tạo bộ nhớ vĩnh viễn cho gehihi: Đã nạp ${dvs.size} dịch vụ, ${gois.size} gói liệu trình, ${khs.size} khách hàng, ${nvs.size} nhân viên.",
                luuNgay = false
            )
        }

        if (aiMessages.isEmpty()) {
            aiMessages.add(
                AiChatMessage(
                    id = "welcome",
                    isUser = false,
                    text = "Xin chào Admin! Tôi là gehihi — trợ lý AI FULL QUYỀN 100% của Khiếm Thị Việt.\n• An toàn tuyệt đối: Mọi lệnh THÊM hoặc XÓA dữ liệu đều được gehihi tóm tắt và hỏi xác nhận lại trước khi thực thi.\n• Bộ nhớ Kép (Hybrid Vault): Lưu trữ tới $MAX_LONG_TERM_MEMORIES sự kiện & $MAX_AI_MESSAGES tin nhắn trên máy kết hợp đồng bộ thông minh lên Đám mây Google!",
                    actionNote = "gehihi Full Quyền • Bộ nhớ Kép 50.000 sự kiện • Đồng bộ Cloud",
                    thoiGian = "${todayDateStr()} ${nowTimeStr()}"
                )
            )
            saveGehihiMemoryVault()
        }

        // Tự động kích hoạt đồng bộ đám mây thời gian thực nếu máy đã kết nối Google Auth
        runCatching {
            if (firebaseAuth?.currentUser != null) {
                startRealtimeCloudSync()
            }
        }
    }

    /**
     * Tự động nâng cấp tài khoản cũ và nạp 10 dịch vụ + 3 gói chuẩn từ file menu_mới_nhất_2.json
     * nếu dữ liệu đang là bộ mẫu cũ (dv_01..dv_04).
     */
    private fun migrateLegacyAccountsAndMenuIfNeeded() {
        var changed = false
        for (i in taiKhoans.indices) {
            val tk = taiKhoans[i]
            if (tk.id == "tk_admin" && tk.username.equals("admin", ignoreCase = true)) {
                taiKhoans[i] = tk.copy(
                    username = DEFAULT_ADMIN_PHONE,
                    matKhauHash = hashPassword(DEFAULT_ADMIN_PASS),
                    matKhauHienThi = DEFAULT_ADMIN_PASS,
                    hoTen = if (tk.hoTen.isBlank()) "Admin Quản trị" else tk.hoTen
                )
                changed = true
            } else if (tk.nvId.isNotBlank()) {
                val linkedNv = nvs.firstOrNull { it.id == tk.nvId }
                val phoneDigits = linkedNv?.sdt?.filter { it.isDigit() }.orEmpty()
                if (phoneDigits.length >= 8 && !tk.username.all { it.isDigit() }) {
                    taiKhoans[i] = tk.copy(
                        username = phoneDigits,
                        matKhauHienThi = tk.matKhauHienThi.ifBlank { "123456" }
                    )
                    changed = true
                } else if (tk.matKhauHienThi.isBlank()) {
                    taiKhoans[i] = tk.copy(matKhauHienThi = "123456")
                    changed = true
                }
            }
        }

        // Nếu đang chứa dịch vụ mẫu cũ (dv_01..dv_04) -> Nâng cấp sang 10 dịch vụ từ menu_mới_nhất_2.json
        // (Không tự nạp lại nếu Admin đã chủ động xóa hết dịch vụ hoặc xóa sạch dữ liệu ứng dụng)
        val userWipedData = prefs.getBoolean("user_wiped_data", false)
        if ((!userWipedData && dvs.isEmpty() && deletedIds.isEmpty()) || dvs.any { it.id.startsWith("dv_0") }) {
            dvs.clear()
            dvs.addAll(danhSachDichVuMenuMoiNhat2())
            gois.clear()
            gois.addAll(danhSachGoiMenuMoiNhat2())

            // Cập nhật các vé mẫu cũ sang ID dịch vụ mới để hiển thị tên và hoa hồng chuẩn
            for (i in luots.indices) {
                val l = luots[i]
                val mappedDvs = l.dvIds.map { oldId ->
                    when (oldId) {
                        "dv_01" -> "379695801677810" // Trị liệu 60 phút
                        "dv_02" -> "380937271360462" // Trị liệu 90 phút
                        "dv_03" -> "379590571336652" // Trị liệu 30 phút
                        "dv_04" -> "382316401953581" // Xông hơi thảo dược
                        else -> oldId
                    }
                }
                luots[i] = l.copy(dvIds = mappedDvs)
            }
            for (i in dks.indices) {
                val dk = dks[i]
                val mappedGoi = when (dk.goiId) {
                    "goi_01" -> "676003166753527"
                    "goi_02" -> "676158327672947"
                    else -> dk.goiId
                }
                dks[i] = dk.copy(goiId = mappedGoi)
            }
            changed = true
        }

        if (changed) {
            saveToPrefs()
        }
    }

    /**
     * Kiểm tra mật khẩu Admin hiện tại trước khi cho phép thực hiện thao tác nhạy cảm (Xóa tất cả dữ liệu ứng dụng).
     */
    fun xacThucMatKhauAdminHienTai(matKhauNhap: String): Boolean {
        val cur = currentUser ?: return false
        if (!cur.isAdmin()) return false
        val clean = matKhauNhap.trim()
        if (clean.isBlank()) return false
        val inputHash = hashPassword(clean)
        val rootAdmin = taiKhoans.firstOrNull { it.id == "tk_admin" }
        val matchCurrent = cur.matKhauHash == inputHash || (cur.matKhauHienThi.isNotBlank() && cur.matKhauHienThi == clean)
        val matchRoot = rootAdmin != null && (rootAdmin.matKhauHash == inputHash || (rootAdmin.matKhauHienThi.isNotBlank() && rootAdmin.matKhauHienThi == clean))
        return matchCurrent || matchRoot
    }

    /**
     * XÓA TẤT CẢ DỮ LIỆU ỨNG DỤNG (Chỉ duy nhất Admin thực hiện được):
     * Xóa sạch Vé, Gói khách, Gói mẫu, Nhân viên KTV, Dịch vụ, Khách hàng, Sổ quỹ, Lịch sử AI và Tài khoản Nhân viên
     * trên cả máy và Cloud Firestore, giữ lại duy nhất tài khoản Admin đang đăng nhập.
     */
    fun xoaTatCaDuLieuUngDungAdmin(
        matKhauNhap: String = "",
        boQuaKiemTraMatKhau: Boolean = false
    ): Result<String> {
        val cur = currentUser ?: return Result.failure(IllegalStateException("Chưa đăng nhập Admin!"))
        if (!cur.isAdmin()) {
            return Result.failure(SecurityException("Chỉ duy nhất tài khoản Admin mới có quyền xóa tất cả dữ liệu!"))
        }
        if (!boQuaKiemTraMatKhau && !xacThucMatKhauAdminHienTai(matKhauNhap)) {
            return Result.failure(IllegalArgumentException("Mật khẩu Admin không chính xác!"))
        }

        // Ghi nhận toàn bộ ID cũ vào deletedIds để các máy khác / Cloud không bao giờ tự gộp ngược lại dữ liệu cũ
        val allIdsToDelete = buildSet {
            addAll(SEED_SAMPLE_IDS)
            nvs.forEach { add(it.id) }
            dvs.forEach { add(it.id) }
            gois.forEach { add(it.id) }
            khs.forEach { add(it.id) }
            dks.forEach { add(it.id) }
            luots.forEach { add(it.id) }
            tcs.forEach { add(it.id) }
            taiKhoans.filter { it.id != "tk_admin" && it.id != cur.id }.forEach { add(it.id) }
        }
        allIdsToDelete.forEach { id ->
            if (id.isNotBlank() && !deletedIds.contains(id)) {
                deletedIds.add(id)
            }
        }

        val countVe = luots.size
        val countKh = khs.size
        val countNv = nvs.size
        val countDv = dvs.size
        val countGoi = gois.size
        val countTc = tcs.size

        // Giữ lại tài khoản Admin gốc và tài khoản Admin đang đăng nhập
        val keptAdmins = taiKhoans.filter { it.id == "tk_admin" || it.id == cur.id }
            .map { it.copy(nvId = "") }
            .distinctBy { it.id }

        nvs.clear()
        dvs.clear()
        gois.clear()
        khs.clear()
        dks.clear()
        luots.clear()
        tcs.clear()
        aiMessages.clear()
        aiLongTermMemories.clear()
        pendingAiConfirmation = null

        taiKhoans.clear()
        if (keptAdmins.isNotEmpty()) {
            taiKhoans.addAll(keptAdmins)
        } else {
            taiKhoans.add(cur.copy(nvId = ""))
        }
        currentUser = taiKhoans.firstOrNull { it.id == cur.id } ?: taiKhoans.firstOrNull()

        prefs.edit().putBoolean("user_wiped_data", true).apply()
        aiLongTermMemories.add("[${todayDateStr()} ${nowTimeStr()}] Admin đã thực hiện Xóa tất cả dữ liệu ứng dụng (Làm mới toàn bộ hệ thống).")
        aiMessages.add(
            AiChatMessage(
                id = "msg_${shortId()}",
                isUser = false,
                text = "Hệ thống đã được làm mới hoàn toàn theo lệnh của Admin. Toàn bộ vé, khách hàng, nhân viên, dịch vụ, gói và sổ quỹ cũ đã được xóa sạch trên cả máy và Đám mây. Tài khoản Admin của bạn vẫn được giữ nguyên.",
                actionNote = "Đã xóa sạch toàn bộ dữ liệu ứng dụng",
                thoiGian = "${todayDateStr()} ${nowTimeStr()}"
            )
        )

        saveToPrefs()
        forcePushCurrentStateToCloud()

        return Result.success(
            "Đã xóa sạch toàn bộ dữ liệu ứng dụng trên máy & Đám mây ($countVe vé, $countKh khách, $countNv KTV, $countDv dịch vụ, $countGoi gói, $countTc phiếu quỹ). Chỉ giữ lại tài khoản Admin!"
        )
    }

    /**
     * Bộ 10 dịch vụ chuẩn trích xuất từ file menu_mới_nhất_2.json của bạn
     */
    fun danhSachDichVuMenuMoiNhat2(): List<DV> = listOf(
        DV("379590571336652", "Trị liệu 30 phút", 30, 100_000L, "Trị liệu bấm huyệt 30 phút", 40_000L),
        DV("379695801677810", "Trị liệu 60 phút", 60, 160_000L, "Trị liệu bấm huyệt 60 phút", 70_000L),
        DV("380937271360462", "Trị liệu 90 phút", 90, 220_000L, "Trị liệu bấm huyệt 90 phút", 100_000L),
        DV("381066495200777", "Trị liệu 120 phút", 120, 280_000L, "Trị liệu bấm huyệt 120 phút", 130_000L),
        DV("381815666098668", "Trị liệu 30 phút + xông hơi", 30, 200_000L, "Kết hợp xông hơi thảo dược", 40_000L),
        DV("381921712080763", "Trị liệu 60 phút + xông hơi", 60, 250_000L, "Kết hợp xông hơi thảo dược", 70_000L),
        DV("382099540258195", "Trị liệu 90 phút + xông hơi", 90, 300_000L, "Kết hợp xông hơi thảo dược", 100_000L),
        DV("382206880583363", "Trị liệu 120 phút + xông hơi", 120, 360_000L, "Kết hợp xông hơi thảo dược", 130_000L),
        DV("382283862142448", "Cạo gió giác hơi", 20, 50_000L, "Giải cảm cạo gió giác hơi", 25_000L),
        DV("382316401953581", "Xông hơi thảo dược", 30, 120_000L, "Xông hơi thảo dược giải cảm", 0L)
    )

    /**
     * Bộ 3 gói liệu trình chuẩn trích xuất từ file menu_mới_nhất_2.json của bạn
     */
    fun danhSachGoiMenuMoiNhat2(): List<Goi> = listOf(
        Goi(
            id = "675809461302872",
            ten = "Liệu trình 10 vé trị liệu 30 phút (11 buổi)",
            dvIds = listOf("379590571336652"),
            soBuoi = 11,
            gia = 1_000_000L,
            chiTietLuot = "379590571336652:11",
            dvTangKem = ""
        ),
        Goi(
            id = "676003166753527",
            ten = "Liệu trình 10 vé trị liệu 60 phút (11 buổi + tặng xông hơi)",
            dvIds = listOf("379695801677810"),
            soBuoi = 11,
            gia = 1_600_000L,
            chiTietLuot = "379695801677810:11",
            dvTangKem = "382316401953581:1"
        ),
        Goi(
            id = "676158327672947",
            ten = "Liệu trình 10 vé trị liệu 90 phút (11 buổi + tặng xông hơi)",
            dvIds = listOf("380937271360462"),
            soBuoi = 11,
            gia = 2_200_000L,
            chiTietLuot = "380937271360462:11",
            dvTangKem = "382316401953581:1"
        )
    )

    fun login(phoneOrUsername: String, rawPassword: String): Boolean {
        val cleanInput = phoneOrUsername.trim()
        val cleanLower = cleanInput.replace(" ", "").replace(".", "").lowercase()
        val normDigits = normalizePhoneDigits(cleanInput)
        val cleanPass = rawPassword.trim()
        val passHash = hashPassword(cleanPass)

        val matched = taiKhoans.firstOrNull { tk ->
            val tkUser = tk.username.trim().replace(" ", "").replace(".", "").lowercase()
            val tkDigits = normalizePhoneDigits(tk.username)
            val tkEmail = tk.email.trim().lowercase()
            val linkedNvPhoneRaw = nvs.firstOrNull { it.id == tk.nvId }?.sdt.orEmpty()
            val linkedNvDigits = normalizePhoneDigits(linkedNvPhoneRaw)

            val userMatches = (tkUser == cleanLower) ||
                (normDigits.length >= 7 && (tkDigits == normDigits || linkedNvDigits == normDigits)) ||
                (tkEmail.isNotBlank() && tkEmail == cleanLower)

            val passMatches = (tk.matKhauHash == passHash) ||
                (tk.matKhauHash == cleanPass) ||
                (tk.matKhauHienThi.isNotBlank() && tk.matKhauHienThi.trim() == cleanPass)

            userMatches && passMatches
        }

        if (matched != null) {
            // Nếu máy nhân viên đã kết nối Gmail mà tài khoản nhân viên chưa gắn Gmail -> tự động gắn Gmail cho lần sau
            val finalMatched = if (matched.email.isBlank() && cloudUserEmail.isNotBlank() && matched.id != "tk_admin") {
                val idx = taiKhoans.indexOfFirst { it.id == matched.id }
                val updated = matched.copy(email = cloudUserEmail.trim().lowercase())
                if (idx >= 0) {
                    taiKhoans[idx] = updated
                    saveToPrefs()
                }
                updated
            } else {
                matched
            }
            currentUser = finalMatched
            prefs.edit().putString(KEY_LOGGED_IN_ACCOUNT_ID, finalMatched.id).apply()
            syncCurrentUserProfileToCloud()
            return true
        }
        return false
    }

    /**
     * Tìm tài khoản nhân viên hoặc Admin khớp với Gmail đang kết nối đám mây trên máy
     */
    fun timTaiKhoanTheoGoogleEmail(email: String = cloudUserEmail): TaiKhoan? {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return null
        return taiKhoans.firstOrNull { it.email.trim().lowercase() == cleanEmail }
    }

    /**
     * Đăng nhập nhanh bằng tài khoản đã được Admin gắn sẵn Gmail
     */
    fun loginBangTaiKhoanDaGanEmail(tk: TaiKhoan): Boolean {
        currentUser = tk
        prefs.edit().putString(KEY_LOGGED_IN_ACCOUNT_ID, tk.id).apply()
        syncCurrentUserProfileToCloud()
        return true
    }

    /**
     * Khi máy phụ bấm Đăng nhập mà chưa kịp nhận bản tin đám mây:
     * Tự động kéo dữ liệu mới nhất từ Firestore về máy, gộp thông minh rồi thử đăng nhập lại ngay.
     */
    suspend fun pullLatestFromCloudAndLogin(phoneOrUsername: String, rawPassword: String): Boolean {
        if (login(phoneOrUsername, rawPassword)) return true
        val db = firestoreDb ?: return false
        if (firebaseAuth?.currentUser == null) return false
        return try {
            val remoteMap = SpaCloudRepository.getWorkspaceSnapshot(db)
            val remotePayload = remoteMap?.get("payloadJson") as? String
            if (!remotePayload.isNullOrBlank()) {
                val localAddedMore = mergeCloudPayloadJson(remotePayload)
                if (localAddedMore) {
                    pushToCloudWorkspace()
                }
            }
            login(phoneOrUsername, rawPassword)
        } catch (e: Exception) {
            Log.e(TAG, "pullLatestFromCloudAndLogin error", e)
            false
        }
    }

    /**
     * Chủ động tải dữ liệu mới nhất từ đám mây về máy ngay lập tức
     */
    suspend fun refreshFromCloudNow(): Boolean {
        val db = firestoreDb ?: return false
        if (firebaseAuth?.currentUser == null) return false
        return try {
            val remoteMap = SpaCloudRepository.getWorkspaceSnapshot(db)
            val remotePayload = remoteMap?.get("payloadJson") as? String
            if (!remotePayload.isNullOrBlank()) {
                val localHadNewItems = mergeCloudPayloadJson(remotePayload)
                if (localHadNewItems) {
                    pushToCloudWorkspace()
                }
                lastCloudSyncTime = "${todayDateStr()} ${nowTimeStr()}"
                cloudSyncStatus = "Đã đồng bộ 100% (${taiKhoans.size} TK • ${khs.size} KH • ${luots.size} vé)"
                true
            } else {
                forcePushCurrentStateToCloud()
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "refreshFromCloudNow error", e)
            false
        }
    }

    fun logout() {
        currentUser = null
        prefs.edit().remove(KEY_LOGGED_IN_ACCOUNT_ID).apply()
    }

    /**
     * Ngắt kết nối tài khoản Google Đám mây hiện tại trên máy để chọn sang một tài khoản Google (Gmail) mới bất kỳ lúc nào.
     * Toàn bộ dữ liệu của quán trên máy và trên không gian chung (khiem_thi_viet_shared) vẫn được giữ nguyên 100%.
     */
    fun disconnectGoogleCloudForSwitch() {
        runCatching { workspaceListener?.remove() }
        workspaceListener = null
        runCatching { firebaseAuth?.signOut() }
        isCloudConnected = false
        cloudUserEmail = ""
        cloudUserName = ""
        cloudSyncStatus = "Đã ngắt Gmail cũ — Sẵn sàng chọn tài khoản Google mới"
    }

    /**
     * Bắt đầu lắng nghe và đồng bộ dữ liệu thời gian thực 100% giữa máy Admin và mọi máy Nhân viên
     * qua Firebase Cloud Firestore (collection spa_workspaces / document khiem_thi_viet_shared).
     */
    fun startRealtimeCloudSync() {
        val auth = firebaseAuth ?: return
        val fbUser = auth.currentUser ?: run {
            isCloudConnected = false
            cloudSyncStatus = "Chưa kết nối đám mây Google"
            return
        }
        val db = firestoreDb ?: return

        isCloudConnected = true
        cloudUserEmail = fbUser.email.orEmpty()
        cloudUserName = fbUser.displayName.orEmpty()
        cloudSyncStatus = "Đang kết nối đồng bộ thời gian thực..."

        syncCurrentUserProfileToCloud()

        workspaceListener?.remove()
        val docRef = db.collection(SpaCloudRepository.WORKSPACE_COLLECTION)
            .document(SpaCloudRepository.SHARED_WORKSPACE_ID)

        workspaceListener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                try {
                    handleFirestoreError(
                        error,
                        OperationType.GET,
                        "${SpaCloudRepository.WORKSPACE_COLLECTION}/${SpaCloudRepository.SHARED_WORKSPACE_ID}"
                    )
                } catch (_: Exception) {
                }
                cloudSyncStatus = "Lỗi đồng bộ đám mây: ${error.localizedMessage ?: "Kiểm tra mạng"}"
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                initialCloudCheckComplete = true
                val remotePayload = snapshot.getString("payloadJson").orEmpty()
                val updatedBy = snapshot.getString("updatedByName").orEmpty().ifBlank { "Máy khác" }
                if (remotePayload.isNotBlank()) {
                    val currentLocalPayload = prefs.getString(KEY_DATA_JSON, "").orEmpty()
                    if (remotePayload != currentLocalPayload) {
                        val localHadUnsyncedItems = mergeCloudPayloadJson(remotePayload)
                        if (localHadUnsyncedItems) {
                            // Nếu máy Admin vừa tạo tài khoản trước khi nối mây -> đẩy ngay bản gộp đầy đủ lên mây
                            pushToCloudWorkspace()
                        }
                    }
                    lastCloudSyncTime = "${todayDateStr()} ${nowTimeStr()}"
                    cloudSyncStatus = "Đã đồng bộ 100% (${taiKhoans.size} TK • ${khs.size} KH • ${luots.size} vé) • Cập nhật bởi: $updatedBy"
                }
            } else if (snapshot != null && !snapshot.exists()) {
                // Nếu trên đám mây chưa có dữ liệu cửa hàng -> Máy đầu tiên đẩy dữ liệu chuẩn lên đám mây
                initialCloudCheckComplete = true
                pushToCloudWorkspace()
            }
        }
    }

    /**
     * Ép đẩy toàn bộ dữ liệu hiện tại của máy lên đám mây ngay lập tức (dành cho Admin hoặc sau khi nhập Backup)
     */
    fun forcePushCurrentStateToCloud() {
        initialCloudCheckComplete = true
        pushToCloudWorkspace()
    }

    /**
     * Tải và khôi phục ngay dữ liệu mới nhất từ Đám mây Firebase (dùng khi vừa cài lại app hoặc bấm nút Đồng bộ & Khôi phục ngay)
     */
    fun dongBoKhoiPhucTuDamMayNgay(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val auth = firebaseAuth
        val fbUser = auth?.currentUser
        val db = firestoreDb
        if (fbUser == null || db == null) {
            onResult(false, "Chưa đăng nhập tài khoản Google. Vui lòng bấm Kết nối Google trước.")
            return
        }
        cloudSyncStatus = "Đang kiểm tra & tải bản sao lưu mới nhất từ Đám mây..."
        db.collection(SpaCloudRepository.WORKSPACE_COLLECTION)
            .document(SpaCloudRepository.SHARED_WORKSPACE_ID)
            .get()
            .addOnSuccessListener { snapshot ->
                initialCloudCheckComplete = true
                if (snapshot != null && snapshot.exists()) {
                    val remotePayload = snapshot.getString("payloadJson").orEmpty()
                    if (remotePayload.isNotBlank()) {
                        val localHadUnsynced = mergeCloudPayloadJson(remotePayload)
                        if (localHadUnsynced) {
                            pushToCloudWorkspace()
                        }
                        lastCloudSyncTime = "${todayDateStr()} ${nowTimeStr()}"
                        val msg = "Đã khôi phục & đồng bộ 100% từ Đám mây (${khs.size} khách • ${luots.size} vé • ${tcs.size} phiếu quỹ)!"
                        cloudSyncStatus = msg
                        onResult(true, msg)
                    } else {
                        pushToCloudWorkspace()
                        onResult(true, "Đã đẩy dữ liệu hiện tại lên Đám mây Google thành công!")
                    }
                } else {
                    pushToCloudWorkspace()
                    onResult(true, "Đã khởi tạo bản sao lưu đầu tiên trên Đám mây Google!")
                }
            }
            .addOnFailureListener { e ->
                val err = "Không thể kết nối Đám mây: ${e.localizedMessage ?: "Kiểm tra mạng Internet"}"
                cloudSyncStatus = err
                onResult(false, err)
            }
    }

    private fun syncCurrentUserProfileToCloud() {
        val auth = firebaseAuth ?: return
        val fbUser = auth.currentUser ?: return
        val db = firestoreDb ?: return
        val spaAcc = currentUser
        val path = "${SpaCloudRepository.USERS_COLLECTION}/${fbUser.uid}"
        val profileData = mapOf(
            "uid" to fbUser.uid,
            "displayName" to (spaAcc?.hoTen?.ifBlank { null } ?: fbUser.displayName ?: "Nhân sự Khiếm Thị Việt").take(150),
            "email" to (fbUser.email ?: "user@khiemthiviet.vn").take(254),
            "spaPhone" to (spaAcc?.username ?: "").take(30),
            "spaRole" to if (spaAcc?.isAdmin() == true) "ADMIN" else "STAFF",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection(SpaCloudRepository.USERS_COLLECTION)
            .document(fbUser.uid)
            .set(profileData)
            .addOnFailureListener { e ->
                if (e is FirebaseFirestoreException) {
                    runCatching { handleFirestoreError(e, OperationType.WRITE, path) }
                }
            }
    }

    private fun pushToCloudWorkspace(payloadOverride: String? = null) {
        if (isApplyingCloudSnapshot) return
        val auth = firebaseAuth ?: return
        val fbUser = auth.currentUser ?: return
        val db = firestoreDb ?: return
        if (!initialCloudCheckComplete) return

        val payloadStr = payloadOverride ?: buildBackupJsonObject(forCloudSync = true).toString()
        val updaterName = currentUser?.hoTen?.takeIf { it.isNotBlank() }
            ?: fbUser.displayName?.takeIf { it.isNotBlank() }
            ?: fbUser.email?.takeIf { it.isNotBlank() }
            ?: "Thiết bị Khiếm Thị Việt"

        val path = "${SpaCloudRepository.WORKSPACE_COLLECTION}/${SpaCloudRepository.SHARED_WORKSPACE_ID}"
        val docData = mapOf(
            "workspaceId" to SpaCloudRepository.SHARED_WORKSPACE_ID,
            "shopName" to "KHIẾM THỊ VIỆT",
            "payloadJson" to payloadStr,
            "accountsCount" to taiKhoans.size.coerceIn(1, 500),
            "customersCount" to khs.size.coerceIn(0, 10000),
            "ticketsCount" to luots.size.coerceIn(0, 50000),
            "updatedByUid" to fbUser.uid,
            "updatedByName" to updaterName.take(200),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection(SpaCloudRepository.WORKSPACE_COLLECTION)
            .document(SpaCloudRepository.SHARED_WORKSPACE_ID)
            .set(docData)
            .addOnSuccessListener {
                lastCloudSyncTime = "${todayDateStr()} ${nowTimeStr()}"
                cloudSyncStatus = "Đã đồng bộ 100% (${taiKhoans.size} TK • ${khs.size} KH • ${luots.size} vé)"
            }
            .addOnFailureListener { e ->
                if (e is FirebaseFirestoreException) {
                    runCatching { handleFirestoreError(e, OperationType.WRITE, path) }
                }
                cloudSyncStatus = "Chờ mạng để đồng bộ (${e.localizedMessage ?: "Offline"})"
            }
    }

    /**
     * Gộp thông minh dữ liệu từ đám mây với dữ liệu trên máy (Smart Cloud Merge):
     * - Không bao giờ làm mất tài khoản nhân viên hoặc vé mà Admin/Nhân viên vừa tạo trên máy trước khi nối mây.
     * - Cập nhật đầy đủ tài khoản mới, mật khẩu mới, vé mới, khách hàng mới từ máy khác gửi lên.
     * - Tôn trọng danh sách ID đã xóa chủ động (deletedIds).
     * @return true nếu máy hiện tại có thêm dữ liệu mới chưa có trong bản đám mây (cần đẩy ngược lên đám mây).
     */
    private fun mergeCloudPayloadJson(remotePayloadJson: String): Boolean {
        isApplyingCloudSnapshot = true
        try {
            val localTksBefore = taiKhoans.toList()
            val localNvsBefore = nvs.toList()
            val localDvsBefore = dvs.toList()
            val localGoisBefore = gois.toList()
            val localKhsBefore = khs.toList()
            val localDksBefore = dks.toList()
            val localLuotsBefore = luots.toList()
            val localTcsBefore = tcs.toList()
            val localMemoriesBefore = aiLongTermMemories.toList()
            val localMessagesBefore = aiMessages.toList()
            val localDeletedBefore = deletedIds.toSet()

            // Nếu máy hiện tại chỉ đang chứa dữ liệu mẫu mặc định lúc mới cài lại app (seed data),
            // khi tải dữ liệu thật từ Đám mây về sẽ loại bỏ hoàn toàn dữ liệu mẫu để khôi phục 100% bản gốc của quán!
            val isLocalOnlyDefaultSeed = localLuotsBefore.all { it.id in SEED_SAMPLE_IDS } &&
                localKhsBefore.all { it.id in SEED_SAMPLE_IDS }

            // Tạm nạp dữ liệu từ đám mây vào bộ nhớ
            prefs.edit().putString(KEY_DATA_JSON, remotePayloadJson).apply()
            loadFromPrefs()

            val allDeleted = (deletedIds.toSet() + localDeletedBefore).toSet()
            deletedIds.clear()
            deletedIds.addAll(allDeleted)

            val remoteTkIds = taiKhoans.map { it.id }.toSet()
            val remoteDkIds = dks.map { it.id }.toSet()
            val remoteLuotIds = luots.map { it.id }.toSet()
            val remoteTcIds = tcs.map { it.id }.toSet()

            var localHadUnsyncedItems = false

            if (!isLocalOnlyDefaultSeed) {
                // Giữ lại mọi Nhân viên được tạo trên máy này mà chưa có trên mây (và chưa bị xóa)
                for (localNv in localNvsBefore) {
                    if (localNv.id in allDeleted || localNv.id in SEED_SAMPLE_IDS) continue
                    val normPhone = normalizePhoneDigits(localNv.sdt)
                    val existsInRemote = nvs.any {
                        it.id == localNv.id || (normPhone.length >= 7 && normalizePhoneDigits(it.sdt) == normPhone)
                    }
                    if (!existsInRemote) {
                        nvs.add(localNv)
                        localHadUnsyncedItems = true
                    }
                }
                nvs.removeAll { it.id in allDeleted }

                // Giữ lại mọi Tài khoản được tạo trên máy này mà chưa có trên mây (và chưa bị xóa)
                for (localTk in localTksBefore) {
                    if (localTk.id in allDeleted || localTk.id in SEED_SAMPLE_IDS) continue
                    val normUser = normalizePhoneDigits(localTk.username)
                    val existingIdx = taiKhoans.indexOfFirst {
                        it.id == localTk.id || (normUser.length >= 7 && normalizePhoneDigits(it.username) == normUser)
                    }
                    if (existingIdx < 0) {
                        taiKhoans.add(localTk)
                        localHadUnsyncedItems = true
                    } else if (currentUser?.isAdmin() == true && localTk.id !in remoteTkIds) {
                        taiKhoans[existingIdx] = localTk
                        localHadUnsyncedItems = true
                    }
                }
                taiKhoans.removeAll { it.id in allDeleted && it.id != "tk_admin" }

                // Giữ lại Dịch vụ & Gói mới tạo trên máy
                for (localDv in localDvsBefore) {
                    if (localDv.id in allDeleted) continue
                    if (dvs.none { it.id == localDv.id }) {
                        dvs.add(localDv)
                        localHadUnsyncedItems = true
                    }
                }
                dvs.removeAll { it.id in allDeleted }

                for (localGoi in localGoisBefore) {
                    if (localGoi.id in allDeleted) continue
                    if (gois.none { it.id == localGoi.id }) {
                        gois.add(localGoi)
                        localHadUnsyncedItems = true
                    }
                }
                gois.removeAll { it.id in allDeleted }

                // Giữ lại Khách hàng mới tạo trên máy
                for (localKh in localKhsBefore) {
                    if (localKh.id in allDeleted || localKh.id in SEED_SAMPLE_IDS) continue
                    val normPhone = normalizePhoneDigits(localKh.sdt)
                    val existsInRemote = khs.any {
                        it.id == localKh.id || (normPhone.length >= 7 && normalizePhoneDigits(it.sdt) == normPhone)
                    }
                    if (!existsInRemote) {
                        khs.add(localKh)
                        localHadUnsyncedItems = true
                    }
                }
                khs.removeAll { it.id in allDeleted }

                // Giữ lại Đăng ký gói, Vé và Sổ quỹ mới tạo trên máy
                for (localDk in localDksBefore) {
                    if (localDk.id in allDeleted || localDk.id in SEED_SAMPLE_IDS) continue
                    if (localDk.id !in remoteDkIds) {
                        dks.add(localDk)
                        localHadUnsyncedItems = true
                    }
                }
                dks.removeAll { it.id in allDeleted }

                for (localLuot in localLuotsBefore) {
                    if (localLuot.id in allDeleted || localLuot.id in SEED_SAMPLE_IDS) continue
                    if (localLuot.id !in remoteLuotIds) {
                        luots.add(0, localLuot)
                        localHadUnsyncedItems = true
                    }
                }
                luots.removeAll { it.id in allDeleted }

                for (localTc in localTcsBefore) {
                    if (localTc.id in allDeleted || localTc.id in SEED_SAMPLE_IDS) continue
                    if (localTc.id !in remoteTcIds) {
                        tcs.add(0, localTc)
                        localHadUnsyncedItems = true
                    }
                }
                tcs.removeAll { it.id in allDeleted }
            }

            // Gộp bộ nhớ dài hạn và lịch sử chat trên máy (Local Vault) với bản Đám mây để không mất bất kỳ ký ức nào
            for (mem in localMemoriesBefore) {
                if (mem.isNotBlank() && !aiLongTermMemories.contains(mem)) {
                    aiLongTermMemories.add(mem)
                }
            }
            nenBoNhoDaiHanCuonChieu()

            val existingMsgIds = aiMessages.map { it.id }.toSet()
            for (msg in localMessagesBefore) {
                if (msg.id !in existingMsgIds && msg.id != "welcome") {
                    aiMessages.add(msg)
                }
            }
            nenTinNhanAiCuonChieu()

            dongBoTatCaPhieuThuChiTuVeDaChot(luuNgay = false)
            val mergedJson = buildBackupJsonObject().toString()
            prefs.edit().putString(KEY_DATA_JSON, mergedJson).apply()
            saveGehihiMemoryVault()

            // Cập nhật lại tham chiếu tài khoản đang đăng nhập trên máy (nếu Admin vừa sửa tên/quyền/mật khẩu)
            val loggedInId = currentUser?.id ?: prefs.getString(KEY_LOGGED_IN_ACCOUNT_ID, null)
            if (!loggedInId.isNullOrBlank()) {
                val updatedAccount = taiKhoans.firstOrNull { it.id == loggedInId }
                if (updatedAccount != null) {
                    currentUser = updatedAccount
                }
            }
            return localHadUnsyncedItems
        } catch (e: Exception) {
            Log.e(TAG, "Failed to merge cloud workspace payload", e)
            return false
        } finally {
            isApplyingCloudSnapshot = false
        }
    }

    // =========================================================================
    // 1. HỆ THỐNG BACKUP XUẤT / NHẬP DỮ LIỆU (HỖ TRỢ CẢ FILE BACKUP APP CŨ & MỚI)
    // =========================================================================

    /**
     * Xuất toàn bộ dữ liệu hiện tại ra chuỗi JSON chuẩn đẹp (tương thích cả 2 chiều)
     */
    fun xuatDuLieuBackupJson(): String {
        val root = buildBackupJsonObject()
        return root.toString(2)
    }

    /**
     * Khôi phục nhanh 10 dịch vụ và 3 gói liệu trình từ file menu_mới_nhất_2.json
     */
    fun napMenuChuanTuFileBackupCu(): String {
        val dsDvMoi = danhSachDichVuMenuMoiNhat2()
        val dsGoiMoi = danhSachGoiMenuMoiNhat2()

        dsDvMoi.forEach { dvMoi ->
            val idx = dvs.indexOfFirst { it.id == dvMoi.id || it.ten.equals(dvMoi.ten, ignoreCase = true) }
            if (idx >= 0) dvs[idx] = dvMoi else dvs.add(dvMoi)
        }
        dsGoiMoi.forEach { goiMoi ->
            val idx = gois.indexOfFirst { it.id == goiMoi.id || it.ten.equals(goiMoi.ten, ignoreCase = true) }
            if (idx >= 0) gois[idx] = goiMoi else gois.add(goiMoi)
        }
        saveToPrefs()
        return "Đã nạp thành công ${dsDvMoi.size} dịch vụ và ${dsGoiMoi.size} gói liệu trình từ menu_mới_nhất_2.json!"
    }

    /**
     * Nhập toàn bộ dữ liệu từ file JSON Backup (Hỗ trợ tự động cả cấu trúc Backup cũ menu_mới_nhất_2.json
     * với trường 'ck', 'tks', 'dvIds' dạng chuỗi VÀ cấu trúc Backup mới).
     */
    fun nhapDuLieuTuJsonBackup(rawJson: String, ghiDeToanBo: Boolean = false): Result<String> {
        return try {
            val root = JSONObject(rawJson.trim())

            if (ghiDeToanBo) {
                nvs.clear()
                dvs.clear()
                gois.clear()
                khs.clear()
                dks.clear()
                luots.clear()
                tcs.clear()
                // Giữ lại tài khoản Admin gốc đang đăng nhập để không bị văng khỏi quyền Admin
                val adminRoot = taiKhoans.firstOrNull { it.id == "tk_admin" } ?: currentUser
                taiKhoans.clear()
                if (adminRoot != null) taiKhoans.add(adminRoot)
            }

            var countNv = 0
            var countDv = 0
            var countGoi = 0
            var countKh = 0
            var countDk = 0
            var countLuot = 0
            var countTc = 0

            // 1. Đọc Nhân viên (nvs)
            root.optJSONArray("nvs")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.opt("id")?.toString()?.trim().orEmpty().ifBlank { "nv_${shortId()}" }
                    val ten = o.optString("ten", "").trim()
                    if (ten.isBlank()) continue
                    val gt = o.optString("gt", "Nam").trim().ifBlank { "Nam" }
                    val sdt = o.optString("sdt", "").trim()
                    val gc = o.optString("gc", "").trim()
                    val nvItem = NV(id = id, ten = ten, gt = gt, sdt = sdt, gc = gc)
                    val existingIdx = nvs.indexOfFirst { it.id == id || (sdt.isNotBlank() && it.sdt == sdt) }
                    if (existingIdx >= 0) nvs[existingIdx] = nvItem else nvs.add(nvItem)
                    countNv++
                }
            }

            // 2. Đọc Dịch vụ (dvs) - Hỗ trợ cả 'chietKhauVnd' (mới) và 'ck' (file backup cũ)
            root.optJSONArray("dvs")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.opt("id")?.toString()?.trim().orEmpty().ifBlank { "dv_${shortId()}" }
                    val ten = o.optString("ten", "").trim()
                    if (ten.isBlank()) continue
                    val phut = o.optInt("phut", 60)
                    val gia = o.optLong("gia", 0L)
                    val moTa = o.optString("moTa", "").trim()
                    val ck = if (o.has("chietKhauVnd")) o.optLong("chietKhauVnd", 0L) else o.optLong("ck", 0L)
                    val dvItem = DV(id = id, ten = ten, phut = phut, gia = gia, moTa = moTa, chietKhauVnd = ck)
                    val existingIdx = dvs.indexOfFirst { it.id == id || it.ten.equals(ten, ignoreCase = true) }
                    if (existingIdx >= 0) dvs[existingIdx] = dvItem else dvs.add(dvItem)
                    countDv++
                }
            }

            // 3. Đọc Gói liệu trình (gois) - Hỗ trợ cả dvIds dạng JSONArray (mới) và String "379590571336652" (cũ)
            root.optJSONArray("gois")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.opt("id")?.toString()?.trim().orEmpty().ifBlank { "goi_${shortId()}" }
                    val ten = o.optString("ten", "").trim().replace("10 bé", "10 vé")
                    if (ten.isBlank()) continue
                    val dvIdsList = mutableListOf<String>()
                    val rawDvIds = o.opt("dvIds")
                    when (rawDvIds) {
                        is JSONArray -> {
                            for (j in 0 until rawDvIds.length()) {
                                val item = rawDvIds.opt(j)?.toString()?.trim().orEmpty()
                                if (item.isNotBlank()) dvIdsList.add(item)
                            }
                        }
                        is String -> {
                            rawDvIds.split(",", ";", "|").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                                dvIdsList.add(it.substringBefore(":"))
                            }
                        }
                    }
                    val soBuoi = o.optInt("soBuoi", 10)
                    val gia = o.optLong("gia", 0L)
                    val chiTietLuot = o.optString("chiTietLuot", "")
                    val dvTangKem = o.optString("dvTangKem", "")
                    val goiItem = Goi(
                        id = id,
                        ten = ten,
                        dvIds = dvIdsList,
                        soBuoi = soBuoi,
                        gia = gia,
                        chiTietLuot = chiTietLuot,
                        dvTangKem = dvTangKem
                    )
                    val existingIdx = gois.indexOfFirst { it.id == id || it.ten.equals(ten, ignoreCase = true) }
                    if (existingIdx >= 0) gois[existingIdx] = goiItem else gois.add(goiItem)
                    countGoi++
                }
            }

            // 4. Đọc Khách hàng (khs) - Định danh theo SĐT hoặc ID
            root.optJSONArray("khs")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.opt("id")?.toString()?.trim().orEmpty().ifBlank { "kh_${shortId()}" }
                    val ten = o.optString("ten", "").trim()
                    if (ten.isBlank()) continue
                    val sdt = o.optString("sdt", "").trim()
                    val khItem = KH(id = id, ten = ten, sdt = sdt)
                    val existingIdx = khs.indexOfFirst {
                        it.id == id || (sdt.length >= 7 && it.sdt.filter { c -> c.isDigit() } == sdt.filter { c -> c.isDigit() })
                    }
                    if (existingIdx >= 0) khs[existingIdx] = khItem else khs.add(khItem)
                    countKh++
                }
            }

            // 5. Đọc Đăng ký gói (dks)
            root.optJSONArray("dks")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.opt("id")?.toString()?.trim().orEmpty().ifBlank { "dk_${shortId()}" }
                    val khId = o.opt("khId")?.toString()?.trim().orEmpty()
                    val goiId = o.opt("goiId")?.toString()?.trim().orEmpty()
                    if (khId.isBlank() || goiId.isBlank()) continue
                    val ngay = o.optString("ngay", todayDateStr())
                    val daDung = o.optBoolean("daDung", false)
                    val luotDaDung = o.optInt("luotDaDung", 0)
                    val dkItem = DK(id = id, khId = khId, goiId = goiId, ngay = ngay, daDung = daDung, luotDaDung = luotDaDung)
                    val existingIdx = dks.indexOfFirst { it.id == id }
                    if (existingIdx >= 0) dks[existingIdx] = dkItem else dks.add(dkItem)
                    countDk++
                }
            }

            // 6. Đọc Vé phục vụ (luots)
            root.optJSONArray("luots")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.opt("id")?.toString()?.trim().orEmpty().ifBlank { "ve_${shortId()}" }
                    val khId = o.opt("khId")?.toString()?.trim().orEmpty()
                    val nvId = o.opt("nvId")?.toString()?.trim().orEmpty()
                    val dvIdsList = mutableListOf<String>()
                    val rawDvIds = o.opt("dvIds")
                    when (rawDvIds) {
                        is JSONArray -> {
                            for (j in 0 until rawDvIds.length()) {
                                val s = rawDvIds.opt(j)?.toString()?.trim().orEmpty()
                                if (s.isNotBlank()) dvIdsList.add(s)
                            }
                        }
                        is String -> {
                            rawDvIds.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { dvIdsList.add(it) }
                        }
                    }
                    val dkId = o.opt("dkId")?.toString()?.trim().orEmpty().let { if (it == "-1" || it == "0") "" else it }
                    val tgbd = o.optString("tgbd", nowTimeStr())
                    val tgkt = o.optString("tgkt", "")
                    val thu = o.optLong("thu", 0L)
                    val daXacNhan = o.optBoolean("daXacNhan", false)
                    val ngay = o.optString("ngay", todayDateStr())
                    val dvNgoai = o.optString("dvNgoai", "").trim()
                    val hoaHongChot = if (o.has("hoaHongChot") && !o.isNull("hoaHongChot")) o.optLong("hoaHongChot") else null
                    val phieuChiHoaHongId = o.optString("phieuChiHoaHongId", "").trim()
                    val phieuThuVeId = o.optString("phieuThuVeId", "").trim()
                    val luotItem = Luot(
                        id = id,
                        khId = khId,
                        nvId = nvId,
                        dvIds = dvIdsList,
                        dkId = dkId,
                        tgbd = tgbd,
                        tgkt = tgkt,
                        thu = thu,
                        daXacNhan = daXacNhan,
                        ngay = ngay,
                        dvNgoai = dvNgoai,
                        hoaHongChot = hoaHongChot,
                        phieuChiHoaHongId = phieuChiHoaHongId,
                        phieuThuVeId = phieuThuVeId
                    )
                    val existingIdx = luots.indexOfFirst { it.id == id }
                    if (existingIdx >= 0) luots[existingIdx] = luotItem else luots.add(luotItem)
                    countLuot++
                }
            }

            // 7. Đọc Sổ quỹ Thu Chi (tcs)
            root.optJSONArray("tcs")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.opt("id")?.toString()?.trim().orEmpty().ifBlank { "tc_${shortId()}" }
                    val loaiStr = o.optString("loai", "THU").uppercase()
                    val loai = if (loaiStr.contains("CHI")) LoaiTC.CHI else LoaiTC.THU
                    val tien = o.optLong("tien", 0L)
                    val lyDo = o.optString("lyDo", "").trim()
                    val ngay = o.optString("ngay", todayDateStr())
                    val tcItem = TC(id = id, loai = loai, tien = tien, lyDo = lyDo, ngay = ngay)
                    val existingIdx = tcs.indexOfFirst { it.id == id }
                    if (existingIdx >= 0) tcs[existingIdx] = tcItem else tcs.add(tcItem)
                    countTc++
                }
            }

            // 8. Đọc Tài khoản (Hỗ trợ cả 'taiKhoans' mới và 'tks' của app cũ)
            val tkArray = root.optJSONArray("taiKhoans") ?: root.optJSONArray("tks")
            tkArray?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val rawUsername = o.optString("username", "").trim()
                    if (rawUsername.isBlank() || rawUsername.equals("admin", ignoreCase = true)) {
                        // Giữ nguyên tài khoản Admin gốc chuẩn 0397772963 / 247208
                        continue
                    }
                    val id = o.opt("id")?.toString()?.trim().orEmpty().ifBlank { "tk_${shortId()}" }
                    val nvIdRaw = o.opt("nvId")?.toString()?.trim().orEmpty().let { if (it == "-1" || it == "0") "" else it }
                    val quyenStr = o.optString("quyen", "STAFF").uppercase()
                    val quyen = if (quyenStr == "OWNER" || quyenStr == "ADMIN") VaiTro.OWNER else VaiTro.STAFF
                    val hoTen = o.optString("hoTen", rawUsername)
                    val perms = mutableSetOf<QuyenHan>()
                    o.optJSONArray("cacQuyenDuocCap")?.let { pArr ->
                        for (j in 0 until pArr.length()) {
                            runCatching { QuyenHan.valueOf(pArr.getString(j)) }.getOrNull()?.let { perms.add(it) }
                        }
                    }
                    val tkItem = TaiKhoan(
                        id = id,
                        username = rawUsername,
                        matKhauHash = o.optString("matKhauHash", hashPassword("123456")),
                        quyen = quyen,
                        nvId = nvIdRaw,
                        hoTen = hoTen,
                        cacQuyenDuocCap = perms,
                        matKhauHienThi = o.optString("matKhauHienThi", "123456")
                    )
                    val existingIdx = taiKhoans.indexOfFirst { it.id == id || it.username.equals(rawUsername, ignoreCase = true) }
                    if (existingIdx >= 0) taiKhoans[existingIdx] = tkItem else taiKhoans.add(tkItem)
                }
            }

            // Tự động đảm bảo mỗi nhân viên đều có 1 tài khoản đăng nhập bằng SĐT
            nvs.forEach { nv ->
                val hasTk = taiKhoans.any { it.nvId == nv.id || (nv.sdt.isNotBlank() && it.username == nv.sdt) }
                if (!hasTk) {
                    val phoneUser = normalizePhoneOrUsername(nv.sdt, nv.ten, taiKhoans.size + 1)
                    taiKhoans.add(
                        TaiKhoan(
                            id = "tk_${shortId()}",
                            username = phoneUser,
                            matKhauHash = hashPassword("123456"),
                            quyen = VaiTro.STAFF,
                            nvId = nv.id,
                            hoTen = nv.ten,
                            cacQuyenDuocCap = emptySet(),
                            matKhauHienThi = "123456"
                        )
                    )
                }
            }

            // Khôi phục cả lịch sử trò chuyện và bộ nhớ dài hạn của gehihi nếu có trong file Backup
            root.optJSONArray("aiLongTermMemories")?.let { mArr ->
                for (i in 0 until mArr.length()) {
                    val m = mArr.optString(i).trim()
                    if (m.isNotBlank() && !aiLongTermMemories.contains(m)) {
                        aiLongTermMemories.add(m)
                    }
                }
                nenBoNhoDaiHanCuonChieu()
            }
            root.optJSONArray("aiMessages")?.let { msgArr ->
                if (ghiDeToanBo && msgArr.length() > 0) aiMessages.clear()
                for (i in 0 until msgArr.length()) {
                    val o = msgArr.optJSONObject(i) ?: continue
                    val id = o.optString("id").ifBlank { "msg_${shortId()}" }
                    val text = o.optString("text")
                    if (text.isNotBlank() && aiMessages.none { it.id == id }) {
                        aiMessages.add(
                            AiChatMessage(
                                id = id,
                                isUser = o.optBoolean("isUser", false),
                                text = text,
                                actionNote = o.optString("actionNote", ""),
                                thoiGian = o.optString("thoiGian", "")
                            )
                        )
                    }
                }
                nenTinNhanAiCuonChieu()
            }

            dongBoTatCaPhieuThuChiTuVeDaChot(luuNgay = false)
            ghiNhoSuKienDaiHan("Nhập Backup thành công: $countDv DV, $countGoi gói, $countKh khách, $countNv KTV, $countLuot vé.", luuNgay = false)
            saveToPrefs()
            Result.success(
                "Đã nhập Backup thành công: $countDv dịch vụ, $countGoi gói liệu trình, $countKh khách hàng, $countNv nhân viên, $countLuot vé, ${tcs.size} giao dịch sổ quỹ."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Backup import failed", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 2. QUẢN TRỊ TÀI KHOẢN, NHÂN VIÊN & PHÂN QUYỀN (CHỈ ADMIN)
    // =========================================================================
    fun themNhanVien(
        ten: String,
        gt: String,
        sdt: String,
        gc: String,
        matKhauKhoiTao: String = "123456",
        laAdmin: Boolean = false,
        email: String = ""
    ): NV? {
        if (currentUser != null && !currentUser.isAdmin()) return null
        val cleanTen = ten.trim().ifBlank { "Nhân viên mới" }
        val cleanSdt = normalizePhoneDigits(sdt).ifBlank { sdt.trim().replace(" ", "") }
        val usernamePhone = normalizePhoneOrUsername(cleanSdt, cleanTen, nvs.size + 1)
        val cleanPass = matKhauKhoiTao.trim().ifBlank { "123456" }
        val cleanEmail = email.trim().lowercase()

        // Nếu số điện thoại này ĐÃ CÓ tài khoản hoặc nhân viên -> cập nhật lại đúng mật khẩu, tên, quyền và email cho SĐT đó
        // (Tuyệt đối không tạo hậu tố _2 ẩn làm nhân viên đăng nhập bị báo sai mật khẩu!)
        val existingTkIdx = taiKhoans.indexOfFirst {
            it.username.equals(usernamePhone, ignoreCase = true) ||
                (cleanSdt.length >= 7 && normalizePhoneDigits(it.username) == cleanSdt)
        }
        val existingNvIdx = nvs.indexOfFirst {
            cleanSdt.length >= 7 && normalizePhoneDigits(it.sdt) == cleanSdt
        }

        val nv: NV
        if (existingNvIdx >= 0) {
            val oldNv = nvs[existingNvIdx]
            nv = oldNv.copy(
                ten = cleanTen,
                gt = gt.trim().ifBlank { oldNv.gt },
                sdt = usernamePhone,
                gc = gc.trim().ifBlank { oldNv.gc }
            )
            nvs[existingNvIdx] = nv
            deletedIds.remove(nv.id)
        } else {
            val nvId = if (existingTkIdx >= 0 && taiKhoans[existingTkIdx].nvId.isNotBlank()) {
                taiKhoans[existingTkIdx].nvId
            } else {
                "nv_${shortId()}"
            }
            nv = NV(
                id = nvId,
                ten = cleanTen,
                gt = gt.trim().ifBlank { "Nam" },
                sdt = usernamePhone,
                gc = gc.trim()
            )
            nvs.add(0, nv)
        }

        if (existingTkIdx >= 0) {
            val oldTk = taiKhoans[existingTkIdx]
            val updatedTk = oldTk.copy(
                username = usernamePhone,
                matKhauHash = hashPassword(cleanPass),
                quyen = if (laAdmin || oldTk.id == "tk_admin") VaiTro.OWNER else VaiTro.STAFF,
                nvId = nv.id,
                hoTen = cleanTen,
                cacQuyenDuocCap = if (laAdmin) QuyenHan.entries.toSet() else oldTk.cacQuyenDuocCap,
                matKhauHienThi = cleanPass,
                email = cleanEmail.ifBlank { oldTk.email }
            )
            taiKhoans[existingTkIdx] = updatedTk
            deletedIds.remove(updatedTk.id)
        } else {
            val tk = TaiKhoan(
                id = "tk_${shortId()}",
                username = usernamePhone,
                matKhauHash = hashPassword(cleanPass),
                quyen = if (laAdmin) VaiTro.OWNER else VaiTro.STAFF,
                nvId = nv.id,
                hoTen = cleanTen,
                cacQuyenDuocCap = if (laAdmin) QuyenHan.entries.toSet() else emptySet(),
                matKhauHienThi = cleanPass,
                email = cleanEmail
            )
            taiKhoans.add(tk)
        }

        saveToPrefs()
        return nv
    }

    fun adminCapNhatTaiKhoanVaNhanVien(
        tkId: String,
        hoTenMoi: String,
        sdtDangNhapMoi: String,
        matKhauMoi: String,
        gtMoi: String = "Nam",
        ghiChuMoi: String = "",
        emailMoi: String? = null
    ): Boolean {
        if (!currentUser.isAdmin()) return false
        val tkIdx = taiKhoans.indexOfFirst { it.id == tkId }
        if (tkIdx < 0) return false
        val oldTk = taiKhoans[tkIdx]

        val cleanPhone = normalizePhoneDigits(sdtDangNhapMoi).ifBlank { sdtDangNhapMoi.trim().replace(" ", "") }
        if (cleanPhone.isBlank()) return false

        val duplicate = taiKhoans.any {
            it.id != tkId && (it.username.equals(cleanPhone, ignoreCase = true) ||
                (cleanPhone.length >= 7 && normalizePhoneDigits(it.username) == cleanPhone))
        }
        if (duplicate) return false

        val cleanName = hoTenMoi.trim().ifBlank { oldTk.hoTen }
        val cleanPass = matKhauMoi.trim()
        val newPassHash = if (cleanPass.isNotBlank()) hashPassword(cleanPass) else oldTk.matKhauHash
        val newPassDisplay = if (cleanPass.isNotBlank()) cleanPass else oldTk.matKhauHienThi
        val newEmail = emailMoi?.trim()?.lowercase() ?: oldTk.email

        val updatedTk = oldTk.copy(
            username = cleanPhone,
            hoTen = cleanName,
            matKhauHash = newPassHash,
            matKhauHienThi = newPassDisplay,
            email = newEmail
        )
        taiKhoans[tkIdx] = updatedTk
        if (currentUser?.id == tkId) {
            currentUser = updatedTk
        }

        if (oldTk.nvId.isNotBlank()) {
            val nvIdx = nvs.indexOfFirst { it.id == oldTk.nvId }
            if (nvIdx >= 0) {
                val oldNv = nvs[nvIdx]
                nvs[nvIdx] = oldNv.copy(
                    ten = cleanName,
                    sdt = cleanPhone,
                    gt = gtMoi.trim().ifBlank { oldNv.gt },
                    gc = ghiChuMoi.trim()
                )
            }
        }

        saveToPrefs()
        return true
    }

    fun toggleMacAdminChoTaiKhoan(tkId: String, capAdmin: Boolean): Boolean {
        if (!currentUser.isAdmin()) return false
        val idx = taiKhoans.indexOfFirst { it.id == tkId }
        if (idx < 0) return false

        if (!capAdmin) {
            val soAdminConLai = taiKhoans.count { it.id != tkId && it.isAdmin() }
            if (soAdminConLai == 0) return false
        }

        val tk = taiKhoans[idx]
        val updated = tk.copy(quyen = if (capAdmin) VaiTro.OWNER else VaiTro.STAFF)
        taiKhoans[idx] = updated
        if (currentUser?.id == tkId) currentUser = updated
        saveToPrefs()
        return true
    }

    fun toggleQuyenNhanVien(tkId: String, quyenHan: QuyenHan, enabled: Boolean) {
        if (!currentUser.isAdmin()) return
        val idx = taiKhoans.indexOfFirst { it.id == tkId }
        if (idx >= 0) {
            val tk = taiKhoans[idx]
            val updatedSet = tk.cacQuyenDuocCap.toMutableSet()
            if (enabled) updatedSet.add(quyenHan) else updatedSet.remove(quyenHan)
            val updatedTk = tk.copy(cacQuyenDuocCap = updatedSet)
            taiKhoans[idx] = updatedTk
            if (currentUser?.id == tkId) currentUser = updatedTk
            saveToPrefs()
        }
    }

    fun datTatCaQuyenNhanVien(tkId: String, capHet: Boolean) {
        if (!currentUser.isAdmin()) return
        val idx = taiKhoans.indexOfFirst { it.id == tkId }
        if (idx >= 0) {
            val tk = taiKhoans[idx]
            val updatedTk = tk.copy(
                cacQuyenDuocCap = if (capHet) QuyenHan.entries.toSet() else emptySet()
            )
            taiKhoans[idx] = updatedTk
            if (currentUser?.id == tkId) currentUser = updatedTk
            saveToPrefs()
        }
    }

    fun xoaNhanVien(nvId: String) {
        if (!currentUser.isAdmin()) return
        val linkedTk = taiKhoans.firstOrNull { it.nvId == nvId }
        if (linkedTk != null && linkedTk.id == currentUser?.id) return
        if (!deletedIds.contains(nvId)) deletedIds.add(nvId)
        taiKhoans.filter { it.nvId == nvId && it.id != "tk_admin" }.forEach {
            if (!deletedIds.contains(it.id)) deletedIds.add(it.id)
        }
        nvs.removeAll { it.id == nvId }
        taiKhoans.removeAll { it.nvId == nvId && it.id != "tk_admin" }
        saveToPrefs()
    }

    fun xoaTaiKhoanTheoId(tkId: String): Boolean {
        if (!currentUser.isAdmin()) return false
        val target = taiKhoans.firstOrNull { it.id == tkId } ?: return false
        if (target.id == currentUser?.id || target.id == "tk_admin") return false
        if (!deletedIds.contains(tkId)) deletedIds.add(tkId)
        if (target.nvId.isNotBlank()) {
            if (!deletedIds.contains(target.nvId)) deletedIds.add(target.nvId)
            nvs.removeAll { it.id == target.nvId }
        }
        taiKhoans.removeAll { it.id == tkId }
        saveToPrefs()
        return true
    }

    // =========================================================================
    // 3. KHÁCH HÀNG & ĐĂNG KÝ GÓI
    // =========================================================================
    fun themKhachHang(ten: String, sdt: String): KH? {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_CUSTOMERS)) return null
        val cleanPhone = sdt.trim()
        // Nếu đã có khách trùng số điện thoại (định danh SĐT), cập nhật tên hoặc trả về khách đó
        if (cleanPhone.filter { it.isDigit() }.length >= 8) {
            val existingIdx = khs.indexOfFirst {
                it.sdt.filter { c -> c.isDigit() } == cleanPhone.filter { c -> c.isDigit() }
            }
            if (existingIdx >= 0) {
                val updated = khs[existingIdx].copy(ten = ten.trim().ifBlank { khs[existingIdx].ten })
                khs[existingIdx] = updated
                saveToPrefs()
                return updated
            }
        }
        val kh = KH(id = "kh_${shortId()}", ten = ten.trim(), sdt = cleanPhone)
        khs.add(0, kh)
        saveToPrefs()
        return kh
    }

    fun suaKhachHang(khId: String, tenMoi: String, sdtMoi: String): Boolean {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_CUSTOMERS)) return false
        val idx = khs.indexOfFirst { it.id == khId }
        if (idx < 0) return false
        khs[idx] = khs[idx].copy(
            ten = tenMoi.trim().ifBlank { khs[idx].ten },
            sdt = sdtMoi.trim()
        )
        saveToPrefs()
        return true
    }

    fun xoaKhachHang(khId: String): Boolean {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_CUSTOMERS)) return false
        khs.removeAll { it.id == khId }
        dks.removeAll { it.khId == khId }
        saveToPrefs()
        return true
    }

    fun dangKyGoiChoKhach(khId: String, goiId: String, giaTuyChinh: Long? = null): DK? {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_CUSTOMERS)) return null
        val kh = khs.firstOrNull { it.id == khId } ?: return null
        val goi = gois.firstOrNull { it.id == goiId } ?: return null
        val soTienThu = giaTuyChinh?.coerceAtLeast(0L) ?: goi.gia
        val dk = DK(
            id = "dk_${shortId()}",
            khId = kh.id,
            goiId = goi.id,
            ngay = todayDateStr(),
            daDung = false,
            luotDaDung = 0
        )
        dks.add(0, dk)
        tcs.add(
            0,
            TC(
                id = "tc_${shortId()}",
                loai = LoaiTC.THU,
                tien = soTienThu,
                lyDo = "Đăng ký gói ${goi.ten} - KH ${kh.ten} (${kh.sdt})",
                ngay = todayDateStr()
            )
        )
        saveToPrefs()
        return dk
    }

    /**
     * Cho phép Admin (hoặc người có quyền khách hàng) sửa trực tiếp tiến độ số buổi đã dùng của gói khách hàng
     */
    fun suaDangKyGoiKhach(
        dkId: String,
        luotDaDungMoi: Int,
        ngayMoi: String? = null
    ): Boolean {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_CUSTOMERS)) return false
        val idx = dks.indexOfFirst { it.id == dkId }
        if (idx < 0) return false
        val oldDk = dks[idx]
        val goi = gois.firstOrNull { it.id == oldDk.goiId }
        val maxBuoi = goi?.soBuoi ?: 999
        val clampedUsed = luotDaDungMoi.coerceIn(0, maxBuoi)
        dks[idx] = oldDk.copy(
            luotDaDung = clampedUsed,
            daDung = clampedUsed >= maxBuoi,
            ngay = ngayMoi?.trim()?.takeIf { it.isNotBlank() } ?: oldDk.ngay
        )
        saveToPrefs()
        return true
    }

    fun xoaDangKyGoiKhach(dkId: String): Boolean {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_CUSTOMERS)) return false
        val removed = dks.any { it.id == dkId }
        if (removed) {
            if (!deletedIds.contains(dkId)) deletedIds.add(dkId)
            dks.removeAll { it.id == dkId }
            saveToPrefs()
        }
        return removed
    }

    // =========================================================================
    // 4. DỊCH VỤ LẺ & GÓI LIỆU TRÌNH
    // =========================================================================
    fun luuDichVu(id: String?, ten: String, phut: Int, gia: Long, moTa: String, chietKhauVnd: Long): DV? {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_SERVICES)) return null
        if (id == null) {
            val newDv = DV(
                id = "dv_${shortId()}",
                ten = ten.trim(),
                phut = phut.coerceAtLeast(5),
                gia = gia.coerceAtLeast(0L),
                moTa = moTa.trim(),
                chietKhauVnd = chietKhauVnd.coerceAtLeast(0L)
            )
            dvs.add(0, newDv)
            saveToPrefs()
            return newDv
        } else {
            val idx = dvs.indexOfFirst { it.id == id }
            if (idx >= 0) {
                val updated = dvs[idx].copy(
                    ten = ten.trim(),
                    phut = phut.coerceAtLeast(5),
                    gia = gia.coerceAtLeast(0L),
                    moTa = moTa.trim(),
                    chietKhauVnd = chietKhauVnd.coerceAtLeast(0L)
                )
                dvs[idx] = updated
                saveToPrefs()
                return updated
            }
            return dvs.firstOrNull()
        }
    }

    fun xoaDichVu(dvId: String) {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_SERVICES)) return
        if (!deletedIds.contains(dvId)) deletedIds.add(dvId)
        dvs.removeAll { it.id == dvId }
        saveToPrefs()
    }

    fun luuGoiLieuTrinh(
        id: String?,
        ten: String,
        dvIds: List<String>,
        soBuoi: Int,
        gia: Long,
        chiTietLuot: String = "",
        dvTangKem: String = ""
    ): Goi? {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_SERVICES)) return null
        val actualDvIds = dvIds.ifEmpty { listOfNotNull(dvs.firstOrNull()?.id) }
        val cleanBuoi = soBuoi.coerceAtLeast(1)
        val cleanGia = gia.coerceAtLeast(0L)
        val cleanTangKem = dvTangKem.trim()
        val finalChiTietLuot = chiTietLuot.trim().ifBlank { "${actualDvIds.firstOrNull().orEmpty()}:$cleanBuoi" }
        return if (id == null) {
            val goi = Goi(
                id = "goi_${shortId()}",
                ten = ten.trim(),
                dvIds = actualDvIds,
                soBuoi = cleanBuoi,
                gia = cleanGia,
                chiTietLuot = finalChiTietLuot,
                dvTangKem = cleanTangKem
            )
            gois.add(0, goi)
            saveToPrefs()
            goi
        } else {
            val idx = gois.indexOfFirst { it.id == id }
            if (idx >= 0) {
                val updated = gois[idx].copy(
                    ten = ten.trim().ifBlank { gois[idx].ten },
                    dvIds = actualDvIds,
                    soBuoi = cleanBuoi,
                    gia = cleanGia,
                    chiTietLuot = finalChiTietLuot,
                    dvTangKem = cleanTangKem
                )
                gois[idx] = updated
                saveToPrefs()
                updated
            } else null
        }
    }

    fun themGoiLieuTrinh(ten: String, dvIds: List<String>, soBuoi: Int, gia: Long, dvTangKem: String = ""): Goi? {
        return luuGoiLieuTrinh(null, ten, dvIds, soBuoi, gia, "", dvTangKem)
    }

    fun xoaGoiLieuTrinh(goiId: String) {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_SERVICES)) return
        if (!deletedIds.contains(goiId)) deletedIds.add(goiId)
        gois.removeAll { it.id == goiId }
        saveToPrefs()
    }

    // =========================================================================
    // 5. VẬN HÀNH VÉ (LUOT) — HỖ TRỢ DỊCH VỤ LẺ, TRỪ GÓI (LT), TẶNG KÈM 🎁, DV NGOÀI & SNAPSHOT KẾ TOÁN
    // =========================================================================
    fun moVeMoi(
        khId: String,
        nvId: String,
        selectedDvIds: List<String>,
        dkId: String = "",
        tgbd: String = nowTimeStr(),
        dvNgoai: String = "",
        thuTuyChinh: Long? = null,
        hoaHongTuyChinh: Long? = null
    ): Luot? {
        if (khId.isBlank() || nvId.isBlank()) return null
        val cleanDvNgoai = dvNgoai.trim()
        val actualDvIds: List<String>
        val duKienThu: Long

        if (dkId.isNotBlank()) {
            val dkIdx = dks.indexOfFirst { it.id == dkId }
            if (dkIdx < 0) return null
            val dk = dks[dkIdx]
            val goi = gois.firstOrNull { it.id == dk.goiId } ?: return null
            if (dk.daDung || dk.luotDaDung >= goi.soBuoi) return null

            val newCount = dk.luotDaDung + 1
            dks[dkIdx] = dk.copy(
                luotDaDung = newCount,
                daDung = newCount >= goi.soBuoi
            )
            actualDvIds = selectedDvIds.ifEmpty { goi.dvIds }
            duKienThu = thuTuyChinh?.coerceAtLeast(0L) ?: 0L
        } else {
            val fallbackDvIds = if (selectedDvIds.isEmpty() && cleanDvNgoai.isBlank()) {
                listOfNotNull(dvs.firstOrNull()?.id)
            } else {
                selectedDvIds
            }
            if (fallbackDvIds.isEmpty() && cleanDvNgoai.isBlank()) return null
            actualDvIds = fallbackDvIds
            duKienThu = thuTuyChinh?.coerceAtLeast(0L)
                ?: actualDvIds.sumOf { id -> dvs.firstOrNull { it.id == id }?.gia ?: 0L }
        }

        val luot = Luot(
            id = "ve_${shortId()}",
            khId = khId,
            nvId = nvId,
            dvIds = actualDvIds,
            dkId = dkId,
            tgbd = tgbd.ifBlank { nowTimeStr() },
            tgkt = "",
            thu = duKienThu,
            daXacNhan = false,
            ngay = todayDateStr(),
            dvNgoai = cleanDvNgoai,
            hoaHongChot = hoaHongTuyChinh?.coerceAtLeast(0L)
        )
        luots.add(0, luot)
        saveToPrefs()
        return luot
    }

    fun hoanThanhVe(luotId: String, tgktInput: String, thucThu: Long) {
        val idx = luots.indexOfFirst { it.id == luotId }
        if (idx < 0) return
        val cur = luots[idx]
        if (!cur.dangLam && !currentUser.canModifyCompletedTicket(cur)) return

        val finalTgkt = if (currentUser.hasPermission(QuyenHan.CAN_EDIT_TIME) && tgktInput.isNotBlank()) {
            tgktInput.trim()
        } else {
            nowTimeStr()
        }
        luots[idx] = cur.copy(
            tgkt = finalTgkt,
            thu = if (cur.dungGoi && !currentUser.isAdmin()) 0L else thucThu.coerceAtLeast(0L)
        )
        saveToPrefs()
    }

    fun capNhatGioVaTienVe(
        luotId: String,
        tgbdMoi: String,
        tgktMoi: String,
        thuMoi: Long? = null,
        hoaHongMoi: Long? = null
    ) {
        val idx = luots.indexOfFirst { it.id == luotId }
        if (idx < 0) return
        val ve = luots[idx]

        if (!ve.dangLam && !currentUser.canModifyCompletedTicket(ve)) return
        if (ve.dangLam && !currentUser.hasPermission(QuyenHan.CAN_EDIT_TIME) && !currentUser.isAdmin()) return

        val newThu = if (thuMoi != null) {
            if (ve.dungGoi && !currentUser.isAdmin()) 0L else thuMoi.coerceAtLeast(0L)
        } else ve.thu

        val newHhChot = hoaHongMoi?.coerceAtLeast(0L) ?: ve.hoaHongChot
        val updatedVe = ve.copy(
            tgbd = tgbdMoi.trim().ifBlank { ve.tgbd },
            tgkt = tgktMoi.trim(),
            thu = newThu,
            hoaHongChot = newHhChot
        )
        luots[idx] = updatedVe
        if (updatedVe.daXacNhan) {
            dongBoPhieuThuChiCuaVeDaChot(idx, updatedVe)
        }
        saveToPrefs()
    }

    /**
     * Dành cho Admin hoặc trợ lý gehihi sửa toàn diện bất kỳ vé nào (Khách, KTV, Dịch vụ, DV ngoài, Giờ, Ngày ca, Giá thu, Hoa hồng KTV).
     * Nếu vé đã chốt, tự động cập nhật đồng bộ cả phiếu THU vé khách và phiếu CHI hoa hồng trong Sổ quỹ và Bảng lương!
     */
    fun suaToanBoVeAdmin(
        veId: String,
        khIdMoi: String? = null,
        nvIdMoi: String? = null,
        dvIdsMoi: List<String>? = null,
        dkIdMoi: String? = null,
        dvNgoaiMoi: String? = null,
        tgbdMoi: String? = null,
        tgktMoi: String? = null,
        ngayMoi: String? = null,
        thuMoi: Long? = null,
        hoaHongMoi: Long? = null
    ): Luot? {
        val idx = luots.indexOfFirst { it.id == veId }
        if (idx < 0) return null
        val ve = luots[idx]
        if (!currentUser.isAdmin() && !currentUser.canModifyCompletedTicket(ve)) return null

        val finalDvIds = dvIdsMoi ?: ve.dvIds
        val finalDkId = dkIdMoi ?: ve.dkId
        val finalDvNgoai = dvNgoaiMoi?.trim() ?: ve.dvNgoai
        val finalThu = thuMoi?.coerceAtLeast(0L) ?: ve.thu

        val finalHoaHongChot: Long? = when {
            hoaHongMoi != null -> hoaHongMoi.coerceAtLeast(0L)
            dvIdsMoi != null -> finalDvIds.sumOf { id -> dvs.firstOrNull { it.id == id }?.chietKhauVnd ?: 0L }
            else -> ve.hoaHongChot
        }

        val updated = ve.copy(
            khId = khIdMoi?.takeIf { it.isNotBlank() } ?: ve.khId,
            nvId = nvIdMoi?.takeIf { it.isNotBlank() } ?: ve.nvId,
            dvIds = finalDvIds,
            dkId = finalDkId,
            dvNgoai = finalDvNgoai,
            tgbd = tgbdMoi?.trim()?.takeIf { it.isNotBlank() } ?: ve.tgbd,
            tgkt = tgktMoi?.trim() ?: ve.tgkt,
            ngay = ngayMoi?.trim()?.takeIf { it.isNotBlank() } ?: ve.ngay,
            thu = finalThu,
            hoaHongChot = finalHoaHongChot
        )
        luots[idx] = updated
        if (updated.daXacNhan) {
            dongBoPhieuThuChiCuaVeDaChot(idx, updated)
        }
        saveToPrefs()
        return luots[idx]
    }

    private fun taoLyDoPhieuThuVe(ve: Luot): String {
        val tenKtv = nvs.firstOrNull { it.id == ve.nvId }?.ten ?: "KTV"
        val tenKh = khs.firstOrNull { it.id == ve.khId }?.ten ?: "Khách lẻ"
        val tenDv = tenDichVuCuaVe(ve)
        return "Thu vé khách — Khách $tenKh ($tenDv, NV $tenKtv)"
    }

    private fun taoLyDoPhieuChiHoaHong(ve: Luot): String {
        val tenKtv = nvs.firstOrNull { it.id == ve.nvId }?.ten ?: "KTV"
        val tenKh = khs.firstOrNull { it.id == ve.khId }?.ten ?: "Khách"
        return if (ve.dungGoi) {
            "Chiết khấu DV trong gói liệu trình — NV $tenKtv (Khách $tenKh)"
        } else {
            "Chiết khấu DV lẻ — NV $tenKtv (Khách $tenKh)"
        }
    }

    /**
     * Kiểm tra một phiếu Sổ quỹ có phải là phiếu THU tự động từ vé khách hay không
     */
    fun laPhieuThuTuVe(tc: TC): Boolean {
        if (tc.loai != LoaiTC.THU) return false
        return tc.lyDo.startsWith("Thu vé khách", ignoreCase = true) ||
            tc.lyDo.startsWith("Thu vé lẻ", ignoreCase = true) ||
            luots.any { it.phieuThuVeId == tc.id }
    }

    /**
     * Kiểm tra một phiếu Sổ quỹ có phải là phiếu CHI hoa hồng / chiết khấu cho KTV hay không
     */
    fun laPhieuChiHoaHongKtv(tc: TC): Boolean {
        if (tc.loai != LoaiTC.CHI) return false
        return tc.lyDo.contains("Chiết khấu DV", ignoreCase = true) ||
            tc.lyDo.contains("Hoa hồng KTV", ignoreCase = true) ||
            tc.lyDo.contains("Hoa hồng nhân viên", ignoreCase = true) ||
            luots.any { it.phieuChiHoaHongId == tc.id }
    }

    /**
     * Tự động đồng bộ cả phiếu THU vé khách (nếu thu > 0đ) và phiếu CHI hoa hồng KTV (nếu hoa hồng > 0đ)
     * cho một vé đã chốt trong Sổ quỹ (tcs).
     */
    private fun dongBoPhieuThuChiCuaVeDaChot(luotIdx: Int, veInput: Luot) {
        var ve = veInput
        val ngayChuan = ve.ngay.ifBlank { todayDateStr() }

        // 1. ĐỒNG BỘ PHIẾU THU VÉ KHÁCH (+150.000đ...)
        val tienThu = ve.thu.coerceAtLeast(0L)
        val lyDoThuChuan = taoLyDoPhieuThuVe(ve)
        val existingThuIdx = if (ve.phieuThuVeId.isNotBlank()) {
            tcs.indexOfFirst { it.id == ve.phieuThuVeId }
        } else {
            val tenKtv = nvs.firstOrNull { it.id == ve.nvId }?.ten.orEmpty()
            val tenKh = khs.firstOrNull { it.id == ve.khId }?.ten.orEmpty()
            tcs.indexOfFirst { tc ->
                tc.loai == LoaiTC.THU &&
                    tc.ngay == ngayChuan &&
                    tc.tien == tienThu &&
                    tc.lyDo.startsWith("Thu vé khách", ignoreCase = true) &&
                    (tenKh.isBlank() || tc.lyDo.contains(tenKh, ignoreCase = true)) &&
                    (tenKtv.isBlank() || tc.lyDo.contains(tenKtv, ignoreCase = true)) &&
                    luots.none { l -> l.id != ve.id && l.phieuThuVeId == tc.id }
            }
        }

        if (tienThu > 0L) {
            if (existingThuIdx >= 0) {
                val oldThu = tcs[existingThuIdx]
                tcs[existingThuIdx] = oldThu.copy(
                    loai = LoaiTC.THU,
                    tien = tienThu,
                    lyDo = lyDoThuChuan,
                    ngay = ngayChuan
                )
                if (ve.phieuThuVeId != oldThu.id) {
                    ve = ve.copy(phieuThuVeId = oldThu.id)
                }
            } else {
                val newThuId = "tc_thu_${ve.id.removePrefix("ve_")}"
                tcs.removeAll { it.id == newThuId }
                tcs.add(
                    0,
                    TC(
                        id = newThuId,
                        loai = LoaiTC.THU,
                        tien = tienThu,
                        lyDo = lyDoThuChuan,
                        ngay = ngayChuan
                    )
                )
                ve = ve.copy(phieuThuVeId = newThuId)
            }
        } else if (existingThuIdx >= 0) {
            val removedId = tcs[existingThuIdx].id
            if (!deletedIds.contains(removedId)) deletedIds.add(removedId)
            tcs.removeAt(existingThuIdx)
            ve = ve.copy(phieuThuVeId = "")
        }

        // 2. ĐỒNG BỘ PHIẾU CHI HOA HỒNG KTV (-70.000đ...)
        val hoaHong = tinhHoaHongVe(ve)
        val lyDoChiChuan = taoLyDoPhieuChiHoaHong(ve)
        val existingChiIdx = if (ve.phieuChiHoaHongId.isNotBlank()) {
            tcs.indexOfFirst { it.id == ve.phieuChiHoaHongId }
        } else {
            val tenKtv = nvs.firstOrNull { it.id == ve.nvId }?.ten.orEmpty()
            val tenKh = khs.firstOrNull { it.id == ve.khId }?.ten.orEmpty()
            tcs.indexOfFirst { tc ->
                tc.loai == LoaiTC.CHI &&
                    tc.ngay == ngayChuan &&
                    (tenKtv.isNotBlank() && tc.lyDo.contains(tenKtv, ignoreCase = true)) &&
                    (tenKh.isBlank() || tc.lyDo.contains(tenKh, ignoreCase = true)) &&
                    (tc.lyDo.contains("Hoa hồng", ignoreCase = true) || tc.lyDo.contains("Chiết khấu", ignoreCase = true)) &&
                    luots.none { l -> l.id != ve.id && l.phieuChiHoaHongId == tc.id }
            }
        }

        if (hoaHong > 0L) {
            if (existingChiIdx >= 0) {
                val oldChi = tcs[existingChiIdx]
                tcs[existingChiIdx] = oldChi.copy(
                    loai = LoaiTC.CHI,
                    tien = hoaHong,
                    lyDo = lyDoChiChuan,
                    ngay = ngayChuan
                )
                ve = ve.copy(phieuChiHoaHongId = oldChi.id, hoaHongChot = hoaHong)
            } else {
                val newChiId = "tc_chi_${ve.id.removePrefix("ve_")}"
                tcs.removeAll { it.id == newChiId }
                tcs.add(
                    0,
                    TC(
                        id = newChiId,
                        loai = LoaiTC.CHI,
                        tien = hoaHong,
                        lyDo = lyDoChiChuan,
                        ngay = ngayChuan
                    )
                )
                ve = ve.copy(phieuChiHoaHongId = newChiId, hoaHongChot = hoaHong)
            }
        } else if (existingChiIdx >= 0) {
            val removedId = tcs[existingChiIdx].id
            if (!deletedIds.contains(removedId)) deletedIds.add(removedId)
            tcs.removeAt(existingChiIdx)
            ve = ve.copy(phieuChiHoaHongId = "", hoaHongChot = 0L)
        }

        luots[luotIdx] = ve
    }

    /**
     * Quét toàn bộ các vé đã chốt (daXacNhan == true) để đảm bảo mọi vé lẻ có thu tiền (thu > 0đ)
     * đều có đầy đủ phiếu THU vé khách song song với phiếu CHI hoa hồng KTV trong Sổ quỹ.
     */
    fun dongBoTatCaPhieuThuChiTuVeDaChot(luuNgay: Boolean = false) {
        var modified = false
        for (i in luots.indices) {
            val ve = luots[i]
            if (ve.daXacNhan) {
                val oldThuId = ve.phieuThuVeId
                val oldChiId = ve.phieuChiHoaHongId
                val hadThuInTc = oldThuId.isNotBlank() && tcs.any { it.id == oldThuId }
                val hadChiInTc = oldChiId.isNotBlank() && tcs.any { it.id == oldChiId }
                dongBoPhieuThuChiCuaVeDaChot(i, ve)
                val updated = luots[i]
                if (updated != ve || (ve.thu > 0L && !hadThuInTc) || (tinhHoaHongVe(ve) > 0L && !hadChiInTc)) {
                    modified = true
                }
            }
        }
        if (modified && luuNgay) {
            saveToPrefs()
        }
    }

    fun xacNhanChotVe(luotId: String) {
        val idx = luots.indexOfFirst { it.id == luotId }
        if (idx < 0) return
        val ve = luots[idx]
        if (!currentUser.canModifyCompletedTicket(ve)) return
        if (ve.daXacNhan) return

        val finalTgkt = ve.tgkt.ifBlank { nowTimeStr() }
        val hoaHong = tinhHoaHongVe(ve)
        val confirmedVe = ve.copy(
            tgkt = finalTgkt,
            daXacNhan = true,
            hoaHongChot = hoaHong
        )
        luots[idx] = confirmedVe
        dongBoPhieuThuChiCuaVeDaChot(idx, confirmedVe)
        saveToPrefs()
    }

    fun xoaVe(luotId: String): Boolean {
        val ve = luots.firstOrNull { it.id == luotId } ?: return false
        if (!currentUser.canDeleteTicket(ve)) return false

        hoanLaiBuoiGoiNeuVeDangLam(ve)
        if (ve.phieuThuVeId.isNotBlank()) {
            if (!deletedIds.contains(ve.phieuThuVeId)) deletedIds.add(ve.phieuThuVeId)
            tcs.removeAll { it.id == ve.phieuThuVeId }
        }
        if (ve.phieuChiHoaHongId.isNotBlank()) {
            if (!deletedIds.contains(ve.phieuChiHoaHongId)) deletedIds.add(ve.phieuChiHoaHongId)
            tcs.removeAll { it.id == ve.phieuChiHoaHongId }
        }
        if (!deletedIds.contains(luotId)) deletedIds.add(luotId)
        luots.removeAll { it.id == luotId }
        saveToPrefs()
        return true
    }

    /**
     * Xóa hàng loạt vé theo phạm vi (DANG_LAM / CHO_CHOT / DA_CHOT / TAT_CA) - Dành cho Admin & gehihi
     */
    fun xoaHangLoatVeAdmin(phamVi: String): Int {
        if (!currentUser.isAdmin()) return 0
        val toDelete = when (phamVi.uppercase()) {
            "DANG_LAM" -> luots.filter { it.dangLam }
            "CHO_CHOT" -> luots.filter { it.choChot }
            "DA_CHOT" -> luots.filter { it.daHoanTat }
            else -> luots.toList()
        }
        if (toDelete.isEmpty()) return 0
        toDelete.forEach { ve ->
            if (ve.dangLam) hoanLaiBuoiGoiNeuVeDangLam(ve)
            if (ve.phieuThuVeId.isNotBlank()) {
                if (!deletedIds.contains(ve.phieuThuVeId)) deletedIds.add(ve.phieuThuVeId)
                tcs.removeAll { it.id == ve.phieuThuVeId }
            }
            if (ve.phieuChiHoaHongId.isNotBlank()) {
                if (!deletedIds.contains(ve.phieuChiHoaHongId)) deletedIds.add(ve.phieuChiHoaHongId)
                tcs.removeAll { it.id == ve.phieuChiHoaHongId }
            }
        }
        val ids = toDelete.map { it.id }.toSet()
        ids.forEach { id -> if (!deletedIds.contains(id)) deletedIds.add(id) }
        luots.removeAll { it.id in ids }
        saveToPrefs()
        return toDelete.size
    }

    private fun hoanLaiBuoiGoiNeuVeDangLam(ve: Luot) {
        if (ve.dangLam && ve.dkId.isNotBlank()) {
            val dkIdx = dks.indexOfFirst { it.id == ve.dkId }
            if (dkIdx >= 0) {
                val dk = dks[dkIdx]
                val newUsed = (dk.luotDaDung - 1).coerceAtLeast(0)
                dks[dkIdx] = dk.copy(luotDaDung = newUsed, daDung = false)
            }
        }
    }

    /**
     * Tính hoa hồng (chiết khấu VNĐ) cho nhân viên KTV:
     * - Nếu vé đã chốt hoặc đã được Admin chỉnh sửa hoa hồng riêng (hoaHongChot != null) -> trả về đúng số tiền tại thời điểm đó.
     * - Nếu chưa chốt -> cộng chietKhauVnd của tất cả DV trong vé (kể cả DV tặng kèm trong gói; DV ngoài nhập tay = 0đ).
     */
    fun tinhHoaHongVe(ve: Luot): Long {
        ve.hoaHongChot?.let { return it }
        return ve.dvIds.sumOf { dvId -> dvs.firstOrNull { it.id == dvId }?.chietKhauVnd ?: 0L }
    }

    /**
     * Hiển thị tên các dịch vụ trong vé (hỗ trợ gom số lượng như "2x Trị liệu 60 phút", kèm "(LT)" nếu làm gói và "DV ngoài")
     */
    fun tenDichVuCuaVe(ve: Luot): String {
        val grouped = ve.dvIds.groupingBy { it }.eachCount()
        val sysNames = grouped.entries.mapNotNull { (dvId, count) ->
            val dv = dvs.firstOrNull { it.id == dvId } ?: return@mapNotNull null
            if (count > 1) "${count}x ${dv.ten}" else dv.ten
        }
        val parts = mutableListOf<String>()
        if (sysNames.isNotEmpty()) parts.add(sysNames.joinToString(", "))
        if (ve.dvNgoai.isNotBlank()) parts.add("DV ngoài: ${ve.dvNgoai}")
        val base = parts.joinToString(" + ").ifBlank {
            if (ve.dungGoi) "Gói liệu trình" else "Dịch vụ lẻ"
        }
        return if (ve.dungGoi) "$base (LT)" else base
    }

    // =========================================================================
    // 6. BÁO CÁO Ô LỊCH (CALENDAR GRID), THU NHẬP KTV & BẢNG KHÁCH HÀNG + GEHIHI SEARCH
    // =========================================================================

    /**
     * Kiểm tra một ngày (yyyy-MM-dd) có khớp với kỳ thời gian (hoặc ô lịch đang chọn) hay không
     */
    fun khopThoiGianBaoCao(dateStr: String, filter: BoLocThoiGian, refDate: String, selectedCellKey: String?): Boolean {
        val cleanDate = dateStr.trim()
        if (!selectedCellKey.isNullOrBlank() && selectedCellKey != "ALL") {
            return cleanDate.startsWith(selectedCellKey)
        }
        return matchesTimeFilter(cleanDate, filter, refDate)
    }

    /**
     * Tính danh sách các Ô lịch (Calendar Grid) hiển thị trực tiếp số doanh thu sau khi trừ hết phí
     */
    fun layDanhSachOLichBaoCao(
        filter: BoLocThoiGian,
        refDate: String = todayDateStr(),
        selectedCellKey: String? = null
    ): List<OLichBaoCaoItem> {
        val cleanRef = refDate.trim().ifBlank { todayDateStr() }
        val yearStr = cleanRef.take(4)
        val yearMonthStr = cleanRef.take(7)

        return when (filter) {
            BoLocThoiGian.NGAY -> {
                // Hiển thị 7 ô lịch ngày xung quanh ngày đang chọn (-3 .. +3 ngày)
                (-3..3).map { delta ->
                    val dStr = shiftReferenceDate(cleanRef, BoLocThoiGian.NGAY, delta)
                    val dayPart = dStr.takeLast(2)
                    val monthPart = dStr.substring(5, 7)
                    tinhOLichChoPrefix(
                        prefix = dStr,
                        tieuDe = "$dayPart/$monthPart",
                        moTaDayDu = "Ngày $dayPart tháng $monthPart năm ${dStr.take(4)}",
                        dangChon = (selectedCellKey ?: cleanRef) == dStr
                    )
                }
            }

            BoLocThoiGian.THANG -> {
                // Hiển thị lưới toàn bộ các ngày trong tháng (01 .. 28/29/30/31)
                val maxDay = getDaysInMonth(cleanRef)
                val monthNum = cleanRef.substring(5, 7)
                (1..maxDay).map { day ->
                    val dayStr = "%02d".format(day)
                    val fullDate = "$yearMonthStr-$dayStr"
                    tinhOLichChoPrefix(
                        prefix = fullDate,
                        tieuDe = "Ng $day",
                        moTaDayDu = "Ngày $day tháng $monthNum năm $yearStr",
                        dangChon = selectedCellKey == fullDate
                    )
                }
            }

            BoLocThoiGian.NAM -> {
                // Hiển thị lưới 12 ô Tháng trong năm (Tháng 1 .. Tháng 12)
                (1..12).map { m ->
                    val mStr = "%02d".format(m)
                    val ymPrefix = "$yearStr-$mStr"
                    tinhOLichChoPrefix(
                        prefix = ymPrefix,
                        tieuDe = "Thg $m",
                        moTaDayDu = "Tháng $m năm $yearStr",
                        dangChon = selectedCellKey == ymPrefix
                    )
                }
            }

            BoLocThoiGian.TAT_CA -> {
                // Hiển thị các ô Năm có dữ liệu + Ô Tổng toàn bộ từ trước đến giờ
                val years = (luots.map { it.ngay.take(4) } + tcs.map { it.ngay.take(4) } + listOf(todayDateStr().take(4)))
                    .filter { it.length == 4 && it.all { c -> c.isDigit() } }
                    .distinct()
                    .sorted()

                val yearCells = years.map { y ->
                    tinhOLichChoPrefix(
                        prefix = y,
                        tieuDe = "Năm $y",
                        moTaDayDu = "Cả năm $y",
                        dangChon = selectedCellKey == y
                    )
                }
                val allCell = tinhOLichChoPrefix(
                    prefix = "ALL",
                    tieuDe = "Tất cả",
                    moTaDayDu = "Từ trước đến giờ",
                    dangChon = selectedCellKey == null || selectedCellKey == "ALL"
                )
                listOf(allCell) + yearCells
            }
        }
    }

    private fun tinhOLichChoPrefix(
        prefix: String,
        tieuDe: String,
        moTaDayDu: String,
        dangChon: Boolean
    ): OLichBaoCaoItem {
        val veChot = if (prefix == "ALL") {
            luots.filter { it.daXacNhan }
        } else {
            luots.filter { it.daXacNhan && it.ngay.startsWith(prefix) }
        }
        val thuVe = veChot.sumOf { it.thu }
        // Chỉ cộng các khoản THU ngoài vé lẻ (mua gói, thu khác) để không bao giờ tính trùng với thuVe
        val thuNgoaiVe = if (prefix == "ALL") {
            tcs.filter { it.loai == LoaiTC.THU && !laPhieuThuTuVe(it) }.sumOf { it.tien }
        } else {
            tcs.filter { it.loai == LoaiTC.THU && !laPhieuThuTuVe(it) && it.ngay.startsWith(prefix) }.sumOf { it.tien }
        }
        val tongChi = if (prefix == "ALL") {
            tcs.filter { it.loai == LoaiTC.CHI }.sumOf { it.tien }
        } else {
            tcs.filter { it.loai == LoaiTC.CHI && it.ngay.startsWith(prefix) }.sumOf { it.tien }
        }
        val tongThu = thuVe + thuNgoaiVe
        val sauTruPhi = tongThu - tongChi

        return OLichBaoCaoItem(
            keyDate = prefix,
            tieuDeO = tieuDe,
            moTaDayDu = moTaDayDu,
            tongThu = tongThu,
            tongPhiChi = tongChi,
            doanhThuSauTruPhi = sauTruPhi,
            soVeDaChot = veChot.size,
            dangChon = dangChon
        )
    }

    /**
     * Bảng thu nhập nhân viên KTV kèm chi tiết làm dịch vụ nào, hoa hồng như nào
     * trong khoảng thời gian (1 Ngày, 1 Tháng, 1 Năm, Từ trước đến giờ, hoặc Ô lịch đang chọn)
     */
    fun layBangLuongChiTiet(
        filter: BoLocThoiGian,
        refDate: String = todayDateStr(),
        selectedCellKey: String? = null
    ): List<BangLuongKtvSummary> {
        val veDaChotKy = luots.filter {
            it.daXacNhan && khopThoiGianBaoCao(it.ngay, filter, refDate, selectedCellKey)
        }
        return nvs.map { nv ->
            val veCuaNv = veDaChotKy.filter { it.nvId == nv.id }
            val chiTietList = veCuaNv.map { ve ->
                val kh = khs.firstOrNull { it.id == ve.khId }
                val tenDv = tenDichVuCuaVe(ve)
                val phut = calcDurationMinutes(ve.tgbd, ve.tgkt, 60)
                val hh = tinhHoaHongVe(ve)
                ChiTietLuotKtv(
                    luotId = ve.id,
                    ngay = ve.ngay,
                    khungGio = "${ve.tgbd} - ${ve.tgkt}",
                    soPhut = phut,
                    tenKhach = kh?.ten ?: "Khách lẻ",
                    sdtKhach = kh?.sdt ?: "",
                    tenDichVu = tenDv,
                    loaiVe = if (ve.dungGoi) "Liệu trình (LT - 0đ)" else "Vé lẻ",
                    tienThuVe = ve.thu,
                    hoaHongKtv = hh
                )
            }
            BangLuongKtvSummary(
                nv = nv,
                tongSoLuot = chiTietList.size,
                tongHoaHong = chiTietList.sumOf { it.hoaHongKtv },
                tongPhutLam = chiTietList.sumOf { it.soPhut },
                danhSachLuot = chiTietList
            )
        }
    }

    /**
     * Bảng thống kê Khách hàng định danh theo Số điện thoại:
     * Lưu tên, SĐT, số lượt dùng dịch vụ lẻ, số lượt dùng gói dịch vụ, tổng tiền khách chi
     * trong khoảng thời gian xác định + tìm kiếm gần đúng hỗ trợ bởi gehihi.
     */
    fun layThongKeKhachHang(
        filter: BoLocThoiGian,
        refDate: String = todayDateStr(),
        selectedCellKey: String? = null,
        searchQuery: String = ""
    ): List<ThongKeKhachHangItem> {
        val veTrongKy = luots.filter {
            it.daXacNhan && khopThoiGianBaoCao(it.ngay, filter, refDate, selectedCellKey)
        }
        val dkTrongKy = dks.filter {
            khopThoiGianBaoCao(it.ngay, filter, refDate, selectedCellKey)
        }

        val allItems = khs.map { kh ->
            val phoneClean = kh.sdt.trim().ifBlank { "Chưa có SĐT" }
            // Gộp các khách cùng SĐT nếu có bản ghi trùng SĐT
            val matchingKhIds = if (phoneClean.filter { it.isDigit() }.length >= 8) {
                khs.filter { it.sdt.filter { c -> c.isDigit() } == phoneClean.filter { c -> c.isDigit() } }
                    .map { it.id }
                    .toSet()
            } else {
                setOf(kh.id)
            }

            val veCuaKhach = veTrongKy.filter { it.khId in matchingKhIds }
            val dkCuaKhach = dkTrongKy.filter { it.khId in matchingKhIds }

            val soLuotLe = veCuaKhach.count { !it.dungGoi }
            val soLuotGoi = veCuaKhach.count { it.dungGoi }
            val tienVeLe = veCuaKhach.sumOf { it.thu }
            val tienMuaGoi = dkCuaKhach.sumOf { dk ->
                gois.firstOrNull { it.id == dk.goiId }?.gia ?: 0L
            }
            val tongChiTieu = tienVeLe + tienMuaGoi

            val chiTietDv = buildList {
                veCuaKhach.forEach { ve ->
                    val dvNames = tenDichVuCuaVe(ve)
                    add("${ve.ngay}: $dvNames (${if (ve.dungGoi) "Trừ gói 0đ (LT)" else formatVnd(ve.thu)})")
                }
                dkCuaKhach.forEach { dk ->
                    val goi = gois.firstOrNull { it.id == dk.goiId }
                    if (goi != null) {
                        add("${dk.ngay}: Mua ${goi.ten} (${formatVnd(goi.gia)} - Đã dùng ${dk.luotDaDung}/${goi.soBuoi} buổi)")
                    }
                }
            }

            val score = tinhDiemTimKiemKhachHang(searchQuery, kh.ten, phoneClean)

            ThongKeKhachHangItem(
                kh = kh,
                sdtDinhDanh = phoneClean,
                soLuotDichVuLe = soLuotLe,
                soLuotDungGoi = soLuotGoi,
                soGoiDangMua = dkCuaKhach.size,
                tongTienDaChi = tongChiTieu,
                chiTietDichVuDaDung = chiTietDv,
                diemKhopTimKiem = score
            )
        }
            // Loại bỏ trùng lặp theo SĐT định danh
            .distinctBy { item ->
                val digits = item.sdtDinhDanh.filter { it.isDigit() }
                if (digits.length >= 8) digits else item.kh.id
            }

        return if (searchQuery.isBlank()) {
            allItems.sortedByDescending { it.tongTienDaChi }
        } else {
            allItems.filter { it.diemKhopTimKiem >= 55 }
                .sortedWith(compareByDescending<ThongKeKhachHangItem> { it.diemKhopTimKiem }.thenByDescending { it.tongTienDaChi })
        }
    }

    /**
     * Gợi ý thông minh của gehihi khi người dùng nhập tên khách viết sai chính tả hoặc gần đúng
     */
    fun goiYTimKiemKhachHangGehihi(searchQuery: String): String {
        val clean = searchQuery.trim()
        if (clean.isBlank()) return ""

        val scored = khs.map { kh ->
            kh to tinhDiemTimKiemKhachHang(clean, kh.ten, kh.sdt)
        }.filter { it.second >= 55 }.sortedByDescending { it.second }

        if (scored.isEmpty()) {
            return "gehihi không tìm thấy khách nào khớp với '$clean'. Hãy thử nhập một phần số điện thoại hoặc họ tên khác."
        }

        val topMatches = scored.take(3).joinToString(", ") { (kh, score) ->
            "${kh.ten} (SĐT: ${kh.sdt}${if (score < 90) " - gần đúng" else ""})"
        }
        return "gehihi gợi ý khách khớp với '$clean': $topMatches"
    }

    // =========================================================================
    // 7. SỔ QUỸ THU / CHI
    // =========================================================================
    fun themGiaoDichSoQuy(loai: LoaiTC, tien: Long, lyDo: String): TC? {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_FINANCE)) return null
        val tc = TC(
            id = "tc_${shortId()}",
            loai = loai,
            tien = tien.coerceAtLeast(0L),
            lyDo = lyDo.trim(),
            ngay = todayDateStr()
        )
        tcs.add(0, tc)
        saveToPrefs()
        return tc
    }

    fun suaGiaoDichSoQuy(
        tcId: String,
        loaiMoi: LoaiTC,
        tienMoi: Long,
        lyDoMoi: String,
        ngayMoi: String? = null
    ): Boolean {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_FINANCE)) return false
        val idx = tcs.indexOfFirst { it.id == tcId }
        if (idx < 0) return false
        val old = tcs[idx]
        val cleanTien = tienMoi.coerceAtLeast(0L)
        val cleanNgay = ngayMoi?.trim()?.takeIf { it.isNotBlank() } ?: old.ngay
        tcs[idx] = old.copy(
            loai = loaiMoi,
            tien = cleanTien,
            lyDo = lyDoMoi.trim().ifBlank { old.lyDo },
            ngay = cleanNgay
        )
        // Đồng bộ ngược lại vào vé nếu đây là phiếu THU vé khách hoặc phiếu CHI hoa hồng KTV của vé
        val veThuIdx = luots.indexOfFirst { it.phieuThuVeId == tcId }
        if (veThuIdx >= 0) {
            luots[veThuIdx] = luots[veThuIdx].copy(thu = cleanTien, ngay = cleanNgay)
        }
        val veChiIdx = luots.indexOfFirst { it.phieuChiHoaHongId == tcId }
        if (veChiIdx >= 0) {
            luots[veChiIdx] = luots[veChiIdx].copy(hoaHongChot = cleanTien, ngay = cleanNgay)
        }
        saveToPrefs()
        return true
    }

    fun xoaGiaoDichSoQuy(tcId: String) {
        if (!currentUser.hasPermission(QuyenHan.CAN_MANAGE_FINANCE)) return
        if (!deletedIds.contains(tcId)) deletedIds.add(tcId)
        val veThuIdx = luots.indexOfFirst { it.phieuThuVeId == tcId }
        if (veThuIdx >= 0) {
            luots[veThuIdx] = luots[veThuIdx].copy(thu = 0L, phieuThuVeId = "")
        }
        val veChiIdx = luots.indexOfFirst { it.phieuChiHoaHongId == tcId }
        if (veChiIdx >= 0) {
            luots[veChiIdx] = luots[veChiIdx].copy(hoaHongChot = 0L, phieuChiHoaHongId = "")
        }
        tcs.removeAll { it.id == tcId }
        saveToPrefs()
    }

    // =========================================================================
    // 8. TRỢ LÝ AI "GEHIHI" — FULL QUYỀN 100%, CÓ XÁC NHẬN LẠI KHI THÊM/XÓA & BỘ NHỚ VĨNH VIỄN
    // =========================================================================

    /**
     * Lưu một sự kiện hoặc ghi chú quan trọng vào bộ nhớ dài hạn của gehihi (với cơ chế tự động nén cuốn chiếu tại mốc 300 mục),
     * đảm bảo ghi chú của Admin được ghim cố định không bao giờ mất, và các nhật ký thao tác cũ được gộp thành bản tóm tắt.
     */
    fun ghiNhoSuKienDaiHan(noiDung: String, luuNgay: Boolean = true) {
        val clean = noiDung.trim()
        if (clean.isBlank()) return
        val timestamped = "[${todayDateStr()} ${nowTimeStr()}] $clean"
        aiLongTermMemories.removeAll { it.endsWith(clean) }
        aiLongTermMemories.add(timestamped)
        nenBoNhoDaiHanCuonChieu()
        if (luuNgay) {
            saveGehihiMemoryVault()
        }
    }

    /**
     * Khi chạm ngưỡng 300 sự kiện:
     * - Giữ nguyên 100% các "Ghi chú Admin" đã dặn (được ghim vĩnh viễn).
     * - Tự động gộp 30 nhật ký thao tác thường ngày cũ nhất thành 1 dòng "[NÉN KÝ ỨC]" cô đọng để giải phóng ô nhớ mà không mất lịch sử.
     */
    private fun nenBoNhoDaiHanCuonChieu() {
        while (aiLongTermMemories.size > MAX_LONG_TERM_MEMORIES) {
            val regularIndices = aiLongTermMemories.indices.filter { idx ->
                val item = aiLongTermMemories[idx]
                !item.contains("Ghi chú Admin", ignoreCase = true) &&
                    !item.contains("[NÉN KÝ ỨC]", ignoreCase = true)
            }
            if (regularIndices.size >= 10) {
                val batchIndices = regularIndices.take(30)
                val batchItems = batchIndices.map { aiLongTermMemories[it] }
                val firstTime = batchItems.firstOrNull()?.substringAfter("[", "")?.substringBefore("]", "") ?: todayDateStr()
                val lastTime = batchItems.lastOrNull()?.substringAfter("[", "")?.substringBefore("]", "") ?: todayDateStr()
                val highlights = batchItems.takeLast(3).joinToString(" | ") {
                    it.substringAfter("] ").take(45)
                }
                val summaryLine = "[$lastTime] [NÉN KÝ ỨC] Đã gộp ${batchItems.size} sự kiện ($firstTime → $lastTime): $highlights..."
                for (i in batchIndices.reversed()) {
                    aiLongTermMemories.removeAt(i)
                }
                aiLongTermMemories.add(0, summaryLine)
            } else {
                // Trường hợp hy hữu toàn bộ đều là bản nén hoặc ghi chú thì gộp 2 mục đầu tiên thành 1
                val first = aiLongTermMemories.removeAt(0)
                val second = aiLongTermMemories.removeAt(0)
                aiLongTermMemories.add(
                    0,
                    "[${todayDateStr()}] [NÉN KÝ ỨC] ${first.substringAfter("] ").take(60)} + ${second.substringAfter("] ").take(60)}"
                )
            }
        }
    }

    private fun themTinNhanAi(isUser: Boolean, text: String, actionNote: String = "") {
        aiMessages.add(
            AiChatMessage(
                id = "msg_${shortId()}",
                isUser = isUser,
                text = text,
                actionNote = actionNote,
                thoiGian = "${todayDateStr()} ${nowTimeStr()}"
            )
        )
        nenTinNhanAiCuonChieu()
        saveGehihiMemoryVault()
    }

    /**
     * Khi chạm ngưỡng 500 tin nhắn:
     * Tự động gộp 50 tin nhắn cũ nhất thành 1 tin nhắn tóm tắt đầu hội thoại để mở rộng chỗ trống liên tục mà không bị nặng máy.
     */
    private fun nenTinNhanAiCuonChieu() {
        while (aiMessages.size > MAX_AI_MESSAGES) {
            val batchSize = 50.coerceAtMost(aiMessages.size - 1)
            if (batchSize <= 1) {
                aiMessages.removeAt(0)
                break
            }
            val oldBatch = (0 until batchSize).map { aiMessages.removeAt(0) }
            val userCmdCount = oldBatch.count { it.isUser }
            val sampleActions = oldBatch.mapNotNull { it.actionNote.takeIf { n -> n.isNotBlank() } }.takeLast(3)
            val summaryText = buildString {
                append("📦 [Đã tự động nén ${oldBatch.size} tin nhắn cũ ($userCmdCount lệnh của Admin)] ")
                if (sampleActions.isNotEmpty()) {
                    append("Các thao tác tiêu biểu: ${sampleActions.joinToString("; ")}.")
                }
            }
            aiMessages.add(
                0,
                AiChatMessage(
                    id = "compact_${shortId()}",
                    isUser = false,
                    text = summaryText,
                    actionNote = "Tự động nén lịch sử cuốn chiếu",
                    thoiGian = oldBatch.lastOrNull()?.thoiGian ?: "${todayDateStr()} ${nowTimeStr()}"
                )
            )
        }
    }

    /**
     * Đóng gói một thao tác THÊM hoặc XÓA vào hàng chờ xác nhận để Admin bấm nút hoặc gõ "Xác nhận" / "Hủy".
     */
    private fun taoYeuCauXacNhanGehihi(
        loai: LoaiLenhChoXacNhan,
        tieuDeTomTat: String,
        chiTietXacNhan: String,
        thucThiBlock: () -> KetQuaAiBot
    ): KetQuaAiBot {
        val pending = LenhChoXacNhanGehihi(
            id = "confirm_${shortId()}",
            loai = loai,
            tieuDeTomTat = tieuDeTomTat,
            chiTietXacNhan = chiTietXacNhan,
            thucThi = thucThiBlock
        )
        pendingAiConfirmation = pending
        return KetQuaAiBot(
            cauTraLoi = "⚠️ XÁC NHẬN LỆNH ${loai.tenHienThi}: $chiTietXacNhan\n\n👉 Để tránh thao tác nhầm, Admin vui lòng bấm nút \"Xác nhận thực hiện\" bên dưới (hoặc nhắn \"xác nhận\" / \"đồng ý\"), hoặc bấm \"Hủy lệnh\" nếu muốn hủy.",
            hanhDongThucThi = "Đang chờ xác nhận: $tieuDeTomTat"
        )
    }

    /**
     * Thực thi lệnh THÊM / XÓA đang chờ sau khi Admin bấm nút "Xác nhận thực hiện" hoặc nhắn "xác nhận".
     */
    fun xacNhanThucThiLenhGehihi(
        onNavigateTab: (Int) -> Unit = {},
        onSwitchTicketSubTab: (Int) -> Unit = {},
        onSwitchPeopleSubTab: (Int) -> Unit = {},
        onInspectStaffPayroll: (String) -> Unit = {},
        onCloseDialogOnAction: (String) -> Unit = {}
    ): KetQuaAiBot? {
        val pending = pendingAiConfirmation ?: return null
        pendingAiConfirmation = null
        val kq = pending.thucThi.invoke()
        ghiNhoSuKienDaiHan("Đã xác nhận & thực thi lệnh ${pending.loai.tenHienThi}: ${pending.tieuDeTomTat} -> ${kq.hanhDongThucThi.ifBlank { kq.cauTraLoi }}", luuNgay = false)
        apDungDieuHuong(kq, onNavigateTab, onSwitchTicketSubTab, onSwitchPeopleSubTab, onInspectStaffPayroll)
        val actionSummary = kq.hanhDongThucThi.ifBlank { "Đã thực thi: ${pending.tieuDeTomTat}" }
        themTinNhanAi(
            isUser = false,
            text = "✅ ĐÃ XÁC NHẬN & THỰC THI: ${kq.cauTraLoi}",
            actionNote = actionSummary
        )
        saveToPrefs()
        if (kq.chuyenTab != null || kq.tuDongDongChat) {
            onCloseDialogOnAction(actionSummary)
        }
        return kq
    }

    /**
     * Hủy bỏ lệnh THÊM / XÓA đang chờ xác nhận.
     */
    fun huyBoLenhChoXacNhanGehihi(): KetQuaAiBot? {
        val pending = pendingAiConfirmation ?: return null
        pendingAiConfirmation = null
        val kq = KetQuaAiBot(
            cauTraLoi = "❎ Đã hủy lệnh ${pending.loai.tenHienThi} (${pending.tieuDeTomTat}). Dữ liệu trên ứng dụng được giữ nguyên an toàn.",
            hanhDongThucThi = "Đã hủy lệnh: ${pending.tieuDeTomTat}"
        )
        themTinNhanAi(
            isUser = false,
            text = kq.cauTraLoi,
            actionNote = kq.hanhDongThucThi
        )
        return kq
    }

    suspend fun guiYeuCauAiBot(
        userPrompt: String,
        onNavigateTab: (Int) -> Unit,
        onSwitchTicketSubTab: (Int) -> Unit,
        onSwitchPeopleSubTab: (Int) -> Unit,
        onInspectStaffPayroll: (String) -> Unit,
        onCloseDialogOnAction: (String) -> Unit = {},
        attachedBitmap: Bitmap? = null,
        attachedFileName: String = "",
        attachedFileContent: String = ""
    ) {
        if (!currentUser.isAdmin()) return
        val rawPrompt = userPrompt.trim()
        val cleanPrompt = when {
            rawPrompt.isNotBlank() -> rawPrompt
            attachedBitmap != null -> "Hãy đọc hình ảnh này và báo cáo hoặc thực hiện yêu cầu phù hợp."
            attachedFileContent.isNotBlank() -> "Hãy kiểm tra và khôi phục dữ liệu từ file $attachedFileName"
            else -> return
        }

        val displayUserText = buildString {
            if (attachedBitmap != null) append("🖼️ [Đính kèm ảnh] ")
            if (attachedFileName.isNotBlank()) append("📄 [File: $attachedFileName] ")
            append(cleanPrompt)
        }
        themTinNhanAi(isUser = true, text = displayUserText)
        aiIsLoading = true

        try {
            val normPrompt = normalizeVietnameseText(cleanPrompt).trim()

            // Nếu Admin đính kèm file JSON Backup (từ nút +), xử lý đọc và xác nhận khôi phục trực tiếp
            if (attachedFileContent.isNotBlank()) {
                val rawJson = attachedFileContent.trim()
                if (rawJson.startsWith("{")) {
                    val previewObj = runCatching { JSONObject(rawJson) }.getOrNull()
                    if (previewObj != null) {
                        val cDv = previewObj.optJSONArray("dvs")?.length() ?: 0
                        val cGoi = previewObj.optJSONArray("gois")?.length() ?: 0
                        val cKh = previewObj.optJSONArray("khs")?.length() ?: 0
                        val cNv = previewObj.optJSONArray("nvs")?.length() ?: 0
                        val cVe = previewObj.optJSONArray("luots")?.length() ?: 0
                        val cTc = previewObj.optJSONArray("tcs")?.length() ?: 0
                        val ghiDe = normPrompt.contains("ghi de") || normPrompt.contains("thay the")
                        val confirmRes = taoYeuCauXacNhanGehihi(
                            loai = LoaiLenhChoXacNhan.THEM,
                            tieuDeTomTat = "Nhập dữ liệu từ file $attachedFileName",
                            chiTietXacNhan = "gehihi đã đọc file \"$attachedFileName\":\n" +
                                "• Gồm: $cDv dịch vụ, $cGoi gói, $cKh khách hàng, $cNv nhân viên KTV, $cVe vé, $cTc phiếu quỹ.\n" +
                                "• Chế độ: ${if (ghiDe) "Ghi đè toàn bộ" else "Gộp thông minh vào dữ liệu hiện có"}.\n" +
                                "Bấm Xác nhận bên dưới để nạp ngay vào ứng dụng!"
                        ) {
                            val importRes = nhapDuLieuTuJsonBackup(rawJson, ghiDe)
                            val msg = importRes.getOrElse { "Lỗi nạp file JSON: ${it.message}" }
                            KetQuaAiBot(
                                cauTraLoi = "gehihi: $msg",
                                hanhDongThucThi = "Đã nạp file $attachedFileName",
                                chuyenTab = 2
                            )
                        }
                        themTinNhanAi(
                            isUser = false,
                            text = confirmRes.cauTraLoi,
                            actionNote = confirmRes.hanhDongThucThi
                        )
                        saveToPrefs()
                        aiIsLoading = false
                        return
                    }
                }
            }

            // BƯỚC 0: Kiểm tra nếu đang có lệnh chờ xác nhận và Admin trả lời Xác nhận hoặc Hủy bằng chữ
            if (pendingAiConfirmation != null && attachedBitmap == null) {
                val laDongY = normPrompt == "xac nhan" ||
                    normPrompt == "dong y" ||
                    normPrompt == "ok" ||
                    normPrompt == "oke" ||
                    normPrompt == "co" ||
                    normPrompt == "yes" ||
                    normPrompt == "chac chan" ||
                    normPrompt == "thuc hien" ||
                    normPrompt == "thuc hien di" ||
                    normPrompt == "lam di" ||
                    normPrompt.startsWith("xac nhan ") ||
                    normPrompt.startsWith("dong y ")

                val laHuyBo = normPrompt == "huy" ||
                    normPrompt == "huy bo" ||
                    normPrompt == "huy lenh" ||
                    normPrompt == "khong" ||
                    normPrompt == "thoi" ||
                    normPrompt == "bo qua" ||
                    normPrompt == "no" ||
                    normPrompt == "cancel"

                if (laDongY) {
                    xacNhanThucThiLenhGehihi(
                        onNavigateTab = onNavigateTab,
                        onSwitchTicketSubTab = onSwitchTicketSubTab,
                        onSwitchPeopleSubTab = onSwitchPeopleSubTab,
                        onInspectStaffPayroll = onInspectStaffPayroll,
                        onCloseDialogOnAction = onCloseDialogOnAction
                    )
                    aiIsLoading = false
                    return
                }
                if (laHuyBo) {
                    huyBoLenhChoXacNhanGehihi()
                    aiIsLoading = false
                    return
                }

                // Nếu người dùng nhập một câu lệnh mới hoặc nói chuyện khác, tự động xóa lệnh chờ cũ để tránh kẹt thẻ
                pendingAiConfirmation = null
            }

            // BƯỚC 0.5: Lệnh dạy gehihi ghi nhớ thông tin dài hạn (VD: "ghi nhớ ...", "hãy nhớ rằng ...")
            if (attachedBitmap == null && (normPrompt.startsWith("ghi nho") || normPrompt.startsWith("hay nho") || normPrompt.startsWith("luu nho"))) {
                val note = cleanPrompt.replaceFirst(Regex("(?i)^(ghi nhớ|ghi nho|hãy nhớ rằng|hay nho rang|hãy nhớ|hay nho|lưu nhớ|luu nho)\\s*[:,-]?\\s*"), "").trim()
                if (note.isNotBlank()) {
                    ghiNhoSuKienDaiHan("Ghi chú Admin dặn: $note")
                    themTinNhanAi(
                        isUser = false,
                        text = "🧠 gehihi đã lưu vào Bộ nhớ vĩnh viễn: \"$note\". Dù bạn thoát ứng dụng hay lâu ngày mới mở lại, gehihi vẫn luôn nhớ thông tin này!",
                        actionNote = "Đã lưu vào Bộ nhớ vĩnh viễn (${aiLongTermMemories.size}/$MAX_LONG_TERM_MEMORIES)"
                    )
                    saveToPrefs()
                    aiIsLoading = false
                    return
                }
            }

            // LƯU Ý: Không dùng regex/contains chặn bắt câu lệnh trước Gemini để tránh nhận nhầm từ khóa
            if (attachedBitmap == null) {
                // Tính toán biểu thức toán học nhanh nếu người dùng chỉ nhập phép tính thuần
                val mathResult = tinhBieuThucToanHoc(cleanPrompt)
                if (mathResult != null) {
                    themTinNhanAi(isUser = false, text = mathResult)
                    aiIsLoading = false
                    return
                }
            }

            // BƯỚC 3: Gọi gehihi (gemini-3.8-flash -> dự phòng gemini-3.5-flash-lite) hỗ trợ cả văn bản & Hình ảnh (Multimodal)
            val fullPrompt = buildGehihiPrompt(cleanPrompt, attachedFileName, attachedFileContent)
            val rawResponseText = goiGeminiVoiDuPhong(fullPrompt, attachedBitmap)

            if (!rawResponseText.isNullOrBlank()) {
                val parsed = phanTichPhanHoiGeminiVaThucThi(rawResponseText, cleanPrompt)
                apDungDieuHuong(parsed, onNavigateTab, onSwitchTicketSubTab, onSwitchPeopleSubTab, onInspectStaffPayroll)
                themTinNhanAi(
                    isUser = false,
                    text = parsed.cauTraLoi.replace("**", ""),
                    actionNote = parsed.hanhDongThucThi
                )
                saveToPrefs()
                if (parsed.tuDongDongChat && pendingAiConfirmation == null) {
                    onCloseDialogOnAction(parsed.hanhDongThucThi.ifBlank { parsed.cauTraLoi })
                }
            } else {
                val localReply = traLoiThongMinhNoiBo(cleanPrompt)
                apDungDieuHuong(localReply, onNavigateTab, onSwitchTicketSubTab, onSwitchPeopleSubTab, onInspectStaffPayroll)
                themTinNhanAi(
                    isUser = false,
                    text = localReply.cauTraLoi.replace("**", ""),
                    actionNote = localReply.hanhDongThucThi
                )
            }
        } catch (e: CancellationException) {
            if (e !is TimeoutCancellationException) throw e
            val localReply = traLoiThongMinhNoiBo(cleanPrompt)
            apDungDieuHuong(localReply, onNavigateTab, onSwitchTicketSubTab, onSwitchPeopleSubTab, onInspectStaffPayroll)
            themTinNhanAi(
                isUser = false,
                text = localReply.cauTraLoi.replace("**", ""),
                actionNote = localReply.hanhDongThucThi
            )
        } catch (e: Exception) {
            Log.e(TAG, "gehihi error, using smart responder", e)
            val localReply = traLoiThongMinhNoiBo(cleanPrompt)
            apDungDieuHuong(localReply, onNavigateTab, onSwitchTicketSubTab, onSwitchPeopleSubTab, onInspectStaffPayroll)
            themTinNhanAi(
                isUser = false,
                text = localReply.cauTraLoi.replace("**", ""),
                actionNote = localReply.hanhDongThucThi
            )
        } finally {
            aiIsLoading = false
        }
    }

    private fun apDungDieuHuong(
        kq: KetQuaAiBot,
        onNavigateTab: (Int) -> Unit,
        onSwitchTicketSubTab: (Int) -> Unit,
        onSwitchPeopleSubTab: (Int) -> Unit,
        onInspectStaffPayroll: (String) -> Unit
    ) {
        kq.chuyenMucVe?.let { onSwitchTicketSubTab(it) }
        kq.chuyenMucNguoi?.let { onSwitchPeopleSubTab(it) }
        kq.chuyenMucAdmin?.let { requestedAdminSubTab = it.coerceIn(0, 2) }
        kq.chuyenTab?.let { onNavigateTab(it) }
        kq.moChiTietLuongNvId?.let { onInspectStaffPayroll(it) }
    }

    private suspend fun goiGeminiVoiDuPhong(prompt: String, bitmap: Bitmap? = null): String? {
        return try {
            withTimeout(35_000L) {
                if (bitmap != null) {
                    val inputContent = content {
                        image(bitmap)
                        text(prompt)
                    }
                    geminiFlashModel.generateContent(inputContent).text
                } else {
                    geminiFlashModel.generateContent(prompt).text
                }
            }
        } catch (e: CancellationException) {
            if (e !is TimeoutCancellationException) throw e
            null
        } catch (e1: Exception) {
            Log.w(TAG, "gemini-3.8-flash failed, trying gemini-3.5-flash-lite", e1)
            try {
                withTimeout(25_000L) {
                    if (bitmap != null) {
                        val inputContent = content {
                            image(bitmap)
                            text(prompt)
                        }
                        geminiLiteModel.generateContent(inputContent).text
                    } else {
                        geminiLiteModel.generateContent(prompt).text
                    }
                }
            } catch (e2: CancellationException) {
                if (e2 !is TimeoutCancellationException) throw e2
                null
            } catch (e2: Exception) {
                Log.e(TAG, "Both Gemini models failed", e2)
                null
            }
        }
    }

    /**
     * Bộ thực thi lệnh trực tiếp FULL QUYỀN 100% của gehihi trên App:
     * Tất cả lệnh XÓA, THÊM và SỬA đều yêu cầu xác nhận lại trước khi thực sự thay đổi dữ liệu.
     */
    private fun thucThiLenhTrucTiepTrenApp(prompt: String): KetQuaAiBot? {
        val norm = normalizeVietnameseText(prompt).trim()

        // 0. LỆNH XÓA / SỬA (YÊU CẦU XÁC NHẬN LẠI TRƯỚC KHI THỰC THI)
        val laLenhThem = (norm.contains("them ") || norm.startsWith("them") ||
            norm.contains("tao ") || norm.startsWith("tao") ||
            norm.contains("mo ve") || norm.contains("lap ve")) &&
            !norm.contains("xoa") && !norm.contains("huy bo")

        val coTuHuyThucSu = Regex("\\b(?:huy bo|huy ve|huy lenh|huy quyen|huy boi|huy don)\\b").containsMatchIn(norm) ||
            (Regex("\\bhuy\\b").containsMatchIn(norm) &&
                !norm.contains("ten huy") && !norm.contains("nhan vien huy") &&
                !norm.contains("ktv huy") && !norm.contains("nv huy") &&
                !norm.contains("bam huyet") && !norm.contains("huyet") && !laLenhThem)

        val coTuXoa = !laLenhThem && (norm.contains("xoa") || coTuHuyThucSu || norm.contains("don sach") ||
            norm.contains("clear") || norm.contains("bo bot") || norm.contains("loai bo") ||
            norm.contains("cho nghi") || norm.contains("sa thai"))
        val coTuSua = !laLenhThem && (norm.contains("sua ") || norm.startsWith("sua") ||
            (norm.contains("cap nhat") && !norm.contains("chua cap nhat") && !norm.contains("chua co")) ||
            norm.contains("doi gia") || norm.contains("doi hoa hong") || norm.contains("doi chiet khau") ||
            norm.contains("chinh gia") || norm.contains("chinh hoa hong") || norm.contains("dieu chinh") ||
            norm.contains("thay doi") || norm.contains("doi ten") || norm.contains("doi sdt"))

        if (coTuXoa && Regex("\\bve\\b").containsMatchIn(norm)) {
            if (luots.isEmpty()) {
                return KetQuaAiBot("Hiện tại danh sách vé đang trống, không có vé nào để xóa.", chuyenTab = 0)
            }

            if (norm.contains("dang lam")) {
                val soVe = luots.count { it.dangLam }
                if (soVe == 0) {
                    return KetQuaAiBot("Hiện không có vé nào ở mục Đang làm để xóa.", chuyenTab = 0, chuyenMucVe = 0)
                }
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa toàn bộ $soVe vé đang làm",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa toàn bộ $soVe vé ở mục Đang làm."
                ) {
                    val count = xoaHangLoatVeAdmin("DANG_LAM")
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã xóa sạch toàn bộ $count vé đang làm theo lệnh của Admin!",
                        hanhDongThucThi = "Đã xóa $count vé đang làm",
                        chuyenTab = 0,
                        chuyenMucVe = 0
                    )
                }
            }

            if (norm.contains("cho chot")) {
                val soVe = luots.count { it.choChot }
                if (soVe == 0) {
                    return KetQuaAiBot("Hiện không có vé nào ở mục Chờ chốt để xóa.", chuyenTab = 0, chuyenMucVe = 1)
                }
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa toàn bộ $soVe vé chờ chốt",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa toàn bộ $soVe vé ở mục Chờ chốt."
                ) {
                    val count = xoaHangLoatVeAdmin("CHO_CHOT")
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã xóa toàn bộ $count vé chờ chốt!",
                        hanhDongThucThi = "Đã xóa $count vé chờ chốt",
                        chuyenTab = 0,
                        chuyenMucVe = 1
                    )
                }
            }

            if (norm.contains("da chot") || norm.contains("hoan thanh")) {
                val soVe = luots.count { it.daHoanTat }
                if (soVe == 0) {
                    return KetQuaAiBot("Hiện không có vé nào ở mục Đã chốt để xóa.", chuyenTab = 0, chuyenMucVe = 2)
                }
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa toàn bộ $soVe vé đã chốt",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa toàn bộ $soVe vé ở mục Đã chốt."
                ) {
                    val count = xoaHangLoatVeAdmin("DA_CHOT")
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã xóa toàn bộ $count vé đã chốt!",
                        hanhDongThucThi = "Đã xóa $count vé đã chốt",
                        chuyenTab = 0,
                        chuyenMucVe = 2
                    )
                }
            }

            if (norm.contains("tat ca") || norm.contains("het") || norm.contains("toan bo") || norm.contains("sach")) {
                val soVe = luots.size
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa tất cả $soVe vé trong hệ thống",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa sạch toàn bộ $soVe vé (bao gồm Đang làm, Chờ chốt và Đã chốt)."
                ) {
                    val count = xoaHangLoatVeAdmin("TAT_CA")
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã xóa toàn bộ $count vé trong danh sách theo lệnh của Admin!",
                        hanhDongThucThi = "Đã xóa toàn bộ $count vé",
                        chuyenTab = 0,
                        chuyenMucVe = 0
                    )
                }
            }

            val target = timVeTheoTenTrongCau(luots, norm) ?: luots.first()
            val khTen = khs.firstOrNull { it.id == target.khId }?.ten ?: "Khách"
            val nvTen = nvs.firstOrNull { it.id == target.nvId }?.ten ?: "KTV"
            val targetId = target.id
            return taoYeuCauXacNhanGehihi(
                loai = LoaiLenhChoXacNhan.XOA,
                tieuDeTomTat = "Xóa vé của khách $khTen (KTV $nvTen)",
                chiTietXacNhan = "Bạn đang yêu cầu xóa vé #${target.id} của khách $khTen (KTV $nvTen, giờ vào ${target.tgbd})."
            ) {
                xoaVe(targetId)
                KetQuaAiBot(
                    cauTraLoi = "gehihi đã xóa vé của khách $khTen khỏi hệ thống.",
                    hanhDongThucThi = "Đã xóa vé khách $khTen",
                    chuyenTab = 0
                )
            }
        }

        // 0A. LỆNH CẤP QUYỀN ADMIN / FULL QUYỀN HOẶC THU HỒI QUYỀN
        if ((norm.contains("cap quyen") || norm.contains("gan mac admin") || norm.contains("len admin") || norm.contains("full quyen") || norm.contains("mo quyen")) &&
            !norm.contains("tao nhan vien") && !norm.contains("tao tai khoan")
        ) {
            val targetTk = taiKhoans.firstOrNull { tk ->
                val shortName = normalizeVietnameseText(tk.hoTen).substringAfterLast(" ")
                (shortName.length >= 2 && norm.contains(shortName)) || norm.contains(tk.username.lowercase())
            }
            if (targetTk != null) {
                val capMacAdmin = norm.contains("admin") || norm.contains("ngang bang") || norm.contains("toan quyen")
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.SUA,
                    tieuDeTomTat = if (capMacAdmin) "Cấp mác Admin cho ${targetTk.hoTen}" else "Mở đủ 6 quyền cho ${targetTk.hoTen}",
                    chiTietXacNhan = if (capMacAdmin) {
                        "Bạn đang yêu cầu cấp quyền Admin toàn hệ thống cho tài khoản ${targetTk.hoTen} (SĐT: ${targetTk.username})."
                    } else {
                        "Bạn đang yêu cầu bật đủ 6 quyền chi tiết cho nhân viên ${targetTk.hoTen} (SĐT: ${targetTk.username})."
                    }
                ) {
                    if (capMacAdmin) {
                        toggleMacAdminChoTaiKhoan(targetTk.id, true)
                        KetQuaAiBot(
                            cauTraLoi = "gehihi đã cấp quyền ngang bằng Admin (gắn mác Admin) cho tài khoản ${targetTk.hoTen} (SĐT đăng nhập: ${targetTk.username}).",
                            hanhDongThucThi = "Đã cấp mác Admin cho ${targetTk.hoTen}",
                            chuyenTab = 5
                        )
                    } else {
                        datTatCaQuyenNhanVien(targetTk.id, true)
                        KetQuaAiBot(
                            cauTraLoi = "gehihi đã bật toàn bộ 6 quyền chi tiết cho nhân viên ${targetTk.hoTen} (${targetTk.username}).",
                            hanhDongThucThi = "Đã mở đủ 6 quyền cho ${targetTk.hoTen}",
                            chuyenTab = 5
                        )
                    }
                }
            }
        }

        if (norm.contains("thu hoi quyen") || norm.contains("go mac admin") || norm.contains("huy quyen") || norm.contains("khoa quyen")) {
            val targetTk = taiKhoans.firstOrNull { tk ->
                val shortName = normalizeVietnameseText(tk.hoTen).substringAfterLast(" ")
                (shortName.length >= 2 && norm.contains(shortName)) || norm.contains(tk.username.lowercase())
            }
            if (targetTk != null) {
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.SUA,
                    tieuDeTomTat = "Thu hồi quyền của ${targetTk.hoTen}",
                    chiTietXacNhan = "Bạn đang yêu cầu gỡ mác Admin và thu hồi các quyền phụ của tài khoản ${targetTk.hoTen} (${targetTk.username})."
                ) {
                    toggleMacAdminChoTaiKhoan(targetTk.id, false)
                    datTatCaQuyenNhanVien(targetTk.id, false)
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã thu hồi quyền Admin và khóa các quyền phụ của ${targetTk.hoTen} (${targetTk.username}), đưa về nhân viên mặc định.",
                        hanhDongThucThi = "Đã thu hồi quyền của ${targetTk.hoTen}",
                        chuyenTab = 5
                    )
                }
            }
        }

        // 0B. LỆNH SỬA / ĐỔI EMAIL (GMAIL), SỐ ĐIỆN THOẠI, MẬT KHẨU HOẶC HỌ TÊN CỦA ADMIN & NHÂN VIÊN
        val laLenhSuaTaiKhoan = !laLenhThem && (coTuSua || norm.contains("doi ") || norm.startsWith("doi")) &&
            !norm.contains("chua cap nhat") && !norm.contains("chua co") &&
            (norm.contains("mail") || norm.contains("gmail") || norm.contains("email") ||
                norm.contains("mat khau") || norm.contains("mk") || norm.contains("pass") ||
                ((norm.contains("sdt") || norm.contains("so dien thoai") || norm.contains("tai khoan")) && !norm.contains("khach")))

        if (laLenhSuaTaiKhoan && !coTuXoa) {
            val normWithoutGender = norm.replace("gioi tinh nam", "").replace("gioi tinh nu", "")
                .replace("gt nam", "").replace("gt nu", "")
            val staffMatched = taiKhoans.firstOrNull { tk ->
                if (tk.id == "tk_admin") false
                else {
                    val shortName = normalizeVietnameseText(tk.hoTen).substringAfterLast(" ")
                    (shortName.length >= 2 && Regex("\\b$shortName\\b").containsMatchIn(normWithoutGender)) ||
                        (tk.username.isNotBlank() && normWithoutGender.contains(tk.username.lowercase()))
                }
            }
            val targetTk = if (norm.contains("admin") || norm.contains("chu quan") || norm.contains("cua toi")) {
                taiKhoans.firstOrNull { it.id == "tk_admin" } ?: currentUser
            } else {
                staffMatched
            }

            if (targetTk != null) {
                val isRootAdmin = targetTk.id == "tk_admin"
                val subTabTarget = if (isRootAdmin) 1 else 0
                val linkedNv = nvs.firstOrNull { it.id == targetTk.nvId }

                val emailFound = Regex("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}")
                    .find(prompt)?.value?.trim()?.lowercase()
                val phoneFound = Regex("(0\\d{8,10})")
                    .findAll(norm).map { it.value }.lastOrNull { it != targetTk.username }
                val passFound = if (norm.contains("mat khau") || norm.contains("mk") || norm.contains("pass")) {
                    Regex("(?:mat khau|mk|pass|thanh|la|moi|:|=)\\s*([a-zA-Z0-9]{4,16})\\s*$")
                        .find(norm)?.groupValues?.get(1)
                } else null
                val nameFound = Regex("(?i)(?:tên thành|ten thanh|tên mới là|ten moi la|đổi tên là|doi ten la)\\s+([\\p{L}\\s]{2,25})")
                    .find(prompt)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }

                // Nếu Admin ra lệnh "sửa mail", "đổi gmail", "sửa tài khoản admin" nhưng chưa gõ giá trị mới trong câu
                if (emailFound == null && phoneFound == null && passFound == null && nameFound == null) {
                    val currentMailDisplay = targetTk.email.ifBlank {
                        if (isRootAdmin && cloudUserEmail.isNotBlank()) cloudUserEmail else "chưa gắn Gmail"
                    }
                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.SUA,
                        tieuDeTomTat = "Mở màn hình Sửa ${if (isRootAdmin) "Tài khoản Admin gốc (Gmail / SĐT / MK)" else "Tài khoản ${targetTk.hoTen}"}",
                        chiTietXacNhan = "Tài khoản ${targetTk.hoTen} hiện tại:\n" +
                            "• SĐT đăng nhập: ${targetTk.username}\n" +
                            "• Gmail liên kết: $currentMailDisplay\n" +
                            "• Mật khẩu: ${targetTk.matKhauHienThi.ifBlank { DEFAULT_ADMIN_PASS }}\n\n" +
                            "👉 Bạn có thể nhắn trực tiếp: \"Sửa mail admin thành ten_moi@gmail.com\" HOẶC bấm \"Xác nhận thực hiện\" bên dưới để gehihi chuyển thẳng màn hình tới mục ${if (isRootAdmin) "Sửa TK Admin gốc" else "Quản lý TK Nhân viên"} cho bạn đổi ngay!"
                    ) {
                        KetQuaAiBot(
                            cauTraLoi = "gehihi đã chuyển màn hình tới mục ${if (isRootAdmin) "Sửa TK Admin gốc (để bạn đổi Gmail, SĐT hoặc chọn tài khoản Google mới)" else "Cấp quyền & Tài khoản Nhân viên"}.",
                            hanhDongThucThi = "Đã mở mục ${if (isRootAdmin) "Sửa TK Admin gốc" else "Sửa TK ${targetTk.hoTen}"}",
                            chuyenTab = 5,
                            chuyenMucAdmin = subTabTarget,
                            tuDongDongChat = true
                        )
                    }
                }

                val hoTenMoi = nameFound ?: targetTk.hoTen
                val sdtMoi = phoneFound ?: targetTk.username
                val emailMoi = emailFound ?: targetTk.email
                val matKhauMoi = passFound ?: targetTk.matKhauHienThi.ifBlank {
                    if (isRootAdmin) DEFAULT_ADMIN_PASS else "123456"
                }

                val changesDesc = buildList {
                    if (emailFound != null) add("Gmail: ${targetTk.email.ifBlank { "chưa gắn" }} ➔ $emailMoi")
                    if (phoneFound != null) add("SĐT đăng nhập: ${targetTk.username} ➔ $sdtMoi")
                    if (passFound != null) add("Mật khẩu: ${targetTk.matKhauHienThi.ifBlank { "***" }} ➔ $matKhauMoi")
                    if (nameFound != null) add("Họ tên: ${targetTk.hoTen} ➔ $hoTenMoi")
                }.joinToString("\n• ", prefix = "• ")

                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.SUA,
                    tieuDeTomTat = "Sửa TK ${targetTk.hoTen}: ${emailFound ?: phoneFound ?: passFound ?: hoTenMoi}",
                    chiTietXacNhan = "Bạn đang yêu cầu sửa tài khoản ${targetTk.hoTen}:\n$changesDesc\n\nSau khi xác nhận, màn hình sẽ tự động chuyển tới mục ${if (isRootAdmin) "Sửa TK Admin gốc" else "Cấp quyền & TK Nhân viên"} để bạn kiểm tra."
                ) {
                    adminCapNhatTaiKhoanVaNhanVien(
                        tkId = targetTk.id,
                        hoTenMoi = hoTenMoi,
                        sdtDangNhapMoi = sdtMoi,
                        matKhauMoi = matKhauMoi,
                        gtMoi = linkedNv?.gt ?: "Nam",
                        ghiChuMoi = linkedNv?.gc ?: "",
                        emailMoi = emailMoi
                    )
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã cập nhật tài khoản ${hoTenMoi}: SĐT $sdtMoi, Gmail ${emailMoi.ifBlank { "chưa gắn" }}, mật khẩu $matKhauMoi.",
                        hanhDongThucThi = "Đã sửa TK $hoTenMoi (${emailFound ?: phoneFound ?: "đã cập nhật"})",
                        chuyenTab = 5,
                        chuyenMucAdmin = subTabTarget,
                        tuDongDongChat = true
                    )
                }
            }
        }

        // 0C. LỆNH NẠP DỮ LIỆU MENU BACKUP CŨ
        if (norm.contains("nap menu") || norm.contains("khoi phuc menu") || norm.contains("menu moi nhat")) {
            val msg = napMenuChuanTuFileBackupCu()
            ghiNhoSuKienDaiHan("Khôi phục 10 dịch vụ & 3 gói từ menu_mới_nhất_2.json", luuNgay = false)
            return KetQuaAiBot(
                cauTraLoi = "gehihi: $msg",
                hanhDongThucThi = "Đã nạp 10 dịch vụ & 3 gói từ menu_mới_nhất_2.json",
                chuyenTab = 2
            )
        }

        // =====================================================================
        // 0D. CÁC LỆNH SỬA TRỰC TIẾP MỌI DỮ LIỆU (DỊCH VỤ, GÓI, VÉ, KHÁCH, SỔ QUỸ, KTV)
        // Tất cả đều qua bước XÁC NHẬN LỆNH SỬA DỮ LIỆU (LoaiLenhChoXacNhan.SUA)
        // =====================================================================
        if (coTuSua && !coTuXoa) {
            // 1) SỬA DỊCH VỤ (VD: "sửa dịch vụ chườm đá nóng giá 80k hoa hồng 30k", "đổi hoa hồng trị liệu 60 phút thành 80k")
            val matchedDv = timDichVuTheoCauLenh(norm)
            if (matchedDv != null && (norm.contains("dich vu") || norm.contains("hoa hong") || norm.contains("chiet khau") || norm.contains("gia ") || norm.contains("bang gia") || !Regex("\\bve\\b").containsMatchIn(norm))) {
                val allAmounts = trichXuatDanhSachSoTien(norm)
                var giaMoi = matchedDv.gia
                var hoaHongMoi = matchedDv.chietKhauVnd

                val giaMatch = Regex("(?:gia|gia ve|gia goc|ve goc)\\s*(?:la|thanh|=|:)?\\s*(\\d+\\s*(?:k|nghin|ngan|tr|trieu|\\d{3,}))").find(norm)
                val hhMatch = Regex("(?:hoa hong|chiet khau|ck)\\s*(?:la|thanh|=|:)?\\s*(\\d+\\s*(?:k|nghin|ngan|tr|trieu|\\d{3,}))").find(norm)

                if (giaMatch != null) {
                    trichXuatSoTien(giaMatch.groupValues[1])?.let { giaMoi = it }
                }
                if (hhMatch != null) {
                    trichXuatSoTien(hhMatch.groupValues[1])?.let { hoaHongMoi = it }
                }
                if (giaMatch == null && hhMatch == null && allAmounts.isNotEmpty()) {
                    if (norm.contains("hoa hong") || norm.contains("chiet khau")) {
                        hoaHongMoi = allAmounts.first()
                        if (allAmounts.size >= 2) giaMoi = allAmounts[1]
                    } else {
                        giaMoi = allAmounts.first()
                        if (allAmounts.size >= 2) hoaHongMoi = allAmounts[1]
                    }
                }
                val phutMoi = Regex("(\\d{2,3})\\s*(?:phut|p)\\b").find(norm)?.groupValues?.get(1)?.toIntOrNull() ?: matchedDv.phut
                val tenMoiMatch = Regex("(?i)(?:đổi tên thành|doi ten thanh|tên mới là|ten moi la)\\s+([^,.;]+)").find(prompt)
                val tenMoi = tenMoiMatch?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() } ?: matchedDv.ten

                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.SUA,
                    tieuDeTomTat = "Sửa dịch vụ ${matchedDv.ten}: Giá ${formatVnd(giaMoi)}, Hoa hồng ${formatVnd(hoaHongMoi)}",
                    chiTietXacNhan = "Bạn đang yêu cầu sửa dịch vụ \"${matchedDv.ten}\" (áp dụng từ thời điểm này trở đi):\n" +
                        "• Tên dịch vụ: $tenMoi ($phutMoi phút)\n" +
                        "• Giá dịch vụ: ${formatVnd(matchedDv.gia)} ➔ ${formatVnd(giaMoi)}\n" +
                        "• Hoa hồng KTV: ${formatVnd(matchedDv.chietKhauVnd)} ➔ ${formatVnd(hoaHongMoi)}"
                ) {
                    luuDichVu(
                        id = matchedDv.id,
                        ten = tenMoi,
                        phut = phutMoi,
                        gia = giaMoi,
                        moTa = matchedDv.moTa,
                        chietKhauVnd = hoaHongMoi
                    )
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã cập nhật dịch vụ \"$tenMoi\": Giá mới ${formatVndSpeakable(giaMoi)}, hoa hồng KTV mới ${formatVndSpeakable(hoaHongMoi)} (áp dụng từ thời điểm hiện tại trở đi).",
                        hanhDongThucThi = "Đã sửa DV $tenMoi: Giá ${formatVnd(giaMoi)} • CK ${formatVnd(hoaHongMoi)}",
                        chuyenTab = 2
                    )
                }
            }

            // 2) SỬA GÓI LIỆU TRÌNH HOẶC SỐ BUỔI GÓI CỦA KHÁCH
            if (norm.contains("goi")) {
                val matchedKh = khs.firstOrNull {
                    val shortName = normalizeVietnameseText(it.ten).substringAfterLast(" ")
                    (shortName.length >= 2 && norm.contains(shortName)) || (it.sdt.isNotBlank() && norm.contains(it.sdt))
                }
                val dkCuaKhach = if (matchedKh != null) dks.firstOrNull { it.khId == matchedKh.id } else null
                val soBuoiMatch = Regex("(\\d{1,2})\\s*(?:buoi|luot|ve)").find(norm)?.groupValues?.get(1)?.toIntOrNull()

                if (matchedKh != null && dkCuaKhach != null && soBuoiMatch != null) {
                    val goiInfo = gois.firstOrNull { it.id == dkCuaKhach.goiId }
                    val tongBuoi = goiInfo?.soBuoi ?: 10
                    val luotDungMoi = if (norm.contains("con lai") || norm.contains("con ")) {
                        (tongBuoi - soBuoiMatch).coerceIn(0, tongBuoi)
                    } else {
                        soBuoiMatch.coerceIn(0, tongBuoi)
                    }
                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.SUA,
                        tieuDeTomTat = "Sửa tiến độ gói của ${matchedKh.ten}: Đã dùng $luotDungMoi/$tongBuoi buổi",
                        chiTietXacNhan = "Bạn đang yêu cầu sửa số buổi đã dùng trong gói \"${goiInfo?.ten ?: "Liệu trình"}\" của khách ${matchedKh.ten}:\n" +
                            "• Đã dùng: ${dkCuaKhach.luotDaDung}/$tongBuoi buổi ➔ $luotDungMoi/$tongBuoi buổi (Còn lại ${tongBuoi - luotDungMoi} buổi)"
                    ) {
                        suaDangKyGoiKhach(dkCuaKhach.id, luotDungMoi)
                        KetQuaAiBot(
                            cauTraLoi = "gehihi đã cập nhật gói của khách ${matchedKh.ten}: Đã dùng $luotDungMoi/$tongBuoi buổi (còn ${tongBuoi - luotDungMoi} buổi).",
                            hanhDongThucThi = "Đã sửa gói KH ${matchedKh.ten}: $luotDungMoi/$tongBuoi buổi",
                            chuyenTab = 1,
                            chuyenMucNguoi = 0
                        )
                    }
                }

                val matchedGoi = gois.firstOrNull { norm.contains(normalizeVietnameseText(it.ten)) } ?: gois.firstOrNull()
                if (matchedGoi != null) {
                    val giaMoi = trichXuatSoTien(norm) ?: matchedGoi.gia
                    val soBuoiGoiMoi = soBuoiMatch ?: matchedGoi.soBuoi
                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.SUA,
                        tieuDeTomTat = "Sửa gói ${matchedGoi.ten}: $soBuoiGoiMoi buổi - ${formatVnd(giaMoi)}",
                        chiTietXacNhan = "Bạn đang yêu cầu sửa gói liệu trình \"${matchedGoi.ten}\":\n" +
                            "• Số buổi: ${matchedGoi.soBuoi} ➔ $soBuoiGoiMoi buổi\n" +
                            "• Giá gói: ${formatVnd(matchedGoi.gia)} ➔ ${formatVnd(giaMoi)}"
                    ) {
                        luuGoiLieuTrinh(
                            id = matchedGoi.id,
                            ten = matchedGoi.ten,
                            dvIds = matchedGoi.dvIds,
                            soBuoi = soBuoiGoiMoi,
                            gia = giaMoi,
                            chiTietLuot = matchedGoi.chiTietLuot,
                            dvTangKem = matchedGoi.dvTangKem
                        )
                        KetQuaAiBot(
                            cauTraLoi = "gehihi đã cập nhật gói \"${matchedGoi.ten}\": $soBuoiGoiMoi buổi, giá ${formatVndSpeakable(giaMoi)}.",
                            hanhDongThucThi = "Đã sửa gói ${matchedGoi.ten}: ${formatVnd(giaMoi)}",
                            chuyenTab = 2
                        )
                    }
                }
            }

            // 3) SỬA VÉ (GIÁ VÉ, HOA HỒNG RIÊNG CỦA VÉ, KTV, KHÁCH, GIỜ VÀO/RA)
            if (Regex("\\bve\\b").containsMatchIn(norm) || norm.contains("gia ve") || norm.contains("hoa hong ve")) {
                val targetVe = timVeTheoTenTrongCau(luots, norm) ?: luots.firstOrNull()
                if (targetVe == null) {
                    return KetQuaAiBot("Hiện chưa có vé nào trong danh sách để sửa.", chuyenTab = 0)
                }
                val khHienTai = khs.firstOrNull { it.id == targetVe.khId }
                val nvHienTai = nvs.firstOrNull { it.id == targetVe.nvId }
                val hoaHongHienTai = tinhHoaHongVe(targetVe)

                val allAmounts = trichXuatDanhSachSoTien(norm)
                var thuMoi = targetVe.thu
                var hoaHongMoi: Long? = targetVe.hoaHongChot ?: hoaHongHienTai

                val giaMatch = Regex("(?:gia|gia ve|thu|tien ve)\\s*(?:la|thanh|=|:)?\\s*(\\d+\\s*(?:k|nghin|ngan|tr|trieu|\\d{3,}))").find(norm)
                val hhMatch = Regex("(?:hoa hong|chiet khau|ck)\\s*(?:la|thanh|=|:)?\\s*(\\d+\\s*(?:k|nghin|ngan|tr|trieu|\\d{3,}))").find(norm)

                if (giaMatch != null) {
                    trichXuatSoTien(giaMatch.groupValues[1])?.let { thuMoi = it }
                }
                if (hhMatch != null) {
                    trichXuatSoTien(hhMatch.groupValues[1])?.let { hoaHongMoi = it }
                }
                if (giaMatch == null && hhMatch == null && allAmounts.isNotEmpty()) {
                    if (norm.contains("hoa hong") || norm.contains("chiet khau")) {
                        hoaHongMoi = allAmounts.first()
                    } else {
                        thuMoi = allAmounts.first()
                        if (allAmounts.size >= 2) hoaHongMoi = allAmounts[1]
                    }
                }

                val nvMoi = nvs.firstOrNull { nv ->
                    val shortName = normalizeVietnameseText(nv.ten).substringAfterLast(" ")
                    nv.id != targetVe.nvId && shortName.length >= 2 && (norm.contains("ktv $shortName") || norm.contains("nhan vien $shortName") || norm.contains("sang $shortName"))
                } ?: nvHienTai

                val timesFound = Regex("\\b([01]?\\d|2[0-3]):([0-5]\\d)\\b").findAll(norm).map { it.value }.toList()
                val tgbdMoi = timesFound.getOrNull(0) ?: targetVe.tgbd
                val tgktMoi = timesFound.getOrNull(1) ?: targetVe.tgkt

                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.SUA,
                    tieuDeTomTat = "Sửa vé khách ${khHienTai?.ten ?: "Khách"}: Thu ${formatVnd(thuMoi)}, Hoa hồng ${formatVnd(hoaHongMoi ?: 0L)}",
                    chiTietXacNhan = "Bạn đang yêu cầu sửa vé #${targetVe.id} của khách ${khHienTai?.ten ?: "Khách"}:\n" +
                        "• KTV phụ trách: ${nvHienTai?.ten ?: "KTV"} ➔ ${nvMoi?.ten ?: "KTV"}\n" +
                        "• Giờ làm: ${targetVe.tgbd}${if (targetVe.tgkt.isNotBlank()) " - ${targetVe.tgkt}" else ""} ➔ $tgbdMoi${if (tgktMoi.isNotBlank()) " - $tgktMoi" else ""}\n" +
                        "• Tiền thu vé: ${formatVnd(targetVe.thu)} ➔ ${formatVnd(thuMoi)}\n" +
                        "• Hoa hồng KTV: ${formatVnd(hoaHongHienTai)} ➔ ${formatVnd(hoaHongMoi ?: 0L)}"
                ) {
                    suaToanBoVeAdmin(
                        veId = targetVe.id,
                        khIdMoi = targetVe.khId,
                        nvIdMoi = nvMoi?.id ?: targetVe.nvId,
                        dvIdsMoi = targetVe.dvIds,
                        dkIdMoi = targetVe.dkId,
                        dvNgoaiMoi = targetVe.dvNgoai,
                        tgbdMoi = tgbdMoi,
                        tgktMoi = tgktMoi,
                        ngayMoi = targetVe.ngay,
                        thuMoi = thuMoi,
                        hoaHongMoi = hoaHongMoi
                    )
                    val subTabVeTarget = when {
                        targetVe.dangLam -> 0
                        targetVe.choChot -> 1
                        else -> 2
                    }
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã cập nhật vé của khách ${khHienTai?.ten ?: "Khách"}: Tiền thu ${formatVndSpeakable(thuMoi)}, hoa hồng KTV ${formatVndSpeakable(hoaHongMoi ?: 0L)} (đã đồng bộ sổ quỹ & bảng lương).",
                        hanhDongThucThi = "Đã sửa vé #${targetVe.id}: Thu ${formatVnd(thuMoi)} • CK ${formatVnd(hoaHongMoi ?: 0L)}",
                        chuyenTab = 0,
                        chuyenMucVe = subTabVeTarget,
                        tuDongDongChat = true
                    )
                }
            }

            // 4) SỬA THÔNG TIN KHÁCH HÀNG
            if (norm.contains("khach")) {
                val matchedKh = khs.firstOrNull {
                    val shortName = normalizeVietnameseText(it.ten).substringAfterLast(" ")
                    (shortName.length >= 2 && norm.contains(shortName)) || (it.sdt.isNotBlank() && norm.contains(it.sdt))
                } ?: khs.firstOrNull()
                if (matchedKh != null) {
                    val newPhone = Regex("(0\\d{8,10})").findAll(norm).map { it.value }.lastOrNull { it != matchedKh.sdt } ?: matchedKh.sdt
                    val nameMatch = Regex("(?i)(?:tên thành|ten thanh|tên mới là|ten moi la|thành|thanh)\\s+([\\p{L}\\s]{2,25})").find(prompt)
                    val newName = nameMatch?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() } ?: matchedKh.ten
                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.SUA,
                        tieuDeTomTat = "Sửa khách hàng ${matchedKh.ten} ➔ $newName ($newPhone)",
                        chiTietXacNhan = "Bạn đang yêu cầu sửa thông tin khách hàng:\n" +
                            "• Họ tên: ${matchedKh.ten} ➔ $newName\n" +
                            "• Số điện thoại: ${matchedKh.sdt} ➔ $newPhone"
                    ) {
                        suaKhachHang(matchedKh.id, newName, newPhone)
                        KetQuaAiBot(
                            cauTraLoi = "gehihi đã cập nhật thông tin khách hàng thành: $newName (SĐT: $newPhone).",
                            hanhDongThucThi = "Đã sửa KH: $newName ($newPhone)",
                            chuyenTab = 1,
                            chuyenMucNguoi = 0,
                            tuDongDongChat = true
                        )
                    }
                }
            }

            // 5) SỬA GIAO DỊCH SỔ QUỸ THU / CHI
            if (norm.contains("so quy") || norm.contains("phieu thu") || norm.contains("phieu chi") || norm.contains("giao dich")) {
                val targetTc = tcs.firstOrNull { norm.contains(normalizeVietnameseText(it.lyDo)) } ?: tcs.firstOrNull()
                val tienMoi = trichXuatSoTien(norm) ?: targetTc?.tien ?: 0L
                if (targetTc != null && tienMoi > 0L) {
                    val loaiMoi = when {
                        norm.contains("phieu thu") || norm.contains("sang thu") -> LoaiTC.THU
                        norm.contains("phieu chi") || norm.contains("sang chi") -> LoaiTC.CHI
                        else -> targetTc.loai
                    }
                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.SUA,
                        tieuDeTomTat = "Sửa giao dịch sổ quỹ: ${targetTc.lyDo} ➔ ${formatVnd(tienMoi)}",
                        chiTietXacNhan = "Bạn đang yêu cầu sửa giao dịch Sổ quỹ:\n" +
                            "• Nội dung: ${targetTc.lyDo}\n" +
                            "• Loại phiếu: ${targetTc.loai.name} ➔ ${loaiMoi.name}\n" +
                            "• Số tiền: ${formatVnd(targetTc.tien)} ➔ ${formatVnd(tienMoi)}"
                    ) {
                        suaGiaoDichSoQuy(targetTc.id, loaiMoi, tienMoi, targetTc.lyDo, targetTc.ngay)
                        KetQuaAiBot(
                            cauTraLoi = "gehihi đã cập nhật giao dịch Sổ quỹ \"${targetTc.lyDo}\" thành ${formatVndSpeakable(tienMoi)}.",
                            hanhDongThucThi = "Đã sửa Sổ quỹ: ${formatVnd(tienMoi)}",
                            chuyenTab = 3,
                            tuDongDongChat = true
                        )
                    }
                }
            }

            // 6) SỬA THÔNG TIN NHÂN VIÊN KTV (HỌ TÊN, SĐT, GIỚI TÍNH, CHUYÊN MÔN)
            if (norm.contains("nhan vien") || norm.contains("ktv")) {
                val matchedNv = nvs.firstOrNull { nv ->
                    val shortName = normalizeVietnameseText(nv.ten).substringAfterLast(" ")
                    (shortName.length >= 2 && norm.contains(shortName)) || (nv.sdt.isNotBlank() && norm.contains(nv.sdt))
                } ?: nvs.firstOrNull()
                if (matchedNv != null) {
                    val newPhone = Regex("(0\\d{8,10})").findAll(norm).map { it.value }.lastOrNull { it != matchedNv.sdt } ?: matchedNv.sdt
                    val nameMatch = Regex("(?i)(?:tên thành|ten thanh|tên mới là|ten moi la|đổi tên là|doi ten la|thành|thanh)\\s+([\\p{L}\\s]{2,25})").find(prompt)
                    val newName = nameMatch?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() } ?: matchedNv.ten
                    val newGt = when {
                        norm.contains("sang nu") || norm.endsWith(" nu") -> "Nữ"
                        norm.contains("sang nam") || norm.endsWith(" nam") -> "Nam"
                        else -> matchedNv.gt
                    }
                    val linkedTk = taiKhoans.firstOrNull { it.nvId == matchedNv.id }
                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.SUA,
                        tieuDeTomTat = "Sửa KTV ${matchedNv.ten} ➔ $newName ($newPhone)",
                        chiTietXacNhan = "Bạn đang yêu cầu sửa thông tin Nhân viên KTV:\n" +
                            "• Họ tên: ${matchedNv.ten} ➔ $newName ($newGt)\n" +
                            "• Số điện thoại: ${matchedNv.sdt} ➔ $newPhone"
                    ) {
                        if (linkedTk != null) {
                            adminCapNhatTaiKhoanVaNhanVien(
                                tkId = linkedTk.id,
                                hoTenMoi = newName,
                                sdtDangNhapMoi = newPhone,
                                matKhauMoi = linkedTk.matKhauHienThi.ifBlank { "123456" },
                                gtMoi = newGt,
                                ghiChuMoi = matchedNv.gc,
                                emailMoi = linkedTk.email
                            )
                        } else {
                            val idx = nvs.indexOfFirst { it.id == matchedNv.id }
                            if (idx >= 0) {
                                nvs[idx] = nvs[idx].copy(ten = newName, sdt = newPhone, gt = newGt)
                                saveToPrefs()
                            }
                        }
                        KetQuaAiBot(
                            cauTraLoi = "gehihi đã cập nhật nhân viên KTV thành: $newName (SĐT: $newPhone, $newGt).",
                            hanhDongThucThi = "Đã sửa KTV: $newName ($newPhone)",
                            chuyenTab = 1,
                            chuyenMucNguoi = 1,
                            tuDongDongChat = true
                        )
                    }
                }
            }
        }

        // 0E. LỆNH ĐIỀU HƯỚNG / CHUYỂN HƯỚNG TỚI MỌI TAB & MỤC CON (ƯU TIÊN TRƯỚC KHI RƠI XUỐNG HỎI ĐÁP)
        val coTuDieuHuong = norm.contains("chuyen huong") ||
            norm.startsWith("chuyen sang") ||
            norm.startsWith("chuyen toi") ||
            norm.startsWith("chuyen den") ||
            norm.startsWith("chuyen qua") ||
            norm.startsWith("di toi") ||
            norm.startsWith("di den") ||
            norm.startsWith("vao muc") ||
            norm.startsWith("vao tab") ||
            norm.startsWith("vao trang") ||
            norm.startsWith("mo muc") ||
            norm.startsWith("mo tab") ||
            norm.startsWith("mo trang") ||
            norm.startsWith("mo man hinh") ||
            norm.startsWith("mo danh sach") ||
            norm.startsWith("xem muc") ||
            norm.startsWith("xem tab") ||
            norm.startsWith("dua toi") ||
            norm.startsWith("quay ve") ||
            norm == "ve trang chu" ||
            norm == "trang chu"

        if (coTuDieuHuong && !coTuXoa && !coTuSua && !norm.contains("them ") && !norm.contains("tao ")) {
            return when {
                (norm.contains("nhan vien") || norm.contains("ktv") || norm.contains("nhan su")) &&
                    (norm.contains("quyen") || norm.contains("phan quyen") || norm.contains("tai khoan")) ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng tới mục Quản lý Tài khoản & Cấp quyền Nhân viên.",
                        hanhDongThucThi = "Chuyển tới mục Cấp quyền & TK Nhân viên",
                        chuyenTab = 5,
                        chuyenMucAdmin = 0,
                        tuDongDongChat = true
                    )
                norm.contains("nhan vien") || norm.contains("ktv") || norm.contains("nhan su") || norm.contains("ky thuat vien") ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng tới mục Danh sách Nhân viên KTV.",
                        hanhDongThucThi = "Chuyển tới mục Nhân viên KTV",
                        chuyenTab = 1,
                        chuyenMucNguoi = 1,
                        tuDongDongChat = true
                    )
                norm.contains("khach") || norm.contains("the goi") || norm.contains("goi khach") ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng tới mục Khách hàng & Thẻ gói.",
                        hanhDongThucThi = "Chuyển tới mục Khách hàng & Gói",
                        chuyenTab = 1,
                        chuyenMucNguoi = 0,
                        tuDongDongChat = true
                    )
                norm.contains("sao luu") || norm.contains("backup") || norm.contains("khoi phuc") || norm.contains("xoa du lieu") ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng tới mục Sao lưu, Khôi phục & Xóa dữ liệu.",
                        hanhDongThucThi = "Chuyển tới mục Sao lưu & Khôi phục",
                        chuyenTab = 5,
                        chuyenMucAdmin = 2,
                        tuDongDongChat = true
                    )
                norm.contains("admin") || norm.contains("mail") || norm.contains("gmail") ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng tới mục Sửa Tài khoản Admin gốc.",
                        hanhDongThucThi = "Chuyển tới mục Sửa TK Admin gốc",
                        chuyenTab = 5,
                        chuyenMucAdmin = 1,
                        tuDongDongChat = true
                    )
                norm.contains("quyen") || norm.contains("tai khoan") || norm.contains("quan tri") ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng tới tab Quyền hạn & Tài khoản.",
                        hanhDongThucThi = "Chuyển tới Tab Quyền hạn",
                        chuyenTab = 5,
                        chuyenMucAdmin = 0,
                        tuDongDongChat = true
                    )
                norm.contains("so quy") || norm.contains("thu chi") || norm.contains("phieu thu") || norm.contains("phieu chi") || norm.contains("quy") ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng tới tab Sổ quỹ Thu Chi.",
                        hanhDongThucThi = "Chuyển tới Tab Sổ quỹ",
                        chuyenTab = 3,
                        tuDongDongChat = true
                    )
                norm.contains("bao cao") || norm.contains("doanh thu") || norm.contains("bang luong") || norm.contains("luong") || norm.contains("hoa hong") || norm.contains("lai rong") ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng tới tab Báo cáo Doanh thu & Bảng lương.",
                        hanhDongThucThi = "Chuyển tới Tab Báo cáo",
                        chuyenTab = 4,
                        tuDongDongChat = true
                    )
                norm.contains("menu") || norm.contains("bang gia") || norm.contains("dich vu") || norm.contains("goi") ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng tới tab Dịch vụ & Gói liệu trình.",
                        hanhDongThucThi = "Chuyển tới Tab Dịch vụ",
                        chuyenTab = 2,
                        tuDongDongChat = true
                    )
                norm.contains("cho chot") ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng tới mục Vé Chờ chốt.",
                        hanhDongThucThi = "Chuyển tới mục Vé Chờ chốt",
                        chuyenTab = 0,
                        chuyenMucVe = 1,
                        tuDongDongChat = true
                    )
                norm.contains("da chot") || norm.contains("lich su ve") ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng tới mục Vé Đã chốt.",
                        hanhDongThucThi = "Chuyển tới mục Vé Đã chốt",
                        chuyenTab = 0,
                        chuyenMucVe = 2,
                        tuDongDongChat = true
                    )
                else ->
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã chuyển hướng về trang chủ Vận hành Vé.",
                        hanhDongThucThi = "Chuyển tới Trang chủ Vé",
                        chuyenTab = 0,
                        chuyenMucVe = 0,
                        tuDongDongChat = true
                    )
            }
        }

        // 1. LỆNH MỞ VÉ / TẠO VÉ / THÊM VÉ / LÊN VÉ / ĐẶT VÉ (YÊU CẦU XÁC NHẬN LẠI TRƯỚC KHI THÊM)
        val laLenhTaoVe = norm.contains("them ve") ||
            norm.contains("mo ve") ||
            norm.contains("tao ve") ||
            norm.contains("len ve") ||
            norm.contains("dat ve") ||
            norm.startsWith("ve moi")
        if (laLenhTaoVe) {
            return chuanBiXacNhanTaoVeTuCauLenh(prompt, norm)
        }

        // 2. LỆNH CHỐT VÉ / XÁC NHẬN VÉ
        val laLenhChotVe = norm.contains("chot ve") ||
            norm.contains("chot tat ca") ||
            norm.contains("chot het") ||
            norm.contains("xac nhan ve")
        if (laLenhChotVe) {
            val choChot = luots.filter { it.choChot }
            if (choChot.isNotEmpty()) {
                val matched = timVeTheoTenTrongCau(choChot, norm)
                val dsCanChot = if (norm.contains("tat ca") || norm.contains("het") || matched == null) choChot else listOf(matched)
                var tongHoaHong = 0L
                dsCanChot.forEach { ve ->
                    tongHoaHong += tinhHoaHongVe(ve)
                    xacNhanChotVe(ve.id)
                }
                ghiNhoSuKienDaiHan("Chốt ${dsCanChot.size} vé, trích hoa hồng ${formatVnd(tongHoaHong)}", luuNgay = false)
                return KetQuaAiBot(
                    cauTraLoi = "gehihi đã chốt thành công ${dsCanChot.size} vé và tự động trích ${formatVndSpeakable(tongHoaHong)} tiền hoa hồng KTV vào Sổ quỹ.",
                    hanhDongThucThi = "Đã chốt ${dsCanChot.size} vé (Chuyển sang mục Đã chốt)",
                    chuyenTab = 0,
                    chuyenMucVe = 2
                )
            }
            val dangLam = luots.filter { it.dangLam }
            if (dangLam.isNotEmpty()) {
                val matched = timVeTheoTenTrongCau(dangLam, norm)
                val dsCanChot = if (norm.contains("tat ca") || norm.contains("het") || matched == null) dangLam else listOf(matched)
                var tongHoaHong = 0L
                dsCanChot.forEach { ve ->
                    hoanThanhVe(ve.id, nowTimeStr(), ve.thu)
                    tongHoaHong += tinhHoaHongVe(ve)
                    xacNhanChotVe(ve.id)
                }
                ghiNhoSuKienDaiHan("Hoàn thành & chốt ${dsCanChot.size} vé, hoa hồng ${formatVnd(tongHoaHong)}", luuNgay = false)
                return KetQuaAiBot(
                    cauTraLoi = "gehihi đã hoàn thành và chốt luôn ${dsCanChot.size} vé đang làm, trích tổng hoa hồng ${formatVndSpeakable(tongHoaHong)} cho KTV.",
                    hanhDongThucThi = "Đã hoàn thành & chốt ${dsCanChot.size} vé",
                    chuyenTab = 0,
                    chuyenMucVe = 2
                )
            }
            return KetQuaAiBot(
                cauTraLoi = "Hiện tại không có vé nào đang chờ chốt hoặc đang làm.",
                chuyenTab = 0
            )
        }

        // 3. LỆNH HOÀN THÀNH VÉ / XONG VÉ / KẾT THÚC VÉ
        val laLenhHoanThanhVe = norm.contains("hoan thanh") ||
            norm.contains("ket thuc ve") ||
            norm.contains("xong ve")
        if (laLenhHoanThanhVe && (norm.contains("ve") || norm.contains("tat ca") || norm.contains("het"))) {
            val dangLam = luots.filter { it.dangLam }
            if (dangLam.isEmpty()) {
                return KetQuaAiBot(
                    cauTraLoi = "Hiện không có vé nào đang làm để hoàn thành.",
                    chuyenTab = 0,
                    chuyenMucVe = 0
                )
            }
            val matched = timVeTheoTenTrongCau(dangLam, norm)
            val dsCanHoanThanh = if (norm.contains("tat ca") || norm.contains("het") || matched == null) dangLam else listOf(matched)
            dsCanHoanThanh.forEach { ve ->
                hoanThanhVe(ve.id, nowTimeStr(), ve.thu)
            }
            ghiNhoSuKienDaiHan("Hoàn thành ${dsCanHoanThanh.size} vé đang làm", luuNgay = false)
            return KetQuaAiBot(
                cauTraLoi = "gehihi đã hoàn thành ${dsCanHoanThanh.size} vé đang làm và chuyển sang mục Chờ chốt.",
                hanhDongThucThi = "Đã hoàn thành ${dsCanHoanThanh.size} vé (Mục Chờ chốt)",
                chuyenTab = 0,
                chuyenMucVe = 1
            )
        }

        // 4. LỆNH XÓA ĐƠN LẺ / XÓA NGẪU NHIÊN / XÓA CẢ CỤM DỮ LIỆU (KHÁCH, NHÂN VIÊN, DỊCH VỤ, GÓI, SỔ QUỸ, TOÀN BỘ APP)
        val coTuTatCa = norm.contains("tat ca") || norm.contains("toan bo") || norm.contains("het") || norm.contains("sach") || norm.contains("ca cum")
        val coTuNgauNhien = norm.contains("ngau nhien") || norm.contains("bat ky") || norm.contains("bot 1") || norm.contains("1 nguoi") || norm.contains("mot nguoi") || norm.contains("1 nhan vien") || norm.contains("mot nhan vien")
        val giuKhungChatDeBaoCao = norm.contains("bao cao ra day") || norm.contains("bao cao tai day") || norm.contains("ra day")

        if (coTuXoa && (norm.contains("lich su chat") || norm.contains("tin nhan chat") || norm.contains("doan chat"))) {
            aiMessages.clear()
            aiMessages.add(
                AiChatMessage(
                    id = "msg_welcome",
                    isUser = false,
                    text = "Đã làm sạch khung trò chuyện. Bộ nhớ sự kiện dài hạn (${aiLongTermMemories.size} ghi nhớ) vẫn được bảo toàn.",
                    actionNote = "Đã làm mới khung chat",
                    thoiGian = "${todayDateStr()} ${nowTimeStr()}"
                )
            )
            saveGehihiMemoryVault()
            return KetQuaAiBot(
                cauTraLoi = "gehihi đã dọn sạch các tin nhắn cũ trong khung trò chuyện.",
                hanhDongThucThi = "Đã dọn lịch sử chat"
            )
        }

        if (coTuXoa && (norm.contains("du lieu") || norm.contains("tat ca du lieu") || norm.contains("toan bo du lieu") ||
            norm.contains("sach du lieu") || norm.contains("reset app") || norm.contains("du lieu ung dung") ||
            norm.contains("het du lieu") || norm.contains("du lieu goc"))) {
            return taoYeuCauXacNhanGehihi(
                loai = LoaiLenhChoXacNhan.XOA,
                tieuDeTomTat = "XÓA SẠCH TẤT CẢ DỮ LIỆU ỨNG DỤNG (Vé, Khách, KTV, Dịch vụ, Gói, Sổ quỹ)",
                chiTietXacNhan = "⚠️ CẢNH BÁO QUAN TRỌNG:\nBạn đang yêu cầu gehihi xóa sạch toàn bộ dữ liệu trên cả máy và Cloud (Vé: ${luots.size}, Khách: ${khs.size}, KTV: ${nvs.size}, Dịch vụ: ${dvs.size}, Gói: ${gois.size}, Sổ quỹ: ${tcs.size}), chỉ giữ lại tài khoản Admin đang đăng nhập."
            ) {
                val res = xoaTatCaDuLieuUngDungAdmin(matKhauNhap = "", boQuaKiemTraMatKhau = true)
                KetQuaAiBot(
                    cauTraLoi = res.getOrElse { "Không thể xóa toàn bộ dữ liệu: ${it.message}" },
                    hanhDongThucThi = "Đã xóa sạch toàn bộ dữ liệu ứng dụng",
                    chuyenTab = 5,
                    chuyenMucAdmin = 2
                )
            }
        }

        if (coTuXoa && (norm.contains("nhan vien") || norm.contains("ktv") || norm.contains("tai khoan"))) {
            if (coTuTatCa) {
                val count = nvs.size
                if (count == 0) return KetQuaAiBot("Danh sách nhân viên KTV hiện đang trống.", chuyenTab = 1, chuyenMucNguoi = 1)
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa toàn bộ $count nhân viên KTV & tài khoản nhân viên",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa toàn bộ $count nhân viên KTV (${nvs.joinToString { it.ten }}) và các tài khoản đăng nhập nhân viên (giữ nguyên tài khoản Admin gốc)."
                ) {
                    val dsTen = nvs.map { it.ten }
                    nvs.toList().forEach { xoaNhanVien(it.id) }
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã xóa toàn bộ ${dsTen.size} nhân viên KTV (${dsTen.joinToString()}). Hiện danh sách KTV còn 0 người.",
                        hanhDongThucThi = "Đã xóa toàn bộ ${dsTen.size} KTV",
                        chuyenTab = if (giuKhungChatDeBaoCao) null else 1,
                        chuyenMucNguoi = 1
                    )
                }
            }
            val matchedNv = nvs.firstOrNull { nv ->
                val fullNorm = normalizeVietnameseText(nv.ten)
                val shortName = fullNorm.substringAfterLast(" ")
                norm.contains(fullNorm) ||
                    (shortName.length >= 2 && Regex("\\b$shortName\\b").containsMatchIn(norm)) ||
                    (nv.sdt.isNotBlank() && norm.contains(nv.sdt))
            } ?: if (coTuNgauNhien) {
                nvs.randomOrNull()
            } else null

            if (matchedNv != null) {
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa nhân viên & tài khoản: ${matchedNv.ten} (${matchedNv.sdt})",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa nhân viên KTV ${matchedNv.ten} (SĐT: ${matchedNv.sdt}) cùng tài khoản đăng nhập của nhân viên này.\nSau khi xác nhận, gehihi sẽ xóa và báo cáo danh sách nhân viên còn lại."
                ) {
                    val tenDaXoa = matchedNv.ten
                    val sdtDaXoa = matchedNv.sdt
                    xoaNhanVien(matchedNv.id)
                    val conLai = if (nvs.isEmpty()) "Không còn nhân viên nào" else nvs.joinToString(", ") { "${it.ten} (${it.sdt})" }
                    onSwitchPeopleSubTabInternal(1)
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã xóa nhân viên KTV $tenDaXoa (SĐT: $sdtDaXoa).\n• Báo cáo nhân sự còn lại (${nvs.size} KTV): $conLai.",
                        hanhDongThucThi = "Đã xóa KTV $tenDaXoa (Còn ${nvs.size} KTV)",
                        chuyenTab = if (giuKhungChatDeBaoCao) null else 1,
                        chuyenMucNguoi = 1
                    )
                }
            } else {
                return KetQuaAiBot("Không tìm thấy nhân viên KTV nào phù hợp với yêu cầu xóa. Vui lòng nói rõ tên hoặc SĐT nhân viên.", chuyenTab = 1, chuyenMucNguoi = 1)
            }
        }

        if (coTuXoa && norm.contains("khach")) {
            if (coTuTatCa) {
                val count = khs.size
                if (count == 0) return KetQuaAiBot("Danh sách khách hàng đang trống.", chuyenTab = 1, chuyenMucNguoi = 0)
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa toàn bộ $count khách hàng",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa toàn bộ $count khách hàng và gói đăng ký liên quan."
                ) {
                    val c = khs.size
                    khs.clear()
                    dks.clear()
                    saveToPrefs()
                    KetQuaAiBot("gehihi đã xóa toàn bộ $c khách hàng.", "Đã xóa $c khách hàng", chuyenTab = 1, chuyenMucNguoi = 0)
                }
            }
            val matchedKh = khs.firstOrNull {
                norm.contains(normalizeVietnameseText(it.ten.substringAfterLast(" "))) || (it.sdt.isNotBlank() && norm.contains(it.sdt))
            } ?: if (coTuNgauNhien) khs.randomOrNull() else khs.firstOrNull()
            if (matchedKh != null) {
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa khách hàng ${matchedKh.ten} (${matchedKh.sdt})",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa khách hàng ${matchedKh.ten} (SĐT: ${matchedKh.sdt})."
                ) {
                    xoaKhachHang(matchedKh.id)
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã xóa khách hàng ${matchedKh.ten} (${matchedKh.sdt}). Danh sách khách hiện còn ${khs.size} người.",
                        hanhDongThucThi = "Đã xóa khách ${matchedKh.ten}",
                        chuyenTab = 1,
                        chuyenMucNguoi = 0
                    )
                }
            } else {
                return KetQuaAiBot("Danh sách khách hàng hiện đang trống.", chuyenTab = 1, chuyenMucNguoi = 0)
            }
        }

        if (coTuXoa && (norm.contains("dich vu") || norm.contains("menu"))) {
            if (coTuTatCa) {
                val count = dvs.size
                if (count == 0) return KetQuaAiBot("Menu dịch vụ hiện đang trống.", chuyenTab = 2)
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa toàn bộ $count dịch vụ trong Menu",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa sạch toàn bộ $count dịch vụ trong Menu của quán."
                ) {
                    val c = dvs.size
                    dvs.clear()
                    prefs.edit().putBoolean("user_wiped_data", true).apply()
                    saveToPrefs()
                    KetQuaAiBot("gehihi đã xóa sạch toàn bộ $c dịch vụ trong Menu.", "Đã xóa toàn bộ $c dịch vụ", chuyenTab = 2)
                }
            }
            val matchedDv = timDichVuTheoCauLenh(norm) ?: if (coTuNgauNhien) dvs.randomOrNull() else dvs.firstOrNull()
            if (matchedDv != null) {
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa dịch vụ ${matchedDv.ten}",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa dịch vụ ${matchedDv.ten} (Giá ${formatVnd(matchedDv.gia)})."
                ) {
                    xoaDichVu(matchedDv.id)
                    KetQuaAiBot("gehihi đã xóa dịch vụ ${matchedDv.ten}. Menu hiện còn ${dvs.size} dịch vụ.", "Đã xóa dịch vụ ${matchedDv.ten}", chuyenTab = 2)
                }
            }
        }

        if (coTuXoa && norm.contains("goi")) {
            if (coTuTatCa) {
                val totalGoi = gois.size
                val totalDk = dks.size
                if (totalGoi == 0 && totalDk == 0) return KetQuaAiBot("Hiện không có gói liệu trình nào để xóa.", chuyenTab = 2)
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa toàn bộ $totalGoi gói mẫu & $totalDk gói khách đăng ký",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa sạch toàn bộ $totalGoi gói liệu trình và $totalDk thẻ gói của khách."
                ) {
                    gois.clear()
                    dks.clear()
                    saveToPrefs()
                    KetQuaAiBot("gehihi đã xóa toàn bộ $totalGoi gói liệu trình và $totalDk thẻ gói của khách.", "Đã xóa toàn bộ gói liệu trình", chuyenTab = 2)
                }
            }
            val matchedGoi = gois.firstOrNull { norm.contains(normalizeVietnameseText(it.ten)) } ?: gois.firstOrNull()
            if (matchedGoi != null) {
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa gói liệu trình ${matchedGoi.ten}",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa gói liệu trình ${matchedGoi.ten} (${matchedGoi.soBuoi} buổi - ${formatVnd(matchedGoi.gia)})."
                ) {
                    xoaGoiLieuTrinh(matchedGoi.id)
                    KetQuaAiBot("gehihi đã xóa gói liệu trình ${matchedGoi.ten}.", "Đã xóa gói ${matchedGoi.ten}", chuyenTab = 2)
                }
            }
        }

        if (coTuXoa && (norm.contains("so quy") || norm.contains("giao dich") || norm.contains("thu chi") || norm.contains("phieu thu") || norm.contains("phieu chi"))) {
            if (coTuTatCa || norm.contains("phieu thu") || norm.contains("phieu chi")) {
                val chiXoaThu = norm.contains("phieu thu") && !norm.contains("phieu chi")
                val chiXoaChi = norm.contains("phieu chi") && !norm.contains("phieu thu")
                val targetList = when {
                    chiXoaThu -> tcs.filter { it.loai == LoaiTC.THU }
                    chiXoaChi -> tcs.filter { it.loai == LoaiTC.CHI }
                    else -> tcs.toList()
                }
                if (targetList.isEmpty()) return KetQuaAiBot("Không có giao dịch Sổ quỹ nào phù hợp để xóa.", chuyenTab = 3)
                val label = when {
                    chiXoaThu -> "toàn bộ ${targetList.size} phiếu THU"
                    chiXoaChi -> "toàn bộ ${targetList.size} phiếu CHI"
                    else -> "toàn bộ ${targetList.size} giao dịch Sổ quỹ"
                }
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa $label",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa $label trong Sổ quỹ."
                ) {
                    val ids = targetList.map { it.id }.toSet()
                    tcs.removeAll { it.id in ids }
                    saveToPrefs()
                    KetQuaAiBot("gehihi đã xóa $label.", "Đã xóa $label", chuyenTab = 3)
                }
            }
            val latest = tcs.firstOrNull()
            if (latest != null) {
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.XOA,
                    tieuDeTomTat = "Xóa giao dịch sổ quỹ: ${latest.lyDo}",
                    chiTietXacNhan = "Bạn đang yêu cầu xóa giao dịch gần nhất: ${latest.lyDo} (${formatVnd(latest.tien)})."
                ) {
                    xoaGiaoDichSoQuy(latest.id)
                    KetQuaAiBot("gehihi đã xóa giao dịch sổ quỹ gần nhất: ${latest.lyDo}.", "Đã xóa giao dịch sổ quỹ", chuyenTab = 3)
                }
            }
        }

        // 5. LỆNH THÊM KHÁCH HÀNG (YÊU CẦU XÁC NHẬN LẠI TRƯỚC KHI THÊM)
        if (norm.contains("them khach") || norm.contains("tao khach")) {
            val phone = Regex("(0\\d{8,10})").find(norm)?.value ?: "Đang cập nhật"
            val nameExtracted = trichXuatTenSauTuKhoa(
                prompt,
                listOf("khách hàng", "khach hang", "thêm khách", "them khach", "tạo khách", "tao khach", "tên là", "ten la")
            ).replace(Regex("0\\d{8,10}"), "").replace(Regex("(?i)sđt|sdt|số điện thoại|-|,|:"), "").trim()
                .ifBlank { "Khách mới ${khs.size + 1}" }
            return taoYeuCauXacNhanGehihi(
                loai = LoaiLenhChoXacNhan.THEM,
                tieuDeTomTat = "Thêm khách hàng $nameExtracted ($phone)",
                chiTietXacNhan = "Bạn đang yêu cầu thêm khách hàng mới: Tên \"$nameExtracted\", SĐT định danh \"$phone\"."
            ) {
                val kh = themKhachHang(nameExtracted, phone)
                KetQuaAiBot(
                    cauTraLoi = "gehihi đã thêm khách hàng ${kh?.ten ?: nameExtracted} (SĐT định danh: ${kh?.sdt ?: phone}) vào danh sách Khách hàng.",
                    hanhDongThucThi = "Đã thêm khách: ${kh?.ten ?: nameExtracted} (${kh?.sdt ?: phone})",
                    chuyenTab = 1,
                    chuyenMucNguoi = 0
                )
            }
        }

        // 6. LỆNH TẠO TÀI KHOẢN MỚI / THÊM NHÂN VIÊN KTV (YÊU CẦU XÁC NHẬN LẠI TRƯỚC KHI THÊM)
        if (norm.contains("them nhan vien") || norm.contains("them ktv") || norm.contains("them nv") ||
            norm.contains("tao nhan vien") || norm.contains("tao ktv") || norm.contains("tao nv") ||
            norm.contains("tao tai khoan") || norm.contains("them tai khoan")
        ) {
            val phoneExplicit = Regex("(?i)(?:sđt|sdt|số điện thoại|so dien thoai|dien thoai|phone|tel)\\s*[:=]?\\s*(\\d{3,12})").find(prompt)?.groupValues?.get(1)
            val phoneStandalone = Regex("\\b(0\\d{8,10}|\\d{9,11}|113|114|115)\\b").find(norm)?.value
            val phone = phoneExplicit?.takeIf { it.isNotBlank() } ?: phoneStandalone?.takeIf { it.isNotBlank() } ?: ""

            val passMatch = Regex("(?:mat khau|mk|pass)\\s*[:=]?\\s*([a-zA-Z0-9]{4,15})").find(norm)
            val pass = passMatch?.groupValues?.get(1) ?: "123456"
            val gt = if (Regex("(?i)(?:giới tính|gioi tinh)?\\s*[:=]?\\s*(?:nu|nữ)").containsMatchIn(prompt)) "Nữ" else "Nam"
            val laAdmin = norm.contains("admin")
            val emailFound = Regex("(?i)\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b").find(prompt)?.value ?: ""

            // Bóc tách tên KTV thông minh trước các từ khóa thuộc tính
            var rawName = trichXuatTenSauTuKhoa(
                prompt,
                listOf(
                    "thêm nhân viên tên là", "them nhan vien ten la", "thêm nhân viên tên", "them nhan vien ten",
                    "thêm ktv tên là", "them ktv ten la", "thêm ktv tên", "them ktv ten",
                    "thêm nv tên là", "them nv ten la", "thêm nv tên", "them nv ten",
                    "tạo nhân viên tên là", "tao nhan vien ten la", "tạo nhân viên tên", "tao nhan vien ten",
                    "tạo ktv tên", "tao ktv ten", "tạo nv tên", "tao nv ten",
                    "thêm nhân viên", "them nhan vien", "thêm ktv", "them ktv", "thêm nv", "them nv",
                    "tạo nhân viên", "tao nhan vien", "tạo ktv", "tao ktv", "tạo nv", "tao nv",
                    "tài khoản cho", "tai khoan cho", "tài khoản", "tai khoan",
                    "nhân viên", "nhan vien", "ktv", "kỹ thuật viên", "tên là", "ten la"
                )
            )
            val splitKeywords = listOf("giới tính", "gioi tinh", "sdt", "sđt", "số điện thoại", "so dien thoai", "mật khẩu", "mat khau", "mk", "pass", "mail", "email", "admin")
            val cutIdx = splitKeywords.map { kw -> rawName.lowercase().indexOf(kw) }.filter { it >= 0 }.minOrNull()
            if (cutIdx != null && cutIdx > 0) {
                rawName = rawName.substring(0, cutIdx)
            }
            val nameExtracted = rawName
                .replace(Regex("(?i)0\\d{8,10}"), "")
                .replace(Regex("(?i)tên\\s+là|ten\\s+la|tên|ten|sđt|sdt|số điện thoại|so dien thoai|admin|nam|nữ|nu|cho|-|,|:"), "")
                .trim()
                .split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                .ifBlank { "KTV Mới" }

            if (phone.isBlank()) {
                return KetQuaAiBot(
                    cauTraLoi = "Để thêm nhân viên \"$nameExtracted\", bạn vui lòng cung cấp Số điện thoại (dùng làm tên đăng nhập tài khoản). Ví dụ:\n👉 \"Thêm nv $nameExtracted sđt 0912345678 giới tính $gt\"",
                    hanhDongThucThi = "Cần SĐT để tạo tài khoản KTV",
                    chuyenTab = 1,
                    chuyenMucNguoi = 1
                )
            }

            return taoYeuCauXacNhanGehihi(
                loai = LoaiLenhChoXacNhan.THEM,
                tieuDeTomTat = "Tạo tài khoản & KTV $nameExtracted (SĐT: $phone)",
                chiTietXacNhan = "Bạn đang yêu cầu tạo nhân viên & tài khoản mới: Họ tên \"$nameExtracted\" ($gt), SĐT đăng nhập \"$phone\", mật khẩu \"$pass\"${if (emailFound.isNotBlank()) ", Gmail: $emailFound" else ""}${if (laAdmin) ", cấp mác Admin" else ""}."
            ) {
                val nv = themNhanVien(
                    ten = nameExtracted,
                    gt = gt,
                    sdt = phone,
                    gc = "Bấm huyệt trị liệu",
                    matKhauKhoiTao = pass,
                    laAdmin = laAdmin
                )
                val tkCreated = taiKhoans.firstOrNull { it.nvId == nv?.id }
                if (tkCreated != null && emailFound.isNotBlank()) {
                    val tkIdx = taiKhoans.indexOfFirst { it.id == tkCreated.id }
                    if (tkIdx >= 0) {
                        taiKhoans[tkIdx] = taiKhoans[tkIdx].copy(email = emailFound)
                        saveToPrefs()
                    }
                }
                KetQuaAiBot(
                    cauTraLoi = "gehihi đã tạo tài khoản nhân viên ${nv?.ten ?: nameExtracted}${if (laAdmin) " (có mác Admin)" else ""}. Tên đăng nhập (SĐT): ${tkCreated?.username ?: phone}, mật khẩu: $pass${if (emailFound.isNotBlank()) ", Gmail: $emailFound" else ""}.",
                    hanhDongThucThi = "Đã tạo TK: ${tkCreated?.username ?: phone} (MK: $pass)",
                    chuyenTab = 1,
                    chuyenMucNguoi = 1
                )
            }
        }

        // 7. LỆNH THÊM DỊCH VỤ MỚI (YÊU CẦU XÁC NHẬN LẠI TRƯỚC KHI THÊM)
        if (norm.contains("them dich vu") || norm.contains("tao dich vu")) {
            val allAmounts = trichXuatDanhSachSoTien(norm)
            val gia = allAmounts.getOrNull(0) ?: 160_000L
            val phut = Regex("(\\d{2,3})\\s*(phut|p)").find(norm)?.groupValues?.get(1)?.toIntOrNull() ?: 60
            val tenDv = trichXuatTenSauTuKhoa(prompt, listOf("dịch vụ", "dich vu")).ifBlank { "Trị liệu mới (${phut}p)" }
            val hoaHong = allAmounts.getOrNull(1) ?: (gia * 40 / 100)
            return taoYeuCauXacNhanGehihi(
                loai = LoaiLenhChoXacNhan.THEM,
                tieuDeTomTat = "Thêm dịch vụ $tenDv (${formatVnd(gia)})",
                chiTietXacNhan = "Bạn đang yêu cầu thêm dịch vụ mới: \"$tenDv\" (${phut} phút), giá ${formatVnd(gia)}, hoa hồng KTV ${formatVnd(hoaHong)}."
            ) {
                val dv = luuDichVu(null, tenDv, phut, gia, "Tạo qua gehihi", hoaHong)
                KetQuaAiBot(
                    cauTraLoi = "gehihi đã thêm dịch vụ ${dv?.ten ?: tenDv}, giá ${formatVndSpeakable(gia)}, hoa hồng KTV ${formatVndSpeakable(hoaHong)}.",
                    hanhDongThucThi = "Đã thêm dịch vụ: ${dv?.ten ?: tenDv}",
                    chuyenTab = 2
                )
            }
        }

        // 7B. LỆNH TẠO GÓI LIỆU TRÌNH MỚI HOẶC ĐĂNG KÝ GÓI CHO KHÁCH
        if (norm.contains("dang ky goi") || norm.contains("mua goi")) {
            val matchedKh = khs.firstOrNull {
                val shortName = normalizeVietnameseText(it.ten).substringAfterLast(" ")
                (shortName.length >= 2 && norm.contains(shortName)) || (it.sdt.isNotBlank() && norm.contains(it.sdt))
            } ?: khs.firstOrNull()
            val matchedGoi = gois.firstOrNull { norm.contains(normalizeVietnameseText(it.ten)) } ?: gois.firstOrNull()
            if (matchedKh != null && matchedGoi != null) {
                val giaThu = trichXuatSoTien(norm) ?: matchedGoi.gia
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.THEM,
                    tieuDeTomTat = "Đăng ký ${matchedGoi.ten} cho khách ${matchedKh.ten} (${formatVnd(giaThu)})",
                    chiTietXacNhan = "Bạn đang yêu cầu đăng ký gói \"${matchedGoi.ten}\" (${matchedGoi.soBuoi} buổi) cho khách ${matchedKh.ten} với số tiền thu ${formatVnd(giaThu)}."
                ) {
                    dangKyGoiChoKhach(matchedKh.id, matchedGoi.id, giaThu)
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã đăng ký gói ${matchedGoi.ten} cho khách ${matchedKh.ten} và ghi nhận phiếu THU ${formatVndSpeakable(giaThu)} vào Sổ quỹ.",
                        hanhDongThucThi = "Đã đăng ký gói cho KH ${matchedKh.ten}",
                        chuyenTab = 1,
                        chuyenMucNguoi = 0
                    )
                }
            }
        }

        if (norm.contains("them goi") || norm.contains("tao goi")) {
            val gia = trichXuatSoTien(norm) ?: 900_000L
            val soBuoi = Regex("(\\d{1,2})\\s*(?:buoi|luot|ve)").find(norm)?.groupValues?.get(1)?.toIntOrNull() ?: 10
            val tenGoi = trichXuatTenSauTuKhoa(prompt, listOf("thêm gói", "them goi", "tạo gói", "tao goi"))
                .replace(Regex("(?i)\\d+\\s*(?:buổi|buoi|lượt|luot|k|nghìn|triệu|tr).*"), "")
                .trim()
                .ifBlank { "Gói liệu trình $soBuoi buổi" }
            return taoYeuCauXacNhanGehihi(
                loai = LoaiLenhChoXacNhan.THEM,
                tieuDeTomTat = "Tạo gói $tenGoi ($soBuoi buổi - ${formatVnd(gia)})",
                chiTietXacNhan = "Bạn đang yêu cầu tạo gói liệu trình mới: \"$tenGoi\" gồm $soBuoi buổi, giá ${formatVnd(gia)}."
            ) {
                val goi = luuGoiLieuTrinh(null, tenGoi, listOfNotNull(dvs.firstOrNull()?.id), soBuoi, gia)
                KetQuaAiBot(
                    cauTraLoi = "gehihi đã tạo gói liệu trình \"${goi?.ten ?: tenGoi}\" ($soBuoi buổi, giá ${formatVndSpeakable(gia)}).",
                    hanhDongThucThi = "Đã tạo gói: ${goi?.ten ?: tenGoi}",
                    chuyenTab = 2
                )
            }
        }

        // 8. LỆNH GHI THU / CHI SỔ QUỸ (YÊU CẦU XÁC NHẬN LẠI TRƯỚC KHI THÊM)
        val soTien = trichXuatSoTien(norm)
        if (soTien != null && soTien >= 1_000L && !coTuSua) {
            if (norm.contains("ghi chi") || norm.startsWith("chi ") || norm.contains("phieu chi") || norm.contains("mua ")) {
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.THEM,
                    tieuDeTomTat = "Ghi phiếu CHI ${formatVnd(soTien)}",
                    chiTietXacNhan = "Bạn đang yêu cầu thêm phiếu CHI ${formatVnd(soTien)} vào Sổ quỹ với nội dung: \"$prompt\"."
                ) {
                    val tc = themGiaoDichSoQuy(LoaiTC.CHI, soTien, prompt)
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã ghi phiếu Chi ${formatVndSpeakable(tc?.tien ?: soTien)} với nội dung: ${tc?.lyDo ?: prompt}.",
                        hanhDongThucThi = "Đã ghi CHI -${formatVnd(tc?.tien ?: soTien)}",
                        chuyenTab = 3
                    )
                }
            }
            if (norm.contains("ghi thu") || norm.startsWith("thu ") || norm.contains("phieu thu")) {
                return taoYeuCauXacNhanGehihi(
                    loai = LoaiLenhChoXacNhan.THEM,
                    tieuDeTomTat = "Ghi phiếu THU ${formatVnd(soTien)}",
                    chiTietXacNhan = "Bạn đang yêu cầu thêm phiếu THU ${formatVnd(soTien)} vào Sổ quỹ với nội dung: \"$prompt\"."
                ) {
                    val tc = themGiaoDichSoQuy(LoaiTC.THU, soTien, prompt)
                    KetQuaAiBot(
                        cauTraLoi = "gehihi đã ghi phiếu Thu ${formatVndSpeakable(tc?.tien ?: soTien)} với nội dung: ${tc?.lyDo ?: prompt}.",
                        hanhDongThucThi = "Đã ghi THU +${formatVnd(tc?.tien ?: soTien)}",
                        chuyenTab = 3
                    )
                }
            }
        }

        return null
    }

    private fun onSwitchPeopleSubTabInternal(subIdx: Int) {
        // Được gọi kèm thông qua KetQuaAiBot.chuyenMucNguoi trong apDungDieuHuong
    }

    private fun timDichVuTheoCauLenh(norm: String): DV? {
        val coXongHoi = norm.contains("xong hoi")
        return dvs.firstOrNull { dv ->
            val dvNorm = normalizeVietnameseText(dv.ten)
            norm.contains(dvNorm)
        } ?: dvs.firstOrNull { dv ->
            val dvNorm = normalizeVietnameseText(dv.ten)
            when {
                norm.contains("da nong") || norm.contains("chuom da") -> dvNorm.contains("da nong") || dvNorm.contains("chuom")
                norm.contains("cao gio") || norm.contains("giac hoi") -> dvNorm.contains("cao gio") || dvNorm.contains("giac hoi")
                norm.contains("120") && coXongHoi -> dv.phut == 120 && dvNorm.contains("xong hoi")
                norm.contains("90") && coXongHoi -> dv.phut == 90 && dvNorm.contains("xong hoi")
                norm.contains("60") && coXongHoi -> dv.phut == 60 && dvNorm.contains("xong hoi")
                norm.contains("30") && coXongHoi -> dv.phut == 30 && dvNorm.contains("xong hoi")
                coXongHoi && !norm.contains("tri lieu") -> dvNorm.contains("xong hoi")
                norm.contains("120") -> dv.phut == 120 && !dvNorm.contains("xong hoi")
                norm.contains("90") -> dv.phut == 90 && !dvNorm.contains("xong hoi")
                norm.contains("30") -> dv.phut == 30 && !dvNorm.contains("xong hoi")
                norm.contains("60") -> dv.phut == 60 && !dvNorm.contains("xong hoi")
                else -> false
            }
        }
    }

    /**
     * Phân tích thông tin Khách, KTV, Dịch vụ từ câu lệnh mở vé và tạo yêu cầu XÁC NHẬN trước khi mở vé thật.
     */
    private fun chuanBiXacNhanTaoVeTuCauLenh(rawPrompt: String, norm: String): KetQuaAiBot {
        val previewNv = nvs.firstOrNull { nv ->
            val fullNorm = normalizeVietnameseText(nv.ten)
            val shortName = fullNorm.substringAfterLast(" ")
            norm.contains(fullNorm) || (shortName.length >= 2 && Regex("\\b$shortName\\b").containsMatchIn(norm))
        } ?: nvs.firstOrNull()

        val previewKh = khs.firstOrNull { kh ->
            val fullNorm = normalizeVietnameseText(kh.ten)
            val shortName = fullNorm.substringAfterLast(" ")
            norm.contains(fullNorm) || (shortName.length >= 2 && Regex("\\b$shortName\\b").containsMatchIn(norm) && shortName != normalizeVietnameseText(previewNv?.ten.orEmpty()).substringAfterLast(" "))
        }
        val khRegex = Regex("(?i)(?:cho khách|khách hàng|khách|cho anh|cho chị|cho bác|cho cô|cho chú)\\s+([\\p{L}\\s]{2,20}?)(?=\\s+(?:với|ktv|làm|dịch vụ|trị liệu|gói|giờ|$)|$)")
        val extractedKhName = khRegex.find(rawPrompt)?.groupValues?.get(1)?.trim()
        val tenKhDuKien = previewKh?.ten ?: extractedKhName?.takeIf { it.isNotBlank() } ?: khs.firstOrNull()?.ten ?: "Khách vãng lai"
        val tenNvDuKien = previewNv?.ten ?: "KTV Mặc định"

        val previewDv = timDichVuTheoCauLenh(norm) ?: dvs.firstOrNull { it.phut == 60 } ?: dvs.firstOrNull()
        val tenDvDuKien = previewDv?.ten ?: "Trị liệu 60 phút"
        val giaDuKien = previewDv?.gia ?: 160_000L

        return taoYeuCauXacNhanGehihi(
            loai = LoaiLenhChoXacNhan.THEM,
            tieuDeTomTat = "Thêm vé mới: $tenKhDuKien - $tenNvDuKien ($tenDvDuKien)",
            chiTietXacNhan = "Bạn đang yêu cầu mở vé mới trên Trang chủ:\n• Khách hàng: $tenKhDuKien\n• KTV phụ trách: $tenNvDuKien\n• Dịch vụ: $tenDvDuKien (${formatVnd(giaDuKien)})"
        ) {
            thucThiTaoVeTuCauLenh(rawPrompt, norm)
        }
    }

    private fun thucThiTaoVeTuCauLenh(rawPrompt: String, norm: String): KetQuaAiBot {
        val matchedNv = nvs.firstOrNull { nv ->
            val fullNorm = normalizeVietnameseText(nv.ten)
            val shortName = fullNorm.substringAfterLast(" ")
            norm.contains(fullNorm) || (shortName.length >= 2 && Regex("\\b$shortName\\b").containsMatchIn(norm))
        } ?: nvs.firstOrNull() ?: themNhanVien("KTV Mặc định", "Nam", "0900000001", "")!!

        var matchedKh = khs.firstOrNull { kh ->
            val fullNorm = normalizeVietnameseText(kh.ten)
            val shortName = fullNorm.substringAfterLast(" ")
            norm.contains(fullNorm) || (shortName.length >= 2 && Regex("\\b$shortName\\b").containsMatchIn(norm) && shortName != normalizeVietnameseText(matchedNv.ten).substringAfterLast(" "))
        }

        if (matchedKh == null) {
            val khRegex = Regex("(?i)(?:cho khách|khách hàng|khách|cho anh|cho chị|cho bác|cho cô|cho chú)\\s+([\\p{L}\\s]{2,20}?)(?=\\s+(?:với|ktv|làm|dịch vụ|trị liệu|gói|giờ|$)|$)")
            val extractedKhName = khRegex.find(rawPrompt)?.groupValues?.get(1)?.trim()
            matchedKh = if (!extractedKhName.isNullOrBlank()) {
                val created = KH(id = "kh_${shortId()}", ten = extractedKhName, sdt = "Đang cập nhật")
                khs.add(0, created)
                created
            } else {
                khs.firstOrNull() ?: KH(id = "kh_${shortId()}", ten = "Khách vãng lai", sdt = "Tại quầy").also { khs.add(0, it) }
            }
        }

        val matchedDv = timDichVuTheoCauLenh(norm) ?: dvs.firstOrNull { it.phut == 60 } ?: dvs.firstOrNull()
            ?: DV("379695801677810", "Trị liệu 60 phút", 60, 160_000L, "", 70_000L).also { dvs.add(0, it) }

        val dungGoi = norm.contains("tru goi") || norm.contains("dung goi") || norm.contains("goi lieu trinh")
        val dkId = if (dungGoi) {
            dks.firstOrNull { it.khId == matchedKh.id && !it.daDung }?.id ?: ""
        } else ""

        val veMoi = moVeMoi(
            khId = matchedKh.id,
            nvId = matchedNv.id,
            selectedDvIds = listOf(matchedDv.id),
            dkId = dkId,
            tgbd = nowTimeStr()
        )

        return if (veMoi != null) {
            KetQuaAiBot(
                cauTraLoi = "gehihi đã tạo vé mới thành công trên Trang chủ (mục Đang làm): Khách ${matchedKh.ten}, KTV ${matchedNv.ten}, dịch vụ ${matchedDv.ten}, bắt đầu lúc ${veMoi.tgbd}, thu ${if (veMoi.dungGoi) "0 đồng (trừ gói LT)" else formatVndSpeakable(veMoi.thu)}.",
                hanhDongThucThi = "Đã thêm vé #${veMoi.id} vào mục Đang làm (${matchedKh.ten} - ${matchedNv.ten})",
                chuyenTab = 0,
                chuyenMucVe = 0
            )
        } else {
            KetQuaAiBot(
                cauTraLoi = "Không thể tạo vé mới. Vui lòng kiểm tra lại danh sách dịch vụ.",
                chuyenTab = 0,
                chuyenMucVe = 0
            )
        }
    }

    private fun buildGehihiPrompt(
        userQuestion: String,
        attachedFileName: String = "",
        attachedFileContent: String = ""
    ): String {
        val today = todayDateStr()
        val veDangLam = luots.filter { it.dangLam }
        val veChoChot = luots.filter { it.choChot }
        val veDaChotHomNay = luots.filter { it.daHoanTat && it.ngay == today }

        val thuVeHomNay = veDaChotHomNay.sumOf { it.thu }
        val hoaHongHomNay = veDaChotHomNay.sumOf { tinhHoaHongVe(it) }
        val thuNgoaiHomNay = tcs.filter { it.loai == LoaiTC.THU && !laPhieuThuTuVe(it) && it.ngay == today }.sumOf { it.tien }
        val chiNgoaiHomNay = tcs.filter { it.loai == LoaiTC.CHI && !laPhieuChiHoaHongKtv(it) && it.ngay == today }.sumOf { it.tien }
        val chuNhanHomNay = (thuVeHomNay + thuNgoaiHomNay) - (hoaHongHomNay + chiNgoaiHomNay)

        val bangLuongThang = layBangLuongChiTiet(BoLocThoiGian.THANG, today)
        val tongThuVeThang = luots.filter { it.daXacNhan && matchesTimeFilter(it.ngay, BoLocThoiGian.THANG, today) }.sumOf { it.thu }
        val thuNgoaiThang = tcs.filter { it.loai == LoaiTC.THU && !laPhieuThuTuVe(it) && matchesTimeFilter(it.ngay, BoLocThoiGian.THANG, today) }.sumOf { it.tien }
        val tongHoaHongThang = bangLuongThang.sumOf { it.tongHoaHong }
        val chiNgoaiThang = tcs.filter { it.loai == LoaiTC.CHI && !laPhieuChiHoaHongKtv(it) && matchesTimeFilter(it.ngay, BoLocThoiGian.THANG, today) }.sumOf { it.tien }
        val tongThuThang = tongThuVeThang + thuNgoaiThang
        val tongChiThang = tongHoaHongThang + chiNgoaiThang
        val chuNhanThang = tongThuThang - tongChiThang
        val tyLeHoaHong = if (tongThuThang > 0) (tongHoaHongThang * 100 / tongThuThang) else 0

        val topDvList = luots.filter { it.daXacNhan && matchesTimeFilter(it.ngay, BoLocThoiGian.THANG, today) }
            .flatMap { it.dvIds }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .mapNotNull { entry -> dvs.firstOrNull { it.id == entry.key }?.let { "${it.ten} (${entry.value} lượt)" } }

        val topKtv = bangLuongThang.maxByOrNull { it.tongSoLuot }
        val soGoiDangDung = dks.count { !it.daDung }

        val recentHistory = aiMessages.takeLast(60)
        val longTermNotes = aiLongTermMemories.takeLast(150)

        return buildString {
            appendLine("Bạn là 'gehihi' — CỐ VẤN KINH DOANH VÀ TRỢ LÝ ĐIỀU HÀNH FULL QUYỀN 100% CỦA CHỦ SPA KHIẾM THỊ VIỆT.")
            appendLine("Trình bày sạch sẽ, lịch sự, chuyên nghiệp, KHÔNG dùng ký tự sao ** markdown.")
            appendLine()
            appendLine("1. NĂNG LỰC TƯ VẤN KINH DOANH & PHÂN TÍCH TÀI CHÍNH:")
            appendLine("   Khi Admin hỏi về tình hình quán, doanh thu, chi phí, lợi nhuận, hiệu quả nhân viên hoặc chiến lược phát triển:")
            appendLine("   - Phân tích sâu sắc dựa trên số liệu thực tế thời gian thực của cơ sở (doanh thu hôm nay, doanh thu tháng, lợi nhuận chủ nhận, tỷ lệ hoa hồng, chi phí ngoài lề).")
            appendLine("   - Chỉ ra điểm mạnh (dịch vụ đông khách, thợ làm tốt) và điểm nghẽn (chi phí ngoài lề phát sinh, thợ ít khách, tỷ lệ khách mua gói).")
            appendLine("   - Đưa ra 2-3 giải pháp hành động cụ thể, khả thi để chủ quán tối ưu lợi nhuận và nâng cao chất lượng dịch vụ.")
            appendLine()
            appendLine("2. NĂNG LỰC HIỂU KHẨU LỆNH TỰ NHIÊN (KHÔNG CẦN ĐÚNG CÚ PHÁP):")
            appendLine("   Khi Admin ra lệnh quản lý (viết tắt, tiếng lóng, ghép câu, nói nhanh: 'thêm nv...', 'mở vé...', 'chốt ca...', 'chi 150k mua...', 'sửa giá...'), hãy tự động bóc tách đúng ý định và gắn ĐÚNG 1 THẺ LỆNH duy nhất ở cuối câu trả lời:")
            appendLine("   - Thêm nhân viên: [ACTION:ADD_STAFF|ten=HọTên|sdt=SĐT|gt=Nam/Nữ|mk=123456|email=Gmail|admin=false]")
            appendLine("   - Mở vé mới: [ACTION:OPEN_TICKET|kh=TênKhách|nv=TênKTV|dv=TênDịchVụ|dunggoi=true/false]")
            appendLine("   - Chốt vé: [ACTION:CONFIRM_TICKETS|scope=ALL/DANG_LAM/CHO_CHOT/TênKTV]")
            appendLine("   - Hoàn thành vé: [ACTION:COMPLETE_TICKET]")
            appendLine("   - Thêm chi tiêu Sổ quỹ: [ACTION:ADD_FINANCE_CHI|tien=SốTiền|lydo=LýDo]")
            appendLine("   - Thêm thu ngoài Sổ quỹ: [ACTION:ADD_FINANCE_THU|tien=SốTiền|lydo=LýDo]")
            appendLine("   - Thêm khách hàng: [ACTION:ADD_CUSTOMER|ten=TênKhách|sdt=SĐT]")
            appendLine("   - Sửa dịch vụ: [ACTION:EDIT_SERVICE|ten=TênDV|gia=GiáMới|hoahong=HoaHồngMới|phut=SốPhút]")
            appendLine("   - Sửa nhân viên: [ACTION:EDIT_STAFF|target=TênKTV|ten=TênMới|sdt=SĐTMới|gt=Nam/Nữ|mk=MKMới|email=GmailMới]")
            appendLine("   - Xóa nhân viên: [ACTION:DELETE_STAFF|target=TênKTV] (TUYỆT ĐỐI chỉ tạo khi Admin yêu cầu đích danh tên nhân viên muốn xóa)")
            appendLine("   - Xóa vé: [ACTION:DELETE_TICKETS|scope=DANG_LAM/CHO_CHOT/DA_CHOT/TAT_CA]")
            appendLine("   - Chuyển màn hình: [ACTION:NAVIGATE_TAB|tab=0..5]")
            appendLine("   - Lưu ghi nhớ dài hạn: [ACTION:SAVE_MEMORY|note=NộiDung]")
            appendLine()
            appendLine("3. NGUYÊN TẮC PHÂN BIỆT Ý ĐỊNH & BÓC TÁCH THÔNG TIN CỦA GEMINI (CỰC KỲ QUAN TRỌNG):")
            appendLine("   - Khi Admin yêu cầu THÊM NHÂN VIÊN MỚI (Ví dụ: 'thêm nhân viên Mai Văn Hoài, giới tính nam, số điện thoại 0968608795, mail chưa cập nhật'):")
            appendLine("     + Xác định chính xác đây là lệnh THÊM (ADD_STAFF). TUYỆT ĐỐI KHÔNG nhận nhầm sang lệnh sửa hay xóa người khác.")
            appendLine("     + Bóc tách trường chuẩn xác: Họ tên = Mai Văn Hoài; SĐT = 0968608795; Giới tính = Nam (chữ 'nam' ở đây là giới tính nam, không phải tên thợ khác!); Email = để rỗng khi ghi 'chưa cập nhật' hoặc 'chưa có'.")
            appendLine("     + Gắn thẻ lệnh duy nhất: [ACTION:ADD_STAFF|ten=Mai Văn Hoài|sdt=0968608795|gt=Nam|mk=123456|email=].")
            appendLine("   - Khi Admin ra lệnh SỬA hoặc XÓA: CHỈ khi người dùng nêu đích danh ai đó cần sửa/xóa. Tuyệt đối không tự sửa ai khác khi người dùng đang muốn thêm mới.")
            appendLine("   - Khi Admin hỏi han, tra cứu doanh thu, tâm sự, hỏi tư vấn chiến lược kinh doanh spa, tính toán hoặc hỏi lịch ngày giờ:")
            appendLine("     + Trả lời tự nhiên, thông minh, sâu sắc, giải thích tận tình và TUYỆT ĐỐI KHÔNG gắn bất kỳ thẻ [ACTION:...] nào.")
            appendLine()
            appendLine("BỨC TRANH TÀI CHÍNH & VẬN HÀNH THỜI GIAN THỰC (Ca ngày $today, từ 06:00 sáng hôm nay đến 06:00 sáng mai):")
            appendLine("• HÔM NAY: ${veDaChotHomNay.size} vé đã chốt, ${veDangLam.size} vé đang làm, ${veChoChot.size} vé chờ chốt.")
            appendLine("  - Thu từ vé hôm nay: ${formatVnd(thuVeHomNay)} (Hoa hồng trích KTV: -${formatVnd(hoaHongHomNay)}).")
            appendLine("  - Thu ngoài lề hôm nay: +${formatVnd(thuNgoaiHomNay)} | Chi ngoài lề: -${formatVnd(chiNgoaiHomNay)}.")
            appendLine("  - CHỦ QUÁN THỰC NHẬN HÔM NAY: ${formatVnd(chuNhanHomNay)}.")
            appendLine("• THÁNG NÀY: Tổng thu gộp ${formatVnd(tongThuThang)} (Vé: ${formatVnd(tongThuVeThang)}, Thu ngoài: ${formatVnd(thuNgoaiThang)}).")
            appendLine("  - Tổng chi phí tháng: -${formatVnd(tongChiThang)} (Hoa hồng KTV: -${formatVnd(tongHoaHongThang)} [chiếm $tyLeHoaHong%], Chi ngoài: -${formatVnd(chiNgoaiThang)}).")
            appendLine("  - CHỦ QUÁN THỰC NHẬN THÁNG NÀY (LÃI RÒNG): ${formatVnd(chuNhanThang)}.")
            appendLine("• NHÂN SỰ & HIỆU SUẤT (${nvs.size} KTV): ${nvs.joinToString { "${it.ten} (${it.gt}, SĐT: ${it.sdt})" }}.")
            if (topKtv != null) appendLine("  - KTV tích cực nhất tháng: ${topKtv.nv.ten} (${topKtv.tongSoLuot} lượt, hoa hồng ${formatVnd(topKtv.tongHoaHong)}).")
            appendLine("• DỊCH VỤ (${dvs.size} dịch vụ): ${dvs.joinToString { "${it.ten} (${it.gia}đ, HH: ${it.chietKhauVnd}đ)" }}.")
            if (topDvList.isNotEmpty()) appendLine("  - Top dịch vụ bán chạy nhất: ${topDvList.joinToString()}.")
            appendLine("• GÓI LIỆU TRÌNH: ${gois.size} gói cấu hình, hiện có $soGoiDangDung gói của khách đang còn buổi.")
            appendLine("• KHÁCH HÀNG: ${khs.size} khách trong danh bạ.")
            appendLine()
            if (attachedFileContent.isNotBlank()) {
                appendLine("TẬP TIN ĐÍNH KÈM CỦA ADMIN ($attachedFileName):")
                appendLine(attachedFileContent.take(8000))
                appendLine()
            }
            if (longTermNotes.isNotEmpty()) {
                appendLine("BỘ NHỚ VĨNH VIỄN CỦA GEHIHI (${longTermNotes.size} sự kiện/lời dặn của Admin):")
                longTermNotes.forEach { note ->
                    appendLine("• $note")
                }
                appendLine()
            }
            if (recentHistory.isNotEmpty()) {
                appendLine("LỊCH SỬ HỘI THOẠI GẦN ĐÂY (${recentHistory.size} tin nhắn):")
                recentHistory.forEach { m ->
                    val role = if (m.isUser) "Admin" else "gehihi"
                    val timePrefix = if (m.thoiGian.isNotBlank()) "[${m.thoiGian}] " else ""
                    appendLine("- $timePrefix$role: ${m.text.take(240)}")
                }
                appendLine()
            }
            appendLine("YÊU CẦU HOẶC KHẨU LỆNH CỦA ADMIN: \"$userQuestion\"")
        }
    }

    private fun phanTichPhanHoiGeminiVaThucThi(rawText: String, originalPrompt: String): KetQuaAiBot {
        val actionRegex = Regex("\\[ACTION:([^\\]]+)\\]")
        val match = actionRegex.find(rawText)
        val cleanReply = rawText.replace(actionRegex, "").replace("**", "").trim()

        if (match != null) {
            val parts = match.groupValues[1].split("|")
            val actionName = parts.firstOrNull()?.trim().orEmpty()
            val params = parts.drop(1).associate {
                val kv = it.split("=", limit = 2)
                kv[0].trim().lowercase() to kv.getOrElse(1) { "" }.trim()
            }
            when (actionName) {
                "DELETE_TICKETS" -> {
                    val scope = params["scope"]?.uppercase().orEmpty().ifBlank { "DANG_LAM" }
                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.XOA,
                        tieuDeTomTat = "Xóa vé theo phạm vi: $scope",
                        chiTietXacNhan = "Bạn đang yêu cầu gehihi xóa vé (Phạm vi: $scope)."
                    ) {
                        val count = if (scope == "ONE") {
                            val first = luots.firstOrNull()
                            if (first != null && xoaVe(first.id)) 1 else 0
                        } else {
                            xoaHangLoatVeAdmin(scope)
                        }
                        KetQuaAiBot(
                            cauTraLoi = cleanReply.ifBlank { "gehihi đã xóa $count vé theo yêu cầu." },
                            hanhDongThucThi = "Đã xóa $count vé ($scope)",
                            chuyenTab = 0
                        )
                    }
                }
                "DELETE_STAFF" -> {
                    val target = params["target"].orEmpty()
                    val synthetic = if (target.equals("ALL", ignoreCase = true)) {
                        "xóa tất cả nhân viên"
                    } else if (target.equals("RANDOM", ignoreCase = true) || target.isBlank()) {
                        "xóa ngẫu nhiên 1 nhân viên"
                    } else {
                        "xóa nhân viên $target"
                    }
                    thucThiLenhTrucTiepTrenApp(synthetic)?.let { return it }
                }
                "DELETE_SERVICE" -> {
                    val target = params["target"].orEmpty()
                    val synthetic = if (target.equals("ALL", ignoreCase = true)) "xóa tất cả dịch vụ" else "xóa dịch vụ $target"
                    thucThiLenhTrucTiepTrenApp(synthetic)?.let { return it }
                }
                "DELETE_PACKAGE" -> {
                    val target = params["target"].orEmpty()
                    val synthetic = if (target.equals("ALL", ignoreCase = true)) "xóa tất cả gói" else "xóa gói $target"
                    thucThiLenhTrucTiepTrenApp(synthetic)?.let { return it }
                }
                "DELETE_CUSTOMER" -> {
                    val target = params["target"].orEmpty()
                    val synthetic = if (target.equals("ALL", ignoreCase = true)) "xóa tất cả khách hàng" else "xóa khách hàng $target"
                    thucThiLenhTrucTiepTrenApp(synthetic)?.let { return it }
                }
                "WIPE_ALL_DATA" -> {
                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.XOA,
                        tieuDeTomTat = "XÓA SẠCH TẤT CẢ DỮ LIỆU ỨNG DỤNG (Vé, Khách, KTV, Dịch vụ, Gói, Sổ quỹ)",
                        chiTietXacNhan = "⚠️ CẢNH BÁO QUAN TRỌNG:\nBạn đang yêu cầu gehihi xóa sạch toàn bộ dữ liệu trên cả máy và Cloud (Vé: ${luots.size}, Khách: ${khs.size}, KTV: ${nvs.size}, Dịch vụ: ${dvs.size}, Gói: ${gois.size}, Sổ quỹ: ${tcs.size}), chỉ giữ lại tài khoản Admin đang đăng nhập."
                    ) {
                        val res = xoaTatCaDuLieuUngDungAdmin(matKhauNhap = "", boQuaKiemTraMatKhau = true)
                        KetQuaAiBot(
                            cauTraLoi = res.getOrElse { "Không thể xóa toàn bộ dữ liệu: ${it.message}" },
                            hanhDongThucThi = "Đã xóa sạch toàn bộ dữ liệu ứng dụng",
                            chuyenTab = 5,
                            chuyenMucAdmin = 2
                        )
                    }
                }
                "OPEN_TICKET" -> {
                    val khName = params["kh"].orEmpty().trim().ifBlank { "Khách vãng lai" }
                    val nvName = params["nv"].orEmpty().trim()
                    val dvName = params["dv"].orEmpty().trim()
                    val dungGoi = params["dunggoi"]?.equals("true", ignoreCase = true) == true

                    val matchedNv = (if (nvName.isNotBlank()) {
                        val normNv = normalizeVietnameseText(nvName)
                        nvs.firstOrNull { normalizeVietnameseText(it.ten).contains(normNv) }
                    } else null) ?: nvs.firstOrNull() ?: themNhanVien("KTV Mặc định", "Nam", "0900000001", "")!!

                    var matchedKh = if (khName.isNotBlank() && khName != "Khách vãng lai") {
                        val normKh = normalizeVietnameseText(khName)
                        khs.firstOrNull { normalizeVietnameseText(it.ten).contains(normKh) }
                    } else null

                    if (matchedKh == null && khName.isNotBlank() && khName != "Khách vãng lai") {
                        val created = KH(id = "kh_${shortId()}", ten = khName, sdt = "Đang cập nhật")
                        khs.add(0, created)
                        matchedKh = created
                    }
                    if (matchedKh == null) {
                        matchedKh = khs.firstOrNull() ?: KH(id = "kh_${shortId()}", ten = "Khách vãng lai", sdt = "Tại quầy").also { khs.add(0, it) }
                    }

                    val matchedDv: DV = (if (dvName.isNotBlank()) {
                        timDichVuTheoCauLenh(normalizeVietnameseText(dvName))
                    } else null) ?: dvs.firstOrNull { it.phut == 60 } ?: dvs.firstOrNull()
                        ?: DV("379695801677810", "Trị liệu 60 phút", 60, 160_000L, "", 70_000L).also { dvs.add(0, it) }

                    val dkId = if (dungGoi) {
                        dks.firstOrNull { it.khId == matchedKh.id && !it.daDung }?.id ?: ""
                    } else ""
                    val giaDuKien = if (dungGoi) 0L else matchedDv.gia

                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.THEM,
                        tieuDeTomTat = "Mở vé: ${matchedKh.ten} - ${matchedNv.ten} (${matchedDv.ten})",
                        chiTietXacNhan = "Bạn đang yêu cầu mở vé mới trên Trang chủ:\n• Khách hàng: ${matchedKh.ten}\n• KTV phụ trách: ${matchedNv.ten}\n• Dịch vụ: ${matchedDv.ten} (${if (dungGoi) "Trừ gói liệu trình" else formatVnd(giaDuKien)})"
                    ) {
                        val veMoi = moVeMoi(
                            khId = matchedKh.id,
                            nvId = matchedNv.id,
                            selectedDvIds = listOf(matchedDv.id),
                            dkId = dkId,
                            tgbd = nowTimeStr()
                        )
                        KetQuaAiBot(
                            cauTraLoi = cleanReply.ifBlank { "gehihi đã tạo vé mới thành công trên Trang chủ (mục Đang làm): Khách ${matchedKh.ten}, KTV ${matchedNv.ten}, dịch vụ ${matchedDv.ten}." },
                            hanhDongThucThi = "Đã thêm vé #${veMoi?.id ?: ""} vào mục Đang làm",
                            chuyenTab = 0,
                            chuyenMucVe = 0
                        )
                    }
                }
                "COMPLETE_TICKET" -> {
                    val dangLam = luots.filter { it.dangLam }
                    if (dangLam.isEmpty()) {
                        return KetQuaAiBot("Hiện không có vé nào ở mục Đang làm để hoàn thành.", chuyenTab = 0, chuyenMucVe = 0)
                    }
                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.THEM,
                        tieuDeTomTat = "Hoàn thành ${dangLam.size} vé đang làm",
                        chiTietXacNhan = "Bạn đang yêu cầu hoàn thành ${dangLam.size} vé đang làm và chuyển sang mục Chờ chốt."
                    ) {
                        dangLam.forEach { hoanThanhVe(it.id, nowTimeStr(), it.thu) }
                        ghiNhoSuKienDaiHan("Hoàn thành ${dangLam.size} vé đang làm", luuNgay = false)
                        KetQuaAiBot(
                            cauTraLoi = cleanReply.ifBlank { "gehihi đã hoàn thành ${dangLam.size} vé đang làm, chuyển sang mục Chờ chốt." },
                            hanhDongThucThi = "Đã hoàn thành ${dangLam.size} vé",
                            chuyenTab = 0,
                            chuyenMucVe = 1
                        )
                    }
                }
                "CONFIRM_TICKETS", "CONFIRM_TICKET" -> {
                    val scope = params["scope"]?.uppercase().orEmpty()
                    val target = params["target"].orEmpty()
                    val dsCanChot = when {
                        scope == "DANG_LAM" -> luots.filter { it.dangLam }
                        scope == "CHO_CHOT" -> luots.filter { it.choChot }
                        target.isNotBlank() -> {
                            val normT = normalizeVietnameseText(target)
                            luots.filter { ve ->
                                if (!ve.dangLam && !ve.choChot) false
                                else {
                                    val nvName = nvs.firstOrNull { it.id == ve.nvId }?.ten.orEmpty()
                                    val khName = khs.firstOrNull { it.id == ve.khId }?.ten.orEmpty()
                                    normalizeVietnameseText(nvName).contains(normT) || normalizeVietnameseText(khName).contains(normT)
                                }
                            }
                        }
                        else -> {
                            val choChot = luots.filter { it.choChot }
                            if (choChot.isNotEmpty()) choChot else luots.filter { it.dangLam }
                        }
                    }
                    if (dsCanChot.isEmpty()) {
                        return KetQuaAiBot("Hiện tại không có vé nào đang làm hoặc chờ chốt để chốt.", chuyenTab = 0)
                    }
                    val tongHh = dsCanChot.sumOf { tinhHoaHongVe(it) }
                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.THEM,
                        tieuDeTomTat = "Chốt ${dsCanChot.size} vé (Hoa hồng: ${formatVnd(tongHh)})",
                        chiTietXacNhan = "Bạn đang yêu cầu chốt ${dsCanChot.size} vé và tự động trích ${formatVnd(tongHh)} hoa hồng KTV vào Sổ quỹ."
                    ) {
                        dsCanChot.forEach { ve ->
                            if (ve.dangLam) hoanThanhVe(ve.id, nowTimeStr(), ve.thu)
                            xacNhanChotVe(ve.id)
                        }
                        KetQuaAiBot(
                            cauTraLoi = cleanReply.ifBlank { "gehihi đã chốt thành công ${dsCanChot.size} vé, trích ${formatVndSpeakable(tongHh)} hoa hồng KTV." },
                            hanhDongThucThi = "Đã chốt ${dsCanChot.size} vé",
                            chuyenTab = 0,
                            chuyenMucVe = 2
                        )
                    }
                }
                "EDIT_TICKET" -> {
                    val khHint = normalizeVietnameseText(params["kh"].orEmpty() + " " + originalPrompt)
                    val targetVe = timVeTheoTenTrongCau(luots, khHint) ?: luots.firstOrNull()
                    if (targetVe != null) {
                        val khTen = khs.firstOrNull { it.id == targetVe.khId }?.ten ?: "Khách"
                        val thuMoi = params["thu"]?.filter { it.isDigit() }?.toLongOrNull() ?: targetVe.thu
                        val hhMoi = params["hoahong"]?.filter { it.isDigit() }?.toLongOrNull() ?: tinhHoaHongVe(targetVe)
                        val nvHint = normalizeVietnameseText(params["nv"].orEmpty())
                        val nvMoi = if (nvHint.isNotBlank()) {
                            nvs.firstOrNull { normalizeVietnameseText(it.ten).contains(nvHint) }
                        } else null
                        return taoYeuCauXacNhanGehihi(
                            loai = LoaiLenhChoXacNhan.SUA,
                            tieuDeTomTat = "Sửa vé khách $khTen: Thu ${formatVnd(thuMoi)}, Hoa hồng ${formatVnd(hhMoi)}",
                            chiTietXacNhan = "Bạn đang yêu cầu sửa vé #${targetVe.id} của khách $khTen:\n• Giá thu: ${formatVnd(targetVe.thu)} ➔ ${formatVnd(thuMoi)}\n• Hoa hồng KTV: ${formatVnd(tinhHoaHongVe(targetVe))} ➔ ${formatVnd(hhMoi)}"
                        ) {
                            suaToanBoVeAdmin(
                                veId = targetVe.id,
                                khIdMoi = targetVe.khId,
                                nvIdMoi = nvMoi?.id ?: targetVe.nvId,
                                dvIdsMoi = targetVe.dvIds,
                                dkIdMoi = targetVe.dkId,
                                dvNgoaiMoi = targetVe.dvNgoai,
                                tgbdMoi = targetVe.tgbd,
                                tgktMoi = targetVe.tgkt,
                                ngayMoi = targetVe.ngay,
                                thuMoi = thuMoi,
                                hoaHongMoi = hhMoi
                            )
                            KetQuaAiBot(
                                cauTraLoi = cleanReply.ifBlank { "gehihi đã cập nhật vé của khách $khTen: Thu ${formatVndSpeakable(thuMoi)}, hoa hồng ${formatVndSpeakable(hhMoi)}." },
                                hanhDongThucThi = "Đã sửa vé khách $khTen",
                                chuyenTab = 0
                            )
                        }
                    }
                }
                "EDIT_SERVICE" -> {
                    val tenHint = normalizeVietnameseText(params["ten"].orEmpty() + " " + originalPrompt)
                    val targetDv = timDichVuTheoCauLenh(tenHint) ?: dvs.firstOrNull()
                    if (targetDv != null) {
                        val giaMoi = params["gia"]?.filter { it.isDigit() }?.toLongOrNull() ?: targetDv.gia
                        val hhMoi = params["hoahong"]?.filter { it.isDigit() }?.toLongOrNull() ?: targetDv.chietKhauVnd
                        val phutMoi = params["phut"]?.filter { it.isDigit() }?.toIntOrNull() ?: targetDv.phut
                        return taoYeuCauXacNhanGehihi(
                            loai = LoaiLenhChoXacNhan.SUA,
                            tieuDeTomTat = "Sửa dịch vụ ${targetDv.ten}: Giá ${formatVnd(giaMoi)}, CK ${formatVnd(hhMoi)}",
                            chiTietXacNhan = "Bạn đang yêu cầu sửa dịch vụ \"${targetDv.ten}\" (áp dụng từ thời điểm hiện tại):\n• Giá: ${formatVnd(targetDv.gia)} ➔ ${formatVnd(giaMoi)}\n• Hoa hồng KTV: ${formatVnd(targetDv.chietKhauVnd)} ➔ ${formatVnd(hhMoi)}"
                        ) {
                            luuDichVu(targetDv.id, targetDv.ten, phutMoi, giaMoi, targetDv.moTa, hhMoi)
                            KetQuaAiBot(
                                cauTraLoi = cleanReply.ifBlank { "gehihi đã sửa dịch vụ ${targetDv.ten}: Giá ${formatVndSpeakable(giaMoi)}, hoa hồng ${formatVndSpeakable(hhMoi)}." },
                                hanhDongThucThi = "Đã sửa DV ${targetDv.ten}",
                                chuyenTab = 2
                            )
                        }
                    }
                }
                "EDIT_PACKAGE" -> {
                    val tenHint = normalizeVietnameseText(params["ten"].orEmpty() + " " + originalPrompt)
                    val targetGoi = gois.firstOrNull { tenHint.contains(normalizeVietnameseText(it.ten)) } ?: gois.firstOrNull()
                    if (targetGoi != null) {
                        val giaMoi = params["gia"]?.filter { it.isDigit() }?.toLongOrNull() ?: targetGoi.gia
                        val soBuoiMoi = params["sobuoi"]?.filter { it.isDigit() }?.toIntOrNull() ?: targetGoi.soBuoi
                        return taoYeuCauXacNhanGehihi(
                            loai = LoaiLenhChoXacNhan.SUA,
                            tieuDeTomTat = "Sửa gói ${targetGoi.ten}: $soBuoiMoi buổi - ${formatVnd(giaMoi)}",
                            chiTietXacNhan = "Bạn đang yêu cầu sửa gói \"${targetGoi.ten}\":\n• Số buổi: ${targetGoi.soBuoi} ➔ $soBuoiMoi buổi\n• Giá gói: ${formatVnd(targetGoi.gia)} ➔ ${formatVnd(giaMoi)}"
                        ) {
                            luuGoiLieuTrinh(targetGoi.id, targetGoi.ten, targetGoi.dvIds, soBuoiMoi, giaMoi, targetGoi.chiTietLuot, targetGoi.dvTangKem)
                            KetQuaAiBot(
                                cauTraLoi = cleanReply.ifBlank { "gehihi đã cập nhật gói ${targetGoi.ten}." },
                                hanhDongThucThi = "Đã sửa gói ${targetGoi.ten}",
                                chuyenTab = 2
                            )
                        }
                    }
                }
                "EDIT_CUSTOMER" -> {
                    val tenHint = normalizeVietnameseText(params["ten"].orEmpty() + " " + originalPrompt)
                    val targetKh = khs.firstOrNull { tenHint.contains(normalizeVietnameseText(it.ten.substringAfterLast(" "))) } ?: khs.firstOrNull()
                    if (targetKh != null) {
                        val tenMoi = params["tenmoi"]?.takeIf { it.isNotBlank() } ?: targetKh.ten
                        val sdtMoi = params["sdt"]?.takeIf { it.isNotBlank() } ?: targetKh.sdt
                        return taoYeuCauXacNhanGehihi(
                            loai = LoaiLenhChoXacNhan.SUA,
                            tieuDeTomTat = "Sửa khách hàng ${targetKh.ten} ➔ $tenMoi ($sdtMoi)",
                            chiTietXacNhan = "Bạn đang yêu cầu sửa khách hàng ${targetKh.ten} thành \"$tenMoi\" (SĐT: $sdtMoi)."
                        ) {
                            suaKhachHang(targetKh.id, tenMoi, sdtMoi)
                            KetQuaAiBot(
                                cauTraLoi = cleanReply.ifBlank { "gehihi đã cập nhật thông tin khách hàng $tenMoi." },
                                hanhDongThucThi = "Đã sửa KH $tenMoi",
                                chuyenTab = 1,
                                chuyenMucNguoi = 0
                            )
                        }
                    }
                }
                "EDIT_CUSTOMER_PACKAGE" -> {
                    val khHint = normalizeVietnameseText(params["kh"].orEmpty() + " " + originalPrompt)
                    val targetKh = khs.firstOrNull { khHint.contains(normalizeVietnameseText(it.ten.substringAfterLast(" "))) }
                    val dk = if (targetKh != null) dks.firstOrNull { it.khId == targetKh.id } else dks.firstOrNull()
                    val daDungMoi = params["dadung"]?.filter { it.isDigit() }?.toIntOrNull()
                    if (dk != null && daDungMoi != null) {
                        val khTen = targetKh?.ten ?: "Khách"
                        return taoYeuCauXacNhanGehihi(
                            loai = LoaiLenhChoXacNhan.SUA,
                            tieuDeTomTat = "Sửa số buổi gói của $khTen ➔ Đã dùng $daDungMoi buổi",
                            chiTietXacNhan = "Bạn đang yêu cầu cập nhật tiến độ gói của khách $khTen: Đã dùng ${dk.luotDaDung} ➔ $daDungMoi buổi."
                        ) {
                            suaDangKyGoiKhach(dk.id, daDungMoi)
                            KetQuaAiBot(
                                cauTraLoi = cleanReply.ifBlank { "gehihi đã cập nhật số buổi đã dùng của khách $khTen thành $daDungMoi buổi." },
                                hanhDongThucThi = "Đã sửa tiến độ gói KH $khTen",
                                chuyenTab = 1,
                                chuyenMucNguoi = 0
                            )
                        }
                    }
                }
                "EDIT_FINANCE" -> {
                    val targetTc = tcs.firstOrNull()
                    val tienMoi = params["tien"]?.filter { it.isDigit() }?.toLongOrNull() ?: targetTc?.tien ?: 0L
                    if (targetTc != null && tienMoi > 0L) {
                        val loaiMoi = if (params["loai"]?.uppercase() == "THU") LoaiTC.THU else if (params["loai"]?.uppercase() == "CHI") LoaiTC.CHI else targetTc.loai
                        val lyDoMoi = params["lydo"]?.takeIf { it.isNotBlank() } ?: targetTc.lyDo
                        return taoYeuCauXacNhanGehihi(
                            loai = LoaiLenhChoXacNhan.SUA,
                            tieuDeTomTat = "Sửa phiếu sổ quỹ: $lyDoMoi (${formatVnd(tienMoi)})",
                            chiTietXacNhan = "Bạn đang yêu cầu sửa giao dịch Sổ quỹ \"${targetTc.lyDo}\": ${formatVnd(targetTc.tien)} ➔ ${formatVnd(tienMoi)}."
                        ) {
                            suaGiaoDichSoQuy(targetTc.id, loaiMoi, tienMoi, lyDoMoi, targetTc.ngay)
                            KetQuaAiBot(
                                cauTraLoi = cleanReply.ifBlank { "gehihi đã sửa giao dịch Sổ quỹ thành ${formatVndSpeakable(tienMoi)}." },
                                hanhDongThucThi = "Đã sửa Sổ quỹ: ${formatVnd(tienMoi)}",
                                chuyenTab = 3,
                                tuDongDongChat = true
                            )
                        }
                    }
                }
                "EDIT_ACCOUNT" -> {
                    val tkHint = normalizeVietnameseText(params["tk"].orEmpty() + " " + originalPrompt)
                    val targetTk = if (tkHint.contains("admin")) {
                        taiKhoans.firstOrNull { it.id == "tk_admin" } ?: currentUser
                    } else {
                        taiKhoans.firstOrNull { tk ->
                            tk.id != "tk_admin" && tkHint.contains(normalizeVietnameseText(tk.hoTen.substringAfterLast(" ")))
                        } ?: taiKhoans.firstOrNull { it.id == "tk_admin" } ?: currentUser
                    }
                    if (targetTk != null) {
                        val isRootAdmin = targetTk.id == "tk_admin"
                        val subTabTarget = if (isRootAdmin) 1 else 0
                        val linkedNv = nvs.firstOrNull { it.id == targetTk.nvId }
                        val emailMoi = params["email"]?.takeIf { it.contains("@") }?.lowercase() ?: targetTk.email
                        val sdtMoi = params["sdt"]?.takeIf { it.isNotBlank() } ?: targetTk.username
                        val mkMoi = params["mk"]?.takeIf { it.isNotBlank() } ?: targetTk.matKhauHienThi.ifBlank { DEFAULT_ADMIN_PASS }
                        val tenMoi = params["ten"]?.takeIf { it.isNotBlank() } ?: targetTk.hoTen
                        return taoYeuCauXacNhanGehihi(
                            loai = LoaiLenhChoXacNhan.SUA,
                            tieuDeTomTat = "Sửa tài khoản ${targetTk.hoTen}",
                            chiTietXacNhan = "Bạn đang yêu cầu cập nhật tài khoản ${targetTk.hoTen}:\n• Họ tên: $tenMoi\n• SĐT: $sdtMoi\n• Gmail: ${emailMoi.ifBlank { "chưa gắn" }}\n• Mật khẩu: $mkMoi"
                        ) {
                            adminCapNhatTaiKhoanVaNhanVien(
                                tkId = targetTk.id,
                                hoTenMoi = tenMoi,
                                sdtDangNhapMoi = sdtMoi,
                                matKhauMoi = mkMoi,
                                gtMoi = linkedNv?.gt ?: "Nam",
                                ghiChuMoi = linkedNv?.gc ?: "",
                                emailMoi = emailMoi
                            )
                            KetQuaAiBot(
                                cauTraLoi = cleanReply.ifBlank { "gehihi đã cập nhật tài khoản $tenMoi." },
                                hanhDongThucThi = "Đã sửa TK $tenMoi",
                                chuyenTab = 5,
                                chuyenMucAdmin = subTabTarget,
                                tuDongDongChat = true
                            )
                        }
                    }
                }
                "ADD_CUSTOMER" -> {
                    val ten = params["ten"].orEmpty().ifBlank { "Khách mới" }
                    val sdt = params["sdt"].orEmpty().ifBlank { "Đang cập nhật" }
                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.THEM,
                        tieuDeTomTat = "Thêm khách hàng $ten ($sdt)",
                        chiTietXacNhan = "Bạn đang yêu cầu thêm khách hàng mới: \"$ten\" (SĐT: $sdt)."
                    ) {
                        val kh = themKhachHang(ten, sdt)
                        KetQuaAiBot(
                            cauTraLoi = cleanReply.ifBlank { "gehihi đã thêm khách hàng ${kh?.ten ?: ten}." },
                            hanhDongThucThi = "Đã thêm khách: ${kh?.ten ?: ten}",
                            chuyenTab = 1,
                            chuyenMucNguoi = 0,
                            tuDongDongChat = true
                        )
                    }
                }
                "ADD_STAFF" -> {
                    val ten = params["ten"].orEmpty().trim().ifBlank { "KTV Mới" }
                    val sdt = params["sdt"]?.filter { it.isDigit() }?.trim().orEmpty()
                    val gt = params["gt"]?.trim()?.takeIf { it.isNotBlank() } ?: "Nam"
                    val mk = params["mk"].orEmpty().trim().ifBlank { "123456" }
                    val email = params["email"]?.trim()?.takeIf { it.contains("@") }.orEmpty()
                    val laAdmin = params["admin"]?.equals("true", ignoreCase = true) == true

                    if (sdt.isBlank()) {
                        return KetQuaAiBot(
                            cauTraLoi = "Để thêm nhân viên \"$ten\", bạn vui lòng cung cấp Số điện thoại (dùng làm tên đăng nhập tài khoản). Ví dụ:\n👉 \"Thêm nv $ten sđt 0912345678 giới tính $gt\"",
                            hanhDongThucThi = "Cần SĐT để tạo tài khoản KTV",
                            chuyenTab = 1,
                            chuyenMucNguoi = 1
                        )
                    }

                    return taoYeuCauXacNhanGehihi(
                        loai = LoaiLenhChoXacNhan.THEM,
                        tieuDeTomTat = "Tạo tài khoản & KTV $ten (SĐT: $sdt)",
                        chiTietXacNhan = "Bạn đang yêu cầu tạo nhân viên & tài khoản mới: Họ tên \"$ten\" ($gt), SĐT đăng nhập \"$sdt\", mật khẩu \"$mk\"${if (email.isNotBlank()) ", Gmail: $email" else ""}${if (laAdmin) ", cấp mác Admin" else ""}."
                    ) {
                        val nv = themNhanVien(
                            ten = ten,
                            gt = gt,
                            sdt = sdt,
                            gc = "Bấm huyệt trị liệu",
                            matKhauKhoiTao = mk,
                            laAdmin = laAdmin
                        )
                        val tkCreated = taiKhoans.firstOrNull { it.nvId == nv?.id }
                        if (tkCreated != null && email.isNotBlank()) {
                            val tkIdx = taiKhoans.indexOfFirst { it.id == tkCreated.id }
                            if (tkIdx >= 0) {
                                taiKhoans[tkIdx] = taiKhoans[tkIdx].copy(email = email)
                                saveToPrefs()
                            }
                        }
                        KetQuaAiBot(
                            cauTraLoi = cleanReply.ifBlank { "gehihi đã tạo tài khoản nhân viên ${nv?.ten ?: ten}${if (laAdmin) " (có mác Admin)" else ""}. Tên đăng nhập (SĐT): ${tkCreated?.username ?: sdt}, mật khẩu: $mk${if (email.isNotBlank()) ", Gmail: $email" else ""}." },
                            hanhDongThucThi = "Đã tạo TK: ${tkCreated?.username ?: sdt} (MK: $mk)",
                            chuyenTab = 1,
                            chuyenMucNguoi = 1
                        )
                    }
                }
                "EDIT_STAFF" -> {
                    val target = params["target"].orEmpty().ifBlank { params["ten"].orEmpty() }
                    val targetTk = taiKhoans.firstOrNull { tk ->
                        val normT = normalizeVietnameseText(target)
                        tk.username == target || normalizeVietnameseText(tk.hoTen).contains(normT)
                    }
                    if (targetTk != null) {
                        val tenMoi = params["ten"]?.takeIf { it.isNotBlank() } ?: targetTk.hoTen
                        val sdtMoi = params["sdt"]?.filter { it.isDigit() }?.takeIf { it.isNotBlank() } ?: targetTk.username
                        val gtMoi = params["gt"]?.takeIf { it.isNotBlank() } ?: "Nam"
                        val mkMoi = params["mk"]?.takeIf { it.isNotBlank() } ?: targetTk.matKhauHienThi.ifBlank { "123456" }
                        val emailMoi = params["email"]?.takeIf { it.contains("@") } ?: targetTk.email
                        return taoYeuCauXacNhanGehihi(
                            loai = LoaiLenhChoXacNhan.SUA,
                            tieuDeTomTat = "Sửa KTV ${targetTk.hoTen} ➔ $tenMoi",
                            chiTietXacNhan = "Bạn đang yêu cầu sửa thông tin KTV ${targetTk.hoTen}:\n• Tên mới: $tenMoi ($gtMoi)\n• SĐT: $sdtMoi\n• Mật khẩu: $mkMoi"
                        ) {
                            adminCapNhatTaiKhoanVaNhanVien(
                                tkId = targetTk.id,
                                hoTenMoi = tenMoi,
                                sdtDangNhapMoi = sdtMoi,
                                matKhauMoi = mkMoi,
                                gtMoi = gtMoi,
                                ghiChuMoi = "Bấm huyệt trị liệu",
                                emailMoi = emailMoi
                            )
                            KetQuaAiBot(
                                cauTraLoi = cleanReply.ifBlank { "gehihi đã cập nhật thông tin KTV $tenMoi." },
                                hanhDongThucThi = "Đã sửa KTV: $tenMoi ($sdtMoi)",
                                chuyenTab = 1,
                                chuyenMucNguoi = 1
                            )
                        }
                    }
                }
                "ADD_FINANCE_CHI" -> {
                    val tien = params["tien"]?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
                    val lydo = params["lydo"].orEmpty().ifBlank { originalPrompt }
                    if (tien > 0) {
                        return taoYeuCauXacNhanGehihi(
                            loai = LoaiLenhChoXacNhan.THEM,
                            tieuDeTomTat = "Ghi phiếu CHI ${formatVnd(tien)}",
                            chiTietXacNhan = "Bạn đang yêu cầu ghi phiếu CHI ${formatVnd(tien)} với lý do: \"$lydo\"."
                        ) {
                            val tc = themGiaoDichSoQuy(LoaiTC.CHI, tien, lydo)
                            KetQuaAiBot(
                                cauTraLoi = cleanReply.ifBlank { "gehihi đã ghi phiếu Chi ${formatVndSpeakable(tc?.tien ?: tien)}." },
                                hanhDongThucThi = "Đã ghi CHI -${formatVnd(tc?.tien ?: tien)}",
                                chuyenTab = 3,
                                tuDongDongChat = true
                            )
                        }
                    }
                }
                "ADD_FINANCE_THU" -> {
                    val tien = params["tien"]?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
                    val lydo = params["lydo"].orEmpty().ifBlank { originalPrompt }
                    if (tien > 0) {
                        return taoYeuCauXacNhanGehihi(
                            loai = LoaiLenhChoXacNhan.THEM,
                            tieuDeTomTat = "Ghi phiếu THU ${formatVnd(tien)}",
                            chiTietXacNhan = "Bạn đang yêu cầu ghi phiếu THU ${formatVnd(tien)} với lý do: \"$lydo\"."
                        ) {
                            val tc = themGiaoDichSoQuy(LoaiTC.THU, tien, lydo)
                            KetQuaAiBot(
                                cauTraLoi = cleanReply.ifBlank { "gehihi đã ghi phiếu Thu ${formatVndSpeakable(tc?.tien ?: tien)}." },
                                hanhDongThucThi = "Đã ghi THU +${formatVnd(tc?.tien ?: tien)}",
                                chuyenTab = 3,
                                tuDongDongChat = true
                            )
                        }
                    }
                }
                "SAVE_MEMORY" -> {
                    val note = params["note"].orEmpty().ifBlank { originalPrompt }
                    ghiNhoSuKienDaiHan("Ghi chú Admin: $note")
                    return KetQuaAiBot(
                        cauTraLoi = cleanReply.ifBlank { "gehihi đã lưu vào Bộ nhớ vĩnh viễn: \"$note\"." },
                        hanhDongThucThi = "Đã lưu vào Bộ nhớ vĩnh viễn"
                    )
                }
                "NAVIGATE_TAB" -> {
                    val normOrig = normalizeVietnameseText(originalPrompt)
                    val tab = params["tab"]?.toIntOrNull()?.coerceIn(0, 5) ?: 0
                    val mucNguoi = if (tab == 1) {
                        if (normOrig.contains("nhan vien") || normOrig.contains("ktv") || normOrig.contains("nhan su")) 1 else 0
                    } else null
                    val mucVe = if (tab == 0) {
                        when {
                            normOrig.contains("cho chot") -> 1
                            normOrig.contains("da chot") -> 2
                            else -> 0
                        }
                    } else null
                    val mucAdmin = if (tab == 5) {
                        when {
                            normOrig.contains("sao luu") || normOrig.contains("backup") -> 2
                            normOrig.contains("admin") || normOrig.contains("mail") || normOrig.contains("gmail") -> 1
                            else -> 0
                        }
                    } else null
                    return KetQuaAiBot(
                        cauTraLoi = cleanReply.ifBlank { "gehihi đã chuyển màn hình tới mục bạn yêu cầu." },
                        hanhDongThucThi = "Đã chuyển màn hình",
                        chuyenTab = tab,
                        chuyenMucVe = mucVe,
                        chuyenMucNguoi = mucNguoi,
                        chuyenMucAdmin = mucAdmin,
                        tuDongDongChat = true
                    )
                }
            }
        }

        return KetQuaAiBot(cauTraLoi = cleanReply)
    }

    private fun tenThuTiengViet(dayOfWeek: Int): String = when (dayOfWeek) {
        Calendar.MONDAY -> "Thứ Hai"
        Calendar.TUESDAY -> "Thứ Ba"
        Calendar.WEDNESDAY -> "Thứ Tư"
        Calendar.THURSDAY -> "Thứ Năm"
        Calendar.FRIDAY -> "Thứ Sáu"
        Calendar.SATURDAY -> "Thứ Bảy"
        Calendar.SUNDAY -> "Chủ Nhật"
        else -> "Trong tuần"
    }

    /**
     * Bộ giải đáp tức thì mọi câu hỏi về Thứ, Ngày, Tháng, Năm, Giờ hiện tại, Ngày mai, Ngày kia, Hôm qua.
     */
    private fun traLoiCauHoiThoiGianVaLich(prompt: String, norm: String): KetQuaAiBot? {
        val hoiThuNgayGio = norm.contains("thu may") ||
            norm.contains("ngay may") ||
            norm.contains("ngay bao nhieu") ||
            norm.contains("may gio") ||
            norm.contains("hom nay la") ||
            norm.contains("ngay mai la") ||
            norm.contains("mai la") ||
            norm.contains("hom qua la") ||
            norm.contains("ngay kia") ||
            norm.contains("ngay mot") ||
            norm == "mai" ||
            norm == "hom nay" ||
            norm == "bay gio"

        if (!hoiThuNgayGio) return null

        val sdfvn = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val nowCal = Calendar.getInstance()
        val thuHomNay = tenThuTiengViet(nowCal.get(Calendar.DAY_OF_WEEK))
        val ngayHomNay = sdfvn.format(nowCal.time)
        val gioHienTai = nowTimeStr()
        val caHienTai = todayDateStr()

        val offsetDays = when {
            norm.contains("ngay kia") || norm.contains("ngay mot") || Regex("\\bmot\\b").containsMatchIn(norm) -> 2
            norm.contains("ngay mai") || Regex("\\bmai\\b").containsMatchIn(norm) -> 1
            norm.contains("hom kia") -> -2
            norm.contains("hom qua") || Regex("\\bqua\\b").containsMatchIn(norm) -> -1
            else -> 0
        }

        val targetCal = (nowCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, offsetDays)
        }
        val thuTarget = tenThuTiengViet(targetCal.get(Calendar.DAY_OF_WEEK))
        val ngayTarget = sdfvn.format(targetCal.time)

        val cauTraLoi = when (offsetDays) {
            1 -> "📅 Ngày mai là **$thuTarget, ngày $ngayTarget**.\n(Hôm nay là $thuHomNay, ngày $ngayHomNay — hiện tại là $gioHienTai, đang thuộc ca ngày $caHienTai)."
            2 -> "📅 Ngày kia (mốt) là **$thuTarget, ngày $ngayTarget**.\n(Hôm nay là $thuHomNay, ngày $ngayHomNay)."
            -1 -> "📅 Hôm qua là **$thuTarget, ngày $ngayTarget**.\n(Hôm nay là $thuHomNay, ngày $ngayHomNay)."
            -2 -> "📅 Hôm kia là **$thuTarget, ngày $ngayTarget**.\n(Hôm nay là $thuHomNay, ngày $ngayHomNay)."
            else -> "📅 Bây giờ là **$gioHienTai — $thuHomNay, ngày $ngayHomNay** (Ca vận hành hiện tại tính từ 6h sáng: $caHienTai)."
        }
        return KetQuaAiBot(cauTraLoi = cauTraLoi)
    }

    private fun tinhBieuThucToanHoc(prompt: String): String? {
        val norm = normalizeVietnameseText(prompt)
            .replace("bang may", "")
            .replace("bang bao nhieu", "")
            .replace("la may", "")
            .replace("la bao nhieu", "")
            .replace("tinh giup", "")
            .replace("tinh", "")
            .replace("?", "")
            .replace("=", "")
            .replace("cong", "+")
            .replace("tru", "-")
            .replace("nhan", "*")
            .replace("x", "*")
            .replace("chia", "/")
            .trim()

        val mathRegex = Regex("^(\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(\\d+(?:\\.\\d+)?)$")
        val match = mathRegex.find(norm) ?: return null
        val a = match.groupValues[1].toDoubleOrNull() ?: return null
        val op = match.groupValues[2]
        val b = match.groupValues[3].toDoubleOrNull() ?: return null

        val res = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b != 0.0) a / b else return "Không thể chia cho 0."
            else -> return null
        }

        val formatNum = { n: Double ->
            if (n % 1.0 == 0.0) n.toLong().toString() else String.format("%.2f", n)
        }
        return "gehihi tính giúp bạn: ${formatNum(a)} $op ${formatNum(b)} = ${formatNum(res)}."
    }

    private fun traLoiThongMinhNoiBo(prompt: String): KetQuaAiBot {
        val norm = normalizeVietnameseText(prompt).trim()
        val today = todayDateStr()

        // 0. BỘ THỰC THI LỆNH TRỰC TIẾP (Xóa, Sửa, Thêm, Điều hướng, Mở vé, Sổ quỹ...):
        // Khi Gemini mất mạng hoặc không phản hồi, thực thi ngay các khẩu lệnh của Admin
        val directAction = thucThiLenhTrucTiepTrenApp(prompt)
        if (directAction != null) return directAction

        // 1. Kiểm tra câu hỏi thời gian / lịch
        traLoiCauHoiThoiGianVaLich(prompt, norm)?.let { return it }

        // 2. Hỏi hoặc thao tác liên quan đến Mail / Gmail / Tài khoản / Mật khẩu
        if (norm.contains("mail") || norm.contains("gmail") || norm.contains("email") ||
            norm.contains("tai khoan") || norm.contains("mat khau") || norm.contains("quyen")
        ) {
            val rootAdmin = taiKhoans.firstOrNull { it.id == "tk_admin" }
            val adminMail = rootAdmin?.email?.ifBlank { cloudUserEmail }.orEmpty().ifBlank { "chưa gắn Gmail" }
            val dsTk = taiKhoans.joinToString("\n• ", prefix = "• ") {
                "${it.hoTen}: SĐT ${it.username}, MK ${it.matKhauHienThi.ifBlank { "123456" }}${if (it.email.isNotBlank()) ", Gmail: ${it.email}" else ""} (${if (it.isAdmin()) "Admin" else "Nhân viên"})"
            }
            val subTab = if (norm.contains("mail") || norm.contains("gmail") || norm.contains("admin")) 1 else 0
            return KetQuaAiBot(
                cauTraLoi = "Thông tin tài khoản trên hệ thống:\n$dsTk\n\n• Gmail Đám mây hiện tại của Admin: $adminMail.\n👉 Bạn có thể nhắn: \"Sửa mail admin thành ten@gmail.com\" hoặc sửa trực tiếp tại mục Sửa TK Admin gốc.",
                hanhDongThucThi = "Mở mục ${if (subTab == 1) "Sửa TK Admin gốc" else "Quản lý Tài khoản"}",
                chuyenTab = 5,
                chuyenMucAdmin = subTab
            )
        }

        // 3. Hỏi về Bảng lương / Hoa hồng KTV
        if (norm.contains("luong") || norm.contains("hoa hong") || norm.contains("chiet khau") || norm.contains("thu nhap")) {
            val bangLuong = layBangLuongChiTiet(BoLocThoiGian.THANG, today)
            val summary = bangLuong.joinToString("\n• ", prefix = "• ") {
                "${it.nv.ten}: ${it.tongSoLuot} lượt (${it.tongPhutLam} phút) — Hoa hồng: ${formatVndSpeakable(it.tongHoaHong)}"
            }
            return KetQuaAiBot(
                cauTraLoi = "Bảng lương hoa hồng KTV tháng này:\n$summary",
                hanhDongThucThi = "Mở tab Báo cáo & Bảng lương",
                chuyenTab = 4
            )
        }

        // 3B. Hỏi về Danh sách Nhân viên KTV / Nhân sự
        if (norm.contains("ktv") || norm.contains("nhan vien") || norm.contains("nhan su")) {
            val dsNv = if (nvs.isEmpty()) {
                "Hiện danh sách nhân viên KTV đang trống (0 người)."
            } else {
                val bangLuong = layBangLuongChiTiet(BoLocThoiGian.THANG, today)
                nvs.joinToString("\n• ", prefix = "• ") { nv ->
                    val luongNv = bangLuong.firstOrNull { it.nv.id == nv.id }
                    "${nv.ten} (${nv.gt}, SĐT: ${nv.sdt}) — Tháng này: ${luongNv?.tongSoLuot ?: 0} lượt, hoa hồng ${formatVnd(luongNv?.tongHoaHong ?: 0L)}"
                }
            }
            return KetQuaAiBot(
                cauTraLoi = "Danh sách Nhân viên KTV hiện tại (${nvs.size} người):\n$dsNv\n\n👉 Bạn có thể nhắn: \"Thêm nhân viên Hùng sđt 0912345678\", \"Xóa nhân viên [Tên]\" hoặc \"Chuyển hướng tới mục nhân viên\".",
                hanhDongThucThi = "Mở mục Danh sách Nhân viên KTV",
                chuyenTab = 1,
                chuyenMucNguoi = 1
            )
        }

        // 4. Hỏi về Khách hàng / Gói liệu trình còn mấy buổi
        if (norm.contains("khach") || norm.contains("buoi") || norm.contains("goi")) {
            val matchedKh = khs.firstOrNull {
                val shortName = normalizeVietnameseText(it.ten).substringAfterLast(" ")
                shortName.length >= 2 && norm.contains(shortName)
            }
            if (matchedKh != null) {
                val dsGoiKh = dks.filter { it.khId == matchedKh.id }
                val goiText = if (dsGoiKh.isEmpty()) {
                    "chưa đăng ký gói liệu trình nào"
                } else {
                    dsGoiKh.joinToString("; ") { dk ->
                        val g = gois.firstOrNull { it.id == dk.goiId }
                        val tong = g?.soBuoi ?: 10
                        "${g?.ten ?: "Gói LT"}: đã dùng ${dk.luotDaDung}/$tong buổi (còn ${tong - dk.luotDaDung} buổi)"
                    }
                }
                return KetQuaAiBot(
                    cauTraLoi = "Khách hàng ${matchedKh.ten} (SĐT: ${matchedKh.sdt}): $goiText.",
                    hanhDongThucThi = "Xem hồ sơ khách ${matchedKh.ten}",
                    chuyenTab = 1,
                    chuyenMucNguoi = 0
                )
            }
            val summaryKh = khs.take(8).joinToString("; ") { "${it.ten} (${it.sdt})" }
            return KetQuaAiBot(
                cauTraLoi = "Hiện quán có ${khs.size} khách hàng và ${dks.size} gói đang hoạt động: $summaryKh.",
                hanhDongThucThi = "Mở danh sách Khách hàng",
                chuyenTab = 1,
                chuyenMucNguoi = 0
            )
        }

        // 5. Hỏi về Dịch vụ / Giá vé / Menu
        val matchedDvAsk = timDichVuTheoCauLenh(norm)
        if (matchedDvAsk != null || norm.contains("dich vu") || norm.contains("bang gia") || norm.contains("menu")) {
            if (matchedDvAsk != null) {
                return KetQuaAiBot(
                    cauTraLoi = "Dịch vụ \"${matchedDvAsk.ten}\" (${matchedDvAsk.phut} phút): Giá thu khách ${formatVndSpeakable(matchedDvAsk.gia)}, hoa hồng KTV ${formatVndSpeakable(matchedDvAsk.chietKhauVnd)} (Chủ quán thực nhận ${formatVndSpeakable(matchedDvAsk.gia - matchedDvAsk.chietKhauVnd)}).",
                    hanhDongThucThi = "Xem dịch vụ ${matchedDvAsk.ten}",
                    chuyenTab = 2
                )
            }
            val dsDvText = dvs.take(6).joinToString("\n• ", prefix = "• ") {
                "${it.ten} (${it.phut}p): Giá ${formatVnd(it.gia)} — CK KTV ${formatVnd(it.chietKhauVnd)}"
            }
            return KetQuaAiBot(
                cauTraLoi = "Bảng giá dịch vụ hiện tại (${dvs.size} dịch vụ, ${gois.size} gói):\n$dsDvText",
                hanhDongThucThi = "Mở tab Dịch vụ & Gói",
                chuyenTab = 2
            )
        }

        // 6. Hỏi về Doanh thu / Lợi nhuận / Chủ quán thực nhận / Sổ quỹ / Báo cáo / Vé
        if (norm.contains("doanh thu") || norm.contains("loi nhuan") || norm.contains("bao cao") ||
            norm.contains("thuc nhan") || norm.contains("chu quan") || norm.contains("lai rong") ||
            norm.contains("bao nhieu ve") || norm.contains("may ve") || norm.contains("tinh hinh") ||
            norm.contains("so quy") || norm.contains("hom nay")
        ) {
            val veHomNay = luots.filter { it.daXacNhan && it.ngay == today }
            val tongThuHomNay = tcs.filter { it.loai == LoaiTC.THU && it.ngay == today }.sumOf { it.tien } +
                veHomNay.filter { ve -> ve.thu > 0L && (ve.phieuThuVeId.isBlank() || tcs.none { it.id == ve.phieuThuVeId }) }.sumOf { it.thu }
            val tongChiHomNay = tcs.filter { it.loai == LoaiTC.CHI && it.ngay == today }.sumOf { it.tien }
            val chuThucNhan = tongThuHomNay - tongChiHomNay
            return KetQuaAiBot(
                cauTraLoi = "📊 Báo cáo ca hôm nay ($today, tính từ 6h sáng):\n" +
                    "• Vé phục vụ: ${luots.count { it.dangLam }} đang làm • ${luots.count { it.choChot }} chờ chốt • ${veHomNay.size} đã chốt.\n" +
                    "• Tổng Thu: +${formatVndSpeakable(tongThuHomNay)}\n" +
                    "• Trả KTV & Chi: -${formatVndSpeakable(tongChiHomNay)}\n" +
                    "• Chủ quán thực nhận (Lãi ròng): ${if (chuThucNhan >= 0) "+" else ""}${formatVndSpeakable(chuThucNhan)}.",
                hanhDongThucThi = "Xem Báo cáo & Lãi ròng",
                chuyenTab = 4
            )
        }

        // 7. Hỏi về Bộ nhớ của gehihi
        if (norm.contains("bo nho") || norm.contains("nho duoc") || norm.contains("ky uc")) {
            return KetQuaAiBot(
                cauTraLoi = "🧠 Bộ nhớ Kép của gehihi đang lưu ${aiMessages.size}/$MAX_AI_MESSAGES tin nhắn và ${aiLongTermMemories.size}/$MAX_LONG_TERM_MEMORIES sự kiện trên máy, đồng thời tự động đồng bộ thông minh dưới ngưỡng 750KB lên Đám mây Google Firestore."
            )
        }

        // 8. Chào hỏi hoặc câu hỏi mở
        val nowCal = Calendar.getInstance()
        val thuHomNay = tenThuTiengViet(nowCal.get(Calendar.DAY_OF_WEEK))
        val ngayHomNay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(nowCal.time)
        return KetQuaAiBot(
            cauTraLoi = "Chào Admin! Hôm nay là $thuHomNay ($ngayHomNay, ${nowTimeStr()}).\n" +
                "• Hiện quán có ${luots.count { it.dangLam }} vé đang làm, ${luots.count { it.choChot }} vé chờ chốt.\n" +
                "• Bạn có thể hỏi lịch/doanh thu hoặc ra lệnh trực tiếp như:\n" +
                "  - \"Sửa mail admin thành ten@gmail.com\"\n" +
                "  - \"Sửa dịch vụ chườm đá nóng giá 80k hoa hồng 30k\"\n" +
                "  - \"Sửa vé khách Tuấn giá 200k hoa hồng 80k\""
        )
    }

    private fun timVeTheoTenTrongCau(danhSach: List<Luot>, normPrompt: String): Luot? {
        return danhSach.firstOrNull { ve ->
            val khTen = normalizeVietnameseText(khs.firstOrNull { it.id == ve.khId }?.ten.orEmpty())
            val nvTen = normalizeVietnameseText(nvs.firstOrNull { it.id == ve.nvId }?.ten.orEmpty())
            val khShort = khTen.substringAfterLast(" ")
            val nvShort = nvTen.substringAfterLast(" ")
            (khShort.length >= 2 && normPrompt.contains(khShort)) ||
                (nvShort.length >= 2 && normPrompt.contains(nvShort))
        }
    }

    private fun trichXuatDanhSachSoTien(norm: String): List<Long> {
        val results = mutableListOf<Long>()
        val tokenRegex = Regex("(\\d+)\\s*(k|nghin|ngan|tr|trieu)\\s*(\\d*)|\\b(\\d{4,9})\\b")
        for (m in tokenRegex.findAll(norm)) {
            val num1 = m.groupValues[1]
            val unit = m.groupValues[2]
            val sub = m.groupValues[3]
            val rawDigits = m.groupValues[4]
            if (num1.isNotBlank()) {
                val base = num1.toLongOrNull() ?: continue
                if (unit == "k" || unit == "nghin" || unit == "ngan") {
                    results.add(base * 1_000L)
                } else if (unit == "tr" || unit == "trieu") {
                    val le = if (sub.isBlank()) 0L else (sub.toLongOrNull() ?: 0L) * 100_000L
                    results.add(base * 1_000_000L + le)
                }
            } else if (rawDigits.isNotBlank() && !rawDigits.startsWith("0")) {
                rawDigits.toLongOrNull()?.let { results.add(it) }
            }
        }
        return results
    }

    private fun trichXuatSoTien(norm: String): Long? {
        return trichXuatDanhSachSoTien(norm).firstOrNull()
    }

    private fun trichXuatTenSauTuKhoa(raw: String, keywords: List<String>): String {
        val lower = raw.lowercase()
        for (kw in keywords) {
            val idx = lower.indexOf(kw)
            if (idx >= 0) {
                val after = raw.substring(idx + kw.length).trim()
                if (after.isNotBlank()) return after
            }
        }
        return ""
    }

    // =========================================================================
    // 9. LƯU TRỮ JSON THÔ VÀO SHAREDPREFERENCES (tamquat_fresh_v2)
    // =========================================================================
    private fun buildBackupJsonObject(forCloudSync: Boolean = false): JSONObject {
        val root = JSONObject()

        root.put("taiKhoans", JSONArray().apply {
            taiKhoans.forEach { tk ->
                put(JSONObject().apply {
                    put("id", tk.id)
                    put("username", tk.username)
                    put("matKhauHash", tk.matKhauHash)
                    put("quyen", tk.quyen.name)
                    put("nvId", tk.nvId)
                    put("hoTen", tk.hoTen)
                    put("cacQuyenDuocCap", JSONArray(tk.cacQuyenDuocCap.map { it.name }))
                    put("matKhauHienThi", tk.matKhauHienThi)
                    put("email", tk.email)
                })
            }
        })

        root.put("deletedIds", JSONArray().apply {
            deletedIds.takeLast(500).forEach { id -> put(id) }
        })

        root.put("nvs", JSONArray().apply {
            nvs.forEach { nv ->
                put(JSONObject().apply {
                    put("id", nv.id)
                    put("ten", nv.ten)
                    put("gt", nv.gt)
                    put("sdt", nv.sdt)
                    put("gc", nv.gc)
                })
            }
        })

        root.put("dvs", JSONArray().apply {
            dvs.forEach { dv ->
                put(JSONObject().apply {
                    put("id", dv.id)
                    put("ten", dv.ten)
                    put("phut", dv.phut)
                    put("gia", dv.gia)
                    put("moTa", dv.moTa)
                    put("chietKhauVnd", dv.chietKhauVnd)
                    put("ck", dv.chietKhauVnd)
                })
            }
        })

        root.put("gois", JSONArray().apply {
            gois.forEach { g ->
                put(JSONObject().apply {
                    put("id", g.id)
                    put("ten", g.ten)
                    put("dvIds", JSONArray(g.dvIds))
                    put("soBuoi", g.soBuoi)
                    put("gia", g.gia)
                    put("chiTietLuot", g.chiTietLuot)
                    put("dvTangKem", g.dvTangKem)
                })
            }
        })

        root.put("khs", JSONArray().apply {
            khs.forEach { kh ->
                put(JSONObject().apply {
                    put("id", kh.id)
                    put("ten", kh.ten)
                    put("sdt", kh.sdt)
                })
            }
        })

        root.put("dks", JSONArray().apply {
            dks.forEach { dk ->
                put(JSONObject().apply {
                    put("id", dk.id)
                    put("khId", dk.khId)
                    put("goiId", dk.goiId)
                    put("ngay", dk.ngay)
                    put("daDung", dk.daDung)
                    put("luotDaDung", dk.luotDaDung)
                })
            }
        })

        root.put("luots", JSONArray().apply {
            luots.forEach { l ->
                put(JSONObject().apply {
                    put("id", l.id)
                    put("khId", l.khId)
                    put("nvId", l.nvId)
                    put("dvIds", JSONArray(l.dvIds))
                    put("dkId", l.dkId)
                    put("dvNgoai", l.dvNgoai)
                    put("tgbd", l.tgbd)
                    put("tgkt", l.tgkt)
                    put("thu", l.thu)
                    put("daXacNhan", l.daXacNhan)
                    put("ngay", l.ngay)
                    if (l.hoaHongChot != null) {
                        put("hoaHongChot", l.hoaHongChot)
                    }
                    put("phieuChiHoaHongId", l.phieuChiHoaHongId)
                    put("phieuThuVeId", l.phieuThuVeId)
                })
            }
        })

        root.put("tcs", JSONArray().apply {
            tcs.forEach { tc ->
                put(JSONObject().apply {
                    put("id", tc.id)
                    put("loai", tc.loai.name)
                    put("tien", tc.tien)
                    put("lyDo", tc.lyDo)
                    put("ngay", tc.ngay)
                })
            }
        })

        // Kiến trúc Bộ nhớ Kép (Hybrid Unlimited Vault):
        // - Trên máy / File Backup: lưu tới 20.000 tin nhắn và 50.000 sự kiện.
        // - Khi đồng bộ qua Cloud Firestore (forCloudSync = true): giữ 100% Ghi chú ghim Admin + Bản nén ký ức + 1.500 sự kiện/600 tin nhắn mới nhất để luôn dưới ngưỡng 1MB của Firestore.
        val msgLimit = if (forCloudSync) CLOUD_SYNC_MAX_AI_MESSAGES else MAX_AI_MESSAGES
        root.put("aiMessages", JSONArray().apply {
            aiMessages.takeLast(msgLimit).forEach { m ->
                put(JSONObject().apply {
                    put("id", m.id)
                    put("isUser", m.isUser)
                    put("text", m.text)
                    put("actionNote", m.actionNote)
                    put("thoiGian", m.thoiGian)
                })
            }
        })

        val memoriesToSave = if (forCloudSync && aiLongTermMemories.size > CLOUD_SYNC_MAX_MEMORIES) {
            val pinned = aiLongTermMemories.filter {
                it.contains("Ghi chú Admin", ignoreCase = true) || it.contains("[NÉN KÝ ỨC]", ignoreCase = true)
            }
            val recent = aiLongTermMemories.takeLast((CLOUD_SYNC_MAX_MEMORIES - pinned.size).coerceAtLeast(300))
            (pinned + recent).distinct()
        } else {
            aiLongTermMemories.takeLast(MAX_LONG_TERM_MEMORIES)
        }
        root.put("aiLongTermMemories", JSONArray().apply {
            memoriesToSave.forEach { note ->
                put(note)
            }
        })

        // CƠ CHẾ TỰ ĐỘNG BẢO VỆ NGƯỠNG 1 MB CỦA FIRESTORE (SAFE CLOUD GUARD <= 750 KB):
        // Nếu sau nhiều năm hoạt động, tổng dung lượng JSON gửi lên Cloud vượt 750 KB (dưới mức giới hạn 900KB của Rules & 1MB của Firestore):
        // - Bước 1: Tự động thu gọn phần tin nhắn/sự kiện phụ trên bản Cloud (trong khi bộ nhớ trên máy vẫn giữ nguyên 100% 50.000 sự kiện).
        // - Bước 2: Nếu sau 5-10 năm số lượng vé lịch sử quá lớn (>10.000 vé), bản Cloud tự động giữ 2.500 vé & 3.000 phiếu quỹ mới nhất để đồng bộ tức thì, không bao giờ bị đầy hay lỗi 1MB!
        if (forCloudSync) {
            var cloudBytes = root.toString().toByteArray(Charsets.UTF_8).size
            if (cloudBytes > MAX_CLOUD_PAYLOAD_BYTES) {
                root.put("aiMessages", JSONArray().apply {
                    aiMessages.takeLast(120).forEach { m ->
                        put(JSONObject().apply {
                            put("id", m.id)
                            put("isUser", m.isUser)
                            put("text", m.text.take(500))
                            put("actionNote", m.actionNote)
                            put("thoiGian", m.thoiGian)
                        })
                    }
                })
                val compactMemories = aiLongTermMemories.filter {
                    it.contains("Ghi chú Admin", ignoreCase = true) || it.contains("[NÉN KÝ ỨC]", ignoreCase = true)
                }.takeLast(300) + aiLongTermMemories.takeLast(200)
                root.put("aiLongTermMemories", JSONArray(compactMemories.distinct()))
                cloudBytes = root.toString().toByteArray(Charsets.UTF_8).size
            }
            if (cloudBytes > MAX_CLOUD_PAYLOAD_BYTES && (luots.size > 2500 || tcs.size > 3000)) {
                val recentLuots = luots.take(2500)
                val recentTcs = tcs.take(3000)
                root.put("luots", JSONArray().apply {
                    recentLuots.forEach { l ->
                        put(JSONObject().apply {
                            put("id", l.id)
                            put("khId", l.khId)
                            put("nvId", l.nvId)
                            put("dvIds", JSONArray(l.dvIds))
                            put("dkId", l.dkId)
                            put("dvNgoai", l.dvNgoai)
                            put("tgbd", l.tgbd)
                            put("tgkt", l.tgkt)
                            put("thu", l.thu)
                            put("daXacNhan", l.daXacNhan)
                            put("ngay", l.ngay)
                            if (l.hoaHongChot != null) put("hoaHongChot", l.hoaHongChot)
                            put("phieuChiHoaHongId", l.phieuChiHoaHongId)
                            put("phieuThuVeId", l.phieuThuVeId)
                        })
                    }
                })
                root.put("tcs", JSONArray().apply {
                    recentTcs.forEach { tc ->
                        put(JSONObject().apply {
                            put("id", tc.id)
                            put("loai", tc.loai.name)
                            put("tien", tc.tien)
                            put("lyDo", tc.lyDo)
                            put("ngay", tc.ngay)
                        })
                    }
                })
            }
        }

        return root
    }

    /**
     * Lưu riêng bộ nhớ vĩnh viễn của gehihi vào cả SharedPreferences VÀ file nội bộ gehihi_memory_vault.json
     * để đảm bảo khi thoát app hoặc nhiều tháng không vào thì bộ nhớ của gehihi không bao giờ bị mất.
     */
    fun saveGehihiMemoryVault() {
        try {
            val vault = JSONObject().apply {
                put("updatedAt", "${todayDateStr()} ${nowTimeStr()}")
                put("aiMessages", JSONArray().apply {
                    aiMessages.takeLast(MAX_AI_MESSAGES).forEach { m ->
                        put(JSONObject().apply {
                            put("id", m.id)
                            put("isUser", m.isUser)
                            put("text", m.text)
                            put("actionNote", m.actionNote)
                            put("thoiGian", m.thoiGian)
                        })
                    }
                })
                put("aiLongTermMemories", JSONArray().apply {
                    aiLongTermMemories.takeLast(MAX_LONG_TERM_MEMORIES).forEach { note ->
                        put(note)
                    }
                })
            }
            val jsonStr = vault.toString()
            prefs.edit().putString(KEY_GEHIHI_MEMORY_JSON, jsonStr).apply()
            runCatching {
                memoryVaultFile.writeText(jsonStr, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save gehihi memory vault", e)
        }
    }

    private fun loadGehihiMemoryVault() {
        val rawFromFile = runCatching {
            if (memoryVaultFile.exists()) memoryVaultFile.readText(Charsets.UTF_8) else null
        }.getOrNull()
        val raw = rawFromFile?.takeIf { it.isNotBlank() } ?: prefs.getString(KEY_GEHIHI_MEMORY_JSON, null) ?: return
        try {
            val vault = JSONObject(raw)
            vault.optJSONArray("aiLongTermMemories")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val note = arr.optString(i).trim()
                    if (note.isNotBlank() && !aiLongTermMemories.contains(note)) {
                        aiLongTermMemories.add(note)
                    }
                }
                nenBoNhoDaiHanCuonChieu()
            }
            vault.optJSONArray("aiMessages")?.let { arr ->
                if (arr.length() > aiMessages.size) {
                    aiMessages.clear()
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue
                        val text = o.optString("text")
                        if (text.isNotBlank()) {
                            aiMessages.add(
                                AiChatMessage(
                                    id = o.optString("id").ifBlank { "msg_${shortId()}" },
                                    isUser = o.optBoolean("isUser", false),
                                    text = text,
                                    actionNote = o.optString("actionNote", ""),
                                    thoiGian = o.optString("thoiGian", "")
                                )
                            )
                        }
                    }
                    nenTinNhanAiCuonChieu()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load gehihi memory vault", e)
        }
    }

    fun saveToPrefs() {
        val root = buildBackupJsonObject()
        val jsonStr = root.toString()
        prefs.edit().putString(KEY_DATA_JSON, jsonStr).apply()
        saveGehihiMemoryVault()
        if (!isApplyingCloudSnapshot) {
            pushToCloudWorkspace(jsonStr)
        }
    }

    private fun loadFromPrefs() {
        val raw = prefs.getString(KEY_DATA_JSON, null) ?: return
        try {
            val root = JSONObject(raw)
            deletedIds.clear()
            root.optJSONArray("deletedIds")?.let { dArr ->
                for (i in 0 until dArr.length()) {
                    val dId = dArr.optString(i).trim()
                    if (dId.isNotBlank() && !deletedIds.contains(dId)) deletedIds.add(dId)
                }
            }

            taiKhoans.clear()
            root.optJSONArray("taiKhoans")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val perms = mutableSetOf<QuyenHan>()
                    o.optJSONArray("cacQuyenDuocCap")?.let { pArr ->
                        for (j in 0 until pArr.length()) {
                            runCatching { QuyenHan.valueOf(pArr.getString(j)) }.getOrNull()?.let { perms.add(it) }
                        }
                    }
                    val hoTenRaw = o.optString("hoTen")
                    val cleanHoTen = if (hoTenRaw.contains("OWNER", ignoreCase = true) || hoTenRaw.contains("Chủ Cơ Sở", ignoreCase = true)) {
                        "Admin Quản trị"
                    } else hoTenRaw
                    taiKhoans.add(
                        TaiKhoan(
                            id = o.optString("id"),
                            username = o.optString("username"),
                            matKhauHash = o.optString("matKhauHash"),
                            quyen = runCatching { VaiTro.valueOf(o.optString("quyen")) }.getOrDefault(VaiTro.STAFF),
                            nvId = o.optString("nvId"),
                            hoTen = cleanHoTen,
                            cacQuyenDuocCap = perms,
                            matKhauHienThi = o.optString("matKhauHienThi", ""),
                            email = o.optString("email", "")
                        )
                    )
                }
            }

            nvs.clear()
            root.optJSONArray("nvs")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    nvs.add(
                        NV(
                            id = o.optString("id"),
                            ten = o.optString("ten"),
                            gt = o.optString("gt"),
                            sdt = o.optString("sdt"),
                            gc = o.optString("gc")
                        )
                    )
                }
            }

            dvs.clear()
            root.optJSONArray("dvs")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val ck = if (o.has("chietKhauVnd")) o.optLong("chietKhauVnd", 0L) else o.optLong("ck", 0L)
                    dvs.add(
                        DV(
                            id = o.opt("id")?.toString().orEmpty(),
                            ten = o.optString("ten"),
                            phut = o.optInt("phut", 60),
                            gia = o.optLong("gia", 0L),
                            moTa = o.optString("moTa"),
                            chietKhauVnd = ck
                        )
                    )
                }
            }

            gois.clear()
            root.optJSONArray("gois")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val ids = mutableListOf<String>()
                    val rawDvIds = o.opt("dvIds")
                    when (rawDvIds) {
                        is JSONArray -> {
                            for (j in 0 until rawDvIds.length()) ids.add(rawDvIds.opt(j)?.toString().orEmpty())
                        }
                        is String -> {
                            rawDvIds.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { ids.add(it) }
                        }
                    }
                    gois.add(
                        Goi(
                            id = o.opt("id")?.toString().orEmpty(),
                            ten = o.optString("ten"),
                            dvIds = ids,
                            soBuoi = o.optInt("soBuoi", 1),
                            gia = o.optLong("gia", 0L),
                            chiTietLuot = o.optString("chiTietLuot", ""),
                            dvTangKem = o.optString("dvTangKem", "")
                        )
                    )
                }
            }

            khs.clear()
            root.optJSONArray("khs")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    khs.add(KH(id = o.opt("id")?.toString().orEmpty(), ten = o.optString("ten"), sdt = o.optString("sdt")))
                }
            }

            dks.clear()
            root.optJSONArray("dks")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dks.add(
                        DK(
                            id = o.opt("id")?.toString().orEmpty(),
                            khId = o.opt("khId")?.toString().orEmpty(),
                            goiId = o.opt("goiId")?.toString().orEmpty(),
                            ngay = o.optString("ngay"),
                            daDung = o.optBoolean("daDung", false),
                            luotDaDung = o.optInt("luotDaDung", 0)
                        )
                    )
                }
            }

            luots.clear()
            root.optJSONArray("luots")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val ids = mutableListOf<String>()
                    o.optJSONArray("dvIds")?.let { dArr ->
                        for (j in 0 until dArr.length()) ids.add(dArr.opt(j)?.toString().orEmpty())
                    }
                    val hoaHongChotVal = if (o.has("hoaHongChot") && !o.isNull("hoaHongChot")) {
                        o.optLong("hoaHongChot", 0L)
                    } else null
                    luots.add(
                        Luot(
                            id = o.opt("id")?.toString().orEmpty(),
                            khId = o.opt("khId")?.toString().orEmpty(),
                            nvId = o.opt("nvId")?.toString().orEmpty(),
                            dvIds = ids,
                            dkId = o.opt("dkId")?.toString().orEmpty(),
                            dvNgoai = o.optString("dvNgoai", ""),
                            tgbd = o.optString("tgbd"),
                            tgkt = o.optString("tgkt"),
                            thu = o.optLong("thu", 0L),
                            daXacNhan = o.optBoolean("daXacNhan", false),
                            ngay = o.optString("ngay", todayDateStr()),
                            hoaHongChot = hoaHongChotVal,
                            phieuChiHoaHongId = o.optString("phieuChiHoaHongId", ""),
                            phieuThuVeId = o.optString("phieuThuVeId", "")
                        )
                    )
                }
            }

            tcs.clear()
            root.optJSONArray("tcs")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    tcs.add(
                        TC(
                            id = o.opt("id")?.toString().orEmpty(),
                            loai = runCatching { LoaiTC.valueOf(o.optString("loai")) }.getOrDefault(LoaiTC.THU),
                            tien = o.optLong("tien", 0L),
                            lyDo = o.optString("lyDo"),
                            ngay = o.optString("ngay", todayDateStr())
                        )
                    )
                }
            }

            root.optJSONArray("aiLongTermMemories")?.let { arr ->
                aiLongTermMemories.clear()
                for (i in 0 until arr.length()) {
                    val note = arr.optString(i).trim()
                    if (note.isNotBlank()) aiLongTermMemories.add(note)
                }
            }

            root.optJSONArray("aiMessages")?.let { arr ->
                aiMessages.clear()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    aiMessages.add(
                        AiChatMessage(
                            id = o.optString("id").ifBlank { "msg_${shortId()}" },
                            isUser = o.optBoolean("isUser", false),
                            text = o.optString("text"),
                            actionNote = o.optString("actionNote", ""),
                            thoiGian = o.optString("thoiGian", "")
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun seedDefaultData() {
        val today = todayDateStr()
        // Tài khoản Admin gốc mặc định: SĐT 0397772963, mật khẩu 247208
        taiKhoans.add(
            TaiKhoan(
                id = "tk_admin",
                username = DEFAULT_ADMIN_PHONE,
                matKhauHash = hashPassword(DEFAULT_ADMIN_PASS),
                quyen = VaiTro.OWNER,
                nvId = "",
                hoTen = "Admin Quản trị",
                cacQuyenDuocCap = QuyenHan.entries.toSet(),
                matKhauHienThi = DEFAULT_ADMIN_PASS
            )
        )

        val nv1 = NV("nv_01", "Nguyễn Văn Minh", "Nam", "0912345678", "Bấm huyệt cổ vai gáy")
        val nv2 = NV("nv_02", "Trần Thị Lan", "Nữ", "0987654321", "Trị liệu & xông hơi thảo dược")
        val nv3 = NV("nv_03", "Lê Hoàng Nam", "Nam", "0933445566", "Cạo gió giác hơi & phục hồi")
        nvs.addAll(listOf(nv1, nv2, nv3))

        taiKhoans.addAll(
            listOf(
                TaiKhoan("tk_nv1", nv1.sdt, hashPassword("123456"), VaiTro.STAFF, nv1.id, nv1.ten, emptySet(), "123456"),
                TaiKhoan("tk_nv2", nv2.sdt, hashPassword("123456"), VaiTro.STAFF, nv2.id, nv2.ten, emptySet(), "123456"),
                TaiKhoan("tk_nv3", nv3.sdt, hashPassword("123456"), VaiTro.STAFF, nv3.id, nv3.ten, emptySet(), "123456")
            )
        )

        // Nạp sẵn 10 dịch vụ và 3 gói liệu trình từ file menu_mới_nhất_2.json
        val menuDvs = danhSachDichVuMenuMoiNhat2()
        val menuGois = danhSachGoiMenuMoiNhat2()
        dvs.addAll(menuDvs)
        gois.addAll(menuGois)

        val kh1 = KH("kh_01", "Anh Tuấn", "0904112233")
        val kh2 = KH("kh_02", "Chị Hương", "0978998877")
        val kh3 = KH("kh_03", "Bác Hùng", "0915667788")
        khs.addAll(listOf(kh1, kh2, kh3))

        val dk1 = DK("dk_01", kh1.id, "676003166753527", today, daDung = false, luotDaDung = 3)
        val dk2 = DK("dk_02", kh2.id, "676158327672947", today, daDung = false, luotDaDung = 2)
        dks.addAll(listOf(dk1, dk2))

        luots.addAll(
            listOf(
                Luot("ve_01", kh1.id, nv1.id, listOf("379695801677810"), dkId = dk1.id, tgbd = "10:15", tgkt = "", thu = 0L, daXacNhan = false, ngay = today),
                Luot("ve_02", kh3.id, nv2.id, listOf("380937271360462"), dkId = "", tgbd = "09:00", tgkt = "10:30", thu = 220_000L, daXacNhan = false, ngay = today),
                Luot("ve_03", kh2.id, nv1.id, listOf("381921712080763"), dkId = "", tgbd = "08:00", tgkt = "09:00", thu = 250_000L, daXacNhan = true, ngay = today),
                Luot("ve_04", kh1.id, nv3.id, listOf("379695801677810"), dkId = dk1.id, tgbd = "14:00", tgkt = "15:00", thu = 0L, daXacNhan = true, ngay = today)
            )
        )

        tcs.addAll(
            listOf(
                TC("tc_01", LoaiTC.THU, 1_600_000L, "Mua Liệu trình 10 vé trị liệu 60 phút - KH ${kh1.ten} (${kh1.sdt})", today),
                TC("tc_02", LoaiTC.CHI, 70_000L, "Chiết khấu DV lẻ — NV ${nv1.ten} (Khách ${kh2.ten})", today),
                TC("tc_03", LoaiTC.CHI, 70_000L, "Chiết khấu DV trong gói liệu trình — NV ${nv3.ten} (Khách ${kh1.ten})", today),
                TC("tc_04", LoaiTC.CHI, 120_000L, "Mua thảo dược xông hơi & khăn ấm", today)
            )
        )
    }

    private fun shortId(): String = UUID.randomUUID().toString().replace("-", "").take(6)

    companion object {
        private const val TAG = "SpaStoreGehihi"
        const val PREFS_NAME = "tamquat_fresh_v2"
        const val KEY_DATA_JSON = "data_json"
        const val KEY_GEHIHI_MEMORY_JSON = "gehihi_memory_vault_json"
        const val GEHIHI_VAULT_FILE_NAME = "gehihi_memory_vault.json"
        const val KEY_LOGGED_IN_ACCOUNT_ID = "logged_in_account_id"
        // Bộ nhớ Kép (Hybrid Unlimited Vault): 20.000 tin nhắn & 50.000 sự kiện trên máy + đồng bộ thông minh lên Cloud
        const val MAX_AI_MESSAGES = 20000
        const val MAX_LONG_TERM_MEMORIES = 50000
        const val CLOUD_SYNC_MAX_AI_MESSAGES = 600
        const val CLOUD_SYNC_MAX_MEMORIES = 1500
        const val MAX_CLOUD_PAYLOAD_BYTES = 750_000
        val SEED_SAMPLE_IDS = setOf(
            "nv_01", "nv_02", "nv_03",
            "tk_nv1", "tk_nv2", "tk_nv3",
            "kh_01", "kh_02", "kh_03",
            "dk_01", "dk_02",
            "ve_01", "ve_02", "ve_03", "ve_04",
            "tc_01", "tc_02", "tc_03", "tc_04",
            "tc_thu_03", "tc_chi_03", "tc_chi_04"
        )
    }
}
