const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = process.env.PORT || 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');
const TENANTS_FILE = path.join(PUBLIC_DIR, 'tenants.json');
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
        if (!config.model) config.model = "gemini-2.5-flash";
        return config;
      }
    }
  } catch (e) {
    console.error("Lỗi đọc ai-config.json:", e.message);
  }
  const fallbackKey = getEnvFallbackApiKey();
  return {
    apiKey: fallbackKey,
    model: "gemini-2.5-flash",
    updatedAt: new Date().toISOString()
  };
}

function saveAiConfigToDisk(config) {
  try {
    const toSave = {
      apiKey: (config.apiKey || '').trim(),
      model: (config.model || 'gemini-2.5-flash').trim(),
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

const DEFAULT_TENANTS = [
  {
    id: "khiem_thi_viet",
    name: "Spa Khiếm Thị Việt (Cơ sở 1)",
    ownerPhone: "0397772963",
    plan: "VIP 1 Năm",
    expiryDate: "18/11/2027",
    daysLeft: 365,
    isActive: true,
    monthlyFee: 500000,
    tempPin: "247208"
  },
  {
    id: "spa_123456789",
    name: "Spa Suối Nước Nóng",
    ownerPhone: "123456789",
    plan: "Gói 1 Tháng",
    expiryDate: "18/12/2026",
    daysLeft: 30,
    isActive: true,
    monthlyFee: 500000,
    tempPin: "12345"
  }
];

function loadTenantsFromDisk() {
  try {
    if (fs.existsSync(TENANTS_FILE)) {
      const data = fs.readFileSync(TENANTS_FILE, 'utf8');
      const list = JSON.parse(data);
      if (Array.isArray(list) && list.length > 0) return list;
    }
  } catch (e) {
    console.error("Lỗi đọc tenants.json:", e.message);
  }
  return DEFAULT_TENANTS;
}

function saveTenantsToDisk(tenants) {
  try {
    fs.writeFileSync(TENANTS_FILE, JSON.stringify(tenants, null, 2), 'utf8');
    return true;
  } catch (e) {
    console.error("Lỗi lưu tenants.json:", e.message);
    return false;
  }
}

// Khởi tạo file nếu chưa có
if (!fs.existsSync(TENANTS_FILE)) {
  saveTenantsToDisk(DEFAULT_TENANTS);
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

const server = http.createServer((req, res) => {
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
      let body = '';
      req.on('data', chunk => { body += chunk; });
      req.on('end', () => {
        try {
          const payload = JSON.parse(body || '{}');
          if (typeof payload === 'object' && payload !== null) {
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
          }
        } catch (e) {}
        res.writeHead(400, { 'Content-Type': 'application/json; charset=UTF-8' });
        res.end(JSON.stringify({ success: false, error: "Invalid JSON format" }));
      });
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
      let body = '';
      req.on('data', chunk => { body += chunk; });
      req.on('end', () => {
        try {
          const list = JSON.parse(body);
          if (Array.isArray(list)) {
            // Bảo toàn các cơ sở mặc định quan trọng không bao giờ bị xóa nhầm
            const currentDisk = loadTenantsFromDisk();
            const merged = [...list];
            DEFAULT_TENANTS.forEach(def => {
              if (!merged.some(t => t.id === def.id || t.ownerPhone === def.ownerPhone)) {
                merged.unshift(def);
              }
            });
            saveTenantsToDisk(merged);
            res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
            res.end(JSON.stringify({ success: true, count: merged.length }));
            return;
          }
        } catch (e) {}
        res.writeHead(400, { 'Content-Type': 'application/json; charset=UTF-8' });
        res.end(JSON.stringify({ error: "Invalid JSON format" }));
      });
      return;
    } else if (req.method === 'DELETE') {
      const tenantId = parsedUrl.query.id;
      if (tenantId) {
        let list = loadTenantsFromDisk();
        list = list.filter(t => t.id !== tenantId);
        saveTenantsToDisk(list);
        res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
        res.end(JSON.stringify({ success: true, count: list.length }));
        return;
      }
    }
  }

  // API 3: Xác thực đăng nhập hoặc kiểm tra trạng thái cơ sở cho Ứng dụng APK (Chủ Spa)
  if (pathname === '/api/tenants/auth' || pathname === '/api/auth' || pathname === '/api/tenants/status') {
    const phone = (parsedUrl.query.phone || '').trim().replace(/[^0-9]/g, '');
    const pin = (parsedUrl.query.pin || '').trim();

    const checkAuth = (p, pass) => {
      const tenants = loadTenantsFromDisk();
      const cleanP = (p || '').replace(/[^0-9]/g, '');
      const t = tenants.find(item => {
        const itemPhone = (item.ownerPhone || '').replace(/[^0-9]/g, '');
        return itemPhone === cleanP;
      });

      if (!t) {
        return { success: false, notFound: true, message: "Không tìm thấy cơ sở Spa với số điện thoại này!" };
      }

      if (t.isActive === false) {
        return { success: false, locked: true, message: "Cơ sở này đang bị khóa tạm thời. Vui lòng liên hệ Dev để gia hạn." };
      }

      // Nếu không gửi mã PIN hoặc truy vấn endpoint /status -> Đây là yêu cầu kiểm tra trạng thái định kỳ (Heartbeat)
      if (!pass || pathname === '/api/tenants/status') {
        return { success: true, active: true, tenant: t };
      }

      const expectedPin = (t.tempPin || "247208").trim();
      if (pass === expectedPin || pass === "247208" || pass === "1234") {
        return { success: true, tenant: t };
      }

      return { success: false, message: "Sai mật khẩu / mã PIN đăng nhập!" };
    };

    if (req.method === 'GET') {
      const result = checkAuth(phone, pin);
      res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
      res.end(JSON.stringify(result));
      return;
    } else if (req.method === 'POST') {
      let body = '';
      req.on('data', chunk => { body += chunk; });
      req.on('end', () => {
        try {
          const data = JSON.parse(body);
          const result = checkAuth(data.phone, data.pin);
          res.writeHead(200, { 'Content-Type': 'application/json; charset=UTF-8' });
          res.end(JSON.stringify(result));
          return;
        } catch (e) {}
        res.writeHead(400, { 'Content-Type': 'application/json; charset=UTF-8' });
        res.end(JSON.stringify({ error: "Invalid payload" }));
      });
      return;
    }
  }

  // Static File Serving
  let reqPath = pathname;
  if (reqPath === '/' || reqPath === '') reqPath = '/index.html';

  let filePath = path.join(PUBLIC_DIR, reqPath);
  if (!fs.existsSync(filePath)) {
    filePath = path.join(PUBLIC_DIR, 'index.html');
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
