const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = process.env.PORT || 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');
const TENANTS_FILE = process.env.TENANTS_FILE || path.join(PUBLIC_DIR, 'tenants.json');
const AI_CONFIG_FILE = path.join(PUBLIC_DIR, 'ai-config.json');

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
        if (val && val !== 'YOUR_GEMINI_API_KEY') {
          return val;
        }
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
  const fallbackKey = getEnvFallbackApiKey();
  return {
    apiKey: fallbackKey,
    model: "gemini-3.5-flash-lite",
    updatedAt: new Date().toISOString()
  };
}

function saveAiConfigToDisk(config) {
  try {
    const toSave = {
      apiKey: (config.apiKey || '').trim(),
      model: (config.model || 'gemini-3.5-flash-lite').trim(),
      updatedAt: new Date().toISOString()
    };
    fs.writeFileSync(AI_CONFIG_FILE, JSON.stringify(toSave, null, 2), 'utf8');
    return toSave;
  } catch (e) {
    console.error("Lỗi lưu ai-config.json:", e.message);
    return null;
  }
}

// Khởi tạo file ai-config.json nếu chưa có (với fallback từ .env)
if (!fs.existsSync(AI_CONFIG_FILE)) {
  const initConfig = loadAiConfigFromDisk();
  saveAiConfigToDisk(initConfig);
}

function loadTenantsFromDisk() {
  try {
    if (fs.existsSync(TENANTS_FILE)) {
      const data = fs.readFileSync(TENANTS_FILE, 'utf8');
      const list = JSON.parse(data);
      if (Array.isArray(list)) return list;
    }
  } catch (e) {
    console.error("Lỗi đọc tenants.json:", e.message);
  }
  return [];
}

function saveTenantsToDisk(tenants) {
  if (!Array.isArray(tenants)) return false;
  const tmpFile = `${TENANTS_FILE}.${process.pid}.${Date.now()}.${Math.random().toString(36).slice(2, 8)}.tmp`;
  try {
    const dir = path.dirname(TENANTS_FILE);
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
    fs.writeFileSync(tmpFile, JSON.stringify(tenants, null, 2), 'utf8');
    fs.renameSync(tmpFile, TENANTS_FILE);
    return true;
  } catch (e) {
    console.error("Lỗi lưu tenants.json:", e.message);
    try { if (fs.existsSync(tmpFile)) fs.unlinkSync(tmpFile); } catch (_) {}
    return false;
  }
}

// Khởi tạo file rỗng [] nếu chưa có
if (!fs.existsSync(TENANTS_FILE)) {
  saveTenantsToDisk([]);
}

const MIME_TYPES = {
  '.html': 'text/html; charset=UTF-8',
  '.js': 'text/javascript',
  '.css': 'text/css',
  '.json': 'application/json; charset=UTF-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.apk': 'application/vnd.android.package-archive'
};

const MAX_BODY_BYTES = 1024 * 1024; // 1MB cap

function asString(v) {
  return Array.isArray(v)
    ? String(v[0] ?? '')
    : (typeof v === 'string' || typeof v === 'number')
      ? String(v)
      : '';
}

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

const server = http.createServer((req, res) => {
  try {
  // CORS Headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  // API 1: Kiểm tra tình trạng máy chủ
  if (pathname === '/api/health') {
    res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
    res.end(JSON.stringify({ status: "OK", time: new Date().toISOString() }));
    return;
  }

  // API 1b: Cấu hình Gemini AI Hệ Thống (System AI Config)
  if (pathname === '/api/system/ai-config') {
    if (req.method === 'GET') {
      const config = loadAiConfigFromDisk();
      res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
      res.end(JSON.stringify({
        success: true,
        apiKey: config.apiKey || "",
        model: config.model || "gemini-2.5-flash",
        updatedAt: config.updatedAt || new Date().toISOString()
      }));
      return;
    } else if (req.method === 'POST') {
      readJsonBody(req, res, (payload) => {
        if (typeof payload === 'object' && payload !== null && !Array.isArray(payload)) {
          const saved = saveAiConfigToDisk(payload);
          if (saved) {
            res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
            res.end(JSON.stringify({
              success: true,
              message: "Cấu hình AI hệ thống đã lưu thành công",
              apiKey: saved.apiKey,
              model: saved.model,
              updatedAt: saved.updatedAt
            }));
            return;
          }
          res.writeHead(500, { 'Content-Type': 'application/json; charset=UTF-8' });
          res.end(JSON.stringify({ success: false, error: "Failed to save AI config" }));
          return;
        }
        res.writeHead(400, { 'Content-Type': 'application/json; charset=UTF-8' });
        res.end(JSON.stringify({ success: false, error: "Invalid JSON format" }));
      }, { allowEmpty: true });
      return;
    }
  }

  // API 2: Lấy danh sách hoặc lưu Tenants
  if (pathname === '/api/tenants') {
    if (req.method === 'GET') {
      const tenants = loadTenantsFromDisk();
      res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
      res.end(JSON.stringify(tenants));
      return;
    } else if (req.method === 'POST') {
      readJsonBody(req, res, (list) => {
        const isValidList = Array.isArray(list) && list.every(item =>
          item && typeof item === 'object' && !Array.isArray(item) &&
          typeof item.id === 'string' && item.id.trim().length > 0 &&
          typeof item.name === 'string' && item.name.trim().length > 0
        );
        if (!isValidList) {
          res.writeHead(400, { 'Content-Type': 'application/json; charset=UTF-8' });
          res.end(JSON.stringify({ error: "Invalid tenants payload" }));
          return;
        }
        const sanitized = list.map(item => ({
          ...item,
          id: item.id.trim(),
          name: item.name.trim(),
          ownerPhone: typeof item.ownerPhone === 'string' ? item.ownerPhone.trim() : ''
        }));
        if (!saveTenantsToDisk(sanitized)) {
          res.writeHead(500, { 'Content-Type': 'application/json; charset=UTF-8' });
          res.end(JSON.stringify({ error: "Failed to save tenants" }));
          return;
        }
        res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
        res.end(JSON.stringify({ success: true, count: sanitized.length }));
      });
      return;
    } else if (req.method === 'DELETE') {
      const deleteAll = asString(parsedUrl.query.all).trim();
      if (deleteAll === 'true') {
        if (!saveTenantsToDisk([])) {
          res.writeHead(500, { 'Content-Type': 'application/json; charset=UTF-8' });
          res.end(JSON.stringify({ error: "Failed to clear tenants" }));
          return;
        }
        res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
        res.end(JSON.stringify({ success: true, count: 0 }));
        return;
      }
      const tenantId = asString(parsedUrl.query.id).trim();
      if (tenantId) {
        let list = loadTenantsFromDisk();
        list = list.filter(t => t.id !== tenantId);
        if (!saveTenantsToDisk(list)) {
          res.writeHead(500, { 'Content-Type': 'application/json; charset=UTF-8' });
          res.end(JSON.stringify({ error: "Failed to save tenants" }));
          return;
        }
        res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
        res.end(JSON.stringify({ success: true, count: list.length }));
        return;
      }
      res.writeHead(400, { 'Content-Type': 'application/json; charset=UTF-8' });
      res.end(JSON.stringify({ error: "Missing id or all=true parameter" }));
      return;
    }
  }

  // API 3: Xác thực đăng nhập hoặc kiểm tra trạng thái cơ sở cho Ứng dụng APK (Chủ Spa)
  if (pathname === '/api/tenants/auth' || pathname === '/api/auth' || pathname === '/api/tenants/status') {
    const phone = asString(parsedUrl.query.phone).trim().replace(/[^0-9]/g, '');
    const pin = asString(parsedUrl.query.pin).trim();
    const queryId = (asString(parsedUrl.query.id) || asString(parsedUrl.query.tenantId)).trim();

    const checkAuth = (p, pass, qId) => {
      const tenants = loadTenantsFromDisk();
      const cleanP = asString(p).replace(/[^0-9]/g, '');
      const cleanQId = asString(qId).trim();
      const t = tenants.find(item => {
        const itemPhone = asString(item.ownerPhone).replace(/[^0-9]/g, '');
        const matchPhone = cleanP && itemPhone === cleanP;
        const matchId = cleanQId && item.id === cleanQId;
        return matchPhone || matchId;
      });

      if (!t) {
        return { success: false, notFound: true, message: "Không tìm thấy cơ sở Spa với số điện thoại này!" };
      }

      if (t.isActive === false) {
        return { success: false, locked: true, message: "Cơ sở này đang bị khóa tạm thời. Vui lòng liên hệ Dev để gia hạn." };
      }

      // Chỉ cho phép kiểm tra trạng thái định kỳ (Heartbeat) không cần PIN trên endpoint /api/tenants/status
      if (pathname === '/api/tenants/status') {
        return { success: true, active: true, tenant: t };
      }

      const cleanPass = asString(pass).trim();
      const expectedPin = asString(t.tempPin).trim();
      if (expectedPin.length > 0 && cleanPass.length > 0 && cleanPass === expectedPin) {
        return { success: true, tenant: t };
      }

      if (cleanPass.length === 0 || expectedPin.length === 0) {
        return { success: false, message: "Thiếu mã PIN hoặc sai mật khẩu!" };
      }

      return { success: false, message: "Sai mật khẩu / mã PIN đăng nhập!" };
    };

    if (req.method === 'GET') {
      const result = checkAuth(phone, pin, queryId);
      res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
      res.end(JSON.stringify(result));
      return;
    } else if (req.method === 'POST') {
      readJsonBody(req, res, (data) => {
        if (!data || typeof data !== 'object' || Array.isArray(data)) {
          res.writeHead(400, { 'Content-Type': 'application/json; charset=UTF-8' });
          res.end(JSON.stringify({ error: "Invalid payload" }));
          return;
        }
        const result = checkAuth(data.phone, data.pin, asString(data.id) || asString(data.tenantId));
        res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
        res.end(JSON.stringify(result));
      });
      return;
    }
  }

  // Static File Serving
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

  if (path.basename(filePath).split(':')[0].toLowerCase() === 'tenants.json') {
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

server.listen(PORT, '0.0.0.0', () => {
  console.log(`=======================================================`);
  console.log(`👑 KHIẾM THỊ VIỆT - DEV SUPER ADMIN PORTAL & API SERVER`);
  console.log(`👉 Web Portal: http://localhost:${PORT}`);
  console.log(`👉 Android Emulator API: http://10.0.2.2:${PORT}`);
  console.log(`👉 LAN API: http://192.168.1.105:${PORT}`);
  console.log(`👉 Sẵn sàng đồng bộ tài khoản Chủ Spa tức thì!`);
  console.log(`=======================================================`);
});
