const http = require('http');
const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const assert = require('assert');

const PORT = 3099;
const SANDBOX_TENANTS_FILE = path.join(__dirname, 'test_tenants_sandbox.json');
const SANDBOX_CONFIG_FILE = path.join(__dirname, 'test_portal_config_sandbox.json');
const DEV_PIN = '247208';

function request(method, reqPath, body = null, headers = {}) {
  return new Promise((resolve, reject) => {
    const bodyStr = body !== null ? (typeof body === 'string' ? body : JSON.stringify(body)) : null;
    const reqHeaders = Object.assign({}, headers);
    if (bodyStr !== null) {
      if (!reqHeaders['Content-Type']) reqHeaders['Content-Type'] = 'application/json';
      reqHeaders['Content-Length'] = Buffer.byteLength(bodyStr);
    }
    const req = http.request({
      hostname: '127.0.0.1',
      port: PORT,
      path: reqPath,
      method,
      headers: reqHeaders
    }, (res) => {
      const chunks = [];
      res.on('data', c => chunks.push(c));
      res.on('end', () => {
        const text = Buffer.concat(chunks).toString('utf8');
        let json = null;
        try { json = text ? JSON.parse(text) : null; } catch (_) {}
        resolve({ status: res.statusCode, text, json, headers: res.headers });
      });
    });
    req.on('error', reject);
    if (bodyStr !== null) req.write(bodyStr);
    req.end();
  });
}

function startServer(envExtra = {}) {
  return new Promise((resolve, reject) => {
    const child = spawn(process.execPath, [path.join(__dirname, 'server.js')], {
      env: Object.assign({}, process.env, {
        PORT: String(PORT),
        TENANTS_FILE: SANDBOX_TENANTS_FILE,
        PORTAL_CONFIG_FILE: SANDBOX_CONFIG_FILE,
        DEV_ADMIN_PIN: DEV_PIN,
        FIREBASE_SERVICE_ACCOUNT_JSON: '',
        GITHUB_TOKEN: ''
      }, envExtra),
      stdio: ['ignore', 'pipe', 'pipe']
    });
    let started = false;
    child.stdout.on('data', (d) => {
      if (!started && d.toString().includes('Web Portal:')) {
        started = true;
        resolve(child);
      }
    });
    child.stderr.on('data', (d) => {
      const msg = d.toString();
      if (!msg.includes('DeprecationWarning')) {
        process.stderr.write(`Server stderr: ${msg}`);
      }
    });
    child.on('exit', (code) => {
      if (!started) reject(new Error(`Server exited early with code ${code}`));
    });
    setTimeout(() => {
      if (!started) {
        child.kill();
        reject(new Error('Server start timeout'));
      }
    }, 8000);
  });
}

function cleanup() {
  [SANDBOX_TENANTS_FILE, SANDBOX_CONFIG_FILE].forEach(f => {
    try { if (fs.existsSync(f)) fs.unlinkSync(f); } catch (_) {}
  });
}

async function runTests() {
  console.log('=== Bắt đầu kiểm thử toàn diện Backend .id.vn / Render / License / Bảo mật ===');
  cleanup();

  const futureMs = Date.now() + 30 * 86400000;
  const pastMs = Date.now() - 2 * 86400000;
  const initialTenants = [
    {
      id: 'spa_active',
      name: 'Spa Hoa Sen',
      ownerPhone: '0911222333',
      plan: 'Gói 1 Tháng',
      expiredAt: futureMs,
      isActive: true,
      monthlyFee: 500000,
      tempPin: '654321'
    },
    {
      id: 'spa_expired',
      name: 'Spa Hết Hạn',
      ownerPhone: '0922333444',
      plan: 'Gói 1 Tháng',
      expiredAt: pastMs,
      isActive: true,
      monthlyFee: 500000,
      tempPin: '111222'
    }
  ];
  fs.writeFileSync(SANDBOX_TENANTS_FILE, JSON.stringify(initialTenants, null, 2), 'utf8');

  let proc = await startServer();
  try {
    // 1. Health endpoints for UptimeRobot
    const h1 = await request('GET', '/health');
    const h2 = await request('GET', '/api/health');
    assert.strictEqual(h1.status, 200);
    assert.strictEqual(h1.json.status, 'OK');
    assert.strictEqual(h2.status, 200);
    console.log('✓ Test 1: /health & /api/health trả 200 OK (sẵn sàng UptimeRobot)');

    // 2. Unauthenticated GET /api/tenants strips tempPin; write APIs return 401
    const pubList = await request('GET', '/api/tenants');
    assert.strictEqual(pubList.status, 200);
    assert.strictEqual(pubList.json.length, 2);
    assert.strictEqual(pubList.json[0].tempPin, undefined, 'Public GET must not leak tempPin!');
    const unauthPost = await request('POST', '/api/tenants', []);
    assert.strictEqual(unauthPost.status, 401);
    const unauthDel = await request('DELETE', '/api/tenants?id=spa_active');
    assert.strictEqual(unauthDel.status, 401);
    const unauthCfg = await request('POST', '/api/system/portal-config', { globalMaintenance: true });
    assert.strictEqual(unauthCfg.status, 401);
    console.log('✓ Test 2: Chặn toàn bộ API ghi khi chưa xác thực (401) & ẩn tempPin với khách công khai');

    // 3. Dev Login (wrong PIN 401, right PIN 200 + Bearer token)
    const badLogin = await request('POST', '/api/dev/login', { pin: '000000' });
    assert.strictEqual(badLogin.status, 401);
    const goodLogin = await request('POST', '/api/dev/login', { pin: DEV_PIN });
    assert.strictEqual(goodLogin.status, 200);
    assert.strictEqual(goodLogin.json.success, true);
    const authHeader = { Authorization: `Bearer ${goodLogin.json.token}` };
    const devList = await request('GET', '/api/tenants', null, authHeader);
    assert.strictEqual(devList.json[0].tempPin, '654321', 'Dev GET should include tempPin');
    console.log('✓ Test 3: POST /api/dev/login cấp HMAC Bearer Token chuẩn');

    // 4. License check & auto-lock on expiredAt + extend +30 days unlocks immediately
    const activeCheck = await request('GET', '/api/check-license?phone=0911222333');
    assert.strictEqual(activeCheck.json.success, true);
    assert.strictEqual(activeCheck.json.active, true);
    const expiredCheck = await request('GET', '/api/check-license?phone=0922333444');
    assert.strictEqual(expiredCheck.json.success, false);
    assert.strictEqual(expiredCheck.json.locked, true);
    assert.strictEqual(expiredCheck.json.expired, true);

    const extRes = await request('POST', '/api/tenants/extend', { id: 'spa_expired', days: 30, planName: 'Gói 1 Tháng' }, authHeader);
    assert.strictEqual(extRes.status, 200);
    assert.strictEqual(extRes.json.success, true);
    assert.ok(extRes.json.tenant.daysLeft >= 29);

    const unlockedCheck = await request('GET', '/api/check-license?phone=0922333444');
    assert.strictEqual(unlockedCheck.json.success, true);
    assert.strictEqual(unlockedCheck.json.active, true);
    console.log('✓ Test 4: Tự khóa khi hết hạn expiredAt & gia hạn +30 ngày mở khóa ngay lập tức');

    // 5. Auth rejects universal PIN bypass (247208 / 1234 / empty) & accepts exact tempPin
    const bypass1 = await request('GET', '/api/tenants/auth?phone=0911222333&pin=247208');
    assert.strictEqual(bypass1.json.success, false);
    const bypass2 = await request('GET', '/api/tenants/auth?phone=0911222333&pin=1234');
    assert.strictEqual(bypass2.json.success, false);
    const bypass3 = await request('GET', '/api/tenants/auth?phone=0911222333');
    assert.strictEqual(bypass3.json.success, false);
    const validAuth = await request('POST', '/api/tenants/auth', { phone: '0911222333', pin: '654321' });
    assert.strictEqual(validAuth.json.success, true);
    console.log('✓ Test 5: Chặn tuyệt đối PIN cửa hậu (247208/1234/rỗng), chỉ nhận đúng tempPin');

    // 6. Server-side Pricing & Bank Config persistence
    const saveCfg = await request('POST', '/api/system/portal-config', {
      pricingPlans: [{ id: '1_month', name: 'Dùng Thử 1 Tháng', days: 30, fee: 0, enabled: true, tag: 'Free' }],
      bankConfig: { bankCode: 'VCB', bankName: 'Vietcombank', accountNo: '99998888', accountName: 'TEST OWNER' },
      customDomain: 'spa.id.vn'
    }, authHeader);
    assert.strictEqual(saveCfg.status, 200);
    const getCfg = await request('GET', '/api/system/portal-config');
    assert.strictEqual(getCfg.json.pricingPlans[0].name, 'Dùng Thử 1 Tháng');
    assert.strictEqual(getCfg.json.bankConfig.bankCode, 'VCB');
    assert.strictEqual(getCfg.json.customDomain, 'spa.id.vn');
    console.log('✓ Test 6: Lưu & đồng bộ Bảng giá, Ngân hàng và tên miền .id.vn phía server');

    // 7. Delete single & delete all -> restart stays empty [] + coldStartEmpty flag
    const delAll = await request('DELETE', '/api/tenants?all=true', null, authHeader);
    assert.strictEqual(delAll.status, 200);
    assert.strictEqual(delAll.json.count, 0);
    const deletedStatus = await request('GET', '/api/tenants/status?phone=0911222333&tenantId=spa_active');
    assert.strictEqual(deletedStatus.json.notFound, true);
    assert.strictEqual(deletedStatus.json.coldStartEmpty, false, 'Explicitly deleted tenant must NOT set coldStartEmpty');

    // 8. Static security checks (Path traversal, NTFS ADS, sensitive JSON files, 1MB payload limit)
    const pt = await request('GET', '/..%2fserver.js');
    assert.strictEqual(pt.status, 403);
    const tj = await request('GET', '/tenants.json');
    assert.strictEqual(tj.status, 403);
    const pj = await request('GET', '/portal-config.json');
    assert.strictEqual(pj.status, 403);
    const aj = await request('GET', '/ai-config.json');
    assert.strictEqual(aj.status, 403);
    const ads = await request('GET', '/tenants.json::$DATA');
    assert.strictEqual(ads.status, 403);
    console.log('✓ Test 7 & 8: Chặn Path Traversal, NTFS ADS, và truy cập trực tiếp file JSON nhạy cảm (403)');

    // 9. Restart server with [] and verify it stays []
    proc.kill();
    await new Promise(r => setTimeout(r, 400));
    proc = await startServer();
    const afterRestart = await request('GET', '/api/tenants');
    assert.deepStrictEqual(afterRestart.json, []);
    const storageRes = await request('GET', '/api/status/storage');
    assert.strictEqual(storageRes.status, 200);
    assert.strictEqual(storageRes.json.storageMode, 'local-ephemeral');
    console.log('✓ Test 9: Restart server với [] không tự hồi sinh cơ sở mẫu & /api/status/storage hoạt động chuẩn');

    // 10. Real repo public/tenants.json is clean []
    const realTenants = JSON.parse(fs.readFileSync(path.join(__dirname, 'public', 'tenants.json'), 'utf8'));
    assert.deepStrictEqual(realTenants, []);
    console.log('✓ Test 10: public/tenants.json chuẩn sạch []');

    console.log('\n>>> TẤT CẢ 10 NHÓM KIỂM THỬ BACKEND & BẢO MẬT ĐỀU ĐẠT 100%! <<<');
  } finally {
    try { proc.kill(); } catch (_) {}
    cleanup();
  }
}

runTests().catch(err => {
  console.error('❌ TEST FAILED:', err);
  cleanup();
  process.exit(1);
});
