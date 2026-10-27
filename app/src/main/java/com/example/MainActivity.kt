package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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

    // Khi cài APK lần đầu hoặc khi đã đăng xuất -> Bắt buộc qua màn hình Đăng nhập 1 lần duy nhất
    if (user == null) {
        OneTimeLoginScreen(store = store)
        return
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var ticketSubTab by rememberSaveable { mutableIntStateOf(0) }
    var peopleSubTab by rememberSaveable { mutableIntStateOf(0) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showAiBotDialog by remember { mutableStateOf(false) }
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
                onOpenAiBot = { showAiBotDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                    label = { Text("Vé", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier
                        .semantics { contentDescription = "Tab 1: Quản lý Vé phục vụ" }
                        .testTag("tab_ve")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    label = { Text("Khách", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier
                        .semantics { contentDescription = "Tab 2: Khách hàng và Nhân viên" }
                        .testTag("tab_nguoi")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Spa, contentDescription = null) },
                    label = { Text("Dịch vụ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
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
                            contentDescription = null
                        )
                    },
                    label = { Text("Sổ quỹ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
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
                            contentDescription = null
                        )
                    },
                    label = { Text("Báo cáo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier
                        .semantics { contentDescription = "Tab 5: Báo cáo doanh thu và Bảng lương nhân viên" }
                        .testTag("tab_doanh_thu")
                )
                if (isAdmin) {
                    NavigationBarItem(
                        selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                        label = { Text("Quyền hạn", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                        onOpenGehihi = { showAiBotDialog = true }
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

    if (showAiBotDialog && isAdmin) {
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
            onDismiss = { showAiBotDialog = false }
        )
    }
}

// =========================================================================
// MÀN HÌNH ĐĂNG NHẬP 1 LẦN DUY NHẤT BẰNG SỐ ĐIỆN THOẠI & MẬT KHẨU
// KÈM KẾT NỐI ĐỒNG BỘ ĐÁM MÂY THỜI GIAN THỰC CHO MỌI MÁY
// =========================================================================
@Composable
fun OneTimeLoginScreen(store: SpaStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var phoneInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var cloudBusy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

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

                // Khối Kết nối Đồng bộ Đám mây Thời gian thực giữa máy Admin và các máy Nhân viên
                Surface(
                    color = if (store.isCloudConnected) {
                        SpaSageGreen.copy(alpha = 0.14f)
                    } else {
                        SpaChampagne.copy(alpha = 0.20f)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (store.isCloudConnected) SpaSageGreen else SpaRosewood
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = if (store.isCloudConnected) {
                                "Đã kết nối đám mây thời gian thực. ${store.cloudSyncStatus}"
                            } else {
                                "Chưa kết nối đám mây Google. Nếu đây là máy phụ hoặc máy nhân viên mới cài, hãy bấm nút Kết nối Đám mây Google để tự động đồng bộ tài khoản, khách hàng và vé từ máy Admin."
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (store.isCloudConnected) {
                                "☁️ ĐÃ ĐỒNG BỘ ĐÁM MÂY THỜI GIAN THỰC"
                            } else {
                                "☁️ ĐỒNG BỘ DỮ LIỆU GIỮA MÁY ADMIN & MÁY NHÂN VIÊN"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = if (store.isCloudConnected) SpaSageGreen else SpaRosewoodDark
                        )
                        Text(
                            text = if (store.isCloudConnected) {
                                "${store.cloudSyncStatus}${if (store.cloudUserEmail.isNotBlank()) " (${store.cloudUserEmail})" else ""}"
                            } else {
                                "Máy nhân viên mới cài APK chỉ cần bấm \"Kết nối Đám mây Google\" 1 lần bên dưới để tự động nhận toàn bộ Tài khoản SĐT, Khách hàng, Vé và Gói dịch vụ từ máy Admin theo thời gian thực."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!store.isCloudConnected) {
                            OutlinedButton(
                                onClick = {
                                    if (!cloudBusy) {
                                        cloudBusy = true
                                        errorMessage = ""
                                        scope.launch {
                                            signInWithGoogleInteractive(
                                                activityContext = context,
                                                onSignedIn = {
                                                    cloudBusy = false
                                                    store.startRealtimeCloudSync()
                                                    Toast.makeText(
                                                        context,
                                                        "Đã kết nối Đám mây! Đang đồng bộ tài khoản & dữ liệu thời gian thực...",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                },
                                                onError = { msg ->
                                                    cloudBusy = false
                                                    errorMessage = msg
                                                }
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .semantics {
                                        contentDescription = "Kết nối Đám mây Google để đồng bộ dữ liệu thời gian thực giữa máy Admin và máy Nhân viên"
                                    }
                                    .testTag("btn_connect_google_cloud")
                            ) {
                                if (cloudBusy) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Đang kết nối đám mây...", fontWeight = FontWeight.Bold)
                                } else {
                                    Text("KẾT NỐI ĐÁM MÂY GOOGLE (ĐỒNG BỘ MỌI MÁY)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        } else {
                            // Khi đã kết nối đám mây: Hiển thị nút tải lại + nhận diện Gmail + danh sách tài khoản đã đồng bộ từ máy Admin
                            val matchedByEmail = store.timTaiKhoanTheoGoogleEmail()
                            if (matchedByEmail != null) {
                                Surface(
                                    color = SpaSageGreen.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "✨ Đã nhận diện Gmail của: ${matchedByEmail.hoTen} (SĐT: ${matchedByEmail.username})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SpaRosewoodDark
                                        )
                                        Button(
                                            onClick = {
                                                store.loginBangTaiKhoanDaGanEmail(matchedByEmail)
                                                Toast.makeText(
                                                    context,
                                                    "Đăng nhập tự động qua Gmail: ${matchedByEmail.hoTen}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SpaSageGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("btn_quick_login_by_gmail")
                                        ) {
                                            Text(
                                                "VÀO NGAY TÀI KHOẢN: ${matchedByEmail.hoTen.uppercase()}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Danh sách Tài khoản đã đồng bộ từ máy Admin (${store.taiKhoans.size} TK) — Chạm tên bạn để điền nhanh SĐT:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SpaRosewoodDark
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                store.taiKhoans.forEach { tk ->
                                    AssistChip(
                                        onClick = {
                                            phoneInput = tk.username
                                            errorMessage = ""
                                            Toast.makeText(
                                                context,
                                                "Đã chọn ${tk.hoTen} (${tk.username}). Hãy nhập mật khẩu Admin cấp cho bạn!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        label = {
                                            Text(
                                                "${tk.hoTen} (${tk.username})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        modifier = Modifier.testTag("chip_synced_account_${tk.id}")
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    if (!cloudBusy) {
                                        cloudBusy = true
                                        errorMessage = ""
                                        scope.launch {
                                            val ok = store.refreshFromCloudNow()
                                            cloudBusy = false
                                            Toast.makeText(
                                                context,
                                                if (ok) "Đã tải danh sách ${store.taiKhoans.size} tài khoản mới nhất từ đám mây!" else "Không thể tải từ đám mây, vui lòng kiểm tra mạng.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("btn_refresh_cloud_accounts")
                            ) {
                                Text(
                                    if (cloudBusy) "Đang tải từ đám mây..." else "🔄 TẢI LẠI TÀI KHOẢN MỚI NHẤT TỪ MÁY ADMIN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    if (!cloudBusy) {
                                        cloudBusy = true
                                        errorMessage = ""
                                        scope.launch {
                                            store.disconnectGoogleCloudForSwitch()
                                            signInWithGoogleInteractive(
                                                activityContext = context,
                                                forceAccountPicker = true,
                                                onSignedIn = {
                                                    store.startRealtimeCloudSync()
                                                    cloudBusy = false
                                                    Toast.makeText(
                                                        context,
                                                        "Đã đổi sang Gmail ${store.cloudUserEmail} và giữ nguyên 100% dữ liệu tiệm!",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                },
                                                onError = { msg ->
                                                    cloudBusy = false
                                                    errorMessage = msg
                                                }
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .semantics {
                                        contentDescription = "Đổi sang tài khoản Google Gmail khác mà vẫn giữ nguyên toàn bộ dữ liệu tiệm"
                                    }
                                    .testTag("btn_switch_cloud_google_login")
                            ) {
                                Text(
                                    "🔁 ĐỔI TÀI KHOẢN GOOGLE (GMAIL) KHÁC",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SpaRosewoodDark
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = {
                        phoneInput = it
                        errorMessage = ""
                    },
                    label = { Text("Tên đăng nhập (Số điện thoại hoặc Gmail)") },
                    placeholder = { Text("Nhập số điện thoại Admin cấp cho bạn") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_phone_input")
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = ""
                    },
                    label = { Text("Mật khẩu") },
                    placeholder = { Text("Nhập mật khẩu Admin cấp (mặc định: 123456)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )

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

                Button(
                    onClick = {
                        if (phoneInput.isBlank() || passwordInput.isBlank()) {
                            errorMessage = "Vui lòng nhập đầy đủ số điện thoại và mật khẩu."
                        } else {
                            cloudBusy = true
                            scope.launch {
                                val ok = store.pullLatestFromCloudAndLogin(phoneInput, passwordInput)
                                cloudBusy = false
                                if (ok) {
                                    Toast.makeText(
                                        context,
                                        "Đăng nhập thành công: ${store.currentUser?.hoTen}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    errorMessage = if (!store.isCloudConnected) {
                                        "Sai số điện thoại hoặc mật khẩu! Nếu tài khoản vừa được Admin tạo trên máy chính, hãy bấm nút 'KẾT NỐI ĐÁM MÂY GOOGLE' phía trên để tải tài khoản mới về máy này."
                                    } else {
                                        "Sai số điện thoại hoặc mật khẩu! Hãy kiểm tra trên máy Admin xem máy Admin đã bấm 'Kết nối Đám mây Google' để đẩy tài khoản mới lên đám mây chưa, hoặc chạm vào tên bạn trong danh sách phía trên rồi nhập lại mật khẩu."
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .semantics {
                            contentDescription = "Đăng nhập và ghi nhớ tài khoản trên máy này"
                        }
                        .testTag("btn_login_submit")
                ) {
                    Text("ĐĂNG NHẬP (GHI NHỚ 1 LẦN)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                HorizontalDivider()

                Text(
                    text = "Lưu ý: Nhân viên chỉ nhập số điện thoại và mật khẩu 1 lần duy nhất. Khi đã kết nối đám mây, mọi vé, khách hàng và gói dịch vụ đều tự động đồng bộ 100% thời gian thực giữa các máy.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    onOpenAiBot: () -> Unit
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
                if (isAdmin) {
                    Surface(
                        onClick = onOpenAiBot,
                        color = SpaChampagne.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, SpaChampagne.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .semantics { contentDescription = "Mở trợ lý AI gehihi cho Admin" }
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
            item { Spacer(modifier = Modifier.height(72.dp)) }
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
    var selectedNvId by remember { mutableStateOf(store.nvs.firstOrNull()?.id ?: "") }
    var dungGoiMode by remember { mutableStateOf(false) }
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
    val selectedGoiDvIds = remember(selectedGoi?.id) {
        mutableStateListOf<String>().apply {
            addAll(dvIdsInSelectedGoi)
        }
    }

    val previewThu = if (dungGoiMode) {
        0L
    } else {
        selectedDvIds.sumOf { id -> store.dvs.firstOrNull { it.id == id }?.gia ?: 0L }
    }
    val previewHoaHong = if (dungGoiMode) {
        selectedGoiDvIds.sumOf { id -> store.dvs.firstOrNull { it.id == id }?.chietKhauVnd ?: 0L }
    } else {
        selectedDvIds.sumOf { id -> store.dvs.firstOrNull { it.id == id }?.chietKhauVnd ?: 0L }
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
                Text("1. Chọn Khách hàng:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

                Text("3. Loại vé:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !dungGoiMode,
                        onClick = { dungGoiMode = false },
                        label = { Text("Vé lẻ tính tiền", fontSize = 14.sp) }
                    )
                    FilterChip(
                        selected = dungGoiMode,
                        onClick = { dungGoiMode = true },
                        label = { Text("Trừ buổi Gói 0đ (LT)", fontSize = 14.sp) }
                    )
                }

                if (!dungGoiMode) {
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
                } else {
                    if (goiCuaKhach.isEmpty()) {
                        Text(
                            "Khách này chưa có gói còn buổi.",
                            color = SpaDanger,
                            fontSize = 14.sp
                        )
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
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Dự kiến Thu khách: ${if (dungGoiMode) "0đ (Trừ gói LT)" else formatVnd(previewThu)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SpaRosewoodDark
                        )
                        Text(
                            text = "Hoa hồng KTV (khi Xác nhận chốt): ${formatVnd(previewHoaHong)}",
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
                    val created = if (dungGoiMode) {
                        store.moVeMoi(
                            khId = selectedKhId,
                            nvId = selectedNvId,
                            selectedDvIds = selectedGoiDvIds.toList(),
                            dkId = selectedDkId,
                            tgbd = tgbdInput,
                            dvNgoai = dvNgoaiInput
                        )
                    } else {
                        store.moVeMoi(
                            khId = selectedKhId,
                            nvId = selectedNvId,
                            selectedDvIds = selectedDvIds.toList(),
                            dkId = "",
                            tgbd = tgbdInput,
                            dvNgoai = dvNgoaiInput
                        )
                    }
                    if (created != null) onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpaRosewood),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Tạo vé ngay", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text("Hủy") }
        }
    )
}

@Composable
fun HoanThanhVeDialog(ve: Luot, store: SpaStore, onDismiss: () -> Unit) {
    val canEditTime = store.currentUser.hasPermission(QuyenHan.CAN_EDIT_TIME)
    var tgktInput by remember { mutableStateOf(nowTimeStr()) }
    var thucThuInput by remember { mutableStateOf(ve.thu.toString()) }
    val hoaHongDuKien = store.tinhHoaHongVe(ve)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hoàn thành vé (Kết thúc)", fontWeight = FontWeight.Bold) },
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
                    OutlinedTextField(
                        value = thucThuInput,
                        onValueChange = { thucThuInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Tiền thực thu khách (Có thể sửa giảm giá / bo)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                    item { Spacer(modifier = Modifier.height(72.dp)) }
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
                    item { Spacer(modifier = Modifier.height(72.dp)) }
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
    onOpenGehihi: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val adminSubTab = store.requestedAdminSubTab
    var showCreateAccountDialog by remember { mutableStateOf(false) }
    var showApkInstallGuideDialog by remember { mutableStateOf(false) }
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

    val subTabTitles = listOf(
        "Cấp quyền & TK NV",
        "Sửa TK Admin gốc",
        "Backup Xuất / Nhập"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = adminSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SpaRosewoodDark
        ) {
            subTabTitles.forEachIndexed { idx, title ->
                Tab(
                    selected = adminSubTab == idx,
                    onClick = { store.selectAdminSubTab(idx) },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("admin_subtab_$idx")
                )
            }
        }

        when (adminSubTab) {
            0 -> {
                val staffAccounts = store.taiKhoans.filter { it.id != "tk_admin" }
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

                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }

            1 -> {
                val rootAdminTk = store.taiKhoans.firstOrNull { it.id == "tk_admin" } ?: store.currentUser
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

                        Spacer(modifier = Modifier.height(72.dp))
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
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var inputPrompt by remember { mutableStateOf("") }
    var showQuickSuggestions by rememberSaveable { mutableStateOf(true) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var attachedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var attachedFileName by remember { mutableStateOf("") }
    var attachedFileContent by remember { mutableStateOf("") }
    val chatScrollState = rememberScrollState()
    val pendingConfirm = store.pendingAiConfirmation

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

    val quickCommands = listOf(
        "Chuyển hướng tới mục nhân viên",
        "Xóa bớt ngẫu nhiên 1 nhân viên rồi báo cáo",
        "Mai là thứ mấy?",
        "Báo cáo doanh thu hôm nay",
        "Sửa mail admin",
        "Sửa dịch vụ Chườm đá nóng giá 80k hoa hồng 30k",
        "Hoàn thành tất cả vé đang làm",
        "Chốt tất cả vé đang chờ",
        "Bảng lương hoa hồng KTV tháng này"
    )

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

    // Render trực tiếp trong cửa sổ chính của Activity (không dùng Dialog riêng) để bàn phím đẩy khung nhập lên 100% chính xác
    Surface(
        color = geminiBg,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. HEADER TỐI GIẢN, GỌN GÀNG
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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
                        tint = geminiAccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "gehihi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = geminiTextPrimary,
                        maxLines = 1
                    )
                    Surface(
                        color = geminiComposerBg,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, geminiChipBorder)
                    ) {
                        Text(
                            text = "Pro • ${store.aiLongTermMemories.size} nhớ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = geminiAccentBlue,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = { showQuickSuggestions = !showQuickSuggestions },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = if (showQuickSuggestions) "Ẩn gợi ý" else "Gợi ý",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = geminiTextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .semantics { contentDescription = "Đóng cửa sổ trợ lý AI gehihi" }
                            .testTag("btn_close_gehihi_dialog")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = geminiTextPrimary
                        )
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
                                        contentDescription = "Admin: $displayMsgText"
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

                            Text(
                                text = displayMsgText,
                                fontSize = 15.sp,
                                lineHeight = 23.sp,
                                color = geminiTextPrimary
                            )

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
                                    .height(46.dp)
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
                                    .height(46.dp)
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
                                    .height(40.dp)
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
                                    .height(40.dp)
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
                                .size(36.dp)
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
                                        text = "Full Quyền ▾",
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
                                },
                                color = if (canSend) Color(0xFF28354A) else geminiChipBg,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    1.dp,
                                    if (canSend) geminiAccentBlue.copy(alpha = 0.55f) else geminiChipBorder
                                ),
                                modifier = Modifier
                                    .size(36.dp)
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Mật khẩu") },
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
