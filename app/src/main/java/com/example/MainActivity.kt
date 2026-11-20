package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.vietqr.VietQrHelper
import com.example.vietqr.ThongTinNganHang
import com.example.vietqr.NganHangInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import android.speech.tts.TextToSpeech
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ContentPaste
import java.util.Locale
import java.util.UUID
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaChampagne
import com.example.ui.theme.SpaDanger
import com.example.ui.theme.SpaRosewood
import com.example.ui.theme.SpaRosewoodDark
import com.example.ui.theme.SpaSageGreen
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "App Check debug secret configured successfully.")
            } else {
                Log.w(TAG, "No debug token found in Intent extra.")
            }
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        actionBar?.hide()
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val store = remember { SpaStore(context.applicationContext) }
            MyApplicationTheme {
                KhiemThiVietApp(store = store)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        configureAppCheck(this, intent)
    }
}

fun sharePlainText(context: Context, title: String, content: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, content)
    }
    val chooser = Intent.createChooser(sendIntent, title).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}

fun shareInstalledApkFile(context: Context) {
    try {
        val sourceApk = File(context.applicationInfo.sourceDir)
        if (!sourceApk.exists()) {
            Toast.makeText(context, "Không tìm thấy gói cài đặt APK gốc trên máy!", Toast.LENGTH_LONG).show()
            return
        }
        val shareDir = File(context.cacheDir, "shared_apk").apply { mkdirs() }
        val targetApk = File(shareDir, "KhiemThiViet-CaiDat.apk")
        sourceApk.copyTo(targetApk, overwrite = true)

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetApk
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, apkUri)
            putExtra(
                Intent.EXTRA_TEXT,
                "Bộ cài đặt ứng dụng Khiếm Thị Việt (KhiemThiViet-CaiDat.apk).\n" +
                    "Hướng dẫn cho Nhân viên:\n" +
                    "1. Chạm vào file .apk để cài đặt.\n" +
                    "2. Mở ứng dụng, bấm 'Kết nối Đám mây Google' 1 lần.\n" +
                    "3. Đăng nhập bằng SĐT / Email và Mật khẩu do Admin cấp."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Gửi bộ cài KhiemThiViet-CaiDat.apk cho máy nhân viên").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Không thể chia sẻ trực tiếp file APK: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

suspend fun attemptGoogleAutoSignIn(
    activityContext: Context,
    onSignedIn: () -> Unit
) {
    val auth = runCatching { FirebaseAuth.getInstance() }.getOrNull() ?: return
    if (auth.currentUser != null) {
        onSignedIn()
        return
    }
    try {
        val webClientId = activityContext.getString(R.string.default_web_client_id)
        if (webClientId.isBlank()) return
        val credentialManager = CredentialManager.create(activityContext)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val result = credentialManager.getCredential(activityContext, request)
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
            onSignedIn()
        }
    } catch (_: NoCredentialException) {
        // Chưa có phiên đăng nhập trước đó; người dùng có thể bấm nút Kết nối Đám mây Google
    } catch (e: GetCredentialException) {
        Log.w(TAG, "Auto sign-in skipped", e)
    } catch (e: Exception) {
        Log.w(TAG, "Auto sign-in failed", e)
    }
}

suspend fun signInWithGoogleInteractive(
    activityContext: Context,
    onSignedIn: () -> Unit,
    onError: (String) -> Unit,
    forceAccountPicker: Boolean = false
) {
    try {
        val webClientId = activityContext.getString(R.string.default_web_client_id)
        val credentialManager = CredentialManager.create(activityContext)
        if (forceAccountPicker) {
            runCatching {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
        }
        val signInOption = GetSignInWithGoogleOption.Builder(webClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()
        val result = credentialManager.getCredential(activityContext, request)
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            FirebaseAuth.getInstance()
                .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .await()
            onSignedIn()
        } else {
            onError("Không nhận được chứng thực Google hợp lệ.")
        }
    } catch (_: GetCredentialCancellationException) {
        onError("Đã hủy chọn tài khoản Google.")
    } catch (e: GetCredentialException) {
        Log.e(TAG, "Credential Manager error", e)
        onError("Không thể mở đăng nhập Google: ${e.localizedMessage ?: "Kiểm tra tài khoản Google trên máy"}")
    } catch (e: Exception) {
        Log.e(TAG, "Google Sign-In error", e)
        onError("Lỗi kết nối đám mây: ${e.localizedMessage ?: "Vui lòng thử lại"}")
    }
}

@Composable
fun KhiemThiVietApp(store: SpaStore) {
    val context = LocalContext.current
    val user = store.currentUser

    LaunchedEffect(Unit) {
        if (runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull() != null) {
            store.startRealtimeCloudSync()
        } else {
            attemptGoogleAutoSignIn(context) {
                store.startRealtimeCloudSync()
            }
        }
    }

    // Bảo trì toàn hệ thống (Global Maintenance): Developer được miễn trừ để hỗ trợ điều hành
    if (store.isGlobalMaintenance && user?.isDev() != true) {
        GlobalMaintenanceScreen(store = store, user = user)
        return
    }

    // Khi cài APK lần đầu hoặc khi đã đăng xuất -> Bắt buộc qua màn hình Đăng nhập 1 lần duy nhất
    if (user == null) {
        OneTimeLoginScreen(store = store)
        return
    }

    // Kill-Switch: Nếu tenant bị khóa, hiển thị màn hình khóa thay vì nội dung app (Developer được miễn trừ để điều hành/gỡ khóa)
    if (store.isTenantLocked && !user.isDev()) {
        TenantLockedScreen(store = store)
        return
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var ticketSubTab by rememberSaveable { mutableIntStateOf(0) }
    var peopleSubTab by rememberSaveable { mutableIntStateOf(0) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showAiBotDialog by remember { mutableStateOf(false) }
    var showAiKeyConfigDialog by remember { mutableStateOf(false) }
    var inspectPayrollNvId by remember { mutableStateOf<String?>(null) }

    val isAdmin = user.isAdmin()
    val canSeeFinanceTab = user.hasPermission(QuyenHan.CAN_MANAGE_FINANCE)
    val canSeeRevenueTab = user.hasPermission(QuyenHan.CAN_VIEW_REVENUE)

    // Nếu đang ở Tab 5 (Quyền hạn Admin) mà bị thu hồi quyền Admin thì tự quay về Tab 0
    if (!isAdmin && selectedTab == 5) {
        selectedTab = 0
    }

    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        topBar = {
            SpaHeaderBar(
                store = store,
                currentUser = user,
                onOpenSwitchAccount = { showAccountDialog = true },
                onOpenAiBot = { showAiBotDialog = true },
                onOpenAiKeyConfig = if (user.isAdmin()) { { showAiKeyConfigDialog = true } } else null
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .heightIn(min = 64.dp)
                    .testTag("bottom_nav_bar"),
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    label = { Text("Vé", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                    alwaysShowLabel = true,
                    modifier = Modifier
                        .semantics { contentDescription = "Tab 1: Quản lý Vé phục vụ" }
                        .testTag("tab_ve")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    label = { Text("Khách", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                    alwaysShowLabel = true,
                    modifier = Modifier
                        .semantics { contentDescription = "Tab 2: Khách hàng và Nhân viên" }
                        .testTag("tab_nguoi")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    label = { Text("Dịch vụ", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                    alwaysShowLabel = true,
                    modifier = Modifier
                        .semantics { contentDescription = "Tab 3: Dịch vụ và Gói liệu trình" }
                        .testTag("tab_menu")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            if (canSeeFinanceTab) Icons.Default.AccountBalanceWallet else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Sổ quỹ", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                    alwaysShowLabel = true,
                    modifier = Modifier
                        .semantics { contentDescription = "Tab 4: Sổ quỹ Thu Chi" }
                        .testTag("tab_so_quy")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            if (canSeeRevenueTab) Icons.Default.Analytics else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Báo cáo", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                    alwaysShowLabel = true,
                    modifier = Modifier
                        .semantics { contentDescription = "Tab 5: Báo cáo doanh thu và Bảng lương nhân viên" }
                        .testTag("tab_doanh_thu")
                )
                if (isAdmin) {
                    NavigationBarItem(
                        selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        label = { Text("Quyền hạn", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                        alwaysShowLabel = true,
                        modifier = Modifier
                            .semantics { contentDescription = "Tab 6 dành riêng cho Admin: Cấp quyền, Tạo tài khoản và Chỉnh sửa thông tin" }
                            .testTag("tab_quyen_han_admin")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> TabVanHanhVe(
                    store = store,
                    mucVeChon = ticketSubTab,
                    onChangeMucVe = { ticketSubTab = it }
                )
                1 -> TabNguoi(
                    store = store,
                    subTab = peopleSubTab,
                    onChangeSubTab = { peopleSubTab = it },
                    onGoToAdminPermissionTab = { selectedTab = 5 }
                )
                2 -> TabMenuVaGoi(store)
                3 -> TabSoQuy(store)
                4 -> TabDoanhThuBaoCao(
                    store = store,
                    initialInspectNvId = inspectPayrollNvId,
                    onClearInspectNvId = { inspectPayrollNvId = null }
                )
                5 -> if (isAdmin) {
                    TabQuyenHanAdmin(
                        store = store,
                        onOpenGehihi = { showAiBotDialog = true },
                        onOpenAiKeyConfig = { showAiKeyConfigDialog = true }
                    )
                } else {
                    TabVanHanhVe(
                        store = store,
                        mucVeChon = ticketSubTab,
                        onChangeMucVe = { ticketSubTab = it }
                    )
                }
            }
        }
    }

    if (showAccountDialog) {
        AccountSwitchDialog(
            store = store,
            onGoToAdminPermissionTab = {
                showAccountDialog = false
                selectedTab = 5
            },
            onDismiss = { showAccountDialog = false }
        )
    }

    if (showAiBotDialog) {
        AdminAiAssistantBotDialog(
            store = store,
            onNavigateTab = { targetTab ->
                selectedTab = targetTab.coerceIn(0, if (store.currentUser.isAdmin()) 5 else 4)
            },
            onSwitchTicketSubTab = { sub ->
                ticketSubTab = sub.coerceIn(0, 2)
            },
            onSwitchPeopleSubTab = { sub ->
                peopleSubTab = sub.coerceIn(0, 1)
            },
            onInspectStaffPayroll = { nvId ->
                inspectPayrollNvId = nvId
                selectedTab = 4
            },
            onDismiss = { showAiBotDialog = false },
            onOpenAiKeyConfig = if (user.isAdmin()) { { showAiKeyConfigDialog = true } } else null
        )
    }

    if (showAiKeyConfigDialog && user.isAdmin()) {
        AiKeyConfigDialog(
            store = store,
            onDismiss = { showAiKeyConfigDialog = false }
        )
    }
}

// =========================================================================
// TIỆN ÍCH KHỬ DỒN CHUỖI COMPOSING IME CHO BÀN PHÍM GIẢ LẬP LDPLAYER / PC
// =========================================================================

/**
 * Giải nén chuỗi composing bị dồn theo quy luật cấp số cộng độ dài tam giác Tn = n*(n+1)/2.
 * Ví dụ: "112123123412345" -> "12345", "112123" -> "123", "112" -> "12".
 * Nếu chuỗi không thỏa mãn cấu trúc dồn composing, trả về nguyên bản chuỗi gốc.
 */
fun unpackTriangularComposing(text: String): String {
    val len = text.length
    if (len < 3) return text
    val n = kotlin.math.sqrt(2.0 * len).toInt()
    if (n * (n + 1) / 2 == len) {
        val candidate = text.takeLast(n)
        val sb = StringBuilder()
        for (i in 1..n) {
            sb.append(candidate.take(i))
        }
        if (sb.toString() == text) {
            return candidate
        }
    }
    return text
}

/**
 * Khử dồn chuỗi IME giữa trạng thái hiện tại (current) và giá trị mới nhận từ bàn phím (incoming).
 * Bảo toàn 100% các thao tác:
 * - Gõ tuần tự từng ký tự trên bàn phím chuẩn (Single Character Invariant)
 * - Gõ phím lặp lại (ví dụ "11", "888888")
 * - Xóa ký tự (Backspace, Clear)
 * - Dán văn bản thông thường (Paste)
 * - Khử dồn triệt để chuỗi lặp composing do LDPlayer PC keyboard gửi tới.
 */
fun deduplicateImeComposing(current: String, incoming: String): String {
    // 1. Không đổi hoặc rỗng
    if (incoming == current || incoming.isEmpty()) {
        return incoming
    }

    // 2. Thao tác xóa ký tự (Backspace / Clear)
    if (incoming.length < current.length) {
        return incoming
    }

    // 3. Bắt đầu từ ô rỗng (ký tự đầu tiên hoặc dán chuỗi)
    if (current.isEmpty()) {
        // 3a. Phát hiện chuỗi nhân đôi nguyên xi: "1234512345" = "12345" + "12345"
        // Chỉ áp dụng khi half >= 4 để tránh false positive với mật khẩu "123123", "1212", "aabb"
        val half = incoming.length / 2
        if (half >= 4 && incoming.length % 2 == 0) {
            val firstHalf = incoming.take(half)
            if (incoming == firstHalf + firstHalf) {
                return firstHalf
            }
        }
        return unpackTriangularComposing(incoming)
    }

    // 4. Bàn phím chuẩn: gõ thêm đúng 1 ký tự (Single Character Invariant)
    // Giữ nguyên tuyệt đối, không xử lý gì thêm để bảo toàn mật khẩu có ký tự trùng lặp
    if (incoming.length - current.length == 1) {
        return incoming
    }

    // 5a. Phát hiện chuỗi nhân đôi nguyên xi khi current == incoming.take(half):
    // Ví dụ: current = "12345", incoming = "1234512345" -> trả về "12345" (giữ nguyên current)
    // Chỉ áp dụng khi current.length >= 4 để không ảnh hưởng mật khẩu hợp lệ dạng "123123"
    if (incoming.length == current.length * 2 && current.length >= 4) {
        val firstHalf = incoming.take(current.length)
        if (firstHalf == current && incoming == firstHalf + firstHalf) {
            return current  // Bỏ qua phần nhân đôi, giữ nguyên
        }
    }

    // 5b. Kiểm tra khử dồn tăng tiến (Incremental Composing Invariant)
    // Trên LDPlayer: incoming = current + suffix, trong đó suffix = current + char_moi
    // Ví dụ: current = "1", incoming = "112" -> suffix = "12" (bắt đầu bằng "1", dài hơn "1")
    // Ví dụ: current = "12", incoming = "12123" -> suffix = "123" (bắt đầu bằng "12", dài hơn "12")
    if (incoming.startsWith(current)) {
        val suffix = incoming.removePrefix(current)
        if (suffix.length > current.length && suffix.startsWith(current)) {
            return suffix
        }
        // 5c. current + newChar -> nhưng LDPlayer gửi cả current lại + thêm chuỗi mới khác current
        // Chỉ nhận nếu suffix là chuỗi mới hoàn toàn (không bắt đầu bằng current)
        if (suffix.length == 1) {
            return incoming  // Gõ 1 ký tự mới thêm vào cuối, hợp lệ
        }
    }

    // 6. Kiểm tra giải nén chuỗi dồn toàn phần nếu bàn phím gửi dồn trễ
    val unpacked = unpackTriangularComposing(incoming)
    if (unpacked != incoming) {
        return unpacked
    }

    return incoming
}

/**
 * Hàm lọc tổng thể cho ô nhập mật khẩu:
 * 1. Làm sạch bộ đệm số điện thoại bị trôi/đè từ ô trên xuống (Vấn đề 1).
 * 2. Khử hiện tượng nhân bản dồn chuỗi composing IME của LDPlayer.
 */
fun sanitizePasswordInput(current: String, incoming: String, phoneInput: String = ""): String {
    // Single Character Invariant & active typing: Nếu người dùng đang gõ tiếp từng ký tự
    // (incoming.length == current.length + 1) hoặc đang xóa lùi (incoming.length == current.length - 1),
    // TUYỆT ĐỐI không can thiệp chuỗi SĐT.
    // Chỉ loại bỏ tiền tố SĐT khi SĐT bị trôi dính ở đầu một chuỗi do buffer collision hoặc paste
    val isSequentialTyping = (incoming.length == current.length + 1) || (incoming.length == current.length - 1)
    val withoutPhone = if (
        !isSequentialTyping &&
        phoneInput.isNotBlank() &&
        phoneInput.length >= 3 &&
        incoming.startsWith(phoneInput) &&
        (current.isEmpty() || incoming.length - current.length >= phoneInput.length)
    ) {
        incoming.removePrefix(phoneInput)
    } else {
        incoming
    }
    return deduplicateImeComposing(current = current, incoming = withoutPhone)
}

/**
 * Helper đối tượng tương thích bổ trợ cho các kịch bản kiểm thử nâng cao.
 */
object ImeInputHelper {
    fun deduplicateImeInput(currentText: String, incomingText: String, phonePrefix: String = ""): String =
        sanitizePasswordInput(current = currentText, incoming = incomingText, phoneInput = phonePrefix)

    fun extractProgressiveComposing(text: String): String? {
        val unpacked = unpackTriangularComposing(text)
        return if (unpacked != text) unpacked else null
    }
}

// =========================================================================
// MÀN HÌNH ĐĂNG NHẬP 1 LẦN DUY NHẤT BẰNG SỐ ĐIỆN THOẠI & MẬT KHẨU
// KÈM KẾT NỐI ĐỒNG BỘ ĐÁM MÂY THỜI GIAN THỰC CHO MỌI MÁY
// =========================================================================
@Composable
fun OneTimeLoginScreen(store: SpaStore) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    // Khắc phục triệt để Autofill của Android nhầm lẫn hoặc đè text giữa các ô
    DisposableEffect(view) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            view.importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
        onDispose {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                view.importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_AUTO
            }
        }
    }

    var phoneInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(true) }
    var isLoggingIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var cloudBusy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        // Thông báo ngắt phiên từ Web Portal / Máy chủ nếu có
        if (!store.facilityTerminationNotice.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Assertive
                        contentDescription = "Thông báo ngắt phiên: ${store.facilityTerminationNotice}"
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = store.facilityTerminationNotice ?: "",
                        color = Color(0xFFC62828),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {
                            heading()
                            contentDescription = "Khiếm Thị Việt. Đăng nhập tài khoản một lần duy nhất bằng số điện thoại và mật khẩu."
                        }
                ) {
                    Text(
                        text = "KHIẾM THỊ VIỆT",
                        color = SpaRosewoodDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Đăng nhập bằng Số điện thoại (Chỉ cần nhập 1 lần duy nhất trên máy này)",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = {
                        phoneInput = it
                        errorMessage = ""
                    },
                    label = { Text("Tên đăng nhập (Số điện thoại hoặc Gmail)") },
                    placeholder = { Text("VD: 0912345678") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        autoCorrect = false
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    trailingIcon = if (phoneInput.isNotEmpty()) {
                        {
                            IconButton(
                                onClick = { phoneInput = "" },
                                modifier = Modifier.semantics { contentDescription = "Xóa số điện thoại" }
                            ) {
                                Icon(Icons.Filled.Clear, contentDescription = "Xóa số điện thoại")
                            }
                        }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_phone_input")
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { newVal ->
                        // Khắc phục triệt để lỗi dính/đè text buffer từ ô SĐT và lỗi nhân bản dồn chuỗi IME trên LDPlayer
                        val cleanVal = sanitizePasswordInput(
                            current = passwordInput,
                            incoming = newVal,
                            phoneInput = phoneInput
                        )
                        passwordInput = cleanVal
                        errorMessage = ""
                    },
                    label = { Text("Mật khẩu") },
                    placeholder = { Text("VD: 123456") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        autoCorrect = false
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (passwordInput.isNotEmpty()) {
                                IconButton(
                                    onClick = { passwordInput = "" },
                                    modifier = Modifier.semantics { contentDescription = "Xóa mật khẩu" }
                                ) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Xóa mật khẩu")
                                }
                            }
                            val iconDesc = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.semantics {
                                    contentDescription = iconDesc
                                }
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = iconDesc
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )

                // R1. Checkbox "Ghi nhớ tài khoản / mật khẩu", mặc định checked, đầy đủ TalkBack semantics
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = rememberMe,
                            role = Role.Checkbox,
                            onValueChange = { rememberMe = it }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = null,
                        modifier = Modifier.semantics {
                            contentDescription = if (rememberMe) {
                                "Ghi nhớ tài khoản và mật khẩu, đã chọn. Tự động đăng nhập vào các lần mở ứng dụng tiếp theo."
                            } else {
                                "Ghi nhớ tài khoản và mật khẩu, chưa chọn. Ứng dụng sẽ không lưu phiên và yêu cầu nhập lại ở lần mở tiếp theo."
                            }
                        }.testTag("checkbox_remember_login")
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Ghi nhớ tài khoản / mật khẩu",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = SpaDanger,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }

                // R2. Nút Đăng nhập có trạng thái chờ ("Đang kiểm tra...") và vô hiệu hóa tương tác
                Button(
                    onClick = {
                        if (phoneInput.isBlank() || passwordInput.isBlank()) {
                            errorMessage = "Vui lòng nhập đầy đủ số điện thoại và mật khẩu."
                        } else {
                            isLoggingIn = true
                            scope.launch {
                                try {
                                    val ok = store.pullLatestFromCloudAndLogin(phoneInput, passwordInput, rememberMe)
                                    if (ok) {
                                        Toast.makeText(
                                            context,
                                            "Đăng nhập thành công: ${store.currentUser?.hoTen}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        val failMsg = store.lastAuthErrorMessage.ifBlank { "❌ Sai số điện thoại hoặc mật khẩu! Vui lòng thử lại" }
                                        errorMessage = failMsg
                                        Toast.makeText(
                                            context,
                                            failMsg,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    val failMsg = store.lastAuthErrorMessage.ifBlank { "❌ Lỗi kết nối đăng nhập: ${e.message ?: "Vui lòng thử lại"}" }
                                    errorMessage = failMsg
                                    Toast.makeText(
                                        context,
                                        failMsg,
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    isLoggingIn = false
                                }
                            }
                        }
                    },
                    enabled = !isLoggingIn && !cloudBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics {
                            contentDescription = if (isLoggingIn) "Đang kiểm tra đăng nhập..." else "Đăng nhập và ghi nhớ tài khoản trên máy này"
                        }
                        .testTag("btn_login_submit")
                ) {
                    if (isLoggingIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang kiểm tra...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    } else {
                        Text("ĐĂNG NHẬP (GHI NHỚ 1 LẦN)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // R4. Nút Mua / Gia hạn gói dịch vụ điều hướng trực tiếp Zalo
                OutlinedButton(
                    onClick = {
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://zalo.me/0397772963")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }.onFailure {
                            Toast.makeText(context, "Hotline/Zalo tư vấn: 0397772963", Toast.LENGTH_LONG).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .semantics {
                            contentDescription = "Mua hoặc gia hạn gói dịch vụ. Chạm đúp để mở Zalo liên hệ trực tiếp nhà phát triển tư vấn đăng ký."
                        }
                        .testTag("btn_login_zalo_consult")
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = SpaRosewood, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("MUA / GIA HẠN GÓI DỊCH VỤ (ZALO)", fontWeight = FontWeight.Bold, color = SpaRosewood, fontSize = 13.sp)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Text(
                    text = "Lưu ý: Chỉ cần nhập số điện thoại và mật khẩu 1 lần duy nhất trên máy này để sử dụng trọn gói. Chưa có tài khoản? Vui lòng liên hệ Zalo (0397772963) để đăng ký.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SpaHeaderBar(
    store: SpaStore,
    currentUser: TaiKhoan?,
    onOpenSwitchAccount: () -> Unit,
    onOpenAiBot: () -> Unit,
    onOpenAiKeyConfig: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAdmin = currentUser.isAdmin()
    val roleSpeech = if (isAdmin) "tài khoản có mác Admin" else "tài khoản nhân viên"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpaRosewoodDark)
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Khiếm Thị Việt. Đang đăng nhập: ${currentUser?.hoTen ?: "Chưa đăng nhập"}, $roleSpeech."
                    }
            ) {
                Text(
                    text = "KHIẾM THỊ VIỆT",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (store.isCloudConnected) {
                        "${currentUser?.hoTen ?: "Admin"} • ☁️ Đã đồng bộ Cloud"
                    } else {
                        "${currentUser?.hoTen ?: "Admin"} • ☁️ Chạm để bật Cloud"
                    },
                    color = if (store.isCloudConnected) Color(0xFFF3E7D8) else SpaChampagne,
                    fontSize = 11.sp,
                    fontWeight = if (store.isCloudConnected) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clickable(enabled = !store.isCloudConnected) {
                            scope.launch {
                                signInWithGoogleInteractive(
                                    activityContext = context,
                                    onSignedIn = {
                                        store.startRealtimeCloudSync()
                                        Toast.makeText(
                                            context,
                                            "Đã bật đồng bộ đám mây thời gian thực!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                        .testTag("btn_header_connect_cloud")
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    onClick = onOpenAiBot,
                    color = SpaChampagne.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .semantics {
                            contentDescription = if (isAdmin) "Mở trợ lý AI gehihi cho Chủ Spa" else "Mở trợ lý AI gehihi cho Kỹ thuật viên"
                        }
                        .testTag("btn_ai_assistant")
                ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SpaChampagne,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "gehihi",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                }

                if (isAdmin && onOpenAiKeyConfig != null) {
                    Surface(
                        onClick = onOpenAiKeyConfig,
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics { contentDescription = "Cài đặt khóa Gemini API Key" }
                            .testTag("btn_header_key_config")
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Key,
                                contentDescription = null,
                                tint = SpaChampagne,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Surface(
                    onClick = onOpenSwitchAccount,
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .semantics { contentDescription = "Đổi tài khoản hoặc đăng xuất" }
                        .testTag("btn_switch_account")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = SpaChampagne,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Đăng xuất",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 0: VẬN HÀNH VÉ TỐI GIẢN CHO TALKBACK
// Nhân viên chỉ thêm vé, hoàn thành vé đang làm, xóa vé đang làm.
// Vé đã hoàn thành (Chờ chốt / Đã chốt) bị khóa sửa/xóa đối với nhân viên thường!
// =========================================================================
@Composable
fun TabVanHanhVe(
    store: SpaStore,
    mucVeChon: Int,
    onChangeMucVe: (Int) -> Unit
) {
    val context = LocalContext.current
    var showMoVeDialog by remember { mutableStateOf(false) }
    var veCanHoanThanh by remember { mutableStateOf<Luot?>(null) }
    var veCanSuaChiTiet by remember { mutableStateOf<Luot?>(null) }

    val veDangLam = store.luots.filter { it.dangLam }
    val veChoChot = store.luots.filter { it.choChot }
    val veDaChotHomNay = store.luots.filter { it.daHoanTat && it.ngay == todayDateStr() }

    val danhSachHienThi = when (mucVeChon) {
        0 -> veDangLam
        1 -> veChoChot
        else -> veDaChotHomNay
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Button(
                    onClick = { showMoVeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .semantics {
                            contentDescription = "Mở vé mới. Chạm đúp để chọn khách hàng, kỹ thuật viên và dịch vụ."
                        }
                        .testTag("btn_open_new_ticket")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MỞ VÉ PHỤC VỤ MỚI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(
                    selectedTabIndex = mucVeChon,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = SpaRosewoodDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = mucVeChon == 0,
                        onClick = { onChangeMucVe(0) },
                        text = {
                            Text(
                                "Đang làm (${veDangLam.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Mục vé đang làm: có ${veDangLam.size} vé"
                        }
                    )
                    Tab(
                        selected = mucVeChon == 1,
                        onClick = { onChangeMucVe(1) },
                        text = {
                            Text(
                                "Chờ chốt (${veChoChot.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Mục vé chờ chốt: có ${veChoChot.size} vé"
                        }
                    )
                    Tab(
                        selected = mucVeChon == 2,
                        onClick = { onChangeMucVe(2) },
                        text = {
                            Text(
                                "Đã chốt (${veDaChotHomNay.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Mục vé đã chốt hôm nay: có ${veDaChotHomNay.size} vé"
                        }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (danhSachHienThi.isEmpty()) {
                item {
                    val emptyMsg = when (mucVeChon) {
                        0 -> "Không có vé nào đang làm."
                        1 -> "Không có vé nào chờ chốt."
                        else -> "Chưa có vé nào chốt hôm nay."
                    }
                    EmptyStateCard(emptyMsg)
                }
            } else {
                items(danhSachHienThi, key = { it.id }) { ve ->
                    VeCardItemMinimal(
                        ve = ve,
                        store = store,
                        onHoanThanh = { veCanHoanThanh = ve },
                        onXacNhanChot = {
                            store.xacNhanChotVe(ve.id)
                            Toast.makeText(
                                context,
                                "Đã chốt vé và tính hoa hồng cho KTV!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onSuaGio = { veCanSuaChiTiet = ve },
                        onXoaVe = {
                            val deleted = store.xoaVe(ve.id)
                            if (deleted) {
                                Toast.makeText(context, "Đã xóa vé!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Vé đã hoàn thành, chỉ Admin mới có quyền xóa!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(96.dp)) }
        }
    }

    if (showMoVeDialog) {
        MoVeMoiDialog(
            store = store,
            onDismiss = {
                onChangeMucVe(0)
                showMoVeDialog = false
            }
        )
    }

    veCanHoanThanh?.let { ve ->
        HoanThanhVeDialog(
            ve = ve,
            store = store,
            onDismiss = { veCanHoanThanh = null }
        )
    }

    veCanSuaChiTiet?.let { ve ->
        SuaGioVeDialog(
            ve = ve,
            store = store,
            onDismiss = { veCanSuaChiTiet = null }
        )
    }
}

@Composable
fun VeCardItemMinimal(
    ve: Luot,
    store: SpaStore,
    onHoanThanh: () -> Unit,
    onXacNhanChot: () -> Unit,
    onSuaGio: () -> Unit,
    onXoaVe: () -> Unit
) {
    val kh = store.khs.firstOrNull { it.id == ve.khId }
    val nv = store.nvs.firstOrNull { it.id == ve.nvId }
    val tenDichVu = store.tenDichVuCuaVe(ve)
    val hoaHong = store.tinhHoaHongVe(ve)

    val user = store.currentUser
    val isAdmin = user.isAdmin()
    val canDeleteThisTicket = user.canDeleteTicket(ve)
    val canModifyCompleted = user.canModifyCompletedTicket(ve)
    val canEditTicket = if (isAdmin) {
        true
    } else if (ve.dangLam) {
        user.hasPermission(QuyenHan.CAN_EDIT_TIME)
    } else {
        canModifyCompleted && user.hasPermission(QuyenHan.CAN_EDIT_TIME)
    }

    val trangThaiText = when {
        ve.dangLam -> "Đang làm từ ${ve.tgbd}"
        ve.choChot -> "Đã hoàn thành • Chờ chốt (${ve.tgbd} - ${ve.tgkt})"
        else -> "Đã chốt (${ve.tgbd} - ${ve.tgkt})"
    }

    val loaiThanhToanText = if (ve.dungGoi) "Trừ gói 0 đồng (LT)" else "Thu ${formatVndSpeakable(ve.thu)}"

    val talkBackSummary = buildString {
        append("$trangThaiText. ")
        append("Khách: ${kh?.ten ?: "Khách lẻ"}. ")
        append("KTV: ${nv?.ten ?: "Chưa rõ"}. ")
        append("Dịch vụ: $tenDichVu. ")
        append("$loaiThanhToanText. ")
        append("Hoa hồng KTV: ${formatVndSpeakable(hoaHong)}.")
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ticket_card_${ve.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = talkBackSummary
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trangThaiText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = when {
                            ve.dangLam -> SpaRosewood
                            ve.choChot -> SpaDanger
                            else -> SpaSageGreen
                        }
                    )
                    Text(
                        text = if (ve.dungGoi) "0đ (LT)" else formatVnd(ve.thu),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = SpaRosewoodDark
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Khách: ${kh?.ten ?: "Khách lẻ"} • KTV: ${nv?.ten ?: "KTV"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "$tenDichVu • Hoa hồng: ${formatVnd(hoaHong)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (ve.dangLam) {
                    Button(
                        onClick = onHoanThanh,
                        colors = ButtonDefaults.buttonColors(containerColor = SpaChampagne),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .semantics {
                                contentDescription = "Hoàn thành vé của khách ${kh?.ten ?: ""}. Chạm đúp để kết thúc giờ làm."
                            }
                            .testTag("btn_complete_ticket_${ve.id}")
                    ) {
                        Text("Hoàn thành vé", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else if (ve.choChot && canModifyCompleted) {
                    Button(
                        onClick = onXacNhanChot,
                        colors = ButtonDefaults.buttonColors(containerColor = SpaSageGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .semantics {
                                contentDescription = "Chốt vé của khách ${kh?.ten ?: ""}, thu ${formatVndSpeakable(ve.thu)} và trích hoa hồng ${formatVndSpeakable(hoaHong)} cho KTV ${nv?.ten ?: ""}."
                            }
                            .testTag("btn_confirm_ticket_${ve.id}")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chốt vé ngay", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else if (!ve.dangLam && !canModifyCompleted) {
                    Text(
                        text = "Vé đã hoàn thành (Khóa sửa/xóa với nhân viên)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (canEditTicket) {
                    OutlinedButton(
                        onClick = onSuaGio,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .semantics {
                                contentDescription = "Sửa vé của khách ${kh?.ten ?: ""}"
                            }
                            .testTag("btn_edit_ticket_${ve.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sửa vé", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (canDeleteThisTicket) {
                    IconButton(
                        onClick = onXoaVe,
                        modifier = Modifier
                            .size(48.dp)
                            .semantics {
                                contentDescription = "Xóa vé của khách ${kh?.ten ?: ""}"
                            }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = SpaDanger)
                    }
                }
            }
        }
    }
}

@Composable
fun MoVeMoiDialog(store: SpaStore, onDismiss: () -> Unit) {
    var selectedKhId by remember { mutableStateOf(store.khs.firstOrNull()?.id ?: "") }
    val currentNvId = store.currentUser?.nvId?.takeIf { id -> store.nvs.any { it.id == id } }
    var selectedNvId by remember {
        mutableStateOf(currentNvId ?: store.nvs.firstOrNull()?.id ?: "")
    }
    // 0: Vé lẻ, 1: Trừ gói có sẵn (0đ), 2: + Đăng ký Gói Mới
    var loaiVeMode by remember { mutableStateOf(0) }
    var showThemKhachNhanh by remember { mutableStateOf(false) }
    var tenKhMoiInput by remember { mutableStateOf("") }
    var sdtKhMoiInput by remember { mutableStateOf("") }

    val selectedDvIds = remember {
        mutableStateListOf<String>().apply {
            store.dvs.firstOrNull()?.id?.let { add(it) }
        }
    }
    var dvNgoaiInput by remember { mutableStateOf("") }
    var tgbdInput by remember { mutableStateOf(nowTimeStr()) }
    val canEditTime = store.currentUser.hasPermission(QuyenHan.CAN_EDIT_TIME)

    val goiCuaKhach = store.dks.filter { it.khId == selectedKhId && !it.daDung }
    var selectedDkId by remember(selectedKhId) {
        mutableStateOf(goiCuaKhach.firstOrNull()?.id ?: "")
    }
    val selectedDk = goiCuaKhach.firstOrNull { it.id == selectedDkId }
    val selectedGoi = store.gois.firstOrNull { it.id == selectedDk?.goiId }

    val dvIdsInSelectedGoi = remember(selectedGoi?.id) {
        buildList {
            selectedGoi?.dvIds?.forEach { if (it.isNotBlank()) add(it) }
            val tangKem = selectedGoi?.dvTangKem.orEmpty()
            if (tangKem.isNotBlank() && !contains(tangKem)) add(tangKem)
        }
    }
    val selectedGoiDvIds = remember { mutableStateListOf<String>() }
    LaunchedEffect(selectedGoi?.id) {
        selectedGoiDvIds.clear()
        selectedGoiDvIds.addAll(dvIdsInSelectedGoi)
    }

    // Đăng ký Gói mới
    var selectedNewGoiId by remember { mutableStateOf(store.gois.firstOrNull()?.id ?: "") }
    val targetNewGoi = store.gois.firstOrNull { it.id == selectedNewGoiId }
    val dvIdsInNewGoi = remember(targetNewGoi?.id) {
        buildList {
            targetNewGoi?.dvIds?.forEach { if (it.isNotBlank()) add(it) }
            val tangKem = targetNewGoi?.dvTangKem.orEmpty()
            if (tangKem.isNotBlank() && !contains(tangKem)) add(tangKem)
        }
    }
    val selectedNewGoiDvIds = remember { mutableStateListOf<String>() }
    LaunchedEffect(targetNewGoi?.id) {
        selectedNewGoiDvIds.clear()
        selectedNewGoiDvIds.addAll(dvIdsInNewGoi)
    }

    val previewThu = when (loaiVeMode) {
        1 -> 0L
        2 -> targetNewGoi?.gia ?: 0L
        else -> selectedDvIds.sumOf { id -> store.dvs.firstOrNull { it.id == id }?.gia ?: 0L }
    }
    val previewHoaHong = when (loaiVeMode) {
        1 -> selectedGoiDvIds.sumOf { id -> store.dvs.firstOrNull { it.id == id }?.chietKhauVnd ?: 0L }
        2 -> selectedNewGoiDvIds.sumOf { id -> store.dvs.firstOrNull { it.id == id }?.chietKhauVnd ?: 0L }
        else -> selectedDvIds.sumOf { id -> store.dvs.firstOrNull { it.id == id }?.chietKhauVnd ?: 0L }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mở vé mới", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("1. Chọn Khách hàng:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(
                        onClick = {
                            tenKhMoiInput = ""
                            sdtKhMoiInput = ""
                            showThemKhachNhanh = true
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = SpaRosewood)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Khách mới", fontSize = 12.sp, color = SpaRosewood, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    store.khs.forEach { kh ->
                        FilterChip(
                            selected = selectedKhId == kh.id,
                            onClick = { selectedKhId = kh.id },
                            label = { Text(kh.ten, fontSize = 14.sp) },
                            modifier = Modifier.semantics {
                                contentDescription = "Khách hàng ${kh.ten}"
                            }
                        )
                    }
                }

                Text("2. Chọn KTV làm:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    store.nvs.forEach { nv ->
                        FilterChip(
                            selected = selectedNvId == nv.id,
                            onClick = { selectedNvId = nv.id },
                            label = { Text(nv.ten, fontSize = 14.sp) },
                            modifier = Modifier.semantics {
                                contentDescription = "Kỹ thuật viên ${nv.ten}"
                            }
                        )
                    }
                }

                Text("3. Chọn Hình thức vé:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = loaiVeMode == 0,
                        onClick = { loaiVeMode = 0 },
                        label = { Text("🎟️ Vé lẻ", fontSize = 14.sp) },
                        modifier = Modifier.semantics { contentDescription = "Vé lẻ tính tiền" }
                    )
                    FilterChip(
                        selected = loaiVeMode == 1,
                        onClick = { loaiVeMode = 1 },
                        label = { Text("📦 Trừ Gói (${goiCuaKhach.size})", fontSize = 14.sp) },
                        modifier = Modifier.semantics { contentDescription = "Trừ buổi gói có sẵn của khách, có ${goiCuaKhach.size} gói" }
                    )
                    FilterChip(
                        selected = loaiVeMode == 2,
                        onClick = { loaiVeMode = 2 },
                        label = { Text("✨ + Đăng ký Gói Mới", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.semantics { contentDescription = "Đăng ký mua gói mới cho khách và làm buổi 1" }
                    )
                }

                if (loaiVeMode == 0) {
                    Text(
                        "Chọn Dịch vụ (Bấm + để chọn nhiều vé cùng dịch vụ):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    store.dvs.forEach { dv ->
                        val qty = selectedDvIds.count { it == dv.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (qty > 0) selectedDvIds.removeAll { it == dv.id }
                                        else selectedDvIds.add(dv.id)
                                    }
                            ) {
                                Text(
                                    text = if (qty > 0) "${qty}x ${dv.ten}" else dv.ten,
                                    fontSize = 14.sp,
                                    fontWeight = if (qty > 0) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (qty > 0) SpaRosewoodDark else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Giá: ${formatVnd(dv.gia)} • Hoa hồng: ${formatVnd(dv.chietKhauVnd)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (qty > 0) {
                                    TextButton(
                                        onClick = {
                                            val idx = selectedDvIds.lastIndexOf(dv.id)
                                            if (idx >= 0) selectedDvIds.removeAt(idx)
                                        },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("-1", color = SpaDanger, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    }
                                    Text(
                                        text = "$qty",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { selectedDvIds.add(dv.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Thêm ${dv.ten}", tint = SpaSageGreen)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dvNgoaiInput,
                        onValueChange = { dvNgoaiInput = it },
                        label = { Text("Dịch vụ ngoài nhập tay (Không tính hoa hồng)") },
                        placeholder = { Text("Bỏ trống nếu dùng dịch vụ trong menu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (loaiVeMode == 1) {
                    if (goiCuaKhach.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SpaDanger.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Khách này chưa có gói liệu trình còn buổi.",
                                color = SpaDanger,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = { loaiVeMode = 2 },
                                colors = ButtonDefaults.buttonColors(containerColor = SpaSageGreen),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Đăng ký Mua Gói Mới cho khách ngay", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    } else {
                        goiCuaKhach.forEach { dk ->
                            val goi = store.gois.firstOrNull { it.id == dk.goiId }
                            if (goi != null) {
                                FilterChip(
                                    selected = selectedDkId == dk.id,
                                    onClick = { selectedDkId = dk.id },
                                    label = {
                                        Text("${goi.ten} (Đã dùng ${dk.luotDaDung}/${goi.soBuoi})")
                                    }
                                )
                            }
                        }
                        if (selectedGoi != null) {
                            Text(
                                "Dịch vụ làm trong buổi gói này (Kể cả 🎁 Tặng kèm vẫn tính hoa hồng cho KTV):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpaRosewoodDark
                            )
                            store.dvs.filter { it.id in dvIdsInSelectedGoi || it.id == selectedGoi.dvTangKem }.forEach { dv ->
                                val isBonus = dv.id == selectedGoi.dvTangKem
                                val checked = selectedGoiDvIds.contains(dv.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (checked) selectedGoiDvIds.remove(dv.id) else selectedGoiDvIds.add(dv.id)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = checked, onCheckedChange = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${if (isBonus) "🎁 Tặng kèm: " else ""}${dv.ten} (CK KTV: ${formatVnd(dv.chietKhauVnd)})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // loaiVeMode == 2: Đăng ký Gói Mới
                    Text(
                        "Chọn Gói Liệu Trình khách muốn mua:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SpaRosewoodDark
                    )
                    if (store.gois.isEmpty()) {
                        Text(
                            "Chưa có gói liệu trình nào trong hệ thống.",
                            color = SpaDanger,
                            fontSize = 13.sp
                        )
                    } else {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            store.gois.forEach { goi ->
                                FilterChip(
                                    selected = selectedNewGoiId == goi.id,
                                    onClick = { selectedNewGoiId = goi.id },
                                    label = {
                                        Text("${goi.ten} (${goi.soBuoi}b • ${formatVnd(goi.gia)})", fontSize = 13.sp)
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Gói ${goi.ten}, ${goi.soBuoi} buổi, giá ${formatVnd(goi.gia)}"
                                    }
                                )
                            }
                        }

                        if (targetNewGoi != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        "Gói: ${targetNewGoi.ten} • ${targetNewGoi.soBuoi} buổi • Giá: ${formatVnd(targetNewGoi.gia)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SpaRosewoodDark
                                    )
                                    Text(
                                        "Dịch vụ làm ngay Buổi 1 (Hoa hồng KTV vẫn tính đầy đủ):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SpaSageGreen
                                    )
                                    store.dvs.filter { it.id in dvIdsInNewGoi || it.id == targetNewGoi.dvTangKem }.forEach { dv ->
                                        val isBonus = dv.id == targetNewGoi.dvTangKem
                                        val checked = selectedNewGoiDvIds.contains(dv.id)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                if (checked) selectedNewGoiDvIds.remove(dv.id) else selectedNewGoiDvIds.add(dv.id)
                                            }
                                            .padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(checked = checked, onCheckedChange = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${if (isBonus) "🎁 Tặng kèm: " else ""}${dv.ten} (CK KTV: ${formatVnd(dv.chietKhauVnd)})",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        val thuDesc = when (loaiVeMode) {
                            1 -> "0đ (Trừ buổi gói có sẵn)"
                            2 -> "${formatVnd(previewThu)} (Tiền mua Gói ${targetNewGoi?.ten ?: ""})"
                            else -> formatVnd(previewThu)
                        }
                        Text(
                            text = "Dự kiến Thu khách: $thuDesc",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SpaRosewoodDark
                        )
                        Text(
                            text = "Hoa hồng KTV buổi này: ${formatVnd(previewHoaHong)}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = SpaSageGreen
                        )
                    }
                }

                if (canEditTime) {
                    OutlinedTextField(
                        value = tgbdInput,
                        onValueChange = { tgbdInput = it },
                        label = { Text("Giờ bắt đầu (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val created = when (loaiVeMode) {
                        1 -> {
                            store.moVeMoi(
                                khId = selectedKhId,
                                nvId = selectedNvId,
                                selectedDvIds = selectedGoiDvIds.toList(),
                                dkId = selectedDkId,
                                tgbd = tgbdInput,
                                dvNgoai = dvNgoaiInput
                            )
                        }
                        2 -> {
                            val newDk = store.dangKyGoiChoKhach(selectedKhId, selectedNewGoiId)
                            if (newDk != null) {
                                store.moVeMoi(
                                    khId = selectedKhId,
                                    nvId = selectedNvId,
                                    selectedDvIds = selectedNewGoiDvIds.toList(),
                                    dkId = newDk.id,
                                    tgbd = tgbdInput,
                                    dvNgoai = dvNgoaiInput
                                )
                            } else null
                        }
                        else -> {
                            store.moVeMoi(
                                khId = selectedKhId,
                                nvId = selectedNvId,
                                selectedDvIds = selectedDvIds.toList(),
                                dkId = "",
                                tgbd = tgbdInput,
                                dvNgoai = dvNgoaiInput
                            )
                        }
                    }
                    if (created != null) onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                modifier = Modifier.height(48.dp)
            ) {
                Text(if (loaiVeMode == 2) "Mua gói & Mở vé buổi 1" else "Tạo vé ngay", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text("Hủy") }
        }
    )

    if (showThemKhachNhanh) {
        AlertDialog(
            onDismissRequest = { showThemKhachNhanh = false },
            title = { Text("Thêm Khách hàng mới", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tenKhMoiInput,
                        onValueChange = { tenKhMoiInput = it },
                        label = { Text("Họ tên khách") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sdtKhMoiInput,
                        onValueChange = { sdtKhMoiInput = it },
                        label = { Text("Số điện thoại") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val tenTrim = tenKhMoiInput.trim()
                        if (tenTrim.isNotBlank()) {
                            val newKh = store.themKhachHang(tenTrim, sdtKhMoiInput.trim())
                            if (newKh != null) {
                                selectedKhId = newKh.id
                            }
                            showThemKhachNhanh = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood)
                ) {
                    Text("Lưu & Chọn", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showThemKhachNhanh = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun HoanThanhVeDialog(ve: Luot, store: SpaStore, onDismiss: () -> Unit) {
    val canEditTime = store.currentUser.hasPermission(QuyenHan.CAN_EDIT_TIME)
    var tgktInput by remember { mutableStateOf(nowTimeStr()) }
    var thucThuInput by remember { mutableStateOf(ve.thu.toString()) }
    var phuongThucThanhToan by remember { mutableStateOf("TIEN_MAT") } // "TIEN_MAT" hoặc "CHUYEN_KHOAN"
    var tienKhachDuaInput by remember { mutableStateOf("") }
    val hoaHongDuKien = store.tinhHoaHongVe(ve)
    var showVietQrDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hoàn thành vé & Thanh toán", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Dịch vụ: ${store.tenDichVuCuaVe(ve)}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("Giờ bắt đầu: ${ve.tgbd}", fontSize = 14.sp)
                if (canEditTime) {
                    OutlinedTextField(
                        value = tgktInput,
                        onValueChange = { tgktInput = it },
                        label = { Text("Giờ kết thúc (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (!ve.dungGoi) {
                    Text("Hình thức thanh toán:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SpaRosewoodDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { phuongThucThanhToan = "TIEN_MAT" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (phuongThucThanhToan == "TIEN_MAT") Color(0xFF2E7D32) else Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text(
                                "💵 Tiền mặt",
                                fontWeight = FontWeight.Bold,
                                color = if (phuongThucThanhToan == "TIEN_MAT") Color.White else Color.DarkGray
                            )
                        }
                        Button(
                            onClick = {
                                phuongThucThanhToan = "CHUYEN_KHOAN"
                                showVietQrDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (phuongThucThanhToan == "CHUYEN_KHOAN") Color(0xFF1565C0) else Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text(
                                "💳 VietQR CK",
                                fontWeight = FontWeight.Bold,
                                color = if (phuongThucThanhToan == "CHUYEN_KHOAN") Color.White else Color.DarkGray
                            )
                        }
                    }

                    OutlinedTextField(
                        value = thucThuInput,
                        onValueChange = { thucThuInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Tiền thực thu khách (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (phuongThucThanhToan == "TIEN_MAT") {
                        OutlinedTextField(
                            value = tienKhachDuaInput,
                            onValueChange = { tienKhachDuaInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Tiền khách đưa (tùy chọn)") },
                            placeholder = { Text("Ví dụ: 300000, 500000...") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        val tienKhachDua = tienKhachDuaInput.toLongOrNull() ?: 0L
                        val thucThu = thucThuInput.toLongOrNull() ?: ve.thu
                        if (tienKhachDua > thucThu && thucThu > 0) {
                            Text(
                                "👉 Tiền thối lại khách: ${formatVnd(tienKhachDua - thucThu)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showVietQrDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp), tint = SpaRosewoodDark)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "📲 Mở lại mã VietQR Napas 247 (${formatVnd(thucThuInput.toLongOrNull() ?: ve.thu)})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SpaRosewoodDark
                            )
                        }
                    }
                } else {
                    Text("Vé đi làm Gói (LT): Thu 0 đồng (Đã trả trước lúc đăng ký gói).", fontWeight = FontWeight.SemiBold, color = SpaRosewoodDark)
                }
                Text(
                    "Hoa hồng KTV (Sẽ sinh phiếu CHI khi bấm Xác nhận chốt): ${formatVnd(hoaHongDuKien)}",
                    fontSize = 13.sp,
                    color = SpaSageGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    store.hoanThanhVe(
                        luotId = ve.id,
                        tgktInput = tgktInput,
                        thucThu = if (ve.dungGoi) 0L else (thucThuInput.toLongOrNull() ?: ve.thu)
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Xác nhận hoàn thành")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text("Đóng") }
        }
    )

    if (showVietQrDialog) {
        VietQrPaymentDialog(
            store = store,
            ve = ve,
            amount = thucThuInput.toLongOrNull() ?: ve.thu,
            onPaymentDone = {
                store.hoanThanhVe(
                    luotId = ve.id,
                    tgktInput = tgktInput,
                    thucThu = thucThuInput.toLongOrNull() ?: ve.thu
                )
                showVietQrDialog = false
                onDismiss()
            },
            onDismiss = { showVietQrDialog = false }
        )
    }
}

@Composable
fun VietQrPaymentDialog(
    store: SpaStore,
    ve: Luot,
    amount: Long,
    onPaymentDone: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bank = store.thongTinNganHang
    val memo = VietQrHelper.taoNoiDungChuyenKhoanVe(ve.id)
    val qrUrl = store.taoVietQrChoVe(ve, amount)
    val speakableAmount = formatVndSpeakable(amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SpaRosewoodDark)
                Spacer(Modifier.width(8.dp))
                Text("Quét Mã VietQR Chuyển Khoản", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Thanh toán VietQR Napas 247. Số tiền: $speakableAmount. Ngân hàng ${bank.bankCode}, số tài khoản ${bank.soTaiKhoan}, chủ tài khoản ${bank.tenChuTaiKhoan}, nội dung $memo."
                    },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card số tiền nổi bật
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SỐ TIỀN THANH TOÁN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SpaRosewoodDark)
                        Text(formatVnd(amount), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = SpaRosewoodDark)
                        Text(speakableAmount, fontSize = 12.sp, color = Color.Gray)
                    }
                }

                if (bank.soTaiKhoan.isBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "⚠️ Chưa cấu hình số tài khoản ngân hàng của cơ sở. Chủ Spa vui lòng vào mục Quyền hạn Admin -> Cấu hình Ngân hàng để nhập STK nhận tiền.",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                } else {
                    // Chi tiết tài khoản nhận tiền
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ngân hàng:", fontSize = 12.sp, color = Color.Gray)
                                Text(VietQrHelper.timNganHangTheoCode(bank.bankCode).ten, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Số tài khoản:", fontSize = 12.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(bank.soTaiKhoan, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SpaRosewoodDark)
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("STK", bank.soTaiKhoan))
                                            Toast.makeText(context, "Đã sao chép STK", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Sao chép số tài khoản", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Chủ tài khoản:", fontSize = 12.sp, color = Color.Gray)
                                Text(bank.tenChuTaiKhoan.ifBlank { "Chủ cơ sở" }, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Nội dung CK:", fontSize = 12.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(memo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD))
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("NoiDung", memo))
                                            Toast.makeText(context, "Đã sao chép nội dung", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Sao chép nội dung", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Nút mở ảnh QR trực quan
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(qrUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Không thể mở link mã QR: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Mở ảnh mã QR / Đưa khách quét", fontSize = 13.sp)
                    }

                    // Ghi chú nhắc nhở chụp màn hình giao dịch
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                            contentDescription = "Lưu ý thanh toán: Quý khách vui lòng chụp lại màn hình giao dịch chuyển khoản thành công và liên hệ qua Zalo khi cần hỗ trợ đối soát."
                        }
                    ) {
                        Text(
                            "📸 Lưu ý: Quý khách vui lòng chụp lại màn hình giao dịch chuyển khoản thành công và liên hệ qua Zalo (0397772963) để được hỗ trợ/đối soát khi cần.",
                            modifier = Modifier.padding(10.dp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPaymentDone,
                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Khách đã chuyển xong → Hoàn thành")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun SuaGioVeDialog(ve: Luot, store: SpaStore, onDismiss: () -> Unit) {
    val isAdmin = store.currentUser.isAdmin()
    var selectedKhId by remember { mutableStateOf(ve.khId) }
    var selectedNvId by remember { mutableStateOf(ve.nvId) }
    val selectedDvIds = remember { mutableStateListOf<String>().apply { addAll(ve.dvIds) } }
    var dvNgoaiInput by remember { mutableStateOf(ve.dvNgoai) }
    var tgbd by remember { mutableStateOf(ve.tgbd) }
    var tgkt by remember { mutableStateOf(ve.tgkt) }
    var ngayInput by remember { mutableStateOf(ve.ngay) }
    var thuInput by remember { mutableStateOf(ve.thu.toString()) }
    var hoaHongInput by remember { mutableStateOf(store.tinhHoaHongVe(ve).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isAdmin) "Sửa toàn bộ thông tin Vé (Admin)" else "Sửa giờ & thông tin vé",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isAdmin) {
                    Text(
                        "Admin có thể sửa trực tiếp Khách, KTV, Dịch vụ, Giá vé và Hoa hồng riêng của vé này. Nếu vé đã chốt, Sổ quỹ và Bảng lương tự động cập nhật theo.",
                        fontSize = 12.sp,
                        color = SpaSageGreen,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text("Khách hàng:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        store.khs.forEach { kh ->
                            FilterChip(
                                selected = selectedKhId == kh.id,
                                onClick = { selectedKhId = kh.id },
                                label = { Text(kh.ten, fontSize = 13.sp) }
                            )
                        }
                    }

                    Text("KTV thực hiện:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        store.nvs.forEach { nv ->
                            FilterChip(
                                selected = selectedNvId == nv.id,
                                onClick = { selectedNvId = nv.id },
                                label = { Text(nv.ten, fontSize = 13.sp) }
                            )
                        }
                    }

                    Text("Dịch vụ trong vé (Bấm +/- để đổi số lượng):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    store.dvs.forEach { dv ->
                        val qty = selectedDvIds.count { it == dv.id }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${if (qty > 0) "${qty}x " else ""}${dv.ten} (${formatVnd(dv.gia)} / CK ${formatVnd(dv.chietKhauVnd)})",
                                fontSize = 12.sp,
                                fontWeight = if (qty > 0) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (qty > 0) {
                                    TextButton(
                                        onClick = {
                                            val idx = selectedDvIds.lastIndexOf(dv.id)
                                            if (idx >= 0) selectedDvIds.removeAt(idx)
                                        },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("-1", color = SpaDanger, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                    }
                                    Text("$qty", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                IconButton(
                                    onClick = { selectedDvIds.add(dv.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = SpaSageGreen)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dvNgoaiInput,
                        onValueChange = { dvNgoaiInput = it },
                        label = { Text("Dịch vụ ngoài (Tự do - không tính CK)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tgbd,
                        onValueChange = { tgbd = it },
                        label = { Text("Giờ vào (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tgkt,
                        onValueChange = { tgkt = it },
                        label = { Text("Giờ ra (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isAdmin) {
                    OutlinedTextField(
                        value = ngayInput,
                        onValueChange = { ngayInput = it },
                        label = { Text("Ngày ca làm việc (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isAdmin || (!ve.dungGoi && store.currentUser.canModifyCompletedTicket(ve))) {
                    OutlinedTextField(
                        value = thuInput,
                        onValueChange = { thuInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Giá vé thực thu khách (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isAdmin) {
                    OutlinedTextField(
                        value = hoaHongInput,
                        onValueChange = { hoaHongInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Hoa hồng KTV của vé này (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isAdmin) {
                        store.suaToanBoVeAdmin(
                            veId = ve.id,
                            khIdMoi = selectedKhId,
                            nvIdMoi = selectedNvId,
                            dvIdsMoi = selectedDvIds.toList(),
                            dkIdMoi = ve.dkId,
                            dvNgoaiMoi = dvNgoaiInput,
                            tgbdMoi = tgbd,
                            tgktMoi = tgkt,
                            ngayMoi = ngayInput,
                            thuMoi = thuInput.toLongOrNull() ?: ve.thu,
                            hoaHongMoi = hoaHongInput.toLongOrNull() ?: store.tinhHoaHongVe(ve)
                        )
                    } else {
                        store.capNhatGioVaTienVe(
                            luotId = ve.id,
                            tgbdMoi = tgbd,
                            tgktMoi = tgkt,
                            thuMoi = thuInput.toLongOrNull() ?: ve.thu
                        )
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Lưu thay đổi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text("Hủy") }
        }
    )
}

// =========================================================================
// TAB 1: KHÁCH HÀNG & NHÂN VIÊN
// Nhân viên mặc định chỉ quan sát được Khách hàng & Gói đã mua của khách.
// Tạo/sửa nhân viên và cấp quyền nằm ở Tab Quyền hạn riêng của Admin!
// =========================================================================
@Composable
fun TabNguoi(
    store: SpaStore,
    subTab: Int,
    onChangeSubTab: (Int) -> Unit,
    onGoToAdminPermissionTab: () -> Unit
) {
    val context = LocalContext.current
    val isAdmin = store.currentUser.isAdmin()
    val canManageCustomers = store.currentUser.hasPermission(QuyenHan.CAN_MANAGE_CUSTOMERS)
    val tabs = listOf("Khách hàng (${store.khs.size})", "Danh sách KTV (${store.nvs.size})")
    val activeSubTab = subTab.coerceIn(0, 1)

    var showThemKhDialog by remember { mutableStateOf(false) }
    var khCanSua by remember { mutableStateOf<KH?>(null) }
    var khXemTienDo by remember { mutableStateOf<KH?>(null) }
    var khDangKyGoi by remember { mutableStateOf<KH?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SpaRosewood
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = activeSubTab == idx,
                    onClick = { onChangeSubTab(idx) },
                    text = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (activeSubTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (canManageCustomers) {
                        item {
                            Button(
                                onClick = { showThemKhDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_add_customer")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Thêm Khách hàng mới", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    } else {
                        item {
                            SectionTitle(
                                title = "Danh sách Khách hàng (${store.khs.size})",
                                subtitle = "Nhân viên quan sát khách hàng và kiểm tra tiến độ gói dịch vụ"
                            )
                        }
                    }

                    items(store.khs, key = { it.id }) { kh ->
                        val soGoiDangCo = store.dks.count { it.khId == kh.id }
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .semantics(mergeDescendants = true) {
                                                contentDescription = "Khách hàng ${kh.ten}, số điện thoại ${kh.sdt}, đã mua $soGoiDangCo gói liệu trình."
                                            }
                                    ) {
                                        Text(kh.ten, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(
                                            "SĐT: ${kh.sdt} • Gói đã mua: $soGoiDangCo",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (canManageCustomers) {
                                        Row {
                                            IconButton(
                                                onClick = { khCanSua = kh },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .semantics { contentDescription = "Sửa thông tin khách ${kh.ten}" }
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, tint = SpaRosewood)
                                            }
                                            IconButton(
                                                onClick = { store.xoaKhachHang(kh.id) },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .semantics { contentDescription = "Xóa khách ${kh.ten}" }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = SpaDanger)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (canManageCustomers) {
                                        OutlinedButton(
                                            onClick = { khDangKyGoi = kh },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .semantics { contentDescription = "Mua gói cho khách ${kh.ten}" }
                                        ) {
                                            Text("+ Mua Gói", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Button(
                                        onClick = { khXemTienDo = kh },
                                        colors = ButtonDefaults.buttonColors(containerColor = SpaChampagne),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .semantics { contentDescription = "Xem tiến độ gói của khách ${kh.ten}" }
                                            .testTag("btn_progress_${kh.id}")
                                    ) {
                                        Text("Xem tiến độ gói", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(96.dp)) }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isAdmin) {
                        item {
                            Button(
                                onClick = onGoToAdminPermissionTab,
                                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_goto_admin_account_tab")
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mở Tab Quyền hạn (Tạo TK & Sửa KTV)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    } else {
                        item {
                            SectionTitle(
                                title = "Danh sách Kỹ thuật viên (${store.nvs.size})",
                                subtitle = "Chỉ Admin mới có quyền tạo tài khoản nhân viên mới hoặc sửa thông tin"
                            )
                        }
                    }

                    items(store.nvs, key = { it.id }) { nv ->
                        val linkedTk = store.taiKhoans.firstOrNull { it.nvId == nv.id }
                        val coMacAdmin = linkedTk.isAdmin()
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = "Nhân viên ${nv.ten}, giới tính ${nv.gt}, số điện thoại đăng nhập ${linkedTk?.username ?: nv.sdt}${if (coMacAdmin) ", có mác Admin" else ""}."
                                        }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${nv.ten} (${nv.gt})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        if (coMacAdmin) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = SpaChampagne,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Admin",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        "SĐT đăng nhập: ${linkedTk?.username ?: nv.sdt}",
                                        fontSize = 13.sp,
                                        color = SpaRosewood
                                    )
                                    if (nv.gc.isNotBlank()) {
                                        Text(
                                            nv.gc,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(96.dp)) }
                }
            }
        }
    }

    if (showThemKhDialog) {
        var ten by remember { mutableStateOf("") }
        var sdt by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showThemKhDialog = false },
            title = { Text("Thêm Khách hàng", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = ten, onValueChange = { ten = it }, label = { Text("Tên khách hàng") }, singleLine = true)
                    OutlinedTextField(
                        value = sdt,
                        onValueChange = { sdt = it },
                        label = { Text("Số điện thoại") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ten.isNotBlank()) {
                            store.themKhachHang(ten, sdt)
                            showThemKhDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                    modifier = Modifier.height(48.dp)
                ) { Text("Lưu khách hàng") }
            },
            dismissButton = { TextButton(onClick = { showThemKhDialog = false }) { Text("Hủy") } }
        )
    }

    khCanSua?.let { kh ->
        var tenMoi by remember { mutableStateOf(kh.ten) }
        var sdtMoi by remember { mutableStateOf(kh.sdt) }
        AlertDialog(
            onDismissRequest = { khCanSua = null },
            title = { Text("Sửa thông tin Khách hàng", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = tenMoi, onValueChange = { tenMoi = it }, label = { Text("Tên khách hàng") }, singleLine = true)
                    OutlinedTextField(
                        value = sdtMoi,
                        onValueChange = { sdtMoi = it },
                        label = { Text("Số điện thoại") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        store.suaKhachHang(kh.id, tenMoi, sdtMoi)
                        khCanSua = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                    modifier = Modifier.height(48.dp)
                ) { Text("Lưu thay đổi") }
            },
            dismissButton = { TextButton(onClick = { khCanSua = null }) { Text("Hủy") } }
        )
    }

    khDangKyGoi?.let { kh ->
        var selectedGoiId by remember { mutableStateOf(store.gois.firstOrNull()?.id ?: "") }
        AlertDialog(
            onDismissRequest = { khDangKyGoi = null },
            title = { Text("Mua Gói cho ${kh.ten}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    store.gois.forEach { goi ->
                        FilterChip(
                            selected = selectedGoiId == goi.id,
                            onClick = { selectedGoiId = goi.id },
                            label = { Text("${goi.ten} (${goi.soBuoi} buổi - ${formatVnd(goi.gia)})") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        store.dangKyGoiChoKhach(kh.id, selectedGoiId)
                        khDangKyGoi = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                    modifier = Modifier.height(48.dp)
                ) { Text("Xác nhận mua gói") }
            },
            dismissButton = { TextButton(onClick = { khDangKyGoi = null }) { Text("Đóng") } }
        )
    }

    khXemTienDo?.let { kh ->
        val dsDk = store.dks.filter { it.khId == kh.id }
        AlertDialog(
            onDismissRequest = { khXemTienDo = null },
            title = { Text("Tiến độ Gói: ${kh.ten}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (dsDk.isEmpty()) {
                        Text("Khách hàng chưa đăng ký gói liệu trình nào.")
                    } else {
                        dsDk.forEach { dk ->
                            val goi = store.gois.firstOrNull { it.id == dk.goiId }
                            if (goi != null) {
                                val conLai = (goi.soBuoi - dk.luotDaDung).coerceAtLeast(0)
                                val progress = (dk.luotDaDung.toFloat() / goi.soBuoi.coerceAtLeast(1)).coerceIn(0f, 1f)
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(goi.ten, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                            if (canManageCustomers) {
                                                IconButton(
                                                    onClick = { store.xoaDangKyGoiKhach(dk.id) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Xóa gói đăng ký", tint = SpaDanger)
                                                }
                                            }
                                        }
                                        Text(
                                            "Đã dùng: ${dk.luotDaDung}/${goi.soBuoi} buổi • Còn lại: $conLai buổi",
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = SpaSageGreen
                                        )
                                        if (canManageCustomers) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Sửa nhanh số buổi đã dùng:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            store.suaDangKyGoiKhach(dk.id, (dk.luotDaDung - 1).coerceAtLeast(0))
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(36.dp)
                                                    ) {
                                                        Text("-1 buổi", fontSize = 12.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    OutlinedButton(
                                                        onClick = {
                                                            store.suaDangKyGoiKhach(dk.id, (dk.luotDaDung + 1).coerceAtMost(goi.soBuoi))
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(36.dp)
                                                    ) {
                                                        Text("+1 buổi", fontSize = 12.sp)
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                val msg = buildString {
                                                    appendLine("KHIẾM THỊ VIỆT - TIẾN ĐỘ GÓI LIỆU TRÌNH")
                                                    appendLine("Khách hàng: ${kh.ten} (${kh.sdt})")
                                                    appendLine("Gói: ${goi.ten}")
                                                    appendLine("Đã dùng: ${dk.luotDaDung}/${goi.soBuoi} buổi (Còn lại $conLai buổi)")
                                                    appendLine("Đặt lịch tại cơ sở & tại nhà: 0867.079.882")
                                                }.trim()
                                                sharePlainText(context, "Tiến độ gói - ${kh.ten}", msg)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Gửi Zalo báo tiến độ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { khXemTienDo = null }) { Text("Đóng") }
            }
        )
    }
}

// =========================================================================
// TAB 2: DỊCH VỤ & GÓI LIỆU TRÌNH (NHÂN VIÊN CHỈ QUAN SÁT, ADMIN SỬA/XÓA)
// =========================================================================
@Composable
fun TabMenuVaGoi(store: SpaStore) {
    val context = LocalContext.current
    val canManage = store.currentUser.hasPermission(QuyenHan.CAN_MANAGE_SERVICES)
    var editingDv by remember { mutableStateOf<DV?>(null) }
    var showDvDialog by remember { mutableStateOf(false) }
    var editingGoi by remember { mutableStateOf<Goi?>(null) }
    var showGoiDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Bảng giá Dịch vụ & Gói",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SpaRosewoodDark
                            )
                            Text(
                                "Phục vụ tại nhà: 0867.079.882",
                                fontSize = 13.sp,
                                color = SpaRosewood
                            )
                        }
                        Button(
                            onClick = {
                                val menuCard = buildString {
                                    appendLine("BẢNG GIÁ KHIẾM THỊ VIỆT")
                                    appendLine("DỊCH VỤ LẺ:")
                                    store.dvs.forEachIndexed { idx, dv ->
                                        appendLine("${idx + 1}. ${dv.ten} (${dv.phut}p): ${formatVnd(dv.gia)}")
                                    }
                                    appendLine()
                                    appendLine("GÓI LIỆU TRÌNH:")
                                    store.gois.forEachIndexed { idx, g ->
                                        appendLine("${idx + 1}. ${g.ten} (${g.soBuoi} buổi): ${formatVnd(g.gia)}")
                                    }
                                    appendLine("Phục vụ tại nhà: 0867.079.882")
                                }.trim()
                                sharePlainText(context, "Bảng giá Khiếm Thị Việt", menuCard)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .semantics { contentDescription = "Chia sẻ bảng giá qua Zalo hoặc tin nhắn" }
                                .testTag("btn_share_menu")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chia sẻ giá")
                        }
                    }

                    if (canManage) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    editingDv = null
                                    showDvDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("+ Thêm Dịch vụ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    editingGoi = null
                                    showGoiDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("+ Tạo Gói", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                title = "Dịch vụ lẻ (${store.dvs.size})",
                subtitle = if (canManage) "Bấm biểu tượng bút để sửa giá/hoa hồng hoặc thùng rác để xóa" else "Chế độ quan sát bảng giá dịch vụ"
            )
        }

        items(store.dvs, key = { it.id }) { dv ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Dịch vụ ${dv.ten}, thời lượng ${dv.phut} phút, giá ${formatVndSpeakable(dv.gia)}, hoa hồng KTV ${formatVndSpeakable(dv.chietKhauVnd)}."
                            }
                    ) {
                        Text(dv.ten, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "Giá: ${formatVnd(dv.gia)} • Hoa hồng KTV: ${formatVnd(dv.chietKhauVnd)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpaRosewood
                        )
                    }
                    if (canManage) {
                        Row {
                            IconButton(
                                onClick = {
                                    editingDv = dv
                                    showDvDialog = true
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .semantics { contentDescription = "Sửa dịch vụ ${dv.ten}" }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = SpaRosewood)
                            }
                            IconButton(
                                onClick = { store.xoaDichVu(dv.id) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .semantics { contentDescription = "Xóa dịch vụ ${dv.ten}" }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = SpaDanger)
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                title = "Gói liệu trình (${store.gois.size})",
                subtitle = if (canManage) "Mua trọn gói nhiều buổi • Bấm bút để sửa giá/số buổi/tặng kèm" else "Mua trọn gói nhiều buổi"
            )
        }

        items(store.gois, key = { it.id }) { goi ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dvTangKemName = store.dvs.firstOrNull { it.id == goi.dvTangKem }?.ten ?: ""
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "${goi.ten}, gồm ${goi.soBuoi} buổi${if (goi.chiTietLuot.isNotBlank()) " (${goi.chiTietLuot})" else ""}, giá trọn gói ${formatVndSpeakable(goi.gia)}${if (dvTangKemName.isNotBlank()) ", tặng kèm $dvTangKemName" else ""}."
                            }
                    ) {
                        Text(goi.ten, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "${goi.soBuoi} buổi${if (goi.chiTietLuot.isNotBlank()) " (${goi.chiTietLuot})" else ""} • Trọn gói: ${formatVnd(goi.gia)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpaSageGreen
                        )
                        if (dvTangKemName.isNotBlank()) {
                            Text(
                                "🎁 Tặng kèm mỗi buổi (Vẫn tính hoa hồng KTV): $dvTangKemName",
                                fontSize = 12.sp,
                                color = SpaRosewood
                            )
                        }
                    }
                    if (canManage) {
                        Row {
                            IconButton(
                                onClick = {
                                    editingGoi = goi
                                    showGoiDialog = true
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .semantics { contentDescription = "Sửa ${goi.ten}" }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = SpaRosewood)
                            }
                            IconButton(
                                onClick = { store.xoaGoiLieuTrinh(goi.id) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .semantics { contentDescription = "Xóa ${goi.ten}" }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = SpaDanger)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    if (showDvDialog) {
        var ten by remember { mutableStateOf(editingDv?.ten ?: "") }
        var phut by remember { mutableStateOf((editingDv?.phut ?: 60).toString()) }
        var gia by remember { mutableStateOf((editingDv?.gia ?: 180000L).toString()) }
        var chietKhau by remember { mutableStateOf((editingDv?.chietKhauVnd ?: 90000L).toString()) }
        var moTa by remember { mutableStateOf(editingDv?.moTa ?: "") }

        AlertDialog(
            onDismissRequest = { showDvDialog = false },
            title = { Text(if (editingDv == null) "Thêm Dịch vụ" else "Sửa Dịch vụ", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Lưu ý: Khi thay đổi Giá vé hoặc Hoa hồng KTV (VNĐ), hệ thống sẽ áp dụng mức mới từ thời điểm lưu trở đi.",
                        fontSize = 12.sp,
                        color = SpaSageGreen
                    )
                    OutlinedTextField(value = ten, onValueChange = { ten = it }, label = { Text("Tên dịch vụ") }, singleLine = true)
                    OutlinedTextField(
                        value = phut,
                        onValueChange = { phut = it.filter { c -> c.isDigit() } },
                        label = { Text("Số phút") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = gia,
                        onValueChange = { gia = it.filter { c -> c.isDigit() } },
                        label = { Text("Giá vé gốc (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = chietKhau,
                        onValueChange = { chietKhau = it.filter { c -> c.isDigit() } },
                        label = { Text("Hoa hồng KTV (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ten.isNotBlank()) {
                            store.luuDichVu(
                                id = editingDv?.id,
                                ten = ten,
                                phut = phut.toIntOrNull() ?: 60,
                                gia = gia.toLongOrNull() ?: 0L,
                                moTa = moTa,
                                chietKhauVnd = chietKhau.toLongOrNull() ?: 0L
                            )
                            showDvDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood)
                ) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showDvDialog = false }) { Text("Hủy") } }
        )
    }

    if (showGoiDialog) {
        var ten by remember(editingGoi?.id) { mutableStateOf(editingGoi?.ten ?: "") }
        var soBuoi by remember(editingGoi?.id) { mutableStateOf((editingGoi?.soBuoi ?: 10).toString()) }
        var gia by remember(editingGoi?.id) { mutableStateOf((editingGoi?.gia ?: 1500000L).toString()) }
        var dvTangKemId by remember(editingGoi?.id) { mutableStateOf(editingGoi?.dvTangKem ?: "") }
        val chosenDvs = remember(editingGoi?.id) {
            mutableStateListOf<String>().apply {
                if (editingGoi != null && editingGoi!!.dvIds.isNotEmpty()) {
                    addAll(editingGoi!!.dvIds)
                } else {
                    store.dvs.firstOrNull()?.id?.let { add(it) }
                }
            }
        }
        AlertDialog(
            onDismissRequest = { showGoiDialog = false },
            title = { Text(if (editingGoi == null) "Tạo Gói liệu trình" else "Sửa Gói liệu trình", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = ten, onValueChange = { ten = it }, label = { Text("Tên gói") }, singleLine = true)
                    OutlinedTextField(
                        value = soBuoi,
                        onValueChange = { soBuoi = it.filter { c -> c.isDigit() } },
                        label = { Text("Số buổi") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = gia,
                        onValueChange = { gia = it.filter { c -> c.isDigit() } },
                        label = { Text("Giá trọn gói (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Text("Dịch vụ chính trong gói:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    store.dvs.forEach { dv ->
                        val checked = chosenDvs.contains(dv.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (checked) chosenDvs.remove(dv.id) else chosenDvs.add(dv.id)
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = checked, onCheckedChange = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${dv.ten} (CK: ${formatVnd(dv.chietKhauVnd)})", fontSize = 13.sp)
                        }
                    }
                    Text("🎁 Dịch vụ tặng kèm mỗi buổi (Vẫn tính hoa hồng KTV):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = dvTangKemId.isBlank(),
                            onClick = { dvTangKemId = "" },
                            label = { Text("Không tặng kèm", fontSize = 12.sp) }
                        )
                        store.dvs.forEach { dv ->
                            FilterChip(
                                selected = dvTangKemId == dv.id,
                                onClick = { dvTangKemId = dv.id },
                                label = { Text("🎁 ${dv.ten}", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ten.isNotBlank()) {
                            store.luuGoiLieuTrinh(
                                id = editingGoi?.id,
                                ten = ten,
                                dvIds = chosenDvs.toList(),
                                soBuoi = soBuoi.toIntOrNull() ?: 5,
                                gia = gia.toLongOrNull() ?: 0L,
                                chiTietLuot = editingGoi?.chiTietLuot ?: "",
                                dvTangKem = dvTangKemId
                            )
                            showGoiDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood)
                ) { Text(if (editingGoi == null) "Tạo gói" else "Lưu thay đổi") }
            },
            dismissButton = { TextButton(onClick = { showGoiDialog = false }) { Text("Hủy") } }
        )
    }
}

// =========================================================================
// TAB 3: SỔ QUỸ THU / CHI
// =========================================================================
@Composable
fun TabSoQuy(store: SpaStore) {
    val canManageFinance = store.currentUser.hasPermission(QuyenHan.CAN_MANAGE_FINANCE)
    var showAddTcDialog by remember { mutableStateOf(false) }
    var editingTc by remember { mutableStateOf<TC?>(null) }

    if (!canManageFinance) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Sổ quỹ đã khóa. Tài khoản nhân viên chỉ được quan sát khách, gói dịch vụ và tạo vé. Cần Admin cấp quyền Quản lý sổ quỹ trong tab Quyền hạn."
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SpaDanger, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Khóa quyền Sổ quỹ Thu / Chi", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Tài khoản của bạn chưa được cấp quyền xem và ghi Sổ quỹ. Vui lòng liên hệ Admin để mở quyền trong Tab Quyền hạn.",
                        fontSize = 14.sp
                    )
                }
            }
        }
        return
    }

    val danhSachThuNgoai = remember(store.tcs.size, store.tcs.map { it.tien }.sum()) {
        store.tcs.filter { it.loai == LoaiTC.THU && !store.laPhieuThuTuVe(it) }
    }
    val danhSachChiNgoai = remember(store.tcs.size, store.tcs.map { it.tien }.sum()) {
        store.tcs.filter { it.loai == LoaiTC.CHI && !store.laPhieuChiHoaHongKtv(it) }
    }

    val tongThuNgoai = danhSachThuNgoai.sumOf { it.tien }
    val tongChiNgoai = danhSachChiNgoai.sumOf { it.tien }

    var initialLoaiAdd by remember { mutableStateOf(LoaiTC.CHI) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // THANH ĐIỀU HÀNH & NÚT NHẬP THU / CHI NGOÀI LỀ
        item(key = "so_quy_header_action") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Sổ quỹ thu chi ngoài lề. Hiện có ${danhSachThuNgoai.size} khoản thu ngoài tổng cộng ${formatVndSpeakable(tongThuNgoai)}, và ${danhSachChiNgoai.size} khoản chi ngoài tổng cộng ${formatVndSpeakable(tongChiNgoai)}."
                            }
                    ) {
                        Text(
                            "SỔ QUỸ NGOÀI LỀ",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = SpaRosewoodDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Quản lý thu chi ngoài lề (bán mỹ phẩm, tiền phòng, nước uống, điện nước, vật tư...). Không cộng dồn vé làm tại quán để tránh trùng lặp.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 2 NÚT THÊM THU NGOÀI & THÊM CHI NGOÀI RÕ RÀNG
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                initialLoaiAdd = LoaiTC.THU
                                showAddTcDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpaSageGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .semantics { contentDescription = "Nhập khoản thu ngoài lề mới" }
                                .testTag("btn_add_income_outside")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Thu ngoài", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                initialLoaiAdd = LoaiTC.CHI
                                showAddTcDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpaDanger),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .semantics { contentDescription = "Nhập khoản chi ngoài lề mới" }
                                .testTag("btn_add_expense_outside")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("- Chi ngoài", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // BỐ CỤC 2 CỘT THU NGOÀI VÀ CHI NGOÀI GỌN GÀNG, CÂN ĐỐI
        item(key = "two_column_tables") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CỘT 1: THU NGOÀI LỀ
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = SpaSageGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SpaSageGreen.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "THU NGOÀI LỀ (${danhSachThuNgoai.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SpaSageGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+${formatVnd(tongThuNgoai)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SpaSageGreen
                            )
                        }
                    }

                    if (danhSachThuNgoai.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Chưa có khoản thu ngoài lề nào",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        danhSachThuNgoai.forEach { tc ->
                            SoQuyCompactCard(
                                tc = tc,
                                onEdit = { editingTc = tc },
                                onDelete = { store.xoaGiaoDichSoQuy(tc.id) }
                            )
                        }
                    }
                }

                // CỘT 2: CHI NGOÀI LỀ
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = SpaDanger.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SpaDanger.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "CHI NGOÀI LỀ (${danhSachChiNgoai.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SpaDanger
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "-${formatVnd(tongChiNgoai)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SpaDanger
                            )
                        }
                    }

                    if (danhSachChiNgoai.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Chưa có khoản chi ngoài lề nào",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        danhSachChiNgoai.forEach { tc ->
                            SoQuyCompactCard(
                                tc = tc,
                                onEdit = { editingTc = tc },
                                onDelete = { store.xoaGiaoDichSoQuy(tc.id) }
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    if (showAddTcDialog) {
        var loai by remember { mutableStateOf(initialLoaiAdd) }
        var tien by remember { mutableStateOf("") }
        var lyDo by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTcDialog = false },
            title = {
                Text(
                    if (loai == LoaiTC.THU) "Ghi nhận Khoản Thu Ngoài" else "Ghi nhận Khoản Chi Ngoài",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = loai == LoaiTC.THU,
                            onClick = { loai = LoaiTC.THU },
                            label = { Text("+ Khoản Thu Ngoài") }
                        )
                        FilterChip(
                            selected = loai == LoaiTC.CHI,
                            onClick = { loai = LoaiTC.CHI },
                            label = { Text("- Khoản Chi Ngoài") }
                        )
                    }
                    OutlinedTextField(
                        value = tien,
                        onValueChange = { tien = it.filter { c -> c.isDigit() } },
                        label = { Text("Số tiền (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lyDo,
                        onValueChange = { lyDo = it },
                        label = { Text("Ghi chú / Lý do (tự viết)") },
                        placeholder = { Text("VD: Bán kem massage, tiền điện, mua chè...") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = tien.toLongOrNull() ?: 0L
                        if (amount > 0 && lyDo.isNotBlank()) {
                            store.themGiaoDichSoQuy(loai, amount, lyDo)
                            showAddTcDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (loai == LoaiTC.THU) SpaSageGreen else SpaDanger)
                ) { Text("Lưu khoản ngoài") }
            },
            dismissButton = { TextButton(onClick = { showAddTcDialog = false }) { Text("Hủy") } }
        )
    }

    editingTc?.let { tc ->
        var loaiMoi by remember(tc.id) { mutableStateOf(tc.loai) }
        var tienMoi by remember(tc.id) { mutableStateOf(tc.tien.toString()) }
        var lyDoMoi by remember(tc.id) { mutableStateOf(tc.lyDo) }
        var ngayMoi by remember(tc.id) { mutableStateOf(tc.ngay) }
        AlertDialog(
            onDismissRequest = { editingTc = null },
            title = { Text("Sửa khoản ngoài lề", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = loaiMoi == LoaiTC.THU, onClick = { loaiMoi = LoaiTC.THU }, label = { Text("+ Khoản Thu") })
                        FilterChip(selected = loaiMoi == LoaiTC.CHI, onClick = { loaiMoi = LoaiTC.CHI }, label = { Text("- Khoản Chi") })
                    }
                    OutlinedTextField(
                        value = tienMoi,
                        onValueChange = { tienMoi = it.filter { c -> c.isDigit() } },
                        label = { Text("Số tiền (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lyDoMoi,
                        onValueChange = { lyDoMoi = it },
                        label = { Text("Ghi chú / Lý do") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ngayMoi,
                        onValueChange = { ngayMoi = it },
                        label = { Text("Ngày (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = tienMoi.toLongOrNull() ?: tc.tien
                        store.suaGiaoDichSoQuy(tc.id, loaiMoi, amount, lyDoMoi, ngayMoi)
                        editingTc = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood)
                ) { Text("Lưu thay đổi") }
            },
            dismissButton = { TextButton(onClick = { editingTc = null }) { Text("Hủy") } }
        )
    }
}

@Composable
private fun SoQuyCompactCard(
    tc: TC,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isThu = tc.loai == LoaiTC.THU
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, if (isThu) SpaSageGreen.copy(alpha = 0.35f) else SpaDanger.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Khoản ${tc.loai.label} ngày ${tc.ngay}, số tiền ${formatVndSpeakable(tc.tien)}, ghi chú: ${tc.lyDo}."
                },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tc.ngay,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = (if (isThu) "+" else "-") + formatVnd(tc.tien),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = if (isThu) SpaSageGreen else SpaDanger
                )
            }

            Text(
                text = tc.lyDo,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .semantics { contentDescription = "Sửa khoản ngoài ${tc.lyDo}" }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = SpaRosewood, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .semantics { contentDescription = "Xóa khoản ngoài ${tc.lyDo}" }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = SpaDanger, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SoQuyTransactionRow(
    tc: TC,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isThu = tc.loai == LoaiTC.THU
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Khoản ${tc.loai.label} ngày ${tc.ngay}, số tiền ${formatVndSpeakable(tc.tien)}, lý do: ${tc.lyDo}."
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${tc.loai.label.uppercase()} • ${tc.ngay}",
                        color = if (isThu) SpaSageGreen else SpaDanger,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = (if (isThu) "+" else "-") + formatVnd(tc.tien),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (isThu) SpaSageGreen else SpaDanger
                    )
                }
                Text(tc.lyDo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "Sửa giao dịch ${tc.lyDo}" }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = SpaRosewood)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "Xóa giao dịch ${tc.lyDo}" }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = SpaDanger)
                }
            }
        }
    }
}

// =========================================================================
// TAB 4: BÁO CÁO Ô LỊCH DOANH THU RÒNG, THU NHẬP KTV CHI TIẾT & BẢNG KHÁCH HÀNG + TÌM KIẾM GEHIHI
// =========================================================================
@Composable
fun TabDoanhThuBaoCao(
    store: SpaStore,
    initialInspectNvId: String?,
    onClearInspectNvId: () -> Unit
) {
    val canViewRevenue = store.currentUser.hasPermission(QuyenHan.CAN_VIEW_REVENUE)
    var filter by rememberSaveable { mutableStateOf(BoLocThoiGian.THANG) }
    var refDate by rememberSaveable { mutableStateOf(todayDateStr()) }
    var selectedCellKey by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedNvIdForDetail by rememberSaveable { mutableStateOf<String?>(null) }
    var customerSearchQuery by rememberSaveable { mutableStateOf("") }

    if (initialInspectNvId != null) {
        selectedNvIdForDetail = initialInspectNvId
        onClearInspectNvId()
    }

    if (!canViewRevenue) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Báo cáo đã khóa. Cần tài khoản có mác Admin hoặc được cấp quyền Xem báo cáo."
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SpaDanger, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Khóa quyền Xem Báo cáo & Bảng lương", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Tài khoản của bạn chưa được cấp quyền xem Báo cáo. Vui lòng liên hệ Admin để bật quyền trong Tab Quyền hạn.",
                        fontSize = 14.sp
                    )
                }
            }
        }
        return
    }

    val context = LocalContext.current
    val oLichList = store.layDanhSachOLichBaoCao(filter, refDate, selectedCellKey)
    val selectedCell = oLichList.firstOrNull { it.keyDate == selectedCellKey }
    val activePeriodLabel = selectedCell?.moTaDayDu ?: formatPeriodLabel(refDate, filter)

    fun dateMatchesActiveScope(dateStr: String): Boolean {
        return store.khopThoiGianBaoCao(dateStr, filter, refDate, selectedCellKey)
    }

    val veDaChot = store.luots.filter { it.daXacNhan && dateMatchesActiveScope(it.ngay) }
    val thuTuVeLe = veDaChot.sumOf { it.thu }
    // Chỉ cộng các phiếu THU ngoài vé (bán gói liệu trình, thu khác) để không tính trùng với thuTuVeLe
    val thuSoQuy = store.tcs.filter {
        it.loai == LoaiTC.THU && !store.laPhieuThuTuVe(it) && dateMatchesActiveScope(it.ngay)
    }.sumOf { it.tien }
    val tongDoanhThu = thuTuVeLe + thuSoQuy

    val bangLuongList = store.layBangLuongChiTiet(filter, refDate, selectedCellKey)
    val tongHoaHongTatCaKtv = bangLuongList.sumOf { it.tongHoaHong }
    val chiKhacNgoaiKtv = store.tcs.filter {
        it.loai == LoaiTC.CHI && !store.laPhieuChiHoaHongKtv(it) && dateMatchesActiveScope(it.ngay)
    }.sumOf { it.tien }
    val tongChiPhi = tongHoaHongTatCaKtv + chiKhacNgoaiKtv
    // Chủ quán thực nhận (Lãi ròng) = Tổng Thu - (Hoa hồng trả KTV + Chi phí khác)
    val doanhThuSauTruHetPhi = tongDoanhThu - tongChiPhi

    val allKhachStats = store.layThongKeKhachHang(filter, refDate, selectedCellKey, "")
    val filteredKhachStats = store.layThongKeKhachHang(filter, refDate, selectedCellKey, customerSearchQuery)
    val gehihiSuggestionText = remember(customerSearchQuery, store.khs.size) {
        store.goiYTimKiemKhachHangGehihi(customerSearchQuery)
    }
    val gehihiSuggestedKhs = remember(customerSearchQuery, filteredKhachStats) {
        if (customerSearchQuery.isBlank()) emptyList() else filteredKhachStats.take(4).map { it.kh }
    }

    val gridColumns = when (filter) {
        BoLocThoiGian.NGAY -> 1
        BoLocThoiGian.THANG -> 4
        BoLocThoiGian.NAM -> 3
        BoLocThoiGian.TAT_CA -> 2
    }

    var cellDialogItem by remember { mutableStateOf<OLichBaoCaoItem?>(null) }
    var showDoanhThuDialog by remember { mutableStateOf(false) }
    var showChiPhiDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. BỘ LỌC KỲ THỜI GIAN (1 NGÀY / 1 THÁNG / 1 NĂM / TỪ TRƯỚC ĐẾN GIỜ)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Khoảng thời gian Báo cáo Ô lịch, Thu nhập KTV & Khách hàng:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BoLocThoiGian.entries.forEach { item ->
                            FilterChip(
                                selected = filter == item,
                                onClick = {
                                    filter = item
                                    selectedCellKey = null
                                },
                                label = { Text(item.label, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                modifier = Modifier
                                    .semantics {
                                        contentDescription = "Chế độ xem ${item.label}"
                                    }
                                    .testTag("chip_report_period_${item.name}")
                            )
                        }
                    }

                    if (filter != BoLocThoiGian.TAT_CA) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    refDate = shiftReferenceDate(refDate, filter, -1)
                                    selectedCellKey = null
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .semantics { contentDescription = "Lùi về kỳ trước" }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                                Text("Trước", fontSize = 13.sp)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        refDate = todayDateStr()
                                        selectedCellKey = null
                                    }
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "Đang xem ${formatPeriodLabel(refDate, filter)}. Chạm đúp để quay về hôm nay."
                                    }
                            ) {
                                Text(
                                    text = formatPeriodLabel(refDate, filter),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = SpaRosewoodDark
                                )
                                Text(
                                    text = "Chạm để về hiện tại",
                                    fontSize = 11.sp,
                                    color = SpaRosewood
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    refDate = shiftReferenceDate(refDate, filter, 1)
                                    selectedCellKey = null
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .semantics { contentDescription = "Tiến sang kỳ tiếp theo" }
                            ) {
                                Text("Sau", fontSize = 13.sp)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }

        // 2. BẢNG TỔNG KẾT 2 Ô CÂN ĐỐI 1:1: Ô DOANH THU & Ô CHI
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Tổng kết $activePeriodLabel: Doanh thu thực nhận sau khi trừ hết phí là ${formatVndSpeakable(doanhThuSauTruHetPhi)}. Tổng chi phí là ${formatVndSpeakable(tongChiPhi)}, gồm hoa hồng KTV ${formatVndSpeakable(tongHoaHongTatCaKtv)} và chi phí khác ${formatVndSpeakable(chiKhacNgoaiKtv)}."
                    }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "TỔNG KẾT DOANH THU ($activePeriodLabel)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = SpaRosewoodDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Ô 1: DOANH THU THỰC NHẬN (Lãi sau khi trừ hết phí)
                        Surface(
                            color = if (doanhThuSauTruHetPhi >= 0) SpaSageGreen.copy(alpha = 0.14f) else SpaDanger.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, if (doanhThuSauTruHetPhi >= 0) SpaSageGreen else SpaDanger),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDoanhThuDialog = true }
                                .semantics {
                                    contentDescription = "Doanh thu chủ quán thực nhận: ${formatVndSpeakable(doanhThuSauTruHetPhi)}. Nhấn để mở bảng chi tiết."
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "DOANH THU (CHỦ NHẬN)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SpaRosewoodDark,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (doanhThuSauTruHetPhi >= 0) "+${formatVnd(doanhThuSauTruHetPhi)}" else "-${formatVnd(-doanhThuSauTruHetPhi)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = if (doanhThuSauTruHetPhi >= 0) SpaSageGreen else SpaDanger,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Đã trừ hết phí • Chạm xem",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Ô 2: TỔNG CHI PHÍ (Gồm cả trích hoa hồng cho KTV và các chi phí vặt)
                        Surface(
                            color = SpaDanger.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, SpaDanger.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showChiPhiDialog = true }
                                .semantics {
                                    contentDescription = "Tổng chi phí: ${formatVndSpeakable(tongChiPhi)}. Nhấn để mở bảng chi tiết."
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "TỔNG CHI PHÍ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SpaDanger,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "-${formatVnd(tongChiPhi)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = SpaDanger,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Gồm hoa hồng & chi vặt • Chạm xem",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Ô LỊCH LÃI RÒNG CHỦ QUÁN (CHẠM VÀO Ô ĐỂ LỌC NHANH)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_grid_report_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ô LỊCH CHỦ QUÁN THỰC NHẬN",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = SpaRosewoodDark
                            )
                            Text(
                                text = if (selectedCell != null) {
                                    "Đang lọc: ${selectedCell.moTaDayDu} (Chạm lại ô để bỏ lọc)"
                                } else {
                                    "Chạm vào ô ngày/tháng bất kỳ để lọc chi tiết bên dưới"
                                },
                                fontSize = 12.sp,
                                color = if (selectedCell != null) SpaRosewood else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (selectedCell != null) {
                            TextButton(
                                onClick = { selectedCellKey = null },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Xem toàn kỳ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    val chunkedCells = oLichList.chunked(gridColumns)
                    chunkedCells.forEach { rowCells ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCells.forEach { cell ->
                                val isSelected = selectedCellKey == cell.keyDate
                                val hasActivity = cell.soVeDaChot > 0 || cell.tongThu > 0L || cell.tongPhiChi > 0L
                                val netColor = when {
                                    cell.doanhThuSauTruPhi > 0L -> SpaSageGreen
                                    cell.doanhThuSauTruPhi < 0L -> SpaDanger
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                val bgColor = when {
                                    isSelected -> SpaRosewood.copy(alpha = 0.16f)
                                    hasActivity -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                }
                                val borderColor = when {
                                    isSelected -> SpaRosewoodDark
                                    hasActivity -> SpaChampagne
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 64.dp)
                                        .background(bgColor, RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = borderColor,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedCellKey = cell.keyDate
                                            cellDialogItem = cell
                                        }
                                        .padding(horizontal = 6.dp, vertical = 8.dp)
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = "${cell.moTaDayDu}: ${cell.soVeDaChot} vé, thu ${formatVndSpeakable(cell.tongThu)}, chi ${formatVndSpeakable(cell.tongPhiChi)}, chủ nhận ${formatVndSpeakable(cell.doanhThuSauTruPhi)}. Chạm đúp để mở cửa sổ chi tiết."
                                        }
                                        .testTag("calendar_cell_${cell.keyDate}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = cell.tieuDeO,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) SpaRosewoodDark else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (hasActivity) formatVndCompact(cell.doanhThuSauTruPhi) else "0đ",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = if (gridColumns >= 4) 12.sp else 14.sp,
                                            color = netColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (cell.soVeDaChot > 0) {
                                            Text(
                                                text = "${cell.soVeDaChot} vé",
                                                fontSize = 10.sp,
                                                color = SpaRosewood
                                            )
                                        }
                                    }
                                }
                            }
                            val emptySlots = gridColumns - rowCells.size
                            repeat(emptySlots) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 4. DỮ LIỆU THU NHẬP CỦA NHÂN VIÊN KÈM CHI TIẾT LÀM DỊCH VỤ NÀO & HOA HỒNG NHƯ NÀO
        item {
            SectionTitle(
                title = "Thu nhập Nhân viên & Chi tiết Dịch vụ / Hoa hồng ($activePeriodLabel)",
                subtitle = "Hiển thị trực tiếp từng KTV làm dịch vụ nào, hoa hồng bao nhiêu trong 1 ngày, 1 tháng, 1 năm hoặc từ trước đến giờ"
            )
        }

        items(bangLuongList, key = { it.nv.id }) { item ->
            val dichVuGroupMap = item.danhSachLuot.groupBy { it.tenDichVu }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payroll_card_${item.nv.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                val chiTietText = if (dichVuGroupMap.isEmpty()) {
                                    "Chưa có lượt làm nào đã chốt."
                                } else {
                                    dichVuGroupMap.entries.joinToString("; ") { (tenDv, ds) ->
                                        "$tenDv: ${ds.size} lượt, hoa hồng ${formatVndSpeakable(ds.sumOf { l -> l.hoaHongKtv })}"
                                    }
                                }
                                contentDescription = "Nhân viên ${item.nv.ten} (${item.nv.sdt}), trong $activePeriodLabel đã làm ${item.tongSoLuot} lượt (${item.tongPhutLam} phút), tổng thu nhập hoa hồng ${formatVndSpeakable(item.tongHoaHong)}. Chi tiết dịch vụ: $chiTietText."
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.nv.ten} (${item.nv.sdt})",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = SpaRosewoodDark
                                )
                                Text(
                                    text = "Số lượt đã chốt: ${item.tongSoLuot} lượt • Tổng giờ làm: ${item.tongPhutLam} phút",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatVnd(item.tongHoaHong),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = SpaSageGreen
                            )
                        }

                        if (dichVuGroupMap.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Chi tiết làm dịch vụ nào & hoa hồng nhận được:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SpaRosewoodDark
                                    )
                                    dichVuGroupMap.entries.forEach { (tenDv, dsLuot) ->
                                        val tongHhDv = dsLuot.sumOf { it.hoaHongKtv }
                                        val donGiaHh = dsLuot.firstOrNull()?.hoaHongKtv ?: 0L
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "• $tenDv (${dsLuot.size} lượt × ${formatVnd(donGiaHh)})",
                                                fontSize = 13.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "+${formatVnd(tongHhDv)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SpaSageGreen
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Chưa phát sinh lượt làm đã chốt trong khoảng thời gian này.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { selectedNvIdForDetail = item.nv.id },
                        colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .semantics {
                                contentDescription = "Xem nhật ký từng khung giờ và gửi phiếu lương Zalo của ${item.nv.ten} trong $activePeriodLabel"
                            }
                            .testTag("btn_detail_payroll_${item.nv.id}")
                    ) {
                        Text(
                            "Xem từng ca làm & Gửi phiếu lương (${item.tongSoLuot} lượt)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    // HỘP THOẠI CHI TIẾT DOANH THU THỰC NHẬN (KHI CHẠM VÀO Ô DOANH THU)
    if (showDoanhThuDialog) {
        AlertDialog(
            onDismissRequest = { showDoanhThuDialog = false },
            title = {
                Column(modifier = Modifier.semantics { heading() }) {
                    Text(
                        "Chi tiết Doanh thu ($activePeriodLabel)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SpaRosewoodDark
                    )
                    Text(
                        "Khoản tiền chủ quán thực nhận sau khi trừ hết chi phí",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = SpaSageGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("1. Thu vé hoàn thành (${veDaChot.size} vé):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("+${formatVnd(thuTuVeLe)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SpaSageGreen)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("2. Thu ngoài lề / Bán gói:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("+${formatVnd(thuSoQuy)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SpaSageGreen)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tổng thu gộp:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("+${formatVnd(tongDoanhThu)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = SpaSageGreen)
                            }
                        }
                    }

                    Surface(
                        color = SpaDanger.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng chi phí khấu trừ:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("-${formatVnd(tongChiPhi)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SpaDanger)
                        }
                    }

                    Surface(
                        color = (if (doanhThuSauTruHetPhi >= 0) SpaSageGreen else SpaDanger).copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("THỰC NHẬN:", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = SpaRosewoodDark)
                            Text(
                                if (doanhThuSauTruHetPhi >= 0) "+${formatVnd(doanhThuSauTruHetPhi)}" else "-${formatVnd(-doanhThuSauTruHetPhi)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = if (doanhThuSauTruHetPhi >= 0) SpaSageGreen else SpaDanger
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDoanhThuDialog = false },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Đóng", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // HỘP THOẠI CHI TIẾT TỔNG CHI PHÍ (KHI CHẠM VÀO Ô CHI PHÍ)
    if (showChiPhiDialog) {
        AlertDialog(
            onDismissRequest = { showChiPhiDialog = false },
            title = {
                Column(modifier = Modifier.semantics { heading() }) {
                    Text(
                        "Chi tiết Tổng chi phí ($activePeriodLabel)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SpaDanger
                    )
                    Text(
                        "Bao gồm hoa hồng KTV và mọi khoản chi ngoài lề",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = SpaDanger.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("1. Hoa hồng trả KTV:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("-${formatVnd(tongHoaHongTatCaKtv)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SpaDanger)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("2. Chi khác / Chi ngoài lề:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("-${formatVnd(chiKhacNgoaiKtv)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SpaDanger)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TỔNG CHI:", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = SpaDanger)
                                Text("-${formatVnd(tongChiPhi)}", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = SpaDanger)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showChiPhiDialog = false },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Đóng", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // CỬA SỔ HỘP THOẠI CHI TIẾT 3 MỤC: SỐ VÉ, SỐ TIỀN THU, SỐ TIỀN CHI KHI BẤM VÀO Ô LỊCH
    cellDialogItem?.let { cell ->
        val cellNet = cell.doanhThuSauTruPhi
        AlertDialog(
            onDismissRequest = { cellDialogItem = null },
            title = {
                Column(modifier = Modifier.semantics { heading() }) {
                    Text(
                        "Chi tiết ngày ${cell.tieuDeO}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        cell.moTaDayDu,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // MỤC 1: SỐ VÉ
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("1. Số vé hoàn thành:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${cell.soVeDaChot} vé", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SpaRosewoodDark)
                        }
                    }

                    // MỤC 2: SỐ TIỀN THU
                    Surface(
                        color = SpaSageGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("2. Số tiền thu:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("+${formatVnd(cell.tongThu)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SpaSageGreen)
                        }
                    }

                    // MỤC 3: SỐ TIỀN CHI
                    Surface(
                        color = SpaDanger.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("3. Số tiền chi:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("-${formatVnd(cell.tongPhiChi)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SpaDanger)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chủ nhận thực tế:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = formatVnd(cellNet),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = if (cellNet >= 0) SpaSageGreen else SpaDanger
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { cellDialogItem = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood)
                ) {
                    Text("Đóng")
                }
            }
        )
    }

    val selectedSummary = bangLuongList.firstOrNull { it.nv.id == selectedNvIdForDetail }
    if (selectedSummary != null) {
        val detail = selectedSummary
        AlertDialog(
            onDismissRequest = { selectedNvIdForDetail = null },
            title = {
                Column(modifier = Modifier.semantics { heading() }) {
                    Text(
                        "Chi tiết Lương: ${detail.nv.ten}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "$activePeriodLabel • ${detail.tongSoLuot} lượt • Hoa hồng: ${formatVnd(detail.tongHoaHong)}",
                        fontSize = 13.sp,
                        color = SpaSageGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (detail.danhSachLuot.isEmpty()) {
                        Text(
                            "Trong $activePeriodLabel, nhân viên ${detail.nv.ten} chưa có lượt làm nào đã chốt.",
                            fontSize = 14.sp
                        )
                    } else {
                        detail.danhSachLuot.forEachIndexed { index, luotItem ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "Lượt ${index + 1}: Ngày ${luotItem.ngay}, khung giờ ${luotItem.khungGio} (${luotItem.soPhut} phút). Khách hàng ${luotItem.tenKhach}. Dịch vụ ${luotItem.tenDichVu} (${luotItem.loaiVe}). Hoa hồng nhận được: ${formatVndSpeakable(luotItem.hoaHongKtv)}."
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Lượt #${index + 1} • ${luotItem.ngay}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SpaRosewoodDark
                                        )
                                        Text(
                                            "+${formatVnd(luotItem.hoaHongKtv)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = SpaSageGreen
                                        )
                                    }
                                    Text(
                                        "Giờ làm: ${luotItem.khungGio} (${luotItem.soPhut} phút)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Khách: ${luotItem.tenKhach} • ${luotItem.loaiVe}",
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Dịch vụ: ${luotItem.tenDichVu}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val phieuLuong = buildString {
                            appendLine("PHIẾU LƯƠNG & HOA HỒNG KTV - KHIẾM THỊ VIỆT")
                            appendLine("KTV: ${detail.nv.ten} (${detail.nv.sdt})")
                            appendLine("Kỳ lương: $activePeriodLabel")
                            appendLine("Tổng số lượt: ${detail.tongSoLuot} lượt (${detail.tongPhutLam} phút)")
                            appendLine("Tổng hoa hồng nhận: ${formatVnd(detail.tongHoaHong)}")
                            appendLine("────────────────────")
                            detail.danhSachLuot.forEachIndexed { i, l ->
                                appendLine("${i + 1}. ${l.ngay} (${l.khungGio}): KH ${l.tenKhach} - ${l.tenDichVu} -> Hoa hồng: +${formatVnd(l.hoaHongKtv)}")
                            }
                        }.trim()
                        sharePlainText(context, "Phiếu lương ${detail.nv.ten}", phieuLuong)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gửi phiếu lương Zalo")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedNvIdForDetail = null }) { Text("Đóng") }
            }
        )
    }
}

// =========================================================================
// TAB 5: TAB RIÊNG VỀ QUYỀN HẠN, TÀI KHOẢN & BACKUP DỮ LIỆU (CHỈ ADMIN)
// =========================================================================
@Composable
fun TabQuyenHanAdmin(
    store: SpaStore,
    onOpenGehihi: () -> Unit,
    onOpenAiKeyConfig: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val adminSubTab = store.requestedAdminSubTab
    var showCreateAccountDialog by remember { mutableStateOf(false) }
    var showApkInstallGuideDialog by remember { mutableStateOf(false) }
    var showTalkBackBackupDialog by remember { mutableStateOf(false) }
    var tkBeingEdited by remember { mutableStateOf<TaiKhoan?>(null) }
    var tkConfirmDelete by remember { mutableStateOf<TaiKhoan?>(null) }

    var backupStatusMsg by remember { mutableStateOf("") }
    var pasteJsonInput by remember { mutableStateOf("") }
    var ghiDeToanBoKhiNhap by rememberSaveable { mutableStateOf(true) }
    var wipeStep by remember { mutableIntStateOf(0) }
    var adminPasswordForWipe by remember { mutableStateOf("") }
    var wipeErrorMsg by remember { mutableStateOf<String?>(null) }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val jsonBytes = store.xuatDuLieuBackupJson().toByteArray(Charsets.UTF_8)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonBytes)
                }
                backupStatusMsg = "Đã xuất toàn bộ dữ liệu ra file Backup JSON thành công!"
                Toast.makeText(context, backupStatusMsg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                backupStatusMsg = "Lỗi khi ghi file Backup: ${e.message}"
                Toast.makeText(context, backupStatusMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val rawJson = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                    it.readText()
                } ?: ""
                val result = store.nhapDuLieuTuJsonBackup(rawJson, ghiDeToanBoKhiNhap)
                val resultMsg = result.fold(
                    onSuccess = { it },
                    onFailure = { "Lỗi định dạng file JSON Backup: ${it.message}" }
                )
                backupStatusMsg = resultMsg
                Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                backupStatusMsg = "Lỗi khi đọc file Backup: ${e.message}"
                Toast.makeText(context, backupStatusMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    val isDevUser = store.currentUser.isDev()
    val subTabTitles = if (isDevUser) {
        listOf("Tài khoản NV", "Chủ cơ sở (Admin)", "Sao lưu dữ liệu", "👑 Dev Console")
    } else {
        listOf("Tài khoản NV", "Chủ cơ sở (Admin)", "Sao lưu dữ liệu")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = adminSubTab.coerceIn(0, subTabTitles.lastIndex),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SpaRosewoodDark,
            edgePadding = 12.dp
        ) {
            subTabTitles.forEachIndexed { idx, title ->
                Tab(
                    selected = adminSubTab == idx,
                    onClick = { store.selectAdminSubTab(idx) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    modifier = Modifier.testTag("admin_subtab_$idx")
                )
            }
        }

        when (adminSubTab) {
            0 -> {
                val staffAccounts = store.taiKhoans.filter { it.id != "tk_admin" && it.quyen != VaiTro.OWNER }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Trung tâm Quyền hạn & Tài khoản Nhân viên",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = SpaRosewoodDark
                                )
                                Text(
                                    "Chỉ Admin mới tạo được tài khoản nhân viên mới, sửa hộ số điện thoại/mật khẩu, cấp quyền xem/sửa từng mục hoặc cấp quyền ngang bằng Admin và thu hồi bất cứ lúc nào.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { showCreateAccountDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .semantics {
                                            contentDescription = "Tạo tài khoản nhân viên mới bằng số điện thoại và mật khẩu"
                                        }
                                        .testTag("btn_admin_create_account")
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ Tạo Tài khoản Nhân viên Mới", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, SpaSageGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "📲 Bộ cài đặt APK & Chia sẻ nhanh cho máy Nhân viên",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = SpaSageGreen
                                )
                                Text(
                                    "• Khi tải file ZIP từ AI Studio về điện thoại: Giải nén ra là có sẵn file KhiemThiViet-CaiDat.apk ngay ở thư mục gốc và trong thư mục CAI_DAT_APK (chạm vào là cài ngay, KHÔNG cần dùng Code Assist).\n" +
                                        "• Khi máy chính đã cài xong: Bấm nút bên dưới để gửi thẳng file APK sang máy nhân viên qua Zalo, Quick Share hoặc Drive.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { shareInstalledApkFile(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SpaSageGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .semantics {
                                                contentDescription = "Gửi trực tiếp file cài đặt APK Khiếm Thị Việt đang cài trên máy này sang máy nhân viên qua Zalo hoặc Quick Share"
                                            }
                                            .testTag("btn_share_installed_apk")
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Gửi file APK cài đặt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { showApkInstallGuideDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .semantics {
                                                contentDescription = "Xem hướng dẫn chi tiết cách cài APK từ file ZIP không cần Code Assist và 3 bước đăng nhập trên máy nhân viên"
                                            }
                                            .testTag("btn_open_apk_guide")
                                    ) {
                                        Text("Hướng dẫn cài ZIP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (staffAccounts.isEmpty()) {
                        item {
                            EmptyStateCard("Chưa có tài khoản nhân viên nào. Bấm '+ Tạo Tài khoản NV' để thêm mới.")
                        }
                    } else {
                        items(staffAccounts, key = { it.id }) { tk ->
                            val isTkAdmin = tk.isAdmin()
                            val linkedNv = store.nvs.firstOrNull { it.id == tk.nvId }
                            val passText = tk.matKhauHienThi.ifBlank { "123456" }

                            Card(
                                shape = RoundedCornerShape(15.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_account_card_${tk.id}")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Thông tin tài khoản đọc 1 chạm trên TalkBack
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics(mergeDescendants = true) {
                                                contentDescription = "Nhân viên ${tk.hoTen}. Số điện thoại đăng nhập: ${tk.username}. Mật khẩu hiện tại: $passText. Trạng thái: ${if (isTkAdmin) "Đang có quyền ngang bằng Admin" else "Nhân viên thường, đã mở ${tk.cacQuyenDuocCap.size} quyền phụ"}."
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = tk.hoTen,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 17.sp,
                                                    color = SpaRosewoodDark
                                                )
                                                if (isTkAdmin) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Surface(
                                                        color = SpaChampagne,
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "Admin",
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "SĐT đăng nhập: ${tk.username} • Mật khẩu: $passText",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = SpaRosewood
                                        )
                                        if (tk.email.isNotBlank()) {
                                            Text(
                                                text = "Gmail đồng bộ gắn kèm: ${tk.email}",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = SpaSageGreen
                                            )
                                        }
                                        if (linkedNv != null) {
                                            Text(
                                                text = "Giới tính: ${linkedNv.gt} • Chuyên môn: ${linkedNv.gc.ifBlank { "KTV" }}",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Hàng nút Sửa hộ thông tin / mật khẩu, Gửi TK/MK & Xóa tài khoản
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { tkBeingEdited = tk },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .semantics {
                                                    contentDescription = "Sửa hộ tên, số điện thoại đăng nhập và mật khẩu cho ${tk.hoTen}"
                                                }
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Sửa SĐT / MK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val info = "Tài khoản KHIẾM THỊ VIỆT của ${tk.hoTen}:\n• SĐT đăng nhập: ${tk.username}\n• Mật khẩu: $passText${if (tk.email.isNotBlank()) "\n• Gmail đồng bộ: ${tk.email}" else ""}\n(Mở app bấm 'Kết nối Đám mây Google' 1 lần rồi đăng nhập SĐT + Mật khẩu này)"
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                clipboard?.setPrimaryClip(ClipData.newPlainText("TK Nhân viên", info))
                                                Toast.makeText(context, "Đã sao chép TK & MK của ${tk.hoTen}!", Toast.LENGTH_SHORT).show()
                                                sharePlainText(context, "Tài khoản ${tk.hoTen}", info)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .semantics {
                                                    contentDescription = "Sao chép và gửi số điện thoại đăng nhập kèm mật khẩu cho nhân viên ${tk.hoTen}"
                                                }
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Gửi TK / MK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        IconButton(
                                            onClick = { tkConfirmDelete = tk },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .semantics {
                                                    contentDescription = "Xóa tài khoản và nhân viên ${tk.hoTen}"
                                                }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = SpaDanger)
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                    // Công tắc Cấp quyền ngang bằng Admin / Thu hồi quyền Admin bất cứ lúc nào
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isTkAdmin) "Đang có quyền ngang bằng Admin" else "Cấp quyền ngang bằng Admin",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = SpaRosewood
                                            )
                                            Text(
                                                text = "Gạt bật để gắn mác Admin (toàn quyền), gạt tắt để thu hồi về Nhân viên thường bất cứ lúc nào",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = isTkAdmin,
                                            onCheckedChange = { enabled ->
                                                val ok = store.toggleMacAdminChoTaiKhoan(tk.id, enabled)
                                                if (!ok) {
                                                    Toast.makeText(
                                                        context,
                                                        "Phải giữ lại ít nhất 1 tài khoản Admin!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.semantics {
                                                contentDescription = "Cấp hoặc thu hồi quyền ngang bằng Admin cho ${tk.hoTen}"
                                            }
                                        )
                                    }

                                    // Nếu là nhân viên thường thì hiển thị 6 công tắc quyền chi tiết
                                    if (!isTkAdmin) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Phân quyền chi tiết (Mặc định: chỉ xem khách, gói & tạo vé):",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(
                                                onClick = {
                                                    val hasAll = tk.cacQuyenDuocCap.size == QuyenHan.entries.size
                                                    store.datTatCaQuyenNhanVien(tk.id, !hasAll)
                                                }
                                            ) {
                                                Text(
                                                    if (tk.cacQuyenDuocCap.size == QuyenHan.entries.size) "Thu hồi hết" else "Mở hết 6 quyền",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                         QuyenHan.entries.forEach { q ->
                                            val checked = tk.cacQuyenDuocCap.contains(q)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .semantics(mergeDescendants = true) {
                                                            contentDescription = "${q.title}: ${q.desc}. ${if (checked) "Đang bật" else "Đang tắt"}."
                                                        }
                                                ) {
                                                    Text(q.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                    Text(
                                                        q.desc,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Switch(
                                                    checked = checked,
                                                    onCheckedChange = { enabled ->
                                                        store.toggleQuyenNhanVien(tk.id, q, enabled)
                                                    },
                                                    modifier = Modifier.semantics {
                                                        contentDescription = "${q.title}: ${if (checked) "Bật" else "Tắt"}"
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(96.dp)) }
                }
            }

            1 -> {
                val rootAdminTk = store.taiKhoans.firstOrNull { it.quyen == VaiTro.OWNER }
                    ?: store.currentUser
                    ?: store.taiKhoans.firstOrNull { it.id == "tk_admin" }
                if (rootAdminTk != null) {
                    var hoTenAdmin by remember(rootAdminTk.id) { mutableStateOf(rootAdminTk.hoTen) }
                    var sdtAdmin by remember(rootAdminTk.id) { mutableStateOf(rootAdminTk.username) }
                    var emailAdmin by remember(rootAdminTk.id, rootAdminTk.email) { mutableStateOf(rootAdminTk.email) }
                    var matKhauAdmin by remember(rootAdminTk.id) {
                        mutableStateOf(rootAdminTk.matKhauHienThi.ifBlank { DEFAULT_ADMIN_PASS })
                    }
                    var switchingGoogle by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Chỉnh sửa Tài khoản Admin Gốc",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = SpaRosewoodDark
                                )
                                Text(
                                    "Admin có toàn quyền đổi số điện thoại đăng nhập, mật khẩu và tài khoản Google (Gmail) liên kết của chính mình tại đây. Khi đổi sang Gmail mới, 100% dữ liệu tiệm trên Đám mây vẫn được giữ nguyên.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = hoTenAdmin,
                                    onValueChange = { hoTenAdmin = it },
                                    label = { Text("Tên hiển thị của Admin") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = sdtAdmin,
                                    onValueChange = { sdtAdmin = it },
                                    label = { Text("Tên đăng nhập Admin (Số điện thoại)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_admin_phone")
                                )

                                OutlinedTextField(
                                    value = emailAdmin,
                                    onValueChange = { emailAdmin = it },
                                    label = { Text("Gmail của Admin (đăng nhập & khôi phục Đám mây)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_admin_email")
                                )

                                OutlinedTextField(
                                    value = matKhauAdmin,
                                    onValueChange = { matKhauAdmin = it },
                                    label = { Text("Mật khẩu Admin") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_admin_password")
                                )

                                Button(
                                    onClick = {
                                        val ok = store.adminCapNhatTaiKhoanVaNhanVien(
                                            tkId = rootAdminTk.id,
                                            hoTenMoi = hoTenAdmin,
                                            sdtDangNhapMoi = sdtAdmin,
                                            matKhauMoi = matKhauAdmin,
                                            emailMoi = emailAdmin
                                        )
                                        if (ok) {
                                            Toast.makeText(
                                                context,
                                                "Đã lưu tài khoản Admin mới: SĐT $sdtAdmin, Gmail ${emailAdmin.ifBlank { "chưa gắn" }}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Không thể lưu: SĐT hoặc Gmail bị trùng với tài khoản khác!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .semantics {
                                            contentDescription = "Lưu thay đổi tên đăng nhập, Gmail và mật khẩu của tài khoản Admin"
                                        }
                                        .testTag("btn_save_admin_credentials")
                                ) {
                                    Text("LƯU THÔNG TIN TÀI KHOẢN ADMIN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SpaSageGreen.copy(alpha = 0.09f)),
                            border = BorderStroke(1.dp, SpaSageGreen.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "☁️ Đổi Tài Khoản Google (Gmail) Đám Mây Của Admin",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = SpaSageGreen
                                )
                                Text(
                                    if (store.isCloudConnected) {
                                        "Đang kết nối Đám mây bằng Gmail: ${store.cloudUserEmail.ifBlank { "Đã xác thực" }}.\n" +
                                            "• Bạn có thể đổi sang bất kỳ tài khoản Google (Gmail) mới nào.\n" +
                                            "• Toàn bộ dữ liệu tiệm (Vé, Sổ quỹ, Bảng lương, Bộ nhớ gehihi) nằm trên kho chung của tiệm nên được GIỮ NGUYÊN 100% khi đổi Gmail."
                                    } else {
                                        "Hiện chưa kết nối Gmail Đám mây trên máy này. Bấm nút bên dưới để chọn tài khoản Google (Gmail) của Admin."
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = {
                                        if (!switchingGoogle) {
                                            switchingGoogle = true
                                            scope.launch {
                                                store.disconnectGoogleCloudForSwitch()
                                                signInWithGoogleInteractive(
                                                    activityContext = context,
                                                    forceAccountPicker = true,
                                                    onSignedIn = {
                                                        store.startRealtimeCloudSync()
                                                        val newEmail = store.cloudUserEmail.trim().lowercase()
                                                        if (newEmail.isNotBlank()) {
                                                            emailAdmin = newEmail
                                                            store.adminCapNhatTaiKhoanVaNhanVien(
                                                                tkId = rootAdminTk.id,
                                                                hoTenMoi = hoTenAdmin,
                                                                sdtDangNhapMoi = sdtAdmin,
                                                                matKhauMoi = matKhauAdmin,
                                                                emailMoi = newEmail
                                                            )
                                                        }
                                                        switchingGoogle = false
                                                        Toast.makeText(
                                                            context,
                                                            "Đã đổi sang Gmail Admin mới (${store.cloudUserEmail}) và đồng bộ 100% dữ liệu tiệm!",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    },
                                                    onError = { err ->
                                                        switchingGoogle = false
                                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SpaSageGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .semantics {
                                            contentDescription = "Chọn và đổi sang tài khoản Google Gmail khác cho Admin"
                                        }
                                        .testTag("btn_switch_admin_google_account")
                                ) {
                                    Text(
                                        if (switchingGoogle) "ĐANG MỞ BẢNG CHỌN GMAIL..." else "🔁 CHỌN / ĐỔI TÀI KHOẢN GOOGLE (GMAIL) KHÁC",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, Color(0xFF0277BD).copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF0277BD))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "💳 Cấu Hình Ngân Hàng Nhận Tiền VietQR",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0277BD)
                                    )
                                }
                                Text(
                                    "Dùng để tự động tạo mã VietQR Napas 247 khi thu tiền vé hoặc gia hạn gói cước, KTV khiếm thị không cần tính tiền hay đọc số tài khoản thủ công.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                var bankCodeInput by remember(store.thongTinNganHang) { mutableStateOf(store.thongTinNganHang.bankCode) }
                                var stkInput by remember(store.thongTinNganHang) { mutableStateOf(store.thongTinNganHang.soTaiKhoan) }
                                var chuTkInput by remember(store.thongTinNganHang) { mutableStateOf(store.thongTinNganHang.tenChuTaiKhoan) }

                                OutlinedTextField(
                                    value = bankCodeInput,
                                    onValueChange = { bankCodeInput = it.uppercase() },
                                    label = { Text("Mã Ngân hàng (MB, VCB, TCB, VPB, ACB, BIDV, ...)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = stkInput,
                                    onValueChange = { stkInput = it.filter { c -> c.isDigit() || c.isLetter() } },
                                    label = { Text("Số tài khoản thụ hưởng") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = chuTkInput,
                                    onValueChange = { chuTkInput = it.uppercase() },
                                    label = { Text("Tên chủ tài khoản (VIẾT HOA KHÔNG DẤU)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        store.capNhatThongTinNganHang(bankCodeInput, stkInput, chuTkInput)
                                        Toast.makeText(context, "Đã lưu cấu hình tài khoản ngân hàng thành công!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("LƯU CẤU HÌNH NGÂN HÀNG VIETQR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        // CARD CẤU HÌNH GEMINI API KEY (ENTRY POINT 2)
                        var showKeyConfigInSubtab by remember { mutableStateOf(false) }
                        if (showKeyConfigInSubtab) {
                            AiKeyConfigDialog(
                                store = store,
                                onDismiss = { showKeyConfigInSubtab = false }
                            )
                        }

                        val hasGeminiKey = store.isGeminiConnected || store.layGeminiApiKey().isNotBlank()
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.8f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Cấu hình Khóa Trí tuệ Nhân tạo Gemini API Key. Trạng thái: " +
                                        (if (hasGeminiKey) "Đã cài đặt key trực tuyến" else "Đang dùng ngoại tuyến 100%")
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Key,
                                        contentDescription = null,
                                        tint = SpaChampagne,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "CẤU HÌNH KHÓA TRÍ TUỆ NHÂN TẠO (GEMINI API KEY)",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SpaChampagne,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    "Cấu hình Gemini API Key cá nhân để trợ lý Gehihi kết nối mô hình Google Gemini 1.5 Flash trực tuyến, hỗ trợ hỏi đáp kiến thức spa chuyên sâu, dịch thuật đa ngôn ngữ và phân tích dữ liệu tổng thể.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    color = if (hasGeminiKey) Color(0xFF1B3B24) else Color(0xFF282A2C),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (hasGeminiKey) Color(0xFF81C995) else Color(0xFF444746))
                                ) {
                                    Text(
                                        text = if (hasGeminiKey) "✨ Trạng thái: Gemini AI Trực Tuyến (${store.layGeminiApiKey().take(6)}...)" else "🟢 Trạng thái: Ngoại tuyến (Đọc dữ liệu tiệm 100%)",
                                        color = if (hasGeminiKey) Color(0xFF81C995) else Color(0xFFC4C7C5),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (onOpenAiKeyConfig != null) {
                                            onOpenAiKeyConfig()
                                        } else {
                                            showKeyConfigInSubtab = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewoodDark),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_open_key_config_subtab1")
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = SpaChampagne, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "CÀI ĐẶT / ĐỔI GEMINI API KEY",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        if (store.currentUser.isAdmin()) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                border = BorderStroke(1.5.dp, SpaDanger.copy(alpha = 0.7f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "🗑️ VÙNG NGUY HIỂM: XÓA TẤT CẢ DỮ LIỆU APP (CHỈ ADMIN)",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SpaDanger,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Xóa sạch toàn bộ dữ liệu trên cả máy và Cloud (Vé, Gói liệu trình, Khách hàng, KTV, Dịch vụ, Sổ quỹ, Lịch sử AI), chỉ giữ lại duy nhất tài khoản Admin đang đăng nhập. Yêu cầu mật khẩu Admin và xác nhận lại 3 lần.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(
                                        onClick = {
                                            adminPasswordForWipe = ""
                                            wipeErrorMsg = null
                                            wipeStep = 1
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SpaDanger),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("btn_wipe_all_app_data_subtab1")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "XÓA TẤT CẢ DỮ LIỆU ỨNG DỤNG (XÁC NHẬN 3 LẦN)",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }
            }

            2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // R2. Nút & Hộp thoại trợ giúp TalkBack về Cơ chế sao lưu 1 tuần & Zalo
                    Button(
                        onClick = { showTalkBackBackupDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SpaRosewoodDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .semantics {
                                contentDescription = "Hướng dẫn trợ năng TalkBack về cơ chế tự động sao lưu dữ liệu một tuần ra thư mục Documents và chia sẻ Zalo"
                            }
                            .testTag("btn_talkback_backup_help")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "📢 HƯỚNG DẪN TALKBACK: SAO LƯU HÀNG TUẦN & ZALO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    if (showTalkBackBackupDialog) {
                        AlertDialog(
                            onDismissRequest = { showTalkBackBackupDialog = false },
                            title = {
                                Text(
                                    "📢 Hướng Dẫn TalkBack: Cơ Chế Sao Lưu Định Kỳ & Zalo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = SpaRosewoodDark
                                )
                            },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(rememberScrollState())
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = "Tính năng Sao lưu dữ liệu hàng tuần. " +
                                                "Hệ thống Khiếm Thị Việt tự động tạo một bản sao lưu toàn bộ dữ liệu của cơ sở vào thư mục Documents trên điện thoại của bạn, định kỳ mỗi tuần một lần. " +
                                                "Dữ liệu sao lưu bao gồm: danh sách khách hàng, nhân viên, bảng giá dịch vụ, các vé phục vụ, sổ quỹ thu chi và ký ức trợ lý Gehihi. " +
                                                "Sau khi tạo file sao lưu, ứng dụng sẽ tự động mở hộp thoại chia sẻ để bạn gửi file trực tiếp sang Zalo. Bạn nên chọn gửi vào mục 'Cloud của tôi' trên Zalo để lưu trữ an toàn vĩnh viễn trên mạng. " +
                                                "Bất cứ lúc nào, bạn cũng có thể bấm nút 'Xuất file Backup' hoặc 'Chia sẻ Zalo' trong màn hình này để sao lưu ngay lập tức. " +
                                                "Khi bạn đổi sang điện thoại mới hoặc lỡ xóa ứng dụng, bạn chỉ cần chọn lại file sao lưu từ Zalo để khôi phục lại toàn bộ dữ liệu một trăm phần trăm, không lo mất mát thông tin của tiệm."
                                        },
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        "1. Tự động sao lưu định kỳ 1 tuần:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SpaRosewood
                                    )
                                    Text(
                                        "Hệ thống Khiếm Thị Việt tự động tạo bản sao lưu toàn bộ cơ sở vào thư mục Documents của điện thoại mỗi tuần 1 lần. File sao lưu định dạng .json chứa đầy đủ: Khách hàng, Nhân viên, Dịch vụ, Vé làm, Sổ quỹ và Ký ức Gehihi.",
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                    Text(
                                        "2. Chia sẻ file sao lưu qua Zalo:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SpaRosewood
                                    )
                                    Text(
                                        "Ứng dụng mở sẵn Intent chia sẻ hệ thống để Chủ Spa gửi file sao lưu vào mục 'Cloud của tôi' trên Zalo để lưu trữ vĩnh viễn trên đám mây an toàn.",
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                    Text(
                                        "3. Khôi phục nhanh chóng khi đổi máy:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SpaRosewood
                                    )
                                    Text(
                                        "Khi đổi điện thoại mới hoặc cài lại ứng dụng, chỉ cần tải file từ Zalo về máy và nhấn 'Nhập dữ liệu từ File Backup' để khôi phục 100% dữ liệu chỉ sau vài giây.",
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = { showTalkBackBackupDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood)
                                ) {
                                    Text("Đã hiểu")
                                }
                            }
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "☁️ Bảo Vệ Đám Mây Tự Động & Backup Thủ Công",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = SpaRosewoodDark
                            )
                            Text(
                                "• An toàn khi lỡ xóa App: Toàn bộ dữ liệu của quán luôn được tự động lưu trên Đám mây Google. Nếu lỡ xóa app hoặc đổi máy mà quên xuất file Backup thủ công, chỉ cần cài lại APK và đăng nhập Google là dữ liệu tự động khôi phục 100%!\n" +
                                    "• Tự động chống tràn 1MB Firestore: Máy chính lưu tới 50.000 sự kiện & 20.000 tin nhắn của gehihi; khi đồng bộ lên Đám mây, hệ thống có bộ lọc thông minh tự giữ gói dữ liệu dưới 750KB (không bao giờ chạm trần 1MB của Firestore), ưu tiên giữ 100% Vé, Sổ quỹ, Bảng lương, Khách hàng, Gói dịch vụ & các ký ức quan trọng nhất.\n" +
                                    "• Ngoài ra vẫn hỗ trợ xuất/nhập file .json thủ công (kể cả file menu_mới_nhất_2.json từ app cũ).",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Dữ liệu hiện có: ${store.dvs.size} dịch vụ • ${store.gois.size} gói • ${store.khs.size} khách • ${store.nvs.size} KTV • ${store.luots.size} vé • ${store.tcs.size} sổ quỹ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SpaRosewood
                            )
                            Text(
                                text = "Trạng thái đám mây: ${store.cloudSyncStatus}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (store.isCloudConnected) SpaSageGreen else SpaDanger
                            )
                            if (store.isCloudConnected) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            store.dongBoKhoiPhucTuDamMayNgay { _, msg ->
                                                backupStatusMsg = msg
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SpaSageGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("btn_admin_cloud_restore_now")
                                    ) {
                                        Text("Khôi phục từ Đám mây", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            store.forcePushCurrentStateToCloud()
                                            Toast.makeText(
                                                context,
                                                "Đã đẩy toàn bộ dữ liệu từ máy Admin lên đám mây cho mọi máy nhân viên!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("btn_admin_force_cloud_push")
                                    ) {
                                        Text("Đẩy lên Đám mây", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        store.disconnectGoogleCloudForSwitch()
                                        signInWithGoogleInteractive(
                                            activityContext = context,
                                            forceAccountPicker = true,
                                            onSignedIn = {
                                                store.startRealtimeCloudSync()
                                                val newEmail = store.cloudUserEmail.trim().lowercase()
                                                val rootAdmin = store.taiKhoans.firstOrNull { it.id == "tk_admin" }
                                                if (newEmail.isNotBlank() && rootAdmin != null) {
                                                    store.adminCapNhatTaiKhoanVaNhanVien(
                                                        tkId = rootAdmin.id,
                                                        hoTenMoi = rootAdmin.hoTen,
                                                        sdtDangNhapMoi = rootAdmin.username,
                                                        matKhauMoi = rootAdmin.matKhauHienThi.ifBlank { DEFAULT_ADMIN_PASS },
                                                        emailMoi = newEmail
                                                    )
                                                }
                                                backupStatusMsg = "Đã đổi sang Gmail ${store.cloudUserEmail} và giữ nguyên 100% dữ liệu tiệm!"
                                                Toast.makeText(context, backupStatusMsg, Toast.LENGTH_LONG).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("btn_backup_switch_google_account")
                            ) {
                                Text(
                                    "🔁 ĐỔI TÀI KHOẢN GOOGLE (GMAIL) CỦA ADMIN",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = SpaRosewoodDark
                                )
                            }
                        }
                    }

                    if (backupStatusMsg.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { liveRegion = LiveRegionMode.Polite }
                        ) {
                            Text(
                                text = backupStatusMsg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SpaSageGreen,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // KHỐI 1: XUẤT TẤT CẢ DỮ LIỆU RA FILE BACKUP JSON
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "1. Xuất toàn bộ dữ liệu ra File Backup (.json)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SpaRosewoodDark
                            )
                            Text(
                                "Xuất tất cả Khách hàng, Dịch vụ, Gói liệu trình, Nhân viên, Vé phục vụ, Sổ quỹ và Tài khoản ra file JSON để lưu trữ hoặc chuyển máy.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = {
                                    val defaultFileName = "backup_khiem_thi_viet_${todayDateStr()}.json"
                                    exportBackupLauncher.launch(defaultFileName)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .semantics {
                                        contentDescription = "Xuất tất cả dữ liệu ra file JSON lưu vào bộ nhớ điện thoại"
                                    }
                                    .testTag("btn_export_backup_file")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("XUẤT FILE BACKUP (.JSON)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val json = store.xuatDuLieuBackupJson()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Backup JSON Khiếm Thị Việt", json))
                                        backupStatusMsg = "Đã sao chép toàn bộ mã JSON Backup vào bộ nhớ tạm!"
                                        Toast.makeText(context, backupStatusMsg, Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sao chép JSON", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val json = store.xuatDuLieuBackupJson()
                                        sharePlainText(
                                            context,
                                            "Backup Khiếm Thị Việt (${todayDateStr()})",
                                            json
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Chia sẻ Zalo / Lưu", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // KHỐI 2: NHẬP TẤT CẢ DỮ LIỆU TỪ FILE BACKUP (.JSON) HOẶC DÁN MÃ JSON
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "2. Nhập dữ liệu từ File Backup (.json) cũ hoặc mới",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SpaRosewoodDark
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (ghiDeToanBoKhiNhap) "Chế độ: Ghi đè theo các mục có trong file" else "Chế độ: Gộp thêm vào dữ liệu hiện có",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SpaRosewood
                                    )
                                    Text(
                                        text = "Nếu file chỉ có Dịch vụ & Gói (như menu_mới_nhất_2.json) thì danh sách Khách hàng & Nhân viên hiện tại vẫn được giữ nguyên an toàn.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = ghiDeToanBoKhiNhap,
                                    onCheckedChange = { ghiDeToanBoKhiNhap = it }
                                )
                            }

                            Button(
                                onClick = {
                                    importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SpaSageGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .semantics {
                                        contentDescription = "Chọn file Backup chấm json từ điện thoại để nhập toàn bộ dữ liệu vào ứng dụng"
                                    }
                                    .testTag("btn_import_backup_file")
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CHỌN FILE BACKUP (.JSON) ĐỂ NHẬP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val msg = store.napMenuChuanTuFileBackupCu()
                                    backupStatusMsg = msg
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_restore_menu_moi_nhat_2")
                            ) {
                                Text(
                                    "Khôi phục ngay 10 Dịch vụ & 3 Gói từ menu_mới_nhất_2.json",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                "Hoặc dán trực tiếp nội dung JSON Backup vào ô dưới đây:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = pasteJsonInput,
                                onValueChange = { pasteJsonInput = it },
                                label = { Text("Dán nội dung file JSON Backup (cũ hoặc mới)") },
                                placeholder = { Text("{\"dvs\": [...], \"gois\": [...], \"khs\": [...]}") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp, max = 180.dp)
                                    .testTag("input_paste_backup_json")
                            )
                            Button(
                                onClick = {
                                    val res = store.nhapDuLieuTuJsonBackup(pasteJsonInput, ghiDeToanBoKhiNhap)
                                    val msg = res.fold(
                                        onSuccess = {
                                            pasteJsonInput = ""
                                            it
                                        },
                                        onFailure = { "Lỗi định dạng JSON: ${it.message}" }
                                    )
                                    backupStatusMsg = msg
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                },
                                enabled = pasteJsonInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_submit_paste_backup")
                            ) {
                                Text("NHẬP DỮ LIỆU TỪ ĐOẠN JSON ĐÃ DÁN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    if (store.currentUser.isAdmin()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            border = BorderStroke(1.5.dp, SpaDanger.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "🗑️ VÙNG NGUY HIỂM: XÓA TẤT CẢ DỮ LIỆU APP (CHỈ ADMIN)",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SpaDanger,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Xóa sạch toàn bộ dữ liệu trên cả máy và Cloud (Vé, Gói liệu trình, Khách hàng, KTV, Dịch vụ, Sổ quỹ, Lịch sử AI), chỉ giữ lại duy nhất tài khoản Admin đang đăng nhập. Yêu cầu mật khẩu Admin và xác nhận lại 3 lần.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = {
                                        adminPasswordForWipe = ""
                                        wipeErrorMsg = null
                                        wipeStep = 1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SpaDanger),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_wipe_all_app_data")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "XÓA TẤT CẢ DỮ LIỆU ỨNG DỤNG",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
            3 -> {
                DevConsoleSubTab(store = store)
            }
        }
    }

    if (store.currentUser.isAdmin() && wipeStep in 1..3) {
        AlertDialog(
            onDismissRequest = { wipeStep = 0 },
            title = {
                Text(
                    when (wipeStep) {
                        1 -> "⚠️ Xác nhận Lần 1/3: Nhập mật khẩu Admin"
                        2 -> "⚠️ Xác nhận Lần 2/3: Kiểm tra phạm vi xóa"
                        else -> "🚨 Xác nhận Lần Cuối (3/3): Xóa vĩnh viễn"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = SpaDanger,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (wipeStep) {
                        1 -> {
                            Text(
                                "Chỉ duy nhất Admin mới được thực hiện chức năng này. Vui lòng nhập mật khẩu Admin hiện tại để mở khóa bước xác nhận tiếp theo:",
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = adminPasswordForWipe,
                                onValueChange = {
                                    adminPasswordForWipe = it
                                    wipeErrorMsg = null
                                },
                                label = { Text("Mật khẩu Admin hiện tại") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_wipe_admin_password")
                            )
                        }
                        2 -> {
                            Text(
                                "Bạn đang chuẩn bị xóa sạch toàn bộ dữ liệu hiện có:\n" +
                                    "• Vé & Lịch sử vé: ${store.luots.size} vé\n" +
                                    "• Khách hàng & Gói: ${store.khs.size} khách, ${store.dks.size} thẻ gói\n" +
                                    "• Nhân viên KTV: ${store.nvs.size} KTV\n" +
                                    "• Menu Dịch vụ & Gói mẫu: ${store.dvs.size} dịch vụ, ${store.gois.size} gói\n" +
                                    "• Sổ quỹ Thu/Chi: ${store.tcs.size} giao dịch\n\n" +
                                    "Bạn có chắc chắn muốn tiếp tục sang bước xác nhận cuối cùng (3/3) không?",
                                fontSize = 13.sp
                            )
                        }
                        3 -> {
                            Text(
                                "ĐÂY LÀ XÁC NHẬN LẦN THỨ 3 (LẦN CUỐI CÙNG)!\n\n" +
                                    "Sau khi bấm nút 'XÓA SẠCH VĨNH VIỄN (3/3)', toàn bộ dữ liệu trên máy và trên Cloud sẽ bị xóa trắng ngay lập tức (chỉ giữ lại tài khoản Admin của bạn).",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpaDanger
                            )
                        }
                    }
                    if (wipeErrorMsg != null) {
                        Text(
                            text = wipeErrorMsg!!,
                            color = SpaDanger,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (wipeStep) {
                            1 -> {
                                if (store.xacThucMatKhauAdminHienTai(adminPasswordForWipe)) {
                                    wipeErrorMsg = null
                                    wipeStep = 2
                                } else {
                                    wipeErrorMsg = "Mật khẩu Admin không đúng! Vui lòng kiểm tra lại."
                                }
                            }
                            2 -> {
                                wipeErrorMsg = null
                                wipeStep = 3
                            }
                            3 -> {
                                val res = store.xoaTatCaDuLieuUngDungAdmin(adminPasswordForWipe)
                                res.fold(
                                    onSuccess = { msg ->
                                        wipeStep = 0
                                        backupStatusMsg = msg
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    },
                                    onFailure = { err ->
                                        wipeErrorMsg = err.message ?: "Không thể xóa dữ liệu."
                                    }
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaDanger),
                    modifier = Modifier.testTag("btn_confirm_wipe_step")
                ) {
                    Text(
                        when (wipeStep) {
                            1 -> "Xác nhận Lần 1/3 ➔"
                            2 -> "Xác nhận Lần 2/3 ➔"
                            else -> "XÓA SẠCH VĨNH VIỄN (3/3)"
                        },
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { wipeStep = 0 },
                    modifier = Modifier.testTag("btn_cancel_wipe_step")
                ) {
                    Text("Hủy bỏ")
                }
            }
        )
    }

    if (showApkInstallGuideDialog) {
        AlertDialog(
            onDismissRequest = { showApkInstallGuideDialog = false },
            title = {
                Text(
                    "Hướng dẫn cài APK từ file ZIP & Cài cho máy Nhân viên",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = SpaRosewoodDark
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "1. Vì sao không cần dùng Code Assist (module.toml)?",
                        fontWeight = FontWeight.Bold,
                        color = SpaRosewood
                    )
                    Text(
                        "Code Assist trên điện thoại chỉ nhận cấu trúc module.toml cũ và không biên dịch được dự án chuẩn Jetpack Compose + Firebase Cloud mới. Do đó hệ thống đã biên dịch sẵn file .apk hoàn chỉnh ngay trong file ZIP cho bạn.",
                        fontSize = 13.sp
                    )

                    Text(
                        "2. Cách cài đặt từ file ZIP trên máy chính:",
                        fontWeight = FontWeight.Bold,
                        color = SpaRosewood
                    )
                    Text(
                        "• Bước 1: Mở ứng dụng Quản lý File (hoặc ZArchiver) trên điện thoại và giải nén file .zip vừa tải về.\n" +
                            "• Bước 2: Ngay ở ngoài cùng thư mục giải nén (hoặc trong thư mục CAI_DAT_APK), chạm vào file KhiemThiViet-CaiDat.apk để cài đặt ngay.",
                        fontSize = 13.sp
                    )

                    Text(
                        "3. Cách cài và đồng bộ cho máy Nhân viên:",
                        fontWeight = FontWeight.Bold,
                        color = SpaSageGreen
                    )
                    Text(
                        "• Bước 1: Trên máy Admin, bấm nút 'Gửi file APK cài đặt' để gửi file KhiemThiViet-CaiDat.apk qua Zalo/Quick Share cho máy nhân viên cài đặt.\n" +
                            "• Bước 2: Máy nhân viên mở app, bấm 'Kết nối Đám mây Google' 1 lần bằng Gmail của nhân viên để tải danh sách tài khoản thời gian thực từ máy chủ.\n" +
                            "• Bước 3: Nhập SĐT (hoặc Gmail) và Mật khẩu do Admin đã tạo để đăng nhập.",
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApkInstallGuideDialog = false
                        shareInstalledApkFile(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaSageGreen)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gửi APK ngay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApkInstallGuideDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    // Hộp thoại Tạo Tài khoản Nhân viên mới (Chỉ Admin)
    if (showCreateAccountDialog) {
        var ten by remember { mutableStateOf("") }
        var gt by remember { mutableStateOf("Nam") }
        var sdt by remember { mutableStateOf("") }
        var matKhau by remember { mutableStateOf("123456") }
        var emailNv by remember { mutableStateOf("") }
        var gc by remember { mutableStateOf("") }
        var capMacAdmin by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateAccountDialog = false },
            title = { Text("Tạo Tài khoản Nhân viên Mới", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Số điện thoại của nhân viên sẽ là Tên đăng nhập. Bạn có thể nhập thêm Gmail trên máy nhân viên (không bắt buộc) để khi máy nhân viên kết nối Gmail đó sẽ tự động nhận diện tài khoản ngay!",
                        fontSize = 12.sp,
                        color = SpaSageGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = ten,
                        onValueChange = { ten = it },
                        label = { Text("Họ tên nhân viên KTV") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sdt,
                        onValueChange = { sdt = it },
                        label = { Text("Số điện thoại (Tên đăng nhập)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = matKhau,
                        onValueChange = { matKhau = it },
                        label = { Text("Mật khẩu cấp cho nhân viên") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emailNv,
                        onValueChange = { emailNv = it },
                        label = { Text("Gmail trên máy nhân viên (Tùy chọn)") },
                        placeholder = { Text("vd: nhanvien@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Nam", "Nữ").forEach { g ->
                            FilterChip(selected = gt == g, onClick = { gt = g }, label = { Text(g) })
                        }
                    }
                    OutlinedTextField(
                        value = gc,
                        onValueChange = { gc = it },
                        label = { Text("Chuyên môn / Ghi chú") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cấp quyền ngang bằng Admin (Mác Admin)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Switch(checked = capMacAdmin, onCheckedChange = { capMacAdmin = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ten.isNotBlank() && sdt.isNotBlank()) {
                            val passFinal = matKhau.ifBlank { "123456" }
                            val nv = store.themNhanVien(
                                ten = ten,
                                gt = gt,
                                sdt = sdt,
                                gc = gc,
                                matKhauKhoiTao = passFinal,
                                laAdmin = capMacAdmin,
                                email = emailNv
                            )
                            if (nv != null) {
                                store.forcePushCurrentStateToCloud()
                                Toast.makeText(
                                    context,
                                    "Đã tạo & đồng bộ đám mây TK ${nv.ten} (SĐT: ${nv.sdt}, MK: $passFinal)",
                                    Toast.LENGTH_LONG
                                ).show()
                                showCreateAccountDialog = false
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng nhập Họ tên và Số điện thoại!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                    modifier = Modifier.height(48.dp)
                ) { Text("Tạo & Đồng bộ") }
            },
            dismissButton = { TextButton(onClick = { showCreateAccountDialog = false }) { Text("Hủy") } }
        )
    }

    // Hộp thoại Admin sửa hộ thông tin, số điện thoại đăng nhập & mật khẩu cho nhân viên
    tkBeingEdited?.let { tk ->
        val linkedNv = store.nvs.firstOrNull { it.id == tk.nvId }
        var tenMoi by remember(tk.id) { mutableStateOf(tk.hoTen) }
        var sdtMoi by remember(tk.id) { mutableStateOf(tk.username) }
        var matKhauMoi by remember(tk.id) { mutableStateOf(tk.matKhauHienThi.ifBlank { "123456" }) }
        var emailMoi by remember(tk.id) { mutableStateOf(tk.email) }
        var gtMoi by remember(tk.id) { mutableStateOf(linkedNv?.gt ?: "Nam") }
        var gcMoi by remember(tk.id) { mutableStateOf(linkedNv?.gc ?: "") }

        AlertDialog(
            onDismissRequest = { tkBeingEdited = null },
            title = { Text("Sửa Tài khoản: ${tk.hoTen}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = tenMoi,
                        onValueChange = { tenMoi = it },
                        label = { Text("Họ tên") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sdtMoi,
                        onValueChange = { sdtMoi = it },
                        label = { Text("Số điện thoại đăng nhập") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = matKhauMoi,
                        onValueChange = { matKhauMoi = it },
                        label = { Text("Mật khẩu mới") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emailMoi,
                        onValueChange = { emailMoi = it },
                        label = { Text("Gmail trên máy nhân viên (Tùy chọn)") },
                        placeholder = { Text("vd: nhanvien@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (linkedNv != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Nam", "Nữ").forEach { g ->
                                FilterChip(selected = gtMoi == g, onClick = { gtMoi = g }, label = { Text(g) })
                            }
                        }
                        OutlinedTextField(
                            value = gcMoi,
                            onValueChange = { gcMoi = it },
                            label = { Text("Chuyên môn / Ghi chú") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = store.adminCapNhatTaiKhoanVaNhanVien(
                            tkId = tk.id,
                            hoTenMoi = tenMoi,
                            sdtDangNhapMoi = sdtMoi,
                            matKhauMoi = matKhauMoi,
                            gtMoi = gtMoi,
                            ghiChuMoi = gcMoi,
                            emailMoi = emailMoi
                        )
                        if (ok) {
                            store.forcePushCurrentStateToCloud()
                            Toast.makeText(context, "Đã cập nhật & đồng bộ tài khoản $tenMoi!", Toast.LENGTH_SHORT).show()
                            tkBeingEdited = null
                        } else {
                            Toast.makeText(context, "Số điện thoại bị trống hoặc trùng lặp!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                    modifier = Modifier.height(48.dp)
                ) { Text("Lưu & Đồng bộ") }
            },
            dismissButton = { TextButton(onClick = { tkBeingEdited = null }) { Text("Hủy") } }
        )
    }

    // Hộp thoại xác nhận xóa tài khoản nhân viên
    tkConfirmDelete?.let { tk ->
        AlertDialog(
            onDismissRequest = { tkConfirmDelete = null },
            title = { Text("Xóa Tài khoản & Nhân viên?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Bạn có chắc chắn muốn xóa tài khoản và nhân viên ${tk.hoTen} (SĐT: ${tk.username}) khỏi hệ thống?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        store.xoaTaiKhoanTheoId(tk.id)
                        Toast.makeText(context, "Đã xóa tài khoản ${tk.hoTen}", Toast.LENGTH_SHORT).show()
                        tkConfirmDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaDanger)
                ) { Text("Xác nhận xóa") }
            },
            dismissButton = { TextButton(onClick = { tkConfirmDelete = null }) { Text("Hủy") } }
        )
    }
}

// =========================================================================
// DEVELOPER SUPER ADMIN CONSOLE: QUẢN TRỊ TOÀN DIỆN SAAS MULTI-TENANT
// =========================================================================
@Composable
fun DevConsoleSubTab(store: SpaStore) {
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var tenantToLock by remember { mutableStateOf<TenantInfo?>(null) }
    var tenantToExtend by remember { mutableStateOf<TenantInfo?>(null) }

    LaunchedEffect(Unit) {
        store.fetchDevAllTenants()
    }

    val totalTenants = store.devAllTenants.size
    val activeTenantsCount = store.devAllTenants.count { !it.isEffectiveLocked }
    val lockedTenantsCount = store.devAllTenants.count { it.isEffectiveLocked }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner cảnh báo nếu Dev đang ở góc nhìn của cơ sở khác
        if (store.activeTenantOverride != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF9800)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "👁️ Chế độ Góc nhìn Spa Đang Bật",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                fontSize = 14.sp
                            )
                            Text(
                                "Bạn đang quản lý & quan sát dữ liệu cơ sở: ${store.activeTenantOverride}",
                                fontSize = 12.sp,
                                color = Color(0xFF5D4037)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                store.devSwitchTenantContext(null)
                                Toast.makeText(context, "Đã quay về cơ sở mặc định", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Về mặc định", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Header & Thẻ thống kê
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "👑 Developer Super Admin Console",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = SpaRosewoodDark
                            )
                            Text(
                                "Giám sát & Quản trị Multi-Tenant toàn hệ thống",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (store.isDevTenantsLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        }
                    }

                    // 3 Ô Thống kê trực quan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tổng số
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("TỔNG CƠ SỞ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("$totalTenants", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SpaRosewoodDark)
                            }
                        }
                        // Đang hoạt động (Xanh lá)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("HOẠT ĐỘNG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text("$activeTenantsCount", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                            }
                        }
                        // Bị khóa (Đỏ)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("BỊ KHÓA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                Text("$lockedTenantsCount", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC62828))
                            }
                        }
                    }

                    // Hàng nút hành động chung
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                store.fetchDevAllTenants { ok, msg ->
                                    Toast.makeText(context, if (ok) "Đã cập nhật danh sách cơ sở" else msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Làm mới", fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Thêm cơ sở mới", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Danh sách Tenants
        item {
            Text(
                "Danh sách các cơ sở Spa (${store.devAllTenants.size}):",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SpaRosewoodDark
            )
        }

        if (store.devAllTenants.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Chưa có cơ sở nào. Bấm 'Thêm cơ sở mới' để bắt đầu.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(store.devAllTenants, key = { it.tenantId }) { tenant ->
                DevTenantCard(
                    tenant = tenant,
                    isCurrentActive = store.activeTenantOverride == tenant.tenantId || (store.activeTenantOverride == null && tenant.tenantId == store.currentTenantId),
                    onToggleLock = {
                        if (tenant.isEffectiveLocked) {
                            // Mở khóa trực tiếp
                            store.devToggleLockTenant(tenant.tenantId, lock = false) { ok, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Mở dialog khóa
                            tenantToLock = tenant
                        }
                    },
                    onOpenExtendPlan = {
                        tenantToExtend = tenant
                    },
                    onSwitchContext = {
                        if (store.activeTenantOverride == tenant.tenantId) {
                            store.devSwitchTenantContext(null)
                            Toast.makeText(context, "Đã thoát chế độ góc nhìn cơ sở", Toast.LENGTH_SHORT).show()
                        } else {
                            store.devSwitchTenantContext(tenant.tenantId)
                            Toast.makeText(context, "Đã chuyển góc nhìn sang cơ sở: ${tenant.shopName}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }

    // Dialogs
    if (showCreateDialog) {
        DevCreateTenantDialog(
            store = store,
            onDismiss = { showCreateDialog = false }
        )
    }

    tenantToLock?.let { tenant ->
        DevLockTenantDialog(
            tenant = tenant,
            store = store,
            onDismiss = { tenantToLock = null }
        )
    }

    tenantToExtend?.let { tenant ->
        DevExtendPlanDialog(
            tenant = tenant,
            store = store,
            onDismiss = { tenantToExtend = null }
        )
    }
}

@Composable
fun DevTenantCard(
    tenant: TenantInfo,
    isCurrentActive: Boolean,
    onToggleLock: () -> Unit,
    onOpenExtendPlan: () -> Unit,
    onSwitchContext: () -> Unit
) {
    val isLocked = tenant.isEffectiveLocked
    val planBadgeColor = when (tenant.planId) {
        "PLAN_ENTERPRISE" -> Color(0xFF6A1B9A)
        "PLAN_PRO" -> Color(0xFF0277BD)
        "PLAN_BASIC" -> Color(0xFF00695C)
        else -> Color(0xFF558B2F)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isCurrentActive) BorderStroke(2.dp, SpaRosewood) else if (isLocked) BorderStroke(1.dp, Color(0xFFFFCDD2)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hàng 1: Tên cơ sở + Badges (Gói cước & Trạng thái)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tenant.shopName.ifBlank { tenant.tenantId },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SpaRosewoodDark
                        )
                        if (isCurrentActive) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SpaRosewood.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "ĐANG XEM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SpaRosewood,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "ID: ${tenant.tenantId}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Badge Gói
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = planBadgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            tenant.planId.removePrefix("PLAN_"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = planBadgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    // Badge Trạng thái
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isLocked) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ) {
                        Text(
                            if (isLocked) "BỊ KHÓA" else "HOẠT ĐỘNG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked) Color(0xFFC62828) else Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Hàng 2: Chi tiết liên hệ & Hạn sử dụng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (tenant.ownerPhone.isNotBlank()) "📞 ${tenant.ownerPhone}" else "📞 Chưa có SĐT",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val expiresText = if (tenant.subscriptionExpiresAt > 0L) {
                    val isExpired = tenant.subscriptionExpiresAt < System.currentTimeMillis()
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val dStr = sdf.format(java.util.Date(tenant.subscriptionExpiresAt))
                    if (isExpired) "⚠️ Hết hạn: $dStr" else "Hạn: $dStr"
                } else {
                    "Hạn: Vô thời hạn"
                }
                Text(
                    text = expiresText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (tenant.subscriptionExpiresAt in 1..System.currentTimeMillis()) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Nếu bị khóa, hiển thị lý do
            if (isLocked && tenant.lockedReason.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Lý do: ${tenant.lockedReason}",
                        fontSize = 12.sp,
                        color = Color(0xFFD84315),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            // Hàng 3: Các nút hành động
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Nút Khóa / Mở Khóa
                Button(
                    onClick = onToggleLock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLocked) Color(0xFF2E7D32) else Color(0xFFC62828)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isLocked) "Mở Khóa" else "Khóa App", fontSize = 12.sp)
                }

                // Nút Gia hạn / Đổi gói
                OutlinedButton(
                    onClick = onOpenExtendPlan,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Gia hạn / Gói", fontSize = 12.sp)
                }

                // Nút Góc nhìn Spa
                OutlinedButton(
                    onClick = onSwitchContext,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isCurrentActive) "Thoát xem" else "Góc nhìn", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DevCreateTenantDialog(
    store: SpaStore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tenantIdInput by remember { mutableStateOf("") }
    var shopNameInput by remember { mutableStateOf("") }
    var ownerPhoneInput by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("PLAN_PRO") }
    var selectedTrialDays by remember { mutableIntStateOf(30) }
    var isSubmitting by remember { mutableStateOf(false) }

    val planOptions = listOf("PLAN_FREE", "PLAN_BASIC", "PLAN_PRO", "PLAN_ENTERPRISE")
    val trialOptions = listOf(14, 30, 90, 365)

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text("➕ Thêm Cơ Sở Spa Mới", fontWeight = FontWeight.Bold, color = SpaRosewoodDark)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = tenantIdInput,
                    onValueChange = { tenantIdInput = it.lowercase().replace(" ", "_") },
                    label = { Text("Mã cơ sở (Tenant ID)") },
                    placeholder = { Text("vd: spa_quan1, anhduong_hcm") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = shopNameInput,
                    onValueChange = { shopNameInput = it },
                    label = { Text("Tên cơ sở Spa") },
                    placeholder = { Text("vd: Spa Khiếm Thị Ánh Dương") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ownerPhoneInput,
                    onValueChange = { ownerPhoneInput = it },
                    label = { Text("SĐT chủ quán / quản lý") },
                    placeholder = { Text("vd: 0987654321") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Gói dịch vụ:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    planOptions.forEach { p ->
                        FilterChip(
                            selected = selectedPlan == p,
                            onClick = { selectedPlan = p },
                            label = { Text(p.removePrefix("PLAN_")) }
                        )
                    }
                }

                Text("Thời hạn dùng thử:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    trialOptions.forEach { d ->
                        FilterChip(
                            selected = selectedTrialDays == d,
                            onClick = { selectedTrialDays = d },
                            label = { Text("$d ngày") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tenantIdInput.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập Tenant ID", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSubmitting = true
                    store.devCreateNewTenant(
                        tenantId = tenantIdInput,
                        shopName = shopNameInput,
                        ownerPhone = ownerPhoneInput,
                        planId = selectedPlan,
                        trialDays = selectedTrialDays
                    ) { ok, msg ->
                        isSubmitting = false
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        if (ok) onDismiss()
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Tạo cơ sở")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun DevLockTenantDialog(
    tenant: TenantInfo,
    store: SpaStore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val commonReasons = listOf(
        "Hết hạn thuê bao dịch vụ SaaS",
        "Chưa thanh toán cước phí định kỳ",
        "Tạm dừng hệ thống để bảo trì cơ sở",
        "Vi phạm điều khoản sử dụng nền tảng"
    )
    var selectedReason by remember { mutableStateOf(commonReasons[0]) }
    var customReason by remember { mutableStateOf("") }
    var qrUrlInput by remember { mutableStateOf(tenant.qrPaymentUrl.ifBlank { "https://img.vietqr.io/image/970422-0987654321-compact2.png?amount=500000&addInfo=GiaHan+${tenant.tenantId}" }) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFC62828))
                Spacer(Modifier.width(8.dp))
                Text("Khóa App Cơ Sở", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Bạn sắp kích hoạt Kill-Switch khóa cơ sở: ${tenant.shopName} (${tenant.tenantId}). Toàn bộ nhân viên và chủ spa sẽ thấy màn hình khóa.",
                    fontSize = 13.sp
                )

                Text("Chọn lý do khóa:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                commonReasons.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = r }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedReason == r && customReason.isBlank(),
                            onCheckedChange = { if (it) { selectedReason = r; customReason = "" } }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(r, fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = customReason,
                    onValueChange = { customReason = it },
                    label = { Text("Hoặc nhập lý do khác") },
                    placeholder = { Text("vd: Yêu cầu của chủ sở hữu...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = qrUrlInput,
                    onValueChange = { qrUrlInput = it },
                    label = { Text("Link QR Thanh toán VietQR (tùy chọn)") },
                    placeholder = { Text("https://img.vietqr.io/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = customReason.ifBlank { selectedReason }
                    isSubmitting = true
                    store.devToggleLockTenant(
                        tenantId = tenant.tenantId,
                        lock = true,
                        reason = finalReason,
                        qrUrl = qrUrlInput
                    ) { ok, msg ->
                        isSubmitting = false
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        if (ok) onDismiss()
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Xác nhận Khóa")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun DevExtendPlanDialog(
    tenant: TenantInfo,
    store: SpaStore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dayOptions = listOf(30, 90, 180, 365)
    var selectedDays by remember { mutableIntStateOf(30) }
    var selectedPlan by remember { mutableStateOf(tenant.planId) }
    val planOptions = listOf("PLAN_FREE", "PLAN_BASIC", "PLAN_PRO", "PLAN_ENTERPRISE")
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text("⏱️ Gia Hạn / Đổi Gói Dịch Vụ", fontWeight = FontWeight.Bold, color = SpaRosewoodDark)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Cơ sở: ${tenant.shopName} (${tenant.tenantId})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                Text("Cộng thêm ngày sử dụng:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dayOptions.forEach { d ->
                        FilterChip(
                            selected = selectedDays == d,
                            onClick = { selectedDays = d },
                            label = { Text("+$d ngày") }
                        )
                    }
                }

                Text("Gói cước thuê bao:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    planOptions.forEach { p ->
                        FilterChip(
                            selected = selectedPlan == p,
                            onClick = { selectedPlan = p },
                            label = { Text(p.removePrefix("PLAN_")) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    store.devExtendSubscription(tenant.tenantId, selectedDays) { ok1, msg1 ->
                        if (selectedPlan != tenant.planId) {
                            store.devChangeTenantPlan(tenant.tenantId, selectedPlan) { ok2, msg2 ->
                                isSubmitting = false
                                Toast.makeText(context, "$msg1 - $msg2", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        } else {
                            isSubmitting = false
                            Toast.makeText(context, msg1, Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Lưu thay đổi")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Hủy")
            }
        }
    )
}

// =========================================================================
// THÀNH PHẦN BÁO CÁO GIÀU THÔNG TIN (RICH REPORT CARD) & TIỆN ÍCH GEHIHI (R2)
// =========================================================================
data class ReportKpiData(
    val tongThu: String,
    val chiPhi: String,
    val laiRong: String,
    val soVe: String? = null,
    val ktvSummary: String? = null
)

fun extractReportKpi(rawText: String): ReportKpiData? {
    val clean = rawText.replace("**", "").replace("*", "")
    val hasKpiKeywords = clean.contains("Tổng thu", ignoreCase = true) ||
        clean.contains("Doanh thu", ignoreCase = true) ||
        clean.contains("Thu:", ignoreCase = true) ||
        clean.contains("Chi phí", ignoreCase = true) ||
        clean.contains("Trả KTV", ignoreCase = true) ||
        clean.contains("Hoa hồng", ignoreCase = true) ||
        clean.contains("Lãi ròng", ignoreCase = true) ||
        clean.contains("Thực nhận", ignoreCase = true) ||
        clean.contains("Lợi nhuận", ignoreCase = true) ||
        clean.contains("Tổng chi", ignoreCase = true)

    if (!hasKpiKeywords) return null

    val thuRegex = Regex("""(?:(?:\(?\s*(?:Tổng\s+[Tt]hu|Doanh\s+[Tt]hu|[Tt]hu)\s*\)?))\s*[:•]?\s*([+\-]?[0-9.,]+(?:\s*(?:đồng|dong|VND|vnđ|đ|k|tr))?)""", RegexOption.IGNORE_CASE)
    val chiRegex = Regex("""(?:(?:\(?\s*(?:Chi\s+phí|Trả\s+KTV|[Tt]ổng\s+[Cc]hi|Hoa\s+hồng(?:\s*&\s*Chi)?)\s*\)?))\s*[:•]?\s*([+\-]?[0-9.,]+(?:\s*(?:đồng|dong|VND|vnđ|đ|k|tr))?)""", RegexOption.IGNORE_CASE)
    val laiRegex = Regex("""(?:Chủ\s+quán\s+thực\s+nhận[^(:\n•]*(?:\([^)]*\))?|\(?\s*(?:Lãi\s+ròng|Thực\s+nhận|Lợi\s+nhuận)\s*\)?|\([^)]*(?:Lãi\s+ròng|Thực\s+nhận|Lợi\s+nhuận)[^)]*\))\s*[:•]?\s*([+\-]?[0-9.,]+(?:\s*(?:đồng|dong|VND|vnđ|đ|k|tr))?)""", RegexOption.IGNORE_CASE)
    val veRegex = Regex("""(\d+\s*vé)""", RegexOption.IGNORE_CASE)
    val ktvRegex = Regex("""(?:KTV|nhân viên)[\s:•\-]+([^\n,]+)""", RegexOption.IGNORE_CASE)

    val matchThu = thuRegex.find(clean)?.groupValues?.get(1)?.trim()
    val matchChi = chiRegex.find(clean)?.groupValues?.get(1)?.trim()
    val matchLai = laiRegex.find(clean)?.groupValues?.get(1)?.trim()
    val matchVe = veRegex.find(clean)?.groupValues?.get(1)?.trim()
    val matchKtv = ktvRegex.find(clean)?.groupValues?.get(1)?.trim()

    if (matchThu == null && matchChi == null && matchLai == null) return null

    fun formatKpiUnit(raw: String): String {
        val trimmed = raw.trim()
        val hasUnit = trimmed.endsWith("đ", ignoreCase = true) ||
            trimmed.endsWith("k", ignoreCase = true) ||
            trimmed.endsWith("tr", ignoreCase = true) ||
            trimmed.endsWith("vnd", ignoreCase = true) ||
            trimmed.endsWith("đồng", ignoreCase = true) ||
            trimmed.endsWith("dong", ignoreCase = true)
        return if (hasUnit) trimmed else "${trimmed}đ"
    }

    val fThu = matchThu ?: "0đ"
    val fChi = matchChi ?: "0đ"
    val fLai = matchLai ?: "0đ"

    return ReportKpiData(
        tongThu = formatKpiUnit(fThu),
        chiPhi = formatKpiUnit(fChi),
        laiRong = formatKpiUnit(fLai),
        soVe = matchVe,
        ktvSummary = matchKtv
    )
}

@Composable
fun RichReportCardComposable(
    kpi: ReportKpiData,
    onNavigateTab: (Int) -> Unit,
    onSwitchPeopleSubTab: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F20)),
        border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Báo cáo KPI tiệm: Tổng thu ${kpi.tongThu}, Chi phí ${kpi.chiPhi}, Lãi ròng ${kpi.laiRong}." +
                    (kpi.soVe?.let { " $it." } ?: "") +
                    (kpi.ktvSummary?.let { " KTV: $it." } ?: "")
            }
            .testTag("card_rich_report_kpi")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    tint = SpaChampagne,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "TỔNG HỢP CHỈ SỐ KPI TIỆM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SpaChampagne
                )
            }

            // 3 Card KPI: Tổng thu (Xanh lá), Chi phí (Đỏ), Lãi ròng (Vàng kim)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Tổng thu (Green)
                Surface(
                    color = Color(0xFF1B3B24),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF81C995).copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tổng Thu",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF81C995).copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = kpi.tongThu,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF81C995),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 2. Chi phí (Red)
                Surface(
                    color = Color(0xFF3E1B1B),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF8A80).copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Chi Phí",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF8A80).copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = kpi.chiPhi,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF8A80),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 3. Lãi ròng (Gold / Amber)
                Surface(
                    color = Color(0xFF3E3215),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Lãi Ròng",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFD54F).copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = kpi.laiRong,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Dòng tóm tắt vé & KTV nếu có
            if (!kpi.soVe.isNullOrBlank() || !kpi.ktvSummary.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    kpi.soVe?.let {
                        Surface(
                            color = Color(0xFF282A2C),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🎫 $it",
                                fontSize = 11.sp,
                                color = Color(0xFFE3E3E3),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    kpi.ktvSummary?.let {
                        Surface(
                            color = Color(0xFF282A2C),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "👥 $it",
                                fontSize = 11.sp,
                                color = Color(0xFFE3E3E3),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Hàng Quick Action Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { onNavigateTab(0) },
                    color = Color(0xFF282A2C),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "Chuyển sang màn hình Mở vé" }
                        .testTag("pill_action_open_tickets")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text("🎫 Mở vé", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    onClick = { onNavigateTab(3) },
                    color = Color(0xFF282A2C),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "Chuyển sang màn hình Xem sổ quỹ" }
                        .testTag("pill_action_view_cashbook")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text("📖 Xem sổ quỹ", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    onClick = {
                        onNavigateTab(1)
                        onSwitchPeopleSubTab(1)
                    },
                    color = Color(0xFF282A2C),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "Chuyển sang màn hình Gọi nhân viên KTV" }
                        .testTag("pill_action_call_ktv")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text("👥 Gọi KTV", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    onClick = { onNavigateTab(4) },
                    color = Color(0xFF282A2C),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "Chuyển sang màn hình Xem báo cáo doanh thu" }
                        .testTag("pill_action_view_reports")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text("📊 Xem báo cáo", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// =========================================================================
// HỘP THOẠI CẤU HÌNH GEMINI API KEY (R3 UI)
// =========================================================================
@Composable
fun AiKeyConfigDialog(
    store: SpaStore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    if (!store.currentUser.isAdmin()) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Chỉ Chủ Spa (Admin) mới có quyền cấu hình API Key!", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
        return
    }
    val scope = rememberCoroutineScope()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var keyInput by rememberSaveable { mutableStateOf(store.layGeminiApiKey()) }
    var showKey by rememberSaveable { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val isConnected = store.isGeminiConnected || store.layGeminiApiKey().isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E1F20),
            border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Hộp thoại Cài Đặt Gemini API Key"
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Tiêu đề hộp thoại
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = SpaRosewoodDark,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.5f)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = null,
                                    tint = SpaChampagne,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "Cài Đặt Gemini API Key",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                "Kết nối AI Google Gemini (gemini-3.5-flash-lite)",
                                fontSize = 11.sp,
                                color = Color(0xFFC4C7C5)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { contentDescription = "Đóng hộp thoại cài đặt API key" }
                            .testTag("btn_close_key_config_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }

                // 2. Huy hiệu trạng thái hiện tại
                Surface(
                    color = if (isConnected) Color(0xFF1B3B24) else Color(0xFF282A2C),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isConnected) Color(0xFF81C995).copy(alpha = 0.6f) else Color(0xFF444746)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {
                            contentDescription = if (isConnected) "Trạng thái: Trực tuyến. Đã có khóa Gemini AI." else "Trạng thái: Ngoại tuyến. Đang đọc dữ liệu tiệm 100%."
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isConnected) "✨" else "🟢",
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isConnected) {
                                val current = store.layGeminiApiKey()
                                val masked = if (current.length > 10) "${current.take(6)}...${current.takeLast(4)}" else current
                                "Trực tuyến: Đã kết nối Gemini API ($masked)"
                            } else {
                                "Ngoại tuyến: Sẵn sàng đọc dữ liệu tiệm 100% không cần key"
                            },
                            color = if (isConnected) Color(0xFF81C995) else Color(0xFFE3E3E3),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 3. Ô nhập liệu API Key
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = {
                        keyInput = it
                        testResult = null
                    },
                    label = { Text("Gemini API Key (AIzaSy... hoặc AQ....)") },
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (keyInput.isNotEmpty()) {
                                IconButton(
                                    onClick = { keyInput = "" },
                                    modifier = Modifier.semantics { contentDescription = "Xóa nội dung ô nhập key" }
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFFC4C7C5))
                                }
                            }
                            IconButton(
                                onClick = { showKey = !showKey },
                                modifier = Modifier.semantics { contentDescription = if (showKey) "Ẩn mã khóa" else "Hiện mã khóa" }
                            ) {
                                Icon(
                                    if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = SpaChampagne
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SpaChampagne,
                        unfocusedBorderColor = Color(0xFF555555),
                        focusedLabelColor = SpaChampagne,
                        unfocusedLabelColor = Color(0xFF999999)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tf_gemini_api_key")
                )

                // 4. Hàng nút thao tác nhanh: Dán & Xóa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text.orEmpty().trim()
                            if (clip.isNotBlank()) {
                                keyInput = clip
                                Toast.makeText(context, "Đã dán key từ bộ nhớ tạm!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Bộ nhớ tạm không có văn bản", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF555555)),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_paste_gemini_key")
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp), tint = SpaChampagne)
                        Spacer(Modifier.width(6.dp))
                        Text("Dán từ bộ nhớ", fontSize = 12.sp, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { keyInput = "" },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF555555)),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_clear_text_key")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF8A80))
                        Spacer(Modifier.width(6.dp))
                        Text("Làm trống ô", fontSize = 12.sp, color = Color.White)
                    }
                }

                // 5. Nút Test kết nối AI
                Button(
                    onClick = {
                        val toTest = keyInput.trim()
                        if (toTest.isBlank()) {
                            testResult = Pair(false, "Vui lòng nhập hoặc dán mã API Key trước khi test.")
                            return@Button
                        }
                        isTesting = true
                        testResult = null
                        scope.launch {
                            val res = store.kiemTraGeminiApiKey(toTest)
                            isTesting = false
                            testResult = res
                        }
                    },
                    enabled = !isTesting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00668B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_test_gemini_key")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Đang kiểm tra kết nối với Google AI...", fontSize = 13.sp, color = Color.White)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFA8C7FA))
                        Spacer(Modifier.width(8.dp))
                        Text("⚡ Test Kết Nối AI (200 OK)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }

                // Kết quả test nếu có
                testResult?.let { (success, msg) ->
                    Surface(
                        color = if (success) Color(0xFF1B3B24) else Color(0xFF3E1B1B),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (success) Color(0xFF81C995) else Color(0xFFFF8A80)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                contentDescription = if (success) "Kiểm tra thành công: $msg" else "Kiểm tra thất bại: $msg"
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (success) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (success) Color(0xFF81C995) else Color(0xFFFF8A80),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = msg,
                                color = if (success) Color(0xFF81C995) else Color(0xFFFF8A80),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 6. Cặp nút Lưu khóa & Gỡ khóa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val clean = keyInput.trim()
                            if (clean.isBlank()) {
                                Toast.makeText(context, "Vui lòng nhập API Key để lưu!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            store.luuGeminiApiKey(clean)
                            Toast.makeText(context, "Đã lưu Gemini API Key thành công!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpaRosewoodDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("btn_save_gemini_key")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SpaChampagne, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("💾 Lưu Khóa Vào Máy", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            store.xoaGeminiApiKey()
                            keyInput = ""
                            testResult = null
                            Toast.makeText(context, "Đã gỡ khóa cá nhân, chuyển sang dùng ngoại tuyến 100%!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80)),
                        border = BorderStroke(1.dp, Color(0xFFFF8A80).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_clear_gemini_key")
                    ) {
                        Text("✕ Gỡ Khóa", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFFFF8A80))
                    }
                }

                // 7. Khung hướng dẫn 3 bước tiếng Việt + Nút mở Google AI Studio
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF282A2C)),
                    border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Hướng dẫn 3 bước lấy Gemini API Key miễn phí. Bước 1: Mở Google AI Studio. Bước 2: Bấm Create API Key. Bước 3: Dán mã vào ứng dụng."
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "💡 Hướng Dẫn 3 Bước Lấy Key Miễn Phí (1 Phút)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SpaChampagne
                        )
                        Text(
                            "• Bước 1: Mở Google AI Studio bằng tài khoản Google bất kỳ.\n" +
                            "• Bước 2: Bấm nút \"Create API key\" (Miễn phí 100%, không yêu cầu thẻ tín dụng).\n" +
                            "• Bước 3: Sao chép chuỗi mã (bắt đầu bằng AIzaSy... hoặc AQ....) và dán vào ô trên, sau đó bấm \"Lưu Khóa Vào Máy\".",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFE3E3E3)
                        )
                        Button(
                            onClick = {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }.onFailure {
                                    Toast.makeText(context, "Không thể mở trình duyệt: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpaChampagne.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_open_google_ai_studio")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = SpaChampagne, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "🌐 Mở Google AI Studio để lấy Key Miễn Phí",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// DIALOG TRỢ LÝ AI "GEHIHI" — ĐIỀU KHIỂN APP TOÀN QUYỀN (CHỈ DÀNH CHO ADMIN)
// Có cơ chế Xác nhận lại trước khi Thêm/Xóa & Bộ nhớ Vĩnh viễn không bao giờ quên
// =========================================================================
@Composable
fun AdminAiAssistantBotDialog(
    store: SpaStore,
    onNavigateTab: (Int) -> Unit,
    onSwitchTicketSubTab: (Int) -> Unit,
    onSwitchPeopleSubTab: (Int) -> Unit,
    onInspectStaffPayroll: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenAiKeyConfig: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var inputPrompt by remember { mutableStateOf("") }
    var showQuickSuggestions by rememberSaveable { mutableStateOf(true) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var attachedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var attachedFileName by remember { mutableStateOf("") }
    var attachedFileContent by remember { mutableStateOf("") }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var showKeyConfigInBot by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    val chatScrollState = rememberScrollState()
    val pendingConfirm = store.pendingAiConfirmation

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale("vi", "VN")
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.onSuccess { bmp ->
                if (bmp != null) {
                    attachedBitmap = bmp
                    showAttachmentMenu = false
                    Toast.makeText(context, "Đã đính kèm ảnh cho gehihi", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val name = uri.lastPathSegment?.substringAfterLast("/") ?: "backup_data.json"
                name to text
            }.onSuccess { (name, text) ->
                if (text.isNotBlank()) {
                    attachedFileName = name
                    attachedFileContent = text
                    showAttachmentMenu = false
                    Toast.makeText(context, "Đã đính kèm file: $name", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen = imeBottomPx > 0

    BackHandler(onBack = onDismiss)

    LaunchedEffect(store.aiMessages.size, pendingConfirm?.id, chatScrollState.maxValue, imeBottomPx) {
        chatScrollState.animateScrollTo(chatScrollState.maxValue)
    }

    val currentUser = store.currentUser
    val isAdmin = currentUser.isAdmin()

    val quickCommands = if (isAdmin) {
        listOf(
            "Chuyển hướng tới mục nhân viên",
            "Mai là thứ mấy?",
            "Báo cáo doanh thu hôm nay",
            "Sửa mail admin",
            "Sửa dịch vụ Chườm đá nóng giá 80k hoa hồng 30k",
            "Hoàn thành tất cả vé đang làm",
            "Chốt tất cả vé đang chờ",
            "Bảng lương hoa hồng KTV tháng này"
        )
    } else {
        listOf(
            "Hôm nay tôi đã làm được mấy vé?",
            "Hoa hồng hôm nay của tôi là bao nhiêu?",
            "Mai là thứ mấy?",
            "Gợi ý kỹ thuật massage bấm huyệt cổ vai gáy",
            "Mẹo chăm sóc khách hàng giúp khách thư giãn",
            "Danh sách dịch vụ của quán"
        )
    }

    val handleActionAutoClose: (String) -> Unit = { summary ->
        Toast.makeText(
            context,
            "✅ $summary (Đã chuyển tới màn hình vừa cập nhật)",
            Toast.LENGTH_LONG
        ).show()
        onDismiss()
    }

    // Bảng màu chuẩn Gemini / AI Studio Mobile Dark Canvas (#131314)
    val geminiBg = Color(0xFF131314)
    val geminiComposerBg = Color(0xFF1E1F20)
    val geminiChipBg = Color(0xFF282A2C)
    val geminiBorder = Color(0xFF333537)
    val geminiChipBorder = Color(0xFF444746)
    val geminiTextPrimary = Color(0xFFE3E3E3)
    val geminiTextSecondary = Color(0xFFC4C7C5)
    val geminiAccentBlue = Color(0xFFA8C7FA)
    val geminiAccentPurple = Color(0xFFD0BCFF)

    if (showKeyConfigInBot && store.currentUser.isAdmin()) {
        AiKeyConfigDialog(
            store = store,
            onDismiss = { showKeyConfigInBot = false }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Xóa Lịch Sử Trò Chuyện?", fontWeight = FontWeight.Bold) },
            text = { Text("Hành động này sẽ làm sạch các tin nhắn hội thoại của gehihi trên thiết bị này. Bộ nhớ tri thức vĩnh viễn của quán vẫn được bảo toàn.") },
            confirmButton = {
                Button(
                    onClick = {
                        store.xoaLichSuTroChuyenGehihi()
                        showClearConfirmDialog = false
                        Toast.makeText(context, "Đã làm sạch lịch sử trò chuyện gehihi!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaDanger)
                ) {
                    Text("Xóa lịch sử", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (isFullscreen) 1f else 0.55f))
            .imePadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = geminiBg,
            shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = if (isFullscreen) {
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            } else {
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. HEADER BAR CHUẨN ROSEWOOD/CHAMPAGNE (R2)
                Surface(
                    color = SpaRosewoodDark,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { heading() }
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SpaChampagne,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "gehihi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Surface(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = if (isAdmin) "Chủ Spa • ${store.aiLongTermMemories.size} nhớ" else "KTV • Trợ lý",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SpaChampagne,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (store.currentUser.isAdmin()) {
                                    // Nút ⚙️/🔑 Cấu hình Gemini Key
                                    IconButton(
                                        onClick = {
                                            if (onOpenAiKeyConfig != null) {
                                                onOpenAiKeyConfig()
                                            } else {
                                                showKeyConfigInBot = true
                                            }
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .semantics { contentDescription = "Cài đặt khóa Gemini API Key" }
                                            .testTag("btn_gehihi_key_settings")
                                    ) {
                                        Icon(
                                            Icons.Default.Key,
                                            contentDescription = null,
                                            tint = SpaChampagne,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Nút Chuyển đổi Fullscreen
                                IconButton(
                                    onClick = { isFullscreen = !isFullscreen },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .semantics { contentDescription = if (isFullscreen) "Thu gọn cửa sổ trợ lý" else "Mở rộng toàn màn hình trợ lý" }
                                        .testTag("btn_toggle_fullscreen")
                                ) {
                                    Icon(
                                        if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Nút Xóa lịch sử trò chuyện
                                IconButton(
                                    onClick = { showClearConfirmDialog = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .semantics { contentDescription = "Xóa lịch sử trò chuyện gehihi" }
                                        .testTag("btn_clear_chat_history")
                                ) {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = Color(0xFFFF8A80),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Nút Gợi ý nhanh
                                TextButton(
                                    onClick = { showQuickSuggestions = !showQuickSuggestions },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(
                                        text = if (showQuickSuggestions) "Ẩn gợi ý" else "Gợi ý",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFC4C7C5)
                                    )
                                }

                                // Nút Đóng dialog
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .semantics { contentDescription = "Đóng cửa sổ trợ lý AI gehihi" }
                                        .testTag("btn_close_gehihi_dialog")
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Dynamic Connection Status Badge
                        Spacer(Modifier.height(4.dp))
                        val isGeminiReady = store.isGeminiConnected || store.layGeminiApiKey().isNotBlank()
                        Surface(
                            color = if (isGeminiReady) Color(0xFF3E3215) else Color(0xFF1B3B24),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (isGeminiReady) SpaChampagne.copy(alpha = 0.7f) else Color(0xFF81C995).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {
                                    contentDescription = if (isGeminiReady) "Trạng thái: Trực tuyến. Gemini AI kết nối." else "Trạng thái: Ngoại tuyến. Sẵn sàng đọc dữ liệu tiệm 100%."
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isGeminiReady) "✨ Trực tuyến: Gemini AI kết nối" else "🟢 Ngoại tuyến: Sẵn sàng đọc dữ liệu tiệm 100%",
                                    color = if (isGeminiReady) SpaChampagne else Color(0xFF81C995),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

            // 2. GỢI Ý NHANH (TỰ ẨN KHI BẬT BÀN PHÍM HOẶC ĐANG GÕ CHỮ ĐỂ NHƯỜNG CHỖ CHO NỘI DUNG CHAT)
            if (showQuickSuggestions && !isKeyboardOpen && inputPrompt.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    quickCommands.forEach { cmd ->
                        Surface(
                            onClick = {
                                scope.launch {
                                    store.guiYeuCauAiBot(
                                        userPrompt = cmd,
                                        onNavigateTab = onNavigateTab,
                                        onSwitchTicketSubTab = onSwitchTicketSubTab,
                                        onSwitchPeopleSubTab = onSwitchPeopleSubTab,
                                        onInspectStaffPayroll = onInspectStaffPayroll,
                                        onCloseDialogOnAction = handleActionAutoClose
                                    )
                                }
                            },
                            color = geminiComposerBg,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, geminiBorder),
                            modifier = Modifier.semantics {
                                contentDescription = "Chạy nhanh với gehihi: $cmd"
                            }
                        ) {
                            Text(
                                text = cmd,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = geminiTextPrimary,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // 3. LUỒNG TRÒ CHUYỆN TRẢI RỘNG TRÊN NỀN TỐI (GIỐNG HỆT ẢNH MẪU)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(chatScrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                store.aiMessages.takeLast(100).forEach { msg ->
                    val isUser = msg.isUser
                    val displayMsgText = msg.text.replace("**", "")
                    if (isUser) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                color = geminiChipBg,
                                shape = RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 6.dp
                                ),
                                border = BorderStroke(1.dp, geminiBorder),
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .semantics(mergeDescendants = true) {
                                        liveRegion = LiveRegionMode.Polite
                                        contentDescription = "${if (isAdmin) "Chủ Spa" else "Kỹ thuật viên"}: $displayMsgText"
                                    }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(
                                        text = displayMsgText,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        color = geminiTextPrimary
                                    )
                                    if (msg.thoiGian.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = msg.thoiGian,
                                            fontSize = 10.sp,
                                            color = geminiTextSecondary.copy(alpha = 0.7f),
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {
                                    liveRegion = LiveRegionMode.Polite
                                    contentDescription = "gehihi: $displayMsgText" +
                                        (if (msg.actionNote.isNotBlank()) ". Trạng thái: ${msg.actionNote}" else "")
                                },
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = geminiAccentBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Surface(
                                        color = geminiChipBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "gehihi",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = geminiTextPrimary,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (msg.thoiGian.isNotBlank()) {
                                    Text(
                                        text = msg.thoiGian,
                                        fontSize = 10.sp,
                                        color = geminiTextSecondary.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            val kpiData = extractReportKpi(displayMsgText)

                            Text(
                                text = displayMsgText,
                                fontSize = 15.sp,
                                lineHeight = 23.sp,
                                color = geminiTextPrimary
                            )

                            // THẺ BÁO CÁO KẾT QUẢ GIÀU THÔNG TIN (RICH REPORT CARD)
                            if (kpiData != null) {
                                RichReportCardComposable(
                                    kpi = kpiData,
                                    onNavigateTab = onNavigateTab,
                                    onSwitchPeopleSubTab = onSwitchPeopleSubTab
                                )
                            }

                            if (msg.actionNote.isNotBlank()) {
                                Surface(
                                    color = geminiComposerBg,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, geminiBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 7.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✓ ${msg.actionNote}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = geminiAccentBlue,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(
                                            onClick = onDismiss,
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(
                                                "Xem màn hình >",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = geminiAccentPurple
                                            )
                                        }
                                    }
                                }
                            }

                            // TIỆN ÍCH ĐIỀU KHIỂN: [📋 Sao chép] & [🔊 Đọc to] & [🔑 Mở Cài Đặt Key]
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(displayMsgText))
                                        Toast.makeText(context, "Đã sao chép câu trả lời của gehihi!", Toast.LENGTH_SHORT).show()
                                    },
                                    color = geminiComposerBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, geminiBorder),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .semantics { contentDescription = "Sao chép câu trả lời vào bộ nhớ tạm" }
                                        .testTag("btn_copy_response")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = geminiTextSecondary, modifier = Modifier.size(14.dp))
                                        Text("Sao chép", fontSize = 11.sp, color = geminiTextSecondary)
                                    }
                                }

                                Surface(
                                    onClick = {
                                        tts?.speak(displayMsgText, TextToSpeech.QUEUE_FLUSH, null, "gehihi_tts_${msg.id}")
                                        view.announceForAccessibility(displayMsgText)
                                    },
                                    color = geminiComposerBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, geminiBorder),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .semantics { contentDescription = "Đọc câu trả lời bằng giọng nói" }
                                        .testTag("btn_speak_response")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = geminiTextSecondary, modifier = Modifier.size(14.dp))
                                        Text("Đọc to", fontSize = 11.sp, color = geminiTextSecondary)
                                    }
                                }

                                if (store.currentUser.isAdmin() && (displayMsgText.contains("Cài Đặt Gemini API Key", ignoreCase = true) ||
                                    msg.actionNote.contains("Gemini API Key", ignoreCase = true))) {
                                    Surface(
                                        onClick = {
                                            if (onOpenAiKeyConfig != null) {
                                                onOpenAiKeyConfig()
                                            } else {
                                                showKeyConfigInBot = true
                                            }
                                        },
                                        color = SpaRosewoodDark,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.7f)),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .semantics { contentDescription = "Mở hộp thoại Cài đặt Gemini API Key" }
                                            .testTag("btn_open_key_from_chat")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Key, contentDescription = null, tint = SpaChampagne, modifier = Modifier.size(14.dp))
                                            Text("Mở Cài Đặt Key", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (store.aiIsLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(8.dp)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = "gehihi đang trả lời"
                            }
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = geminiAccentBlue
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "gehihi đang xử lý...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = geminiTextSecondary
                        )
                    }
                }
            }

            // 4. HỘP XÁC NHẬN AN TOÀN KHI ADMIN RA LỆNH THÊM, SỬA HOẶC XÓA (GHIM NGAY TRÊN Ô NHẬP)
            if (pendingConfirm != null) {
                val accentColor = when (pendingConfirm.loai) {
                    LoaiLenhChoXacNhan.XOA -> Color(0xFFFF8A80)
                    LoaiLenhChoXacNhan.SUA -> geminiAccentBlue
                    LoaiLenhChoXacNhan.THEM -> Color(0xFF81C995)
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = geminiComposerBg
                    ),
                    border = BorderStroke(1.5.dp, accentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .semantics(mergeDescendants = true) {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = "Yêu cầu xác nhận lệnh ${pendingConfirm.loai.tenHienThi}: ${pendingConfirm.tieuDeTomTat}. ${pendingConfirm.chiTietXacNhan}. Vui lòng chạm Xác nhận thực hiện hoặc Hủy lệnh."
                        }
                        .testTag("card_gehihi_pending_confirm")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = accentColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "XÁC NHẬN ${pendingConfirm.loai.tenHienThi}",
                                    color = accentColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = pendingConfirm.tieuDeTomTat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = geminiTextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = pendingConfirm.chiTietXacNhan,
                            fontSize = 13.sp,
                            color = geminiTextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    store.xacNhanThucThiLenhGehihi(
                                        onNavigateTab = onNavigateTab,
                                        onSwitchTicketSubTab = onSwitchTicketSubTab,
                                        onSwitchPeopleSubTab = onSwitchPeopleSubTab,
                                        onInspectStaffPayroll = onInspectStaffPayroll,
                                        onCloseDialogOnAction = handleActionAutoClose
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = Color(0xFF131314)
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .semantics {
                                        contentDescription = "Xác nhận thực hiện lệnh ${pendingConfirm.tieuDeTomTat} và chuyển tới màn hình vừa sửa"
                                    }
                                    .testTag("btn_confirm_gehihi_action")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Xác nhận & Xem ngay", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    store.huyBoLenhChoXacNhanGehihi()
                                },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color(0xFFFF8A80)),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .semantics {
                                        contentDescription = "Hủy bỏ lệnh ${pendingConfirm.tieuDeTomTat}"
                                    }
                                    .testTag("btn_cancel_gehihi_action")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFFF8A80))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hủy", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // 5. KHUNG NHẬP ĐA TẦNG BO GÓC NẰM NGAY TRÊN BÀN PHÍM (CHUẨN 100% THEO ẢNH MẪU)
            Surface(
                color = geminiComposerBg,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, geminiBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // TẦNG 1 (TÙY CHỌN): THẺ ĐÍNH KÈM ẢNH / FILE JSON GIỐNG HỆT [screenshot_1.png / Image  X] TRONG ẢNH MẪU
                    if (attachedBitmap != null || attachedFileName.isNotBlank()) {
                        Surface(
                            color = geminiChipBg,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, geminiChipBorder),
                            modifier = Modifier.fillMaxWidth(0.75f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (attachedBitmap != null) {
                                        Image(
                                            bitmap = attachedBitmap!!.asImageBitmap(),
                                            contentDescription = "Ảnh đính kèm",
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "screenshot_attached.png",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = geminiTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Image",
                                                fontSize = 11.sp,
                                                color = geminiTextSecondary
                                            )
                                        }
                                    } else {
                                        Column {
                                            Text(
                                                text = attachedFileName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = geminiTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "JSON • ${attachedFileContent.length} ký tự",
                                                fontSize = 11.sp,
                                                color = geminiTextSecondary
                                            )
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        attachedBitmap = null
                                        attachedFileName = ""
                                        attachedFileContent = ""
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Bỏ đính kèm",
                                        tint = geminiTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Menu chọn Ảnh hoặc File JSON khi bấm nút (+)
                    if (showAttachmentMenu) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = {
                                    pickPhotoLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                color = geminiChipBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, geminiChipBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_attach_image_gehihi")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "🖼️ Đính kèm Ảnh",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = geminiTextPrimary
                                    )
                                }
                            }
                            Surface(
                                onClick = {
                                    pickFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                                },
                                color = geminiChipBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, geminiChipBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_attach_file_gehihi")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "📄 Đính kèm File JSON",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = geminiTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // TẦNG 2: VÙNG NHẬP CHỮ TRẢI RỘNG (KHÔNG BỊ VIỀN HAY NÚT CHE KHUẤT)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 28.dp, max = 110.dp)
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputPrompt.isEmpty()) {
                            Text(
                                text = if (pendingConfirm != null) "Gõ 'xác nhận' hoặc 'hủy'..."
                                else "Nhắn cho gehihi hoặc ra lệnh Thêm/Sửa/Xóa...",
                                fontSize = 15.sp,
                                color = Color(0xFF8E918F),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = inputPrompt,
                            onValueChange = { inputPrompt = it },
                            textStyle = TextStyle(
                                color = geminiTextPrimary,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            ),
                            cursorBrush = SolidColor(geminiAccentBlue),
                            maxLines = 5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_ai_prompt")
                        )
                    }

                    // TẦNG 3: THANH CÔNG CỤ DƯỚI ĐÁY KHUNG NHẬP: [ (+) ] bên trái  ---  [ Full Quyền ▾ ] [ ↑ ] bên phải
                    val canSend = (inputPrompt.isNotBlank() || attachedBitmap != null || attachedFileContent.isNotBlank()) && !store.aiIsLoading
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Nút (+) tròn có viền bên trái giống hệt ảnh mẫu
                        Surface(
                            onClick = { showAttachmentMenu = !showAttachmentMenu },
                            color = if (showAttachmentMenu) geminiAccentBlue else geminiChipBg,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, geminiChipBorder),
                            modifier = Modifier
                                .size(48.dp)
                                .semantics {
                                    contentDescription = "Đính kèm ảnh hoặc file JSON cho gehihi"
                                }
                                .testTag("btn_toggle_attachment_gehihi")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (showAttachmentMenu) Color(0xFF131314) else geminiTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Cụm nút bên phải: [ Full Quyền ▾ ] + Nút mũi tên lên [ ↑ ]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { showQuickSuggestions = !showQuickSuggestions },
                                color = geminiChipBg,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, geminiChipBorder),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (isAdmin) "Full Quyền ▾" else "KTV ▾",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = geminiTextPrimary
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    if (canSend) {
                                        val textToSend = inputPrompt.trim()
                                        val bmpToSend = attachedBitmap
                                        val fileNameToSend = attachedFileName
                                        val fileContentToSend = attachedFileContent
                                        if (textToSend.isNotBlank() || bmpToSend != null || fileContentToSend.isNotBlank()) {
                                            inputPrompt = ""
                                            attachedBitmap = null
                                            attachedFileName = ""
                                            attachedFileContent = ""
                                            showAttachmentMenu = false

                                            val norm = textToSend.lowercase().trim()
                                            val isKeyConfigIntent = norm.contains("cai dat api key") || norm.contains("cài đặt api key") ||
                                                norm.contains("sua api key") || norm.contains("sửa api key") ||
                                                norm.contains("doi api key") || norm.contains("đổi api key") ||
                                                norm.contains("cau hinh gemini") || norm.contains("cấu hình gemini") ||
                                                norm.contains("cai api key") || norm.contains("cài api key") ||
                                                norm.contains("nhap api key") || norm.contains("nhập api key")

                                            if (isKeyConfigIntent && bmpToSend == null && fileContentToSend.isBlank()) {
                                                if (!store.currentUser.isAdmin()) {
                                                    store.aiMessages.add(
                                                        AiChatMessage(
                                                            id = UUID.randomUUID().toString(),
                                                            isUser = true,
                                                            text = textToSend,
                                                            thoiGian = "${todayDateStr()} ${nowTimeStr()}"
                                                        )
                                                    )
                                                    store.aiMessages.add(
                                                        AiChatMessage(
                                                            id = UUID.randomUUID().toString(),
                                                            isUser = false,
                                                            text = "Dạ, tính năng Cài Đặt Gemini API Key chỉ dành riêng cho Chủ Spa (Quản trị viên) để bảo mật hệ thống. Bạn vui lòng liên hệ Chủ Spa để được hỗ trợ nhé!",
                                                            actionNote = "Từ chối quyền cấu hình Key",
                                                            thoiGian = "${todayDateStr()} ${nowTimeStr()}"
                                                        )
                                                    )
                                                    store.saveGehihiMemoryVault()
                                                } else {
                                                    if (onOpenAiKeyConfig != null) {
                                                        onOpenAiKeyConfig()
                                                    } else {
                                                        showKeyConfigInBot = true
                                                    }
                                                    store.aiMessages.add(
                                                        AiChatMessage(
                                                            id = UUID.randomUUID().toString(),
                                                            isUser = true,
                                                            text = textToSend,
                                                            thoiGian = "${todayDateStr()} ${nowTimeStr()}"
                                                        )
                                                    )
                                                    store.aiMessages.add(
                                                        AiChatMessage(
                                                            id = UUID.randomUUID().toString(),
                                                            isUser = false,
                                                            text = "Dạ, em đã mở hộp thoại Cài Đặt Gemini API Key để bạn cấu hình khóa trực quan rồi nhé! Bạn có thể dán mã khóa, kiểm tra kết nối 200 OK và lưu vào máy.",
                                                            actionNote = "Cài đặt Gemini API Key",
                                                            thoiGian = "${todayDateStr()} ${nowTimeStr()}"
                                                        )
                                                    )
                                                    store.saveGehihiMemoryVault()
                                                }
                                            } else {
                                                scope.launch {
                                                    store.guiYeuCauAiBot(
                                                        userPrompt = textToSend,
                                                        attachedBitmap = bmpToSend,
                                                        attachedFileName = fileNameToSend,
                                                        attachedFileContent = fileContentToSend,
                                                        onNavigateTab = onNavigateTab,
                                                        onSwitchTicketSubTab = onSwitchTicketSubTab,
                                                        onSwitchPeopleSubTab = onSwitchPeopleSubTab,
                                                        onInspectStaffPayroll = onInspectStaffPayroll,
                                                        onCloseDialogOnAction = handleActionAutoClose
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                color = if (canSend) Color(0xFF28354A) else geminiChipBg,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    1.dp,
                                    if (canSend) geminiAccentBlue.copy(alpha = 0.55f) else geminiChipBorder
                                ),
                                modifier = Modifier
                                    .size(48.dp)
                                    .semantics {
                                        contentDescription = "Gửi yêu cầu cho gehihi"
                                    }
                                    .testTag("btn_send_ai_prompt")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.KeyboardArrowUp,
                                        contentDescription = null,
                                        tint = if (canSend) geminiAccentBlue else geminiTextSecondary.copy(alpha = 0.45f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// =========================================================================
// DIALOG TÀI KHOẢN: TÁCH BIỆT HOÀN TOÀN NHÂN VIÊN VÀ ADMIN
// Nhân viên không thể tự ý chuyển sang Admin nếu không nhập đúng SĐT + Mật khẩu!
// =========================================================================
@Composable
fun AccountSwitchDialog(
    store: SpaStore,
    onGoToAdminPermissionTab: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val current = store.currentUser
    val isCurrentAdmin = current.isAdmin()

    var phoneInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tài khoản đang đăng nhập", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (current != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(14.dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "Đang đăng nhập: ${current.hoTen}, số điện thoại ${current.username}, ${if (isCurrentAdmin) "có mác Admin" else "tài khoản nhân viên"}."
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(current.hoTen, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                if (isCurrentAdmin) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = SpaChampagne,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Admin",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                "SĐT đăng nhập: ${current.username}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SpaRosewoodDark
                            )
                            Text(
                                if (isCurrentAdmin) "Quyền hạn: Toàn quyền quản trị (Admin)"
                                else "Quyền hạn: Nhân viên (Cần sửa SĐT/mật khẩu vui lòng nhờ Admin sửa hộ)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isCurrentAdmin) {
                    Button(
                        onClick = onGoToAdminPermissionTab,
                        colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mở Tab Quyền hạn & Quản lý Tài khoản", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    "Đăng nhập sang tài khoản khác (Nhập Số điện thoại & Mật khẩu):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Số điện thoại đăng nhập") },
                    placeholder = { Text("VD: 0912345678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Mật khẩu") },
                    placeholder = { Text("VD: 123456") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        store.logout()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .semantics {
                            contentDescription = "Đăng xuất khỏi tài khoản hiện tại để quay về màn hình đăng nhập"
                        }
                ) {
                    Text("Đăng xuất khỏi máy này", color = SpaDanger, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phoneInput.isNotBlank() && passwordInput.isNotBlank()) {
                        val ok = store.login(phoneInput, passwordInput)
                        if (ok) {
                            Toast.makeText(
                                context,
                                "Đã chuyển sang tài khoản ${store.currentUser?.hoTen}!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Sai số điện thoại hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                modifier = Modifier.height(48.dp)
            ) {
                Text(if (phoneInput.isNotBlank()) "Đăng nhập" else "Đóng")
            }
        },
        dismissButton = {
            if (phoneInput.isNotBlank()) {
                TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text("Đóng") }
            }
        }
    )
}

@Composable
fun GlobalMaintenanceScreen(
    store: SpaStore,
    user: TaiKhoan?
) {
    val context = LocalContext.current
    val isOwnerOrAdmin = user?.isAdmin() == true || user?.isOwner() == true
    val maintenanceMsg = store.globalMaintenanceMessage.ifBlank {
        "Hệ thống đang bảo trì nâng cấp kỹ thuật định kỳ để nâng cao chất lượng dịch vụ."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .semantics(mergeDescendants = true) {
                heading()
                contentDescription = if (isOwnerOrAdmin) {
                    "Hệ thống đang bảo trì toàn diện. $maintenanceMsg. Bạn có thể bấm nút Liên hệ Kỹ thuật Dev qua Zalo để được hỗ trợ trực tiếp."
                } else {
                    "Hệ thống đang bảo trì, vui lòng liên hệ Chủ Spa."
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = Color(0xFFFF9800).copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🛠️", fontSize = 36.sp)
                    }
                }

                Text(
                    text = "HỆ THỐNG ĐANG BẢO TRÌ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = SpaRosewoodDark,
                    textAlign = TextAlign.Center
                )

                if (isOwnerOrAdmin) {
                    // Màn hình dành cho Chủ Spa / Admin
                    Text(
                        text = maintenanceMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Surface(
                        color = SpaChampagne.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Khi kỹ thuật viên Dev hoàn tất bảo trì trên hệ thống, ứng dụng sẽ tự động mở lại bình thường mà không cần đăng nhập lại.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpaRosewood,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://zalo.me/0397772963")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }.onFailure {
                                Toast.makeText(context, "Hotline/Zalo Kỹ thuật Dev: 0397772963", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .semantics {
                                contentDescription = "Liên hệ Kỹ thuật Dev qua Zalo"
                            }
                            .testTag("btn_contact_dev_zalo")
                    ) {
                        Text("💬 LIÊN HỆ KỸ THUẬT DEV QUA ZALO", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    // Màn hình dành cho Kỹ thuật viên (KTV)
                    Text(
                        text = "Hệ thống đang bảo trì, vui lòng liên hệ Chủ Spa.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Ứng dụng sẽ tự động mở lại ngay khi Chủ Spa và hệ thống kết thúc thời gian bảo trì định kỳ.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TenantLockedScreen(
    store: SpaStore,
    onDismissDev: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isDev = store.currentUser?.isDev() == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
            .semantics { contentDescription = "Tài khoản bị khóa. Vui lòng liên hệ hỗ trợ để gia hạn." },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFFFF6B6B),
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "Tài Khoản Tạm Thời Bị Khóa",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            if (store.tenantLockReason.isNotBlank()) {
                Text(
                    text = store.tenantLockReason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFD93D),
                    textAlign = TextAlign.Center
                )
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Text(
                text = "Vui lòng quét mã VietQR bên dưới để gia hạn gói dịch vụ, hoặc liên hệ hotline hỗ trợ.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            if (store.tenantLockQrUrl.isNotBlank()) {
                // Hiển thị URL thanh toán
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "🔗 ${store.tenantLockQrUrl}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }
            }

            // Ghi chú nhắc nhở chụp màn hình thanh toán
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C4E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                    contentDescription = "Lưu ý sau khi thanh toán: Quý khách vui lòng chụp lại màn hình giao dịch chuyển khoản thành công và liên hệ nhà phát triển qua Zalo để được kích hoạt sử dụng tức thì."
                }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📸 LƯU Ý SAU KHI THANH TOÁN:", color = Color(0xFFFFD93D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "Quý khách vui lòng chụp lại màn hình giao dịch chuyển khoản thành công và liên hệ nhà phát triển qua Zalo (0397772963) để được xác nhận và kích hoạt ngày dùng tức thì!",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Thoát góc nhìn cơ sở dành cho Super Admin Developer
            if (isDev || onDismissDev != null) {
                Button(
                    onClick = {
                        if (onDismissDev != null) {
                            onDismissDev()
                        } else {
                            store.devSwitchTenantContext(null)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Thoát góc nhìn Spa (Về Bàn điều khiển Dev)")
                }
            }

            OutlinedButton(
                onClick = {
                    runCatching {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://zalo.me/0397772963")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }.onFailure {
                        Toast.makeText(context, "Hotline/Zalo hỗ trợ: 0397772963", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                modifier = Modifier.semantics {
                    contentDescription = "Liên hệ hỗ trợ qua Zalo nhà phát triển"
                }
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Liên hệ hỗ trợ (Zalo)")
            }

            if (isDev) {
                Button(
                    onClick = {
                        store.devToggleLockTenant(store.currentTenantId, false, "Mở khóa bởi Super Admin")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("👑 Mở khóa ngay (Quyền Super Admin)", color = Color.White)
                }
            }

            TextButton(
                onClick = { store.logout() },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Đăng xuất tài khoản")
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                heading()
                contentDescription = "$title. $subtitle"
            }
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = SpaRosewoodDark
        )
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = message
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(message, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
