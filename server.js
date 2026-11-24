const http = require('http');
const https = require('https');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');
const TENANTS_FILE = process.env.TENANTS_FILE || path.join(PUBLIC_DIR, 'tenants.json');
const PORTAL_CONFIG_FILE = process.env.PORTAL_CONFIG_FILE || path.join(PUBLIC_DIR, 'portal-config.json');
const AI_CONFIG_FILE = path.join(PUBLIC_DIR, 'ai-config.json');

const DEV_ADMIN_PIN = (process.env.DEV_ADMIN_PIN || '247208').trim();
const DEV_SESSION_SECRET = (process.env.DEV_SESSION_SECRET || crypto.createHash('sha256').update(`ktv_saas_${DEV_ADMIN_PIN}`).digest('hex'));
const CUSTOM_DOMAIN = (process.env.CUSTOM_DOMAIN || 'hoangdev08.id.vn').trim();
const MAX_BODY_BYTES = 1024 * 1024; // 1MB cap
const SERVER_START_TIME = Date.now();

const DEFAULT_PRICING_PLANS = [
  { id: "2_weeks", name: "Dùng Thử 1 Tháng", days: 30, fee: 0, enabled: true, tag: "Miễn Phí 100%" },
  { id: "1_month", name: "Gói 1 Tháng", days: 30, fee: 500000, enabled: true, tag: "Tiêu chuẩn" },
  { id: "3_months", name: "Gói 3 Tháng", days: 90, fee: 1400000, enabled: true, tag: "Tiết kiệm 100k" },
  { id: "6_months", name: "Gói 6 Tháng", days: 180, fee: 2600000, enabled: true, tag: "Phổ biến nhất" },
  { id: "1_year", name: "VIP 1 Năm", days: 365, fee: 5000000, enabled: true, tag: "VIP Tiết kiệm 1Tr" }
];

const DEFAULT_BANK_CONFIG = {
  bankCode: "MB",
  bankName: "MBBank (Quân Đội)",
  accountNo: "0397772963",
  accountName: "DEV SUPER ADMIN",
  apkDownloadUrl: ""
};

// =========================================================================
// HELPERS: DATE / LICENSE / CRYPTO / HTTP
// =========================================================================

function asString(v) {
  return Array.isArray(v)
    ? String(v[0] ?? '')
    : (typeof v === 'string' || typeof v === 'number')
      ? String(v)
      : '';
}

function formatExpiryDate(ms) {
  const d = new Date(ms);
  const dd = String(d.getDate()).padStart(2, '0');
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const yyyy = d.getFullYear();
  return `${dd}/${mm}/${yyyy}`;
}

function parseExpiryDateMs(str) {
  if (!str || typeof str !== 'string') return 0;
  const parts = str.trim().split('/');
  if (parts.length !== 3) return 0;
  const dd = parseInt(parts[0], 10);
  const mm = parseInt(parts[1], 10);
  const yyyy = parseInt(parts[2], 10);
  if (!dd || !mm || !yyyy) return 0;
  return new Date(yyyy, mm - 1, dd, 23, 59, 59, 999).getTime();
}

function normalizeTenant(raw) {
  const now = Date.now();
  const item = Object.assign({}, raw);
  item.id = asString(item.id).trim();
  item.name = asString(item.name).trim();
  item.ownerPhone = asString(item.ownerPhone).trim();
  item.plan = asString(item.plan || 'Gói 1 Tháng').trim();
  item.tempPin = asString(item.tempPin).trim();
  item.monthlyFee = Number(item.monthlyFee) || 500000;
  item.isActive = item.isActive !== false;

  let expiredAt = Number(item.expiredAt) || 0;
  if (!expiredAt && item.expiryDate) {
    expiredAt = parseExpiryDateMs(item.expiryDate);
  }
  if (!expiredAt) {
    const fallbackDays = Number.isFinite(Number(item.daysLeft)) ? Number(item.daysLeft) : 30;
    expiredAt = now + fallbackDays * 86400000;
  }
  item.expiredAt = expiredAt;
  item.expiryDate = formatExpiryDate(expiredAt);
  item.daysLeft = Math.ceil((expiredAt - now) / 86400000);
  return item;
}

function stripSensitiveTenantFields(t) {
  const copy = Object.assign({}, t);
  delete copy.tempPin;
  return copy;
}

function safeEqual(a, b) {
  const bufA = Buffer.from(String(a || ''), 'utf8');
  const bufB = Buffer.from(String(b || ''), 'utf8');
  if (bufA.length !== bufB.length) return false;
  return crypto.timingSafeEqual(bufA, bufB);
}

function createDevToken() {
  const exp = Date.now() + 24 * 60 * 60 * 1000; // 24 hours
  const payload = Buffer.from(JSON.stringify({ role: 'DEV', exp })).toString('base64url');
  const sig = crypto.createHmac('sha256', DEV_SESSION_SECRET).update(payload).digest('base64url');
  return `${payload}.${sig}`;
}

function verifyDevToken(token) {
  if (!token || typeof token !== 'string') return false;
  const parts = token.split('.');
  if (parts.length !== 2) return false;
  const [payload, sig] = parts;
  const expectedSig = crypto.createHmac('sha256', DEV_SESSION_SECRET).update(payload).digest('base64url');
  if (!safeEqual(sig, expectedSig)) return false;
  try {
    const decoded = JSON.parse(Buffer.from(payload, 'base64url').toString('utf8'));
    return decoded && decoded.role === 'DEV' && Number(decoded.exp) > Date.now();
  } catch (_) {
    return false;
  }
}

function isDevAuthenticated(req) {
  const authHeader = asString(req.headers['authorization']).trim();
  if (authHeader.toLowerCase().startsWith('bearer ')) {
    const token = authHeader.slice(7).trim();
    if (verifyDevToken(token)) return true;
  }
  const pinHeader = asString(req.headers['x-dev-pin']).trim();
  if (pinHeader && safeEqual(pinHeader, DEV_ADMIN_PIN)) return true;
  return false;
}

function httpsRequestJson(method, urlStr, headers = {}, bodyObj = null) {
  return new Promise((resolve, reject) => {
    const u = new URL(urlStr);
    const bodyStr = bodyObj !== null ? (typeof bodyObj === 'string' ? bodyObj : JSON.stringify(bodyObj)) : null;
    const reqHeaders = Object.assign({}, headers);
    if (bodyStr !== null) {
      if (!reqHeaders['Content-Type']) reqHeaders['Content-Type'] = 'application/json';
      reqHeaders['Content-Length'] = Buffer.byteLength(bodyStr);
    }
    const req = https.request({
      hostname: u.hostname,
      port: u.port || 443,
      path: u.pathname + u.search,
      method,
      headers: reqHeaders,
      timeout: 12000
    }, (res) => {
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => {
        const text = Buffer.concat(chunks).toString('utf8');
        let json = null;
        try { json = text ? JSON.parse(text) : null; } catch (_) {}
        resolve({ status: res.statusCode, text, json });
      });
    });
    req.on('error', reject);
    req.on('timeout', () => {
      req.destroy(new Error('HTTPS request timeout'));
    });
    if (bodyStr !== null) req.write(bodyStr);
    req.end();
  });
}

// =========================================================================
// CLOUD PERSISTENCE LAYER (FIRESTORE SERVICE ACCOUNT / GITHUB DATA BRANCH)
// =========================================================================

function getFirebaseServiceAccount() {
  try {
    if (process.env.FIREBASE_SERVICE_ACCOUNT_JSON && process.env.FIREBASE_SERVICE_ACCOUNT_JSON.trim().startsWith('{')) {
      const parsed = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_JSON.trim());
      if (parsed.project_id && parsed.client_email && parsed.private_key) {
        return {
          projectId: parsed.project_id,
          clientEmail: parsed.client_email,
          privateKey: String(parsed.private_key).replace(/\\n/g, '\n'),
          databaseId: (process.env.FIREBASE_DATABASE_ID || '(default)').trim()
        };
      }
    }
    if (process.env.FIREBASE_PROJECT_ID && process.env.FIREBASE_CLIENT_EMAIL && process.env.FIREBASE_PRIVATE_KEY) {
      return {
        projectId: process.env.FIREBASE_PROJECT_ID.trim(),
        clientEmail: process.env.FIREBASE_CLIENT_EMAIL.trim(),
        privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n'),
        databaseId: (process.env.FIREBASE_DATABASE_ID || '(default)').trim()
      };
    }
  } catch (e) {
    console.error("Lỗi đọc FIREBASE_SERVICE_ACCOUNT_JSON:", e.message);
  }
  return null;
}

const FIREBASE_SA = getFirebaseServiceAccount();
const GITHUB_TOKEN = (process.env.GITHUB_TOKEN || '').trim();
const GITHUB_REPO = (process.env.GITHUB_REPO || 'hoanggas71-pixel/Khi-m-Th-Vi-t').trim();
const GITHUB_DATA_BRANCH = (process.env.GITHUB_DATA_BRANCH || 'render-data').trim();

let cachedGoogleToken = { token: null, expiresAt: 0 };

async function getGoogleAccessToken() {
  if (!FIREBASE_SA) return null;
  if (cachedGoogleToken.token && cachedGoogleToken.expiresAt > Date.now() + 60000) {
    return cachedGoogleToken.token;
  }
  const nowSec = Math.floor(Date.now() / 1000);
  const header = Buffer.from(JSON.stringify({ alg: 'RS256', typ: 'JWT' })).toString('base64url');
  const claim = Buffer.from(JSON.stringify({
    iss: FIREBASE_SA.clientEmail,
    scope: 'https://www.googleapis.com/auth/datastore',
    aud: 'https://oauth2.googleapis.com/token',
    iat: nowSec,
    exp: nowSec + 3600
  })).toString('base64url');
  const signInput = `${header}.${claim}`;
  const signer = crypto.createSign('RSA-SHA256');
  signer.update(signInput);
  const signature = signer.sign(FIREBASE_SA.privateKey, 'base64url');
  const assertion = `${signInput}.${signature}`;

  const formBody = `grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=${encodeURIComponent(assertion)}`;
  const res = await httpsRequestJson('POST', 'https://oauth2.googleapis.com/token', {
    'Content-Type': 'application/x-www-form-urlencoded'
  }, formBody);
  if (res.status === 200 && res.json && res.json.access_token) {
    cachedGoogleToken = {
      token: res.json.access_token,
      expiresAt: Date.now() + ((res.json.expires_in || 3600) - 120) * 1000
    };
    return cachedGoogleToken.token;
  }
  throw new Error(`Google OAuth failed (${res.status}): ${res.text.slice(0, 200)}`);
}

function getFirestoreStateDocUrl() {
  const dbId = encodeURIComponent(FIREBASE_SA.databaseId || '(default)');
  const proj = encodeURIComponent(FIREBASE_SA.projectId);
  return `https://firestore.googleapis.com/v1/projects/${proj}/databases/${dbId}/documents/portal_state/main`;
}

async function loadStateFromFirestore() {
  if (!FIREBASE_SA) return null;
  const token = await getGoogleAccessToken();
  const res = await httpsRequestJson('GET', getFirestoreStateDocUrl(), {
    'Authorization': `Bearer ${token}`
  });
  if (res.status === 404) return { empty: true };
  if (res.status !== 200 || !res.json || !res.json.fields) {
    throw new Error(`Firestore read failed (${res.status})`);
  }
  const payloadStr = res.json.fields.payloadJson && res.json.fields.payloadJson.stringValue;
  if (!payloadStr) return { empty: true };
  return JSON.parse(payloadStr);
}

async function saveStateToFirestore(stateObj) {
  if (!FIREBASE_SA) return false;
  const token = await getGoogleAccessToken();
  const body = {
    fields: {
      payloadJson: { stringValue: JSON.stringify(stateObj) },
      updatedAt: { stringValue: new Date().toISOString() },
      tenantsCount: { integerValue: String((stateObj.tenants || []).length) }
    }
  };
  const res = await httpsRequestJson('PATCH', getFirestoreStateDocUrl(), {
    'Authorization': `Bearer ${token}`
  }, body);
  if (res.status >= 200 && res.status < 300) return true;
  throw new Error(`Firestore write failed (${res.status}): ${res.text.slice(0, 200)}`);
}

async function loadStateFromGithubBranch() {
  if (!GITHUB_TOKEN) return null;
  const apiUrl = `https://api.github.com/repos/${GITHUB_REPO}/contents/portal-state.json?ref=${encodeURIComponent(GITHUB_DATA_BRANCH)}`;
  const res = await httpsRequestJson('GET', apiUrl, {
    'Authorization': `Bearer ${GITHUB_TOKEN}`,
    'User-Agent': 'KhiemThiViet-Portal-Server',
    'Accept': 'application/vnd.github+json'
  });
  if (res.status === 404) return { empty: true };
  if (res.status !== 200 || !res.json || !res.json.content) {
    throw new Error(`GitHub branch read failed (${res.status})`);
  }
  const decoded = Buffer.from(res.json.content, 'base64').toString('utf8');
  return JSON.parse(decoded);
}

async function saveStateToGithubBranch(stateObj) {
  if (!GITHUB_TOKEN) return false;
  const headers = {
    'Authorization': `Bearer ${GITHUB_TOKEN}`,
    'User-Agent': 'KhiemThiViet-Portal-Server',
    'Accept': 'application/vnd.github+json'
  };
  const apiUrl = `https://api.github.com/repos/${GITHUB_REPO}/contents/portal-state.json?ref=${encodeURIComponent(GITHUB_DATA_BRANCH)}`;
  let sha = undefined;
  const existing = await httpsRequestJson('GET', apiUrl, headers);
  if (existing.status === 200 && existing.json && existing.json.sha) {
    sha = existing.json.sha;
  }
  const putUrl = `https://api.github.com/repos/${GITHUB_REPO}/contents/portal-state.json`;
  const payload = {
    message: `chore(data): sync portal state (${new Date().toISOString()})`,
    content: Buffer.from(JSON.stringify(stateObj, null, 2), 'utf8').toString('base64'),
    branch: GITHUB_DATA_BRANCH
  };
  if (sha) payload.sha = sha;
  const putRes = await httpsRequestJson('PUT', putUrl, headers, payload);
  return putRes.status >= 200 && putRes.status < 300;
}

// =========================================================================
// ATOMIC DISK STORAGE + IN-MEMORY CACHE
// =========================================================================

function atomicWriteJson(filePath, dataObj) {
  const tmpFile = `${filePath}.${process.pid}.${Date.now()}.${Math.random().toString(36).slice(2, 8)}.tmp`;
  try {
    const dir = path.dirname(filePath);
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
    fs.writeFileSync(tmpFile, JSON.stringify(dataObj, null, 2), 'utf8');
    fs.renameSync(tmpFile, filePath);
    return true;
  } catch (e) {
    console.error(`Lỗi lưu ${path.basename(filePath)}:`, e.message);
    try { if (fs.existsSync(tmpFile)) fs.unlinkSync(tmpFile); } catch (_) {}
    return false;
  }
}

function readLocalTenantsDisk() {
  try {
    if (fs.existsSync(TENANTS_FILE)) {
      const data = fs.readFileSync(TENANTS_FILE, 'utf8');
      const list = JSON.parse(data);
      if (Array.isArray(list)) return list.map(normalizeTenant);
    }
  } catch (e) {
    console.error("Lỗi đọc tenants.json:", e.message);
  }
  return [];
}

function readLocalPortalConfigDisk() {
  try {
    if (fs.existsSync(PORTAL_CONFIG_FILE)) {
      const data = fs.readFileSync(PORTAL_CONFIG_FILE, 'utf8');
      const cfg = JSON.parse(data);
      if (cfg && typeof cfg === 'object') return cfg;
    }
  } catch (e) {
    console.error("Lỗi đọc portal-config.json:", e.message);
  }
  return {};
}

const initialLocalCfg = readLocalPortalConfigDisk();
let memoryState = {
  tenants: readLocalTenantsDisk(),
  deletedIds: Array.isArray(initialLocalCfg.deletedIds) ? initialLocalCfg.deletedIds : [],
  pricingPlans: Array.isArray(initialLocalCfg.pricingPlans) && initialLocalCfg.pricingPlans.length > 0
    ? initialLocalCfg.pricingPlans
    : JSON.parse(JSON.stringify(DEFAULT_PRICING_PLANS)),
  bankConfig: Object.assign({}, DEFAULT_BANK_CONFIG, initialLocalCfg.bankConfig || {}),
  globalMaintenance: Boolean(initialLocalCfg.globalMaintenance),
  globalMaintenanceMsg: asString(initialLocalCfg.globalMaintenanceMsg || "Hệ thống đang bảo trì nâng cấp kỹ thuật định kỳ để nâng cao chất lượng dịch vụ."),
  customDomain: asString(initialLocalCfg.customDomain || CUSTOM_DOMAIN),
  updatedAt: initialLocalCfg.updatedAt || null
};

let storageStatus = {
  mode: FIREBASE_SA ? 'firestore' : (GITHUB_TOKEN ? 'github-branch' : 'local-ephemeral'),
  cloudSynced: false,
  lastCloudSyncAt: null,
  lastCloudError: null,
  hasMutationSinceBoot: false
};

// Khởi tạo file tenants rỗng [] nếu chưa có
if (!fs.existsSync(TENANTS_FILE)) {
  atomicWriteJson(TENANTS_FILE, []);
}

function persistMemoryStateToDisk() {
  const okTenants = atomicWriteJson(TENANTS_FILE, memoryState.tenants);
  const okCfg = atomicWriteJson(PORTAL_CONFIG_FILE, {
    deletedIds: memoryState.deletedIds.slice(-200),
    pricingPlans: memoryState.pricingPlans,
    bankConfig: memoryState.bankConfig,
    globalMaintenance: memoryState.globalMaintenance,
    globalMaintenanceMsg: memoryState.globalMaintenanceMsg,
    customDomain: memoryState.customDomain,
    updatedAt: memoryState.updatedAt
  });
  return okTenants && okCfg;
}

function triggerCloudBackupAsync() {
  if (storageStatus.mode === 'firestore') {
    saveStateToFirestore(memoryState)
      .then(() => {
        storageStatus.cloudSynced = true;
        storageStatus.lastCloudSyncAt = new Date().toISOString();
        storageStatus.lastCloudError = null;
      })
      .catch((err) => {
        storageStatus.cloudSynced = false;
        storageStatus.lastCloudError = err.message;
        console.error("Firestore sync error:", err.message);
      });
  } else if (storageStatus.mode === 'github-branch') {
    saveStateToGithubBranch(memoryState)
      .then(() => {
        storageStatus.cloudSynced = true;
        storageStatus.lastCloudSyncAt = new Date().toISOString();
        storageStatus.lastCloudError = null;
      })
      .catch((err) => {
        storageStatus.cloudSynced = false;
        storageStatus.lastCloudError = err.message;
        console.error("GitHub branch sync error:", err.message);
      });
  }
}

async function hydrateStateFromCloudOnStartup() {
  if (storageStatus.mode === 'local-ephemeral') return;
  try {
    const remote = storageStatus.mode === 'firestore'
      ? await loadStateFromFirestore()
      : await loadStateFromGithubBranch();
    if (remote && !remote.empty && typeof remote === 'object') {
      if (Array.isArray(remote.tenants)) {
        memoryState.tenants = remote.tenants.map(normalizeTenant);
      }
      if (Array.isArray(remote.deletedIds)) {
        memoryState.deletedIds = remote.deletedIds;
      }
      if (Array.isArray(remote.pricingPlans) && remote.pricingPlans.length > 0) {
        memoryState.pricingPlans = remote.pricingPlans;
      }
      if (remote.bankConfig && typeof remote.bankConfig === 'object') {
        memoryState.bankConfig = Object.assign({}, DEFAULT_BANK_CONFIG, remote.bankConfig);
      }
      if (remote.globalMaintenance !== undefined) {
        memoryState.globalMaintenance = Boolean(remote.globalMaintenance);
      }
      if (remote.globalMaintenanceMsg) {
        memoryState.globalMaintenanceMsg = asString(remote.globalMaintenanceMsg);
      }
      if (remote.customDomain) {
        memoryState.customDomain = asString(remote.customDomain);
      }
      memoryState.updatedAt = remote.updatedAt || new Date().toISOString();
      persistMemoryStateToDisk();
      storageStatus.cloudSynced = true;
      storageStatus.lastCloudSyncAt = new Date().toISOString();
      storageStatus.lastCloudError = null;
      console.log(`✅ Đã nạp ${memoryState.tenants.length} cơ sở từ Cloud (${storageStatus.mode})!`);
    } else if (remote && remote.empty) {
      storageStatus.cloudSynced = true;
      storageStatus.lastCloudSyncAt = new Date().toISOString();
      console.log(`ℹ️ Cloud (${storageStatus.mode}) sẵn sàng (hiện đang có 0 cơ sở).`);
    }
  } catch (err) {
    storageStatus.cloudSynced = false;
    storageStatus.lastCloudError = err.message;
    console.error(`⚠️ Không thể nạp trạng thái từ Cloud (${storageStatus.mode}):`, err.message);
  }
}

// =========================================================================
// AI CONFIG HELPERS (GIỮ NGUYÊN TƯƠNG THÍCH .env & public/ai-config.json)
// =========================================================================

function getEnvFallbackApiKey() {
  if (process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY.trim() && process.env.GEMINI_API_KEY !== 'YOUR_GEMINI_API_KEY') {
    return process.env.GEMINI_API_KEY.trim();
  }
  const envPath = path.join(__dirname, '.env');
  if (fs.existsSync(envPath)) {
    try {
      const content = fs.readFileSync(envPath, 'utf8');
      const match = content.match(/^\s*GEMINI_API_KEY\s*=\s*(.+)$/m);
      if (match && match[1]) {
        const val = match[1].trim().replace(/^['"]|['"]$/g, '');
        if (val && val !== 'YOUR_GEMINI_API_KEY') return val;
      }
    } catch (e) {
      console.error("Lỗi đọc .env:", e.message);
    }
  }
  return "";
}

function loadAiConfigFromDisk() {
  try {
    if (fs.existsSync(AI_CONFIG_FILE)) {
      const data = fs.readFileSync(AI_CONFIG_FILE, 'utf8');
      const config = JSON.parse(data);
      if (config && typeof config === 'object') {
        if (!config.apiKey || !config.apiKey.trim()) {
          const fallback = getEnvFallbackApiKey();
          if (fallback) config.apiKey = fallback;
        }
        if (!config.model) config.model = "gemini-3.5-flash-lite";
        return config;
      }
    }
  } catch (e) {
    console.error("Lỗi đọc ai-config.json:", e.message);
  }
  return {
    apiKey: getEnvFallbackApiKey(),
    model: "gemini-3.5-flash-lite",
    updatedAt: new Date().toISOString()
  };
}

function saveAiConfigToDisk(config) {
  const toSave = {
    apiKey: (config.apiKey || '').trim(),
    model: (config.model || 'gemini-3.5-flash-lite').trim(),
    updatedAt: new Date().toISOString()
  };
  return atomicWriteJson(AI_CONFIG_FILE, toSave) ? toSave : null;
}

if (!fs.existsSync(AI_CONFIG_FILE)) {
  const initConfig = loadAiConfigFromDisk();
  saveAiConfigToDisk(initConfig);
}

// =========================================================================
// REQUEST BODY PARSER
// =========================================================================

const MIME_TYPES = {
  '.html': 'text/html; charset=UTF-8',
  '.js': 'text/javascript; charset=UTF-8',
  '.css': 'text/css; charset=UTF-8',
  '.json': 'application/json; charset=UTF-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.apk': 'application/vnd.android.package-archive'
};

function readJsonBody(req, res, onParsed, { allowEmpty = false } = {}) {
  const chunks = [];
  let totalBytes = 0;
  let aborted = false;

  req.on('data', chunk => {
    if (aborted) return;
    totalBytes += chunk.length;
    if (totalBytes > MAX_BODY_BYTES) {
      aborted = true;
      req.pause();
      res.writeHead(413, {
        'Content-Type': 'application/json; charset=UTF-8',
        'Connection': 'close'
      });
      res.end(JSON.stringify({ success: false, error: "Payload too large" }), () => {
        req.destroy();
      });
      return;
    }
    chunks.push(chunk);
  });

  req.on('end', () => {
    if (aborted) return;
    let parsed;
    try {
      const raw = Buffer.concat(chunks).toString('utf8');
      parsed = (allowEmpty && !raw.trim()) ? {} : JSON.parse(raw);
    } catch (e) {
      res.writeHead(400, { 'Content-Type': 'application/json; charset=UTF-8' });
      res.end(JSON.stringify({ success: false, error: "Invalid JSON format" }));
      return;
    }
    try {
      onParsed(parsed);
    } catch (err) {
      console.error("Lỗi xử lý request body:", err);
      if (!res.headersSent) {
        res.writeHead(500, { 'Content-Type': 'application/json; charset=UTF-8' });
      }
      if (!res.writableEnded) {
        res.end(JSON.stringify({ success: false, error: "Internal Server Error" }));
      }
    }
  });
}

function sendJson(res, statusCode, payload) {
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=UTF-8',
    'Cache-Control': 'no-store'
  });
  res.end(JSON.stringify(payload));
}

function requireDevAuth(req, res) {
  if (!isDevAuthenticated(req)) {
    sendJson(res, 401, { success: false, error: "Unauthorized — Yêu cầu đăng nhập Dev Super Admin!" });
    return false;
  }
  return true;
}

// =========================================================================
// HTTP SERVER
// =========================================================================

const server = http.createServer((req, res) => {
  try {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Dev-Pin');

    if (req.method === 'OPTIONS') {
      res.writeHead(204);
      res.end();
      return;
    }

    const reqUrl = new URL(req.url || '/', `http://${req.headers.host || 'localhost'}`);
    const pathname = reqUrl.pathname;
    const query = reqUrl.searchParams;

    // ---------------------------------------------------------------------
    // API 1: Health Check (UptimeRobot 24/7 anti-sleep — cực nhẹ, 0 DB I/O)
    // ---------------------------------------------------------------------
    if (pathname === '/health' || pathname === '/api/health') {
      sendJson(res, 200, {
        status: "OK",
        time: new Date().toISOString(),
        uptimeSeconds: Math.floor((Date.now() - SERVER_START_TIME) / 1000)
      });
      return;
    }

    // ---------------------------------------------------------------------
    // API 1b: Storage & Domain Persistence Status
    // ---------------------------------------------------------------------
    if (pathname === '/api/status/storage' && req.method === 'GET') {
      sendJson(res, 200, {
        ok: true,
        storageMode: storageStatus.mode,
        cloudSynced: storageStatus.cloudSynced,
        lastCloudSyncAt: storageStatus.lastCloudSyncAt,
        lastCloudError: storageStatus.lastCloudError,
        hasMutationSinceBoot: storageStatus.hasMutationSinceBoot,
        tenantsCount: memoryState.tenants.length,
        customDomain: memoryState.customDomain || CUSTOM_DOMAIN,
        uptimeSeconds: Math.floor((Date.now() - SERVER_START_TIME) / 1000)
      });
      return;
    }

    // ---------------------------------------------------------------------
    // API 1c: Dev Super Admin Login (server-side PIN check -> HMAC Token)
    // ---------------------------------------------------------------------
    if (pathname === '/api/dev/login' && req.method === 'POST') {
      readJsonBody(req, res, (body) => {
        const pin = asString(body && body.pin).trim();
        if (!pin || !safeEqual(pin, DEV_ADMIN_PIN)) {
          sendJson(res, 401, { success: false, error: "Sai mã PIN quản trị Dev Super Admin!" });
          return;
        }
        const token = createDevToken();
        sendJson(res, 200, {
          success: true,
          token,
          expiresInSeconds: 86400,
          storageMode: storageStatus.mode,
          cloudSynced: storageStatus.cloudSynced
        });
      });
      return;
    }

    // ---------------------------------------------------------------------
    // API 1d: Portal Config (Pricing Plans, Bank Info, Custom Domain, Maintenance)
    // ---------------------------------------------------------------------
    if (pathname === '/api/system/portal-config') {
      if (req.method === 'GET') {
        sendJson(res, 200, {
          success: true,
          pricingPlans: memoryState.pricingPlans,
          bankConfig: memoryState.bankConfig,
          globalMaintenance: memoryState.globalMaintenance,
          globalMaintenanceMsg: memoryState.globalMaintenanceMsg,
          customDomain: memoryState.customDomain || CUSTOM_DOMAIN,
          updatedAt: memoryState.updatedAt
        });
        return;
      } else if (req.method === 'POST') {
        if (!requireDevAuth(req, res)) return;
        readJsonBody(req, res, (payload) => {
          if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
            sendJson(res, 400, { success: false, error: "Invalid portal config payload" });
            return;
          }
          if (Array.isArray(payload.pricingPlans) && payload.pricingPlans.length > 0) {
            memoryState.pricingPlans = payload.pricingPlans.map(p => ({
              id: asString(p.id).trim() || `plan_${Date.now()}`,
              name: asString(p.name).trim() || 'Gói dịch vụ',
              days: Math.max(1, parseInt(p.days, 10) || 30),
              fee: Math.max(0, Number(p.fee) || 0),
              enabled: p.enabled !== false,
              tag: asString(p.tag).trim()
            }));
          }
          if (payload.bankConfig && typeof payload.bankConfig === 'object') {
            memoryState.bankConfig = Object.assign({}, memoryState.bankConfig, {
              bankCode: asString(payload.bankConfig.bankCode || memoryState.bankConfig.bankCode).trim(),
              bankName: asString(payload.bankConfig.bankName || memoryState.bankConfig.bankName).trim(),
              accountNo: asString(payload.bankConfig.accountNo || memoryState.bankConfig.accountNo).trim(),
              accountName: asString(payload.bankConfig.accountName || memoryState.bankConfig.accountName).trim(),
              apkDownloadUrl: asString(payload.bankConfig.apkDownloadUrl ?? memoryState.bankConfig.apkDownloadUrl).trim()
            });
          }
          if (payload.globalMaintenance !== undefined) {
            memoryState.globalMaintenance = Boolean(payload.globalMaintenance);
          }
          if (payload.globalMaintenanceMsg !== undefined) {
            memoryState.globalMaintenanceMsg = asString(payload.globalMaintenanceMsg).trim();
          }
          if (payload.customDomain !== undefined) {
            memoryState.customDomain = asString(payload.customDomain).trim();
          }
          memoryState.updatedAt = new Date().toISOString();
          storageStatus.hasMutationSinceBoot = true;
          persistMemoryStateToDisk();
          triggerCloudBackupAsync();
          sendJson(res, 200, {
            success: true,
            pricingPlans: memoryState.pricingPlans,
            bankConfig: memoryState.bankConfig,
            globalMaintenance: memoryState.globalMaintenance,
            globalMaintenanceMsg: memoryState.globalMaintenanceMsg,
            customDomain: memoryState.customDomain,
            updatedAt: memoryState.updatedAt
          });
        });
        return;
      }
    }

    // ---------------------------------------------------------------------
    // API 1e: Cấu hình Gemini AI Hệ Thống (System AI Config)
    // ---------------------------------------------------------------------
    if (pathname === '/api/system/ai-config') {
      if (req.method === 'GET') {
        const config = loadAiConfigFromDisk();
        const isDev = isDevAuthenticated(req);
        sendJson(res, 200, {
          success: true,
          configured: Boolean(config.apiKey && config.apiKey.trim()),
          apiKey: isDev ? (config.apiKey || "") : "",
          model: config.model || "gemini-3.5-flash-lite",
          updatedAt: config.updatedAt || new Date().toISOString()
        });
        return;
      } else if (req.method === 'POST') {
        if (!requireDevAuth(req, res)) return;
        readJsonBody(req, res, (payload) => {
          if (typeof payload === 'object' && payload !== null && !Array.isArray(payload)) {
            const saved = saveAiConfigToDisk(payload);
            if (saved) {
              sendJson(res, 200, {
                success: true,
                message: "Cấu hình AI hệ thống đã lưu thành công",
                apiKey: saved.apiKey,
                model: saved.model,
                updatedAt: saved.updatedAt
              });
              return;
            }
            sendJson(res, 500, { success: false, error: "Failed to save AI config" });
            return;
          }
          sendJson(res, 400, { success: false, error: "Invalid JSON format" });
        }, { allowEmpty: true });
        return;
      }
    }

    // ---------------------------------------------------------------------
    // API 2: Quản lý Danh sách Cơ sở (Tenants)
    // ---------------------------------------------------------------------
    // ---------------------------------------------------------------------
    // API 2b: Chủ Spa tự đổi mã PIN đăng nhập (xác thực bằng PIN cũ)
    // ---------------------------------------------------------------------
    if (pathname === '/api/tenants/change-pin' && req.method === 'POST') {
      readJsonBody(req, res, (body) => {
        const phone = asString(body && body.phone).trim().replace(/[^0-9]/g, '');
        const id = asString(body && body.id).trim();
        const oldPin = asString(body && body.oldPin).trim();
        const newPin = asString(body && body.newPin).trim();

        if ((!phone && !id) || !oldPin || !newPin) {
          sendJson(res, 400, { success: false, error: "Vui lòng nhập đầy đủ mã PIN cũ và mã PIN mới!" });
          return;
        }
        if (newPin.length < 4 || newPin.length > 32) {
          sendJson(res, 400, { success: false, error: "Mã PIN mới phải từ 4 đến 32 ký tự!" });
          return;
        }
        const idx = memoryState.tenants.findIndex(item => {
          const itemPhone = asString(item.ownerPhone).replace(/[^0-9]/g, '');
          return (id && item.id === id) || (phone && itemPhone === phone);
        });
        if (idx < 0) {
          sendJson(res, 404, { success: false, error: "Không tìm thấy cơ sở Spa tương ứng!" });
          return;
        }
        const current = normalizeTenant(memoryState.tenants[idx]);
        if (!current.tempPin || !safeEqual(oldPin, current.tempPin)) {
          sendJson(res, 401, { success: false, error: "Mã PIN hiện tại không chính xác!" });
          return;
        }
        memoryState.tenants[idx] = normalizeTenant(Object.assign({}, current, { tempPin: newPin }));
        memoryState.updatedAt = new Date().toISOString();
        storageStatus.hasMutationSinceBoot = true;
        if (!persistMemoryStateToDisk()) {
          sendJson(res, 500, { success: false, error: "Không thể lưu mã PIN mới lên máy chủ!" });
          return;
        }
        triggerCloudBackupAsync();
        sendJson(res, 200, {
          success: true,
          message: "Đổi mã PIN Chủ Spa thành công! Vui lòng dùng mã PIN mới cho các lần đăng nhập tiếp theo."
        });
      });
      return;
    }

    if (pathname === '/api/tenants/extend' && req.method === 'POST') {
      if (!requireDevAuth(req, res)) return;
      readJsonBody(req, res, (body) => {
        const id = asString(body && body.id).trim();
        const days = parseInt(body && body.days, 10) || 0;
        const planName = asString(body && body.planName).trim();
        if (!id || days <= 0) {
          sendJson(res, 400, { success: false, error: "Thiếu mã cơ sở hoặc số ngày gia hạn hợp lệ" });
          return;
        }
        const idx = memoryState.tenants.findIndex(t => t.id === id);
        if (idx < 0) {
          sendJson(res, 404, { success: false, error: "Không tìm thấy cơ sở cần gia hạn" });
          return;
        }
        const t = normalizeTenant(memoryState.tenants[idx]);
        const baseMs = Math.max(Date.now(), t.expiredAt || 0);
        const newExpiredAt = baseMs + days * 86400000;
        const updated = normalizeTenant(Object.assign({}, t, {
          expiredAt: newExpiredAt,
          expiryDate: formatExpiryDate(newExpiredAt),
          isActive: true,
          plan: planName || t.plan
        }));
        memoryState.tenants[idx] = updated;
        memoryState.updatedAt = new Date().toISOString();
        storageStatus.hasMutationSinceBoot = true;
        persistMemoryStateToDisk();
        triggerCloudBackupAsync();
        sendJson(res, 200, { success: true, tenant: updated });
      });
      return;
    }

    if (pathname === '/api/tenants') {
      if (req.method === 'GET') {
        const isDev = isDevAuthenticated(req);
        const list = memoryState.tenants.map(normalizeTenant);
        memoryState.tenants = list;
        sendJson(res, 200, isDev ? list : list.map(stripSensitiveTenantFields));
        return;
      } else if (req.method === 'POST') {
        if (!requireDevAuth(req, res)) return;
        readJsonBody(req, res, (list) => {
          const isValidList = Array.isArray(list) && list.every(item =>
            item && typeof item === 'object' && !Array.isArray(item) &&
            typeof item.id === 'string' && item.id.trim().length > 0 &&
            typeof item.name === 'string' && item.name.trim().length > 0
          );
          if (!isValidList) {
            sendJson(res, 400, { error: "Invalid tenants payload" });
            return;
          }
          const sanitized = list.map(normalizeTenant);
          const nextIds = new Set(sanitized.map(x => x.id));
          // Record removed tenant IDs explicitly
          memoryState.tenants.forEach(old => {
            if (!nextIds.has(old.id) && !memoryState.deletedIds.includes(old.id)) {
              memoryState.deletedIds.push(old.id);
            }
          });
          // Unmark re-added tenant IDs
          memoryState.deletedIds = memoryState.deletedIds.filter(id => !nextIds.has(id));
          memoryState.tenants = sanitized;
          memoryState.updatedAt = new Date().toISOString();
          storageStatus.hasMutationSinceBoot = true;
          if (!persistMemoryStateToDisk()) {
            sendJson(res, 500, { error: "Failed to save tenants" });
            return;
          }
          triggerCloudBackupAsync();
          sendJson(res, 200, { success: true, count: sanitized.length });
        });
        return;
      } else if (req.method === 'DELETE') {
        if (!requireDevAuth(req, res)) return;
        const deleteAll = asString(query.get('all')).trim();
        if (deleteAll === 'true') {
          memoryState.tenants.forEach(t => {
            if (!memoryState.deletedIds.includes(t.id)) memoryState.deletedIds.push(t.id);
          });
          memoryState.tenants = [];
          memoryState.updatedAt = new Date().toISOString();
          storageStatus.hasMutationSinceBoot = true;
          if (!persistMemoryStateToDisk()) {
            sendJson(res, 500, { error: "Failed to clear tenants" });
            return;
          }
          triggerCloudBackupAsync();
          sendJson(res, 200, { success: true, count: 0 });
          return;
        }
        const tenantId = asString(query.get('id')).trim();
        if (tenantId) {
          memoryState.tenants = memoryState.tenants.filter(t => t.id !== tenantId);
          if (!memoryState.deletedIds.includes(tenantId)) {
            memoryState.deletedIds.push(tenantId);
          }
          memoryState.updatedAt = new Date().toISOString();
          storageStatus.hasMutationSinceBoot = true;
          if (!persistMemoryStateToDisk()) {
            sendJson(res, 500, { error: "Failed to save tenants" });
            return;
          }
          triggerCloudBackupAsync();
          sendJson(res, 200, { success: true, count: memoryState.tenants.length });
          return;
        }
        sendJson(res, 400, { error: "Missing id or all=true parameter" });
        return;
      }
    }

    // ---------------------------------------------------------------------
    // API 3: Xác thực Đăng nhập & Kiểm tra Bản quyền / License cho App Android & Web
    // ---------------------------------------------------------------------
    if (
      pathname === '/api/tenants/auth' ||
      pathname === '/api/auth' ||
      pathname === '/api/tenants/status' ||
      pathname === '/api/check-license'
    ) {
      const phone = asString(query.get('phone')).trim().replace(/[^0-9]/g, '');
      const pin = asString(query.get('pin')).trim();
      const queryId = (asString(query.get('id')) || asString(query.get('tenantId'))).trim();

      const checkAuth = (p, pass, qId) => {
        const cleanP = asString(p).replace(/[^0-9]/g, '');
        const cleanQId = asString(qId).trim();
        const isStatusEndpoint = (pathname === '/api/tenants/status' || pathname === '/api/check-license');
        const list = memoryState.tenants.map(normalizeTenant);
        memoryState.tenants = list;

        const t = list.find(item => {
          const itemPhone = asString(item.ownerPhone).replace(/[^0-9]/g, '');
          const matchPhone = cleanP && itemPhone === cleanP;
          const matchId = cleanQId && item.id === cleanQId;
          return matchPhone || matchId;
        });

        if (!t) {
          const wasExplicitlyDeleted = Boolean(
            (cleanQId && memoryState.deletedIds.includes(cleanQId)) ||
            (cleanP && memoryState.deletedIds.includes(`spa_${cleanP}`))
          );
          // Nếu Render vừa tỉnh giấc từ chế độ local-ephemeral trống (chưa gắn Cloud DB & chưa có xóa rõ ràng),
          // đánh dấu coldStartEmpty để tránh đá nhầm app đang chạy, trừ khi cơ sở đó nằm trong deletedIds.
          const coldStartEmpty = (
            isStatusEndpoint &&
            storageStatus.mode === 'local-ephemeral' &&
            !storageStatus.hasMutationSinceBoot &&
            list.length === 0 &&
            !wasExplicitlyDeleted
          );
          return {
            success: false,
            notFound: true,
            coldStartEmpty,
            serverTime: Date.now(),
            message: "Không tìm thấy cơ sở Spa với số điện thoại này!"
          };
        }

        if (t.isActive === false) {
          return {
            success: false,
            locked: true,
            serverTime: Date.now(),
            expiredAt: t.expiredAt,
            message: "Cơ sở này đang bị khóa tạm thời. Vui lòng liên hệ Dev để gia hạn."
          };
        }

        if (t.expiredAt && t.expiredAt <= Date.now()) {
          return {
            success: false,
            locked: true,
            expired: true,
            serverTime: Date.now(),
            expiredAt: t.expiredAt,
            message: `Gói dịch vụ của cơ sở ${t.name} đã hết hạn (${t.expiryDate}). Vui lòng gia hạn để tiếp tục sử dụng.`
          };
        }

        if (isStatusEndpoint) {
          return {
            success: true,
            active: true,
            serverTime: Date.now(),
            expiredAt: t.expiredAt,
            tenant: stripSensitiveTenantFields(t)
          };
        }

        const cleanPass = asString(pass).trim();
        const expectedPin = asString(t.tempPin).trim();
        if (expectedPin.length > 0 && cleanPass.length > 0 && safeEqual(cleanPass, expectedPin)) {
          return {
            success: true,
            serverTime: Date.now(),
            expiredAt: t.expiredAt,
            tenant: stripSensitiveTenantFields(t)
          };
        }

        if (cleanPass.length === 0 || expectedPin.length === 0) {
          return { success: false, message: "Thiếu mã PIN hoặc sai mật khẩu!" };
        }

        return { success: false, message: "Sai mật khẩu / mã PIN đăng nhập!" };
      };

      if (req.method === 'GET') {
        sendJson(res, 200, checkAuth(phone, pin, queryId));
        return;
      } else if (req.method === 'POST') {
        readJsonBody(req, res, (data) => {
          if (!data || typeof data !== 'object' || Array.isArray(data)) {
            sendJson(res, 400, { error: "Invalid payload" });
            return;
          }
          sendJson(res, 200, checkAuth(data.phone, data.pin, asString(data.id) || asString(data.tenantId)));
        });
        return;
      }
    }

    // ---------------------------------------------------------------------
    // STATIC FILE SERVING (Chống Path Traversal, NTFS ADS, chặn file cấu hình nhạy cảm)
    // ---------------------------------------------------------------------
    const publicRoot = path.resolve(PUBLIC_DIR);
    let decodedPath;
    try {
      decodedPath = decodeURIComponent(pathname || '/');
    } catch (e) {
      res.writeHead(400, { 'Content-Type': 'text/plain; charset=UTF-8' });
      res.end('Bad Request');
      return;
    }
    if (decodedPath.includes('\0')) {
      res.writeHead(400, { 'Content-Type': 'text/plain; charset=UTF-8' });
      res.end('Bad Request');
      return;
    }
    if (decodedPath.includes(':')) {
      res.writeHead(403, { 'Content-Type': 'text/plain; charset=UTF-8' });
      res.end('Forbidden');
      return;
    }

    const relPath = (decodedPath === '/' || decodedPath === '') ? 'index.html' : decodedPath.replace(/^\/+/, '');
    let filePath = path.resolve(publicRoot, relPath);

    if (filePath !== publicRoot && !filePath.startsWith(publicRoot + path.sep)) {
      res.writeHead(403, { 'Content-Type': 'text/plain; charset=UTF-8' });
      res.end('Forbidden');
      return;
    }

    const baseName = path.basename(filePath).split(':')[0].toLowerCase();
    if (baseName === 'tenants.json' || baseName === 'portal-config.json' || baseName === 'ai-config.json') {
      res.writeHead(403, { 'Content-Type': 'text/plain; charset=UTF-8' });
      res.end('Forbidden');
      return;
    }

    if (!fs.existsSync(filePath)) {
      filePath = path.join(publicRoot, 'index.html');
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    fs.stat(filePath, (err, stats) => {
      if (err || !stats.isFile()) {
        res.writeHead(404, { 'Content-Type': 'text/plain; charset=UTF-8' });
        res.end('Không tìm thấy tệp yêu cầu');
        return;
      }

      const headers = {
        'Content-Type': contentType,
        'Content-Length': stats.size,
        'Accept-Ranges': 'bytes'
      };

      if (ext === '.apk') {
        headers['Content-Disposition'] = 'attachment; filename="KhiemThiViet-CaiDat.apk"';
      }

      if (req.method === 'HEAD') {
        res.writeHead(200, headers);
        res.end();
        return;
      }

      res.writeHead(200, headers);
      const stream = fs.createReadStream(filePath);
      stream.pipe(res);
    });
  } catch (err) {
    console.error("Lỗi xử lý request:", err);
    if (!res.headersSent) {
      res.writeHead(500, { 'Content-Type': 'application/json; charset=UTF-8' });
    }
    if (!res.writableEnded) {
      res.end(JSON.stringify({ success: false, error: "Internal Server Error" }));
    }
  }
});

hydrateStateFromCloudOnStartup().finally(() => {
  server.listen(PORT, '0.0.0.0', () => {
    console.log(`=======================================================`);
    console.log(`👑 KHIẾM THỊ VIỆT SAAS - WEB PORTAL & LICENSE API SERVER`);
    console.log(`👉 Web Portal: http://localhost:${PORT}`);
    console.log(`👉 Health Check (UptimeRobot): /health & /api/health`);
    console.log(`👉 Storage Mode: ${storageStatus.mode} (Cloud Synced: ${storageStatus.cloudSynced})`);
    if (CUSTOM_DOMAIN) console.log(`👉 Custom Domain: https://${CUSTOM_DOMAIN}`);
    console.log(`=======================================================`);
  });
});
