/**
 * test_facility_deletion_permanent.js
 * Comprehensive integration test for R3: Permanent Facility Deletion, Empty State [], and Backend Hardening.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const assert = require('assert');

const TEST_PORT = 3099;
const TEST_PORT_MISSING = 3100;
const SANDBOX_FILE = path.join(__dirname, 'test_tenants_sandbox.json');
const MISSING_SANDBOX_FILE = path.join(__dirname, 'test_tenants_missing_sandbox.json');

const INITIAL_SANDBOX_DATA = [
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

function request(method, pathUrl, data = null, port = TEST_PORT) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: '127.0.0.1',
      port,
      path: pathUrl,
      method,
      headers: {
        'Content-Type': 'application/json'
      }
    };

    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', chunk => { body += chunk; });
      res.on('end', () => {
        try {
          const parsed = body ? JSON.parse(body) : null;
          resolve({ status: res.statusCode, body: parsed, raw: body });
        } catch (e) {
          resolve({ status: res.statusCode, raw: body });
        }
      });
    });

    req.on('error', reject);
    if (data !== null && data !== undefined) {
      req.write(typeof data === 'string' ? data : JSON.stringify(data));
    }
    req.end();
  });
}

async function waitForServer(port = TEST_PORT, retries = 30, delayMs = 150) {
  for (let i = 0; i < retries; i++) {
    try {
      const res = await request('GET', '/api/health', null, port);
      if (res.status === 200) return true;
    } catch (e) {}
    await new Promise(r => setTimeout(r, delayMs));
  }
  throw new Error("Server failed to start on test port " + port);
}

async function run() {
  console.log("=== Bắt đầu kiểm thử R3: Xóa sạch cơ sở [], không hồi sinh & Bảo mật Backend ===");

  fs.writeFileSync(SANDBOX_FILE, JSON.stringify(INITIAL_SANDBOX_DATA, null, 2), 'utf8');
  if (fs.existsSync(MISSING_SANDBOX_FILE)) fs.unlinkSync(MISSING_SANDBOX_FILE);
  console.log("✓ Đã khởi tạo sandbox:", SANDBOX_FILE);

  const env = Object.assign({}, process.env, {
    PORT: TEST_PORT.toString(),
    TENANTS_FILE: SANDBOX_FILE
  });

  const serverProc = spawn('node', ['server.js'], { env, cwd: __dirname });
  serverProc.stderr.on('data', d => console.error("Server stderr:", d.toString()));

  try {
    await waitForServer(TEST_PORT);
    console.log("✓ Server test đang chạy tại port", TEST_PORT);

    // Test 1: GET /api/tenants trả về danh sách ban đầu gồm 2 cơ sở
    const resGet1 = await request('GET', '/api/tenants');
    assert.strictEqual(resGet1.status, 200);
    assert.strictEqual(resGet1.body.length, 2);
    assert.ok(resGet1.body.some(t => t.id === 'spa_123456789'), "Danh sách ban đầu phải có spa_123456789");
    console.log("✓ Test 1: GET /api/tenants trả về 2 cơ sở ban đầu");

    // Test 2: Xóa cơ sở 'spa_123456789' qua DELETE /api/tenants?id=spa_123456789
    const resDel = await request('DELETE', '/api/tenants?id=spa_123456789');
    assert.strictEqual(resDel.status, 200);
    assert.strictEqual(resDel.body.success, true);
    assert.strictEqual(resDel.body.count, 1);
    const diskAfterDel1 = JSON.parse(fs.readFileSync(SANDBOX_FILE, 'utf8'));
    assert.strictEqual(diskAfterDel1.length, 1);
    assert.ok(!diskAfterDel1.some(t => t.id === 'spa_123456789'));
    console.log("✓ Test 2: DELETE /api/tenants?id=spa_123456789 xóa thành công");

    // Test 3: Xóa 'khiem_thi_viet' qua DELETE /api/tenants?id=khiem_thi_viet -> Thành công (200), disk là []
    const resDelAdmin = await request('DELETE', '/api/tenants?id=khiem_thi_viet');
    assert.strictEqual(resDelAdmin.status, 200);
    assert.strictEqual(resDelAdmin.body.success, true);
    assert.strictEqual(resDelAdmin.body.count, 0);
    const diskAfterDelAll = JSON.parse(fs.readFileSync(SANDBOX_FILE, 'utf8'));
    assert.deepStrictEqual(diskAfterDelAll, []);
    console.log("✓ Test 3: Xóa được 'khiem_thi_viet' (HTTP 200), disk về mảng rỗng []");

    // Test 4: POST /api/tenants với [] -> 200, count 0, không hồi sinh DEFAULT_TENANTS
    const resPostEmpty = await request('POST', '/api/tenants', []);
    assert.strictEqual(resPostEmpty.status, 200);
    assert.strictEqual(resPostEmpty.body.count, 0);
    assert.deepStrictEqual(JSON.parse(fs.readFileSync(SANDBOX_FILE, 'utf8')), []);
    console.log("✓ Test 4: POST /api/tenants với [] lưu chính xác []");

    // Test 5: Restart server với sandbox [] và boot server mới với TENANTS_FILE chưa tồn tại -> đều trả về []
    serverProc.kill();
    await new Promise(r => setTimeout(r, 500));

    const serverProc2 = spawn('node', ['server.js'], { env, cwd: __dirname });
    try {
      await waitForServer(TEST_PORT);
      const resGetAfterRestart = await request('GET', '/api/tenants');
      assert.strictEqual(resGetAfterRestart.status, 200);
      assert.deepStrictEqual(resGetAfterRestart.body, []);
      console.log("✓ Test 5a: Sau khi Restart server với [], không hồi sinh bất kỳ cơ sở nào");

      // Test 6: DELETE /api/tenants?all=true xóa sạch toàn bộ danh sách, và DELETE thiếu tham số -> 400
      const seedRes = await request('POST', '/api/tenants', [
        { id: "spa_999", name: "Spa Test PIN", ownerPhone: "0999888777", isActive: true, tempPin: "998877" },
        { id: "spa_nopin", name: "Spa No PIN", ownerPhone: "0955667788", isActive: true, tempPin: "" }
      ]);
      assert.strictEqual(seedRes.status, 200);
      assert.strictEqual(seedRes.body.count, 2);

      // Test PIN bypass rejection (1234 & 247208 phải bị từ chối; 998877 thành công)
      const auth1234 = await request('GET', '/api/tenants/auth?phone=0999888777&pin=1234');
      assert.strictEqual(auth1234.status, 200);
      assert.strictEqual(auth1234.body.success, false);

      const auth247208 = await request('GET', '/api/tenants/auth?phone=0999888777&pin=247208');
      assert.strictEqual(auth247208.status, 200);
      assert.strictEqual(auth247208.body.success, false);

      // Test empty/omitted PIN on /api/tenants/auth and /api/auth (GET & POST) must fail
      const authEmptyGet = await request('GET', '/api/tenants/auth?phone=0999888777&pin=');
      assert.strictEqual(authEmptyGet.status, 200);
      assert.strictEqual(authEmptyGet.body.success, false);

      const authOmittedGet = await request('GET', '/api/tenants/auth?phone=0999888777');
      assert.strictEqual(authOmittedGet.status, 200);
      assert.strictEqual(authOmittedGet.body.success, false);

      const authEmptyPost = await request('POST', '/api/tenants/auth', { phone: '0999888777', pin: '' });
      assert.strictEqual(authEmptyPost.status, 200);
      assert.strictEqual(authEmptyPost.body.success, false);

      const authOmittedPost = await request('POST', '/api/tenants/auth', { phone: '0999888777' });
      assert.strictEqual(authOmittedPost.status, 200);
      assert.strictEqual(authOmittedPost.body.success, false);

      const authLegacyEmptyGet = await request('GET', '/api/auth?phone=0999888777&pin=');
      assert.strictEqual(authLegacyEmptyGet.status, 200);
      assert.strictEqual(authLegacyEmptyGet.body.success, false);

      const authNoPinTenantGet = await request('GET', '/api/tenants/auth?phone=0955667788&pin=');
      assert.strictEqual(authNoPinTenantGet.status, 200);
      assert.strictEqual(authNoPinTenantGet.body.success, false);

      // Android Heartbeat on /api/tenants/status still succeeds without PIN
      const statusHeartbeat = await request('GET', '/api/tenants/status?phone=0999888777&tenantId=spa_999');
      assert.strictEqual(statusHeartbeat.status, 200);
      assert.strictEqual(statusHeartbeat.body.success, true);
      assert.strictEqual(statusHeartbeat.body.active, true);

      const authValid = await request('GET', '/api/tenants/auth?phone=0999888777&pin=998877');
      assert.strictEqual(authValid.status, 200);
      assert.strictEqual(authValid.body.success, true);

      const authPostBad = await request('POST', '/api/tenants/auth', { phone: '0999888777', pin: '1234' });
      assert.strictEqual(authPostBad.status, 200);
      assert.strictEqual(authPostBad.body.success, false);

      const authPostValid = await request('POST', '/api/tenants/auth', { phone: '0999888777', pin: '998877' });
      assert.strictEqual(authPostValid.status, 200);
      assert.strictEqual(authPostValid.body.success, true);
      console.log("✓ Test 6: Từ chối universal PIN bypass & empty/omitted PIN trên /auth, giữ heartbeat /status");

      // Test HPP (HTTP Parameter Pollution) không làm crash server
      const hppGet = await request('GET', '/api/tenants/auth?phone=0911223344&phone=0911223344&pin=556677');
      assert.strictEqual(hppGet.status, 200);
      assert.strictEqual(hppGet.body.success, false);

      const hppPost = await request('POST', '/api/tenants/auth', { phone: ['0999888777'], pin: ['998877'] });
      assert.strictEqual(hppPost.status, 200);
      assert.strictEqual(hppPost.body.success, true);

      const hppDel = await request('DELETE', '/api/tenants?id=nonexistent&id=nonexistent2');
      assert.strictEqual(hppDel.status, 200);

      const healthAfterHpp = await request('GET', '/api/health');
      assert.strictEqual(healthAfterHpp.status, 200);
      console.log("✓ Test 6b: Chống HPP (duplicate query params & array body) không crash server");

      // Test DELETE thiếu tham số -> 400; DELETE ?all=true -> 200 & count 0
      const delMissing = await request('DELETE', '/api/tenants');
      assert.strictEqual(delMissing.status, 400);

      const delAll = await request('DELETE', '/api/tenants?all=true');
      assert.strictEqual(delAll.status, 200);
      assert.strictEqual(delAll.body.success, true);
      assert.strictEqual(delAll.body.count, 0);
      assert.deepStrictEqual(JSON.parse(fs.readFileSync(SANDBOX_FILE, 'utf8')), []);
      console.log("✓ Test 7: DELETE ?all=true xóa sạch về [], DELETE thiếu tham số trả về 400");

      // Test 8: Bảo mật Static & Validation & Body Cap 1MB
      const trav1 = await request('GET', '/../../.env');
      assert.strictEqual(trav1.status, 403);

      const trav2 = await request('GET', '/%2e%2e/server.js');
      assert.strictEqual(trav2.status, 403);

      const staticTenants = await request('GET', '/tenants.json');
      assert.strictEqual(staticTenants.status, 403);

      const staticTenantsAds = await request('GET', '/tenants.json::$DATA');
      assert.strictEqual(staticTenantsAds.status, 403);

      const badEncoding = await request('GET', '/%E0%A4%A');
      assert.strictEqual(badEncoding.status, 400);

      const badPayloadObj = await request('POST', '/api/tenants', {});
      assert.strictEqual(badPayloadObj.status, 400);

      const badPayloadItem = await request('POST', '/api/tenants', [{ id: '', name: 'Test' }]);
      assert.strictEqual(badPayloadItem.status, 400);

      const badPayloadWhitespaceName = await request('POST', '/api/tenants', [{ id: 'a', name: '   ' }]);
      assert.strictEqual(badPayloadWhitespaceName.status, 400);

      const oversized = JSON.stringify([{ id: 'x', name: 'A'.repeat(1024 * 1024 + 100) }]);
      const res413 = await request('POST', '/api/tenants', oversized);
      assert.strictEqual(res413.status, 413);
      console.log("✓ Test 8: Chặn Path Traversal (403), chặn /tenants.json & NTFS ADS (403), validate 400, giới hạn 1MB (413)");
    } finally {
      serverProc2.kill();
    }

    // Test 5b: Boot server với TENANTS_FILE chưa tồn tại -> khởi tạo []
    const envMissing = Object.assign({}, process.env, {
      PORT: TEST_PORT_MISSING.toString(),
      TENANTS_FILE: MISSING_SANDBOX_FILE
    });
    const serverProcMissing = spawn('node', ['server.js'], { env: envMissing, cwd: __dirname });
    try {
      await waitForServer(TEST_PORT_MISSING);
      const resMissing = await request('GET', '/api/tenants', null, TEST_PORT_MISSING);
      assert.strictEqual(resMissing.status, 200);
      assert.deepStrictEqual(resMissing.body, []);
      assert.deepStrictEqual(JSON.parse(fs.readFileSync(MISSING_SANDBOX_FILE, 'utf8')), []);
      console.log("✓ Test 5b: Server khởi động khi TENANTS_FILE chưa tồn tại tạo file rỗng [] (không hồi sinh mẫu)");
    } finally {
      serverProcMissing.kill();
    }

    // Test 9: Kiểm tra file production `public/tenants.json` là mảng rỗng []
    const prodFile = path.join(__dirname, 'public', 'tenants.json');
    assert.ok(fs.existsSync(prodFile), "public/tenants.json phải tồn tại");
    const prodTenants = JSON.parse(fs.readFileSync(prodFile, 'utf8'));
    assert.ok(Array.isArray(prodTenants) && prodTenants.length === 0, "public/tenants.json phải là mảng rỗng []");
    console.log("✓ Test 9: public/tenants.json chuẩn sạch là []");

    console.log("\n>>> TẤT CẢ CÁC TEST CASE R3 & HARDENING ĐỀU ĐẠT CHUẨN HOÀN TOÀN! <<<");
  } finally {
    try { serverProc.kill(); } catch (e) {}
    if (fs.existsSync(SANDBOX_FILE)) {
      fs.unlinkSync(SANDBOX_FILE);
    }
    if (fs.existsSync(MISSING_SANDBOX_FILE)) {
      fs.unlinkSync(MISSING_SANDBOX_FILE);
    }
    console.log("✓ Đã dọn dẹp sandbox test files.");
  }
}

run().catch(err => {
  console.error("Test failed:", err);
  for (const f of [SANDBOX_FILE, MISSING_SANDBOX_FILE]) {
    if (fs.existsSync(f)) {
      try { fs.unlinkSync(f); } catch (e) {}
    }
  }
  process.exit(1);
});
