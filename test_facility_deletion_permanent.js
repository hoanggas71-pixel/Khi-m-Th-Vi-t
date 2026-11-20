/**
 * test_facility_deletion_permanent.js
 * Comprehensive integration test for R3: Permanent Facility Deletion on Web & Backend.
 * Tests sandbox isolation, DELETE endpoint, POST endpoint non-resurrection, and restart recovery.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const assert = require('assert');

const TEST_PORT = 3099;
const SANDBOX_FILE = path.join(__dirname, 'test_tenants_sandbox.json');

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

function request(method, pathUrl, data = null) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: '127.0.0.1',
      port: TEST_PORT,
      path: pathUrl,
      method: method,
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
    if (data) {
      req.write(typeof data === 'string' ? data : JSON.stringify(data));
    }
    req.end();
  });
}

async function waitForServer(retries = 30, delayMs = 150) {
  for (let i = 0; i < retries; i++) {
    try {
      const res = await request('GET', '/api/health');
      if (res.status === 200) return true;
    } catch (e) {}
    await new Promise(r => setTimeout(r, delayMs));
  }
  throw new Error("Server failed to start on test port " + TEST_PORT);
}

async function run() {
  console.log("=== Bắt đầu kiểm thử R3: Xóa cơ sở vĩnh viễn trên Web & Backend ===");

  // Chuẩn bị sandbox file
  fs.writeFileSync(SANDBOX_FILE, JSON.stringify(INITIAL_SANDBOX_DATA, null, 2), 'utf8');
  console.log("✓ Đã khởi tạo sandbox:", SANDBOX_FILE);

  // Khởi động server với sandbox file và test port
  const env = Object.assign({}, process.env, {
    PORT: TEST_PORT.toString(),
    TENANTS_FILE: SANDBOX_FILE
  });

  const serverProc = spawn('node', ['server.js'], { env, cwd: __dirname });
  
  serverProc.stderr.on('data', d => console.error("Server stderr:", d.toString()));

  try {
    await waitForServer();
    console.log("✓ Server test đang chạy tại port", TEST_PORT);

    // Test 1: GET /api/tenants trả về danh sách ban đầu gồm 2 cơ sở
    const resGet1 = await request('GET', '/api/tenants');
    assert.strictEqual(resGet1.status, 200);
    assert.strictEqual(resGet1.body.length, 2);
    assert.ok(resGet1.body.some(t => t.id === 'spa_123456789'), "Danh sách ban đầu phải có spa_123456789");
    console.log("✓ Test 1: GET /api/tenants trả về 2 cơ sở ban đầu");

    // Test 2: Thử xóa master root default tenant 'khiem_thi_viet' -> Phải bị chặn 403
    const resDelAdmin = await request('DELETE', '/api/tenants?id=khiem_thi_viet');
    assert.strictEqual(resDelAdmin.status, 403);
    console.log("✓ Test 2: Bảo vệ cơ sở quản trị hệ thống mặc định 'khiem_thi_viet' thành công (HTTP 403)");

    // Test 3: Xóa cơ sở 'spa_123456789' qua DELETE /api/tenants?id=spa_123456789
    const resDel = await request('DELETE', '/api/tenants?id=spa_123456789');
    assert.strictEqual(resDel.status, 200);
    assert.strictEqual(resDel.body.success, true);
    assert.strictEqual(resDel.body.count, 1);
    console.log("✓ Test 3: DELETE /api/tenants?id=spa_123456789 thành công");

    // Test 4: Kiểm tra trực tiếp file disk sandbox -> spa_123456789 phải biến mất hoàn toàn
    const diskContentAfterDelete = JSON.parse(fs.readFileSync(SANDBOX_FILE, 'utf8'));
    assert.strictEqual(diskContentAfterDelete.length, 1);
    assert.strictEqual(diskContentAfterDelete[0].id, 'khiem_thi_viet');
    assert.ok(!diskContentAfterDelete.some(t => t.id === 'spa_123456789'), "Disk file không được chứa spa_123456789 sau khi xóa");
    console.log("✓ Test 4: File sandbox trên disk đã xóa vĩnh viễn 'spa_123456789'");

    // Test 5: Mô phỏng Frontend gọi saveTenants() bằng POST /api/tenants với danh sách còn lại
    // Server TUYỆT ĐỐI KHÔNG tự ý unshift/hồi sinh 'spa_123456789'
    const resPost = await request('POST', '/api/tenants', diskContentAfterDelete);
    assert.strictEqual(resPost.status, 200);
    assert.strictEqual(resPost.body.count, 1);

    const diskContentAfterPost = JSON.parse(fs.readFileSync(SANDBOX_FILE, 'utf8'));
    assert.strictEqual(diskContentAfterPost.length, 1);
    assert.ok(!diskContentAfterPost.some(t => t.id === 'spa_123456789'), "POST /api/tenants không được hồi sinh spa_123456789");
    console.log("✓ Test 5: POST /api/tenants lưu chính xác danh sách, không hồi sinh cơ sở đã xóa");

    // Test 6: Dừng server và khởi động lại để kiểm tra khôi phục sau Restart
    serverProc.kill();
    await new Promise(r => setTimeout(r, 600));

    // Khởi động server lần 2
    const serverProc2 = spawn('node', ['server.js'], { env, cwd: __dirname });
    try {
      await waitForServer();
      const resGetAfterRestart = await request('GET', '/api/tenants');
      assert.strictEqual(resGetAfterRestart.status, 200);
      assert.strictEqual(resGetAfterRestart.body.length, 1);
      assert.ok(!resGetAfterRestart.body.some(t => t.id === 'spa_123456789'), "Sau khi khởi động lại server, spa_123456789 không được xuất hiện lại");
      console.log("✓ Test 6: Sau khi Restart server, 'spa_123456789' không bị hồi sinh");
    } finally {
      serverProc2.kill();
    }

    // Test 7: Kiểm tra file production `public/tenants.json` không còn spa_123456789
    const prodFile = path.join(__dirname, 'public', 'tenants.json');
    if (fs.existsSync(prodFile)) {
      const prodTenants = JSON.parse(fs.readFileSync(prodFile, 'utf8'));
      assert.ok(!prodTenants.some(t => t.id === 'spa_123456789'), "public/tenants.json không được chứa spa_123456789");
      assert.ok(prodTenants.some(t => t.id === 'khiem_thi_viet'), "public/tenants.json phải bảo lưu cơ sở khiem_thi_viet");
      console.log("✓ Test 7: public/tenants.json chuẩn sạch, không chứa cơ sở xóa và giữ nguyên khiem_thi_viet");
    }

    console.log("\n>>> TẤT CẢ 7/7 TEST CASE CHO R3 ĐỀU ĐẠT CHUẨN HOÀN TOÀN! <<<");
  } finally {
    try { serverProc.kill(); } catch (e) {}
    if (fs.existsSync(SANDBOX_FILE)) {
      fs.unlinkSync(SANDBOX_FILE);
      console.log("✓ Đã dọn dẹp sandbox test file.");
    }
  }
}

run().catch(err => {
  console.error("Test failed:", err);
  if (fs.existsSync(SANDBOX_FILE)) {
    try { fs.unlinkSync(SANDBOX_FILE); } catch (e) {}
  }
  process.exit(1);
});
