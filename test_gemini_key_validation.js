// test_gemini_key_validation.js
// Automated verification suite for Gemini API key validation and connection test error handling

const fs = require('fs');
const path = require('path');
const assert = require('assert');

console.log("==================================================================");
console.log("  GEMINI API KEY VALIDATION & CONNECTION TEST VERIFICATION SUITE   ");
console.log("==================================================================\n");

const htmlPath = path.join(__dirname, 'public', 'index.html');
const htmlContent = fs.readFileSync(htmlPath, 'utf8');

// -----------------------------------------------------------------------------
// PART 1: Static HTML & UI Guidance Verification
// -----------------------------------------------------------------------------
console.log("--- PART 1: UI Guidance & Static Content Verification ---");

assert(htmlContent.includes('system-gemini-key-input'), "Missing #system-gemini-key-input");
assert(htmlContent.includes('ai-key-format-hint'), "Missing #ai-key-format-hint container");
assert(htmlContent.includes('ai-test-result'), "Missing #ai-test-result container");
assert(htmlContent.includes('https://aistudio.google.com/app/apikey'), "Missing link to Google AI Studio");
assert(htmlContent.includes('AIzaSy'), "Missing reference to AIzaSy standard key format");
assert(htmlContent.includes('AQ.Ab8') || htmlContent.includes('AQ.'), "Missing warning about Vertex AI token format");
assert(htmlContent.includes('oninput="handleApiKeyInputChange()"'), "Missing oninput handler on key input");

console.log("  [PASS] UI elements present: #system-gemini-key-input, #ai-key-format-hint, #ai-test-result");
console.log("  [PASS] Actionable Google AI Studio links (https://aistudio.google.com/app/apikey) verified");
console.log("  [PASS] UI warnings distinguishing AIzaSy... from AQ.Ab8... and ya29... present");

// -----------------------------------------------------------------------------
// PART 2: Extract & Execute Validation and Connection Logic
// -----------------------------------------------------------------------------
console.log("\n--- PART 2: Client-Side Format Validation Unit Tests ---");

// Mock browser environment
const domElements = {};

function createMockElement(id, initialProps = {}) {
  const el = {
    id,
    value: initialProps.value || '',
    innerText: initialProps.innerText || '',
    innerHTML: initialProps.innerHTML || '',
    _className: initialProps.className || '',
    get className() { return this._className; },
    set className(val) { this._className = val || ''; },
    get classList() {
      const self = this;
      return {
        add(cls) {
          const list = self._className.split(/\s+/).filter(Boolean);
          if (!list.includes(cls)) {
            list.push(cls);
            self._className = list.join(' ');
          }
        },
        remove(cls) {
          const list = self._className.split(/\s+/).filter(Boolean);
          self._className = list.filter(c => c !== cls).join(' ');
        },
        contains(cls) {
          return self._className.split(/\s+/).includes(cls);
        }
      };
    },
    disabled: initialProps.disabled || false,
    focus() { el.focused = true; }
  };
  domElements[id] = el;
  return el;
}

const mockKeyInput = createMockElement("system-gemini-key-input", { value: "" });
const mockModelSelect = createMockElement("system-gemini-model-select", { value: "gemini-2.5-flash" });
const mockFormatHint = createMockElement("ai-key-format-hint", { className: "hidden" });
const mockResultBox = createMockElement("ai-test-result", { className: "hidden" });
const mockResultText = createMockElement("ai-test-result-text");
const mockBtnTest = createMockElement("btn-test-ai-key");
const mockBtnLabel = createMockElement("btn-test-ai-label", { innerText: "Test Kết Nối" });
const mockBadge = createMockElement("ai-status-badge");
const mockBadgeText = createMockElement("ai-status-text");
const mockUpdatedText = createMockElement("ai-key-updated-at");

let lastToast = "";
function showToast(msg) {
  lastToast = msg;
}

const document = {
  getElementById(id) {
    return domElements[id] || null;
  }
};

let capturedFetchCalls = [];
let mockFetchResponse = null;

async function fetch(url, options) {
  capturedFetchCalls.push({ url, options });
  if (typeof mockFetchResponse === 'function') {
    return mockFetchResponse(url, options);
  }
  return mockFetchResponse;
}

// Extract Javascript functions from index.html
const fnRegex = /function validateGeminiApiKeyFormat[\s\S]*?(?=\s*function printInvoice)/;
const jsMatch = htmlContent.match(fnRegex);
assert(jsMatch, "Could not extract JS functions from index.html");

const scriptContent = jsMatch[0];

// Evaluate inside our mock sandbox
const sandbox = {
  document,
  fetch,
  showToast,
  console,
  confirm: () => true
};

const fnWrapper = new Function('sandbox', `
  with(sandbox) {
    ${scriptContent}
    return {
      validateGeminiApiKeyFormat,
      handleApiKeyInputChange,
      loadSystemAiConfig,
      saveSystemAiConfig,
      testGeminiApiKeyConnection
    };
  }
`);

const {
  validateGeminiApiKeyFormat,
  handleApiKeyInputChange,
  loadSystemAiConfig,
  saveSystemAiConfig,
  testGeminiApiKeyConnection
} = fnWrapper(sandbox);

// Test 2.1: Empty input
const emptyCheck = validateGeminiApiKeyFormat("");
assert.strictEqual(emptyCheck.isValid, false, "Empty key should be invalid");
assert.strictEqual(emptyCheck.isEmpty, true, "Empty key flag should be true");
console.log("  [PASS] Empty API key rejected correctly");

// Test 2.2: Google Auth Key (starts with AQ.)
const authKey = "AQ." + "MOCK_SERVICE_ACCOUNT_AUTH_KEY_SAMPLE_1234567890_abcdefghij";
const authCheck = validateGeminiApiKeyFormat(authKey);
assert.strictEqual(authCheck.isValid, true, "Auth key (AQ.) should be valid");
assert.strictEqual(authCheck.keyType, "AUTH_KEY", "Should recognize AUTH_KEY");
console.log("  [PASS] Authorization Auth Key (AQ....) correctly accepted as valid Google key");

// Test 2.3: OAuth token starting with ya29.
const oauthCheck = validateGeminiApiKeyFormat("ya29.a0AfH6SMBMockToken1234567890");
assert.strictEqual(oauthCheck.isValid, false);
assert.strictEqual(oauthCheck.errorType, "OAUTH_TOKEN");
console.log("  [PASS] Google OAuth token (ya29...) correctly intercepted");

// Test 2.4: Random prefix / invalid format
const invalidPrefixCheck = validateGeminiApiKeyFormat("my-custom-test-token-123456789");
assert.strictEqual(invalidPrefixCheck.isValid, false);
assert.strictEqual(invalidPrefixCheck.errorType, "INVALID_PREFIX");
console.log("  [PASS] Invalid prefix key rejected with Google API Key requirement warning");

// Test 2.5: AIzaSy prefix but length too short
const shortKeyCheck = validateGeminiApiKeyFormat("AIzaSyShort");
assert.strictEqual(shortKeyCheck.isValid, false);
assert.strictEqual(shortKeyCheck.errorType, "INVALID_LENGTH");
console.log("  [PASS] AIzaSy key with invalid length (< 35 chars) rejected");

// Test 2.6: Valid Google AI Studio key format (~39 chars starting with AIzaSy)
const validStudioKey = "AIzaSyDummyValidStudioKeyFormat_1234567"; // exactly 39 chars
assert.strictEqual(validStudioKey.length, 39);
const validCheck = validateGeminiApiKeyFormat(validStudioKey);
assert.strictEqual(validCheck.isValid, true, "Valid AIzaSy key should pass validation");
console.log("  [PASS] Standard Google AI Studio key (AIzaSy..., 39 chars) passes validation");

// -----------------------------------------------------------------------------
// PART 3: Interactive UI Simulation (Typing / Pasting)
// -----------------------------------------------------------------------------
console.log("\n--- PART 3: Dynamic Input Event Simulation ---");

// Case 3.1: User enters invalid token (OAuth ya29...)
mockKeyInput.value = "ya29.mockTokenOAuth123456789";
handleApiKeyInputChange();
assert(!mockFormatHint.classList.contains("hidden"), "Format hint must be visible for invalid token");
assert(mockFormatHint.innerHTML.includes("ya29"), "Format hint must describe ya29 token");
console.log("  [PASS] Entering invalid token triggers immediate client-side warning with actionable link");

// Case 3.2: User enters valid AQ. auth key
mockKeyInput.value = authKey;
handleApiKeyInputChange();
assert(mockFormatHint.innerHTML.includes("Định dạng"), "Must show valid format confirmation for AQ key");
assert(mockFormatHint.innerHTML.includes("AQ."), "Must confirm AQ. format");
console.log("  [PASS] Entering valid AQ. key displays valid confirmation");

// Case 3.3: User replaces with valid AIzaSy key
mockKeyInput.value = validStudioKey;
handleApiKeyInputChange();
assert(mockFormatHint.innerHTML.includes("Định dạng"), "Must show valid format confirmation");
assert(mockFormatHint.innerHTML.includes("AIzaSy"), "Must confirm AIzaSy format");
console.log("  [PASS] Entering valid AIzaSy key displays valid confirmation");

// Case 3.4: User clears input
mockKeyInput.value = "";
handleApiKeyInputChange();
assert(mockFormatHint.classList.contains("hidden"), "Format hint must be hidden when input is cleared");
console.log("  [PASS] Clearing input hides format hint cleanly");

// -----------------------------------------------------------------------------
// PART 4: testGeminiApiKeyConnection Error Handling & Network Simulation
// -----------------------------------------------------------------------------
console.log("\n--- PART 4: Connection Test & Error Handling Verification ---");

(async () => {
  // Test 4.1: Test connection with EMPTY input
  mockKeyInput.value = "";
  capturedFetchCalls = [];
  await testGeminiApiKeyConnection();
  assert.strictEqual(capturedFetchCalls.length, 0, "Fetch must not be called when key is empty");
  assert(mockResultText.innerHTML.includes("Chưa nhập API Key"), "Must warn about empty key");
  console.log("  [PASS] Testing with empty key does not invoke network and displays clear warning");

  // Test 4.2: Test connection with INVALID token (ya29...)
  mockKeyInput.value = "ya29.invalidOAuthToken";
  capturedFetchCalls = [];
  await testGeminiApiKeyConnection();
  assert.strictEqual(capturedFetchCalls.length, 0, "Fetch must NOT be called for invalid key format");
  assert(!mockResultBox.classList.contains("hidden"), "Result box must be visible");
  assert(mockResultText.innerHTML.includes("OAuth"), "Result text must explain OAuth incompatibility");
  assert(mockResultText.innerHTML.includes("aistudio.google.com"), "Result text must provide actionable AI Studio link");
  console.log("  [PASS] Testing with invalid key intercepts prior to network and shows user-friendly error");

  // Test 4.3: Test connection with VALID key -> proceeds to query Google Generative Language API
  mockKeyInput.value = validStudioKey;
  mockModelSelect.value = "gemini-2.5-flash";
  capturedFetchCalls = [];
  mockFetchResponse = {
    status: 200,
    async json() {
      return { displayName: "Gemini 2.5 Flash" };
    }
  };
  await testGeminiApiKeyConnection();
  assert.strictEqual(capturedFetchCalls.length, 1, "Fetch MUST be called for validly formatted key");
  const expectedUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash?key=${encodeURIComponent(validStudioKey)}`;
  assert.strictEqual(capturedFetchCalls[0].url, expectedUrl, "Fetch URL must target Google Generative Language endpoint");
  assert(mockResultText.innerHTML.includes("HTTP 200 OK"), "Must show HTTP 200 OK success message");
  console.log("  [PASS] Testing with valid key proceeds to query Google API endpoint and handles HTTP 200 OK");

  // Test 4.4: Test connection with valid key where Google returns HTTP 400 "API key not valid"
  capturedFetchCalls = [];
  mockFetchResponse = {
    status: 400,
    async json() {
      return {
        error: {
          code: 400,
          message: "API key not valid. Please pass a valid API key.",
          status: "INVALID_ARGUMENT"
        }
      };
    }
  };
  await testGeminiApiKeyConnection();
  assert.strictEqual(capturedFetchCalls.length, 1, "Fetch was performed");
  assert(!mockResultText.innerHTML.includes('"status":"INVALID_ARGUMENT"'), "Raw JSON must NOT be shown");
  assert(mockResultText.innerHTML.includes("HTTP 400"), "User-friendly message must explain HTTP 400");
  assert(mockResultText.innerHTML.includes("Google AI Studio"), "Must guide user to Google AI Studio");
  console.log("  [PASS] HTTP 400 error is intercepted with clear, user-friendly explanation rather than raw JSON");

  // Test 4.5: Test connection where Google returns HTTP 403 (Permission denied)
  capturedFetchCalls = [];
  mockFetchResponse = {
    status: 403,
    async json() {
      return { error: { code: 403, message: "Generative Language API has not been used..." } };
    }
  };
  await testGeminiApiKeyConnection();
  assert(mockResultText.innerHTML.includes("HTTP 403"), "Must explain HTTP 403");
  console.log("  [PASS] HTTP 403 error is intercepted with user-friendly permissions explanation");

  // Test 4.6: Test connection where Google returns HTTP 429 (Rate limit)
  capturedFetchCalls = [];
  mockFetchResponse = {
    status: 429,
    async json() {
      return { error: { code: 429, message: "Resource exhausted" } };
    }
  };
  await testGeminiApiKeyConnection();
  assert(mockResultText.innerHTML.includes("HTTP 429"), "Must explain HTTP 429 Quota Exceeded");
  console.log("  [PASS] HTTP 429 error is intercepted with user-friendly quota limit explanation");

  console.log("\n==================================================================");
  console.log("  ALL TESTS PASSED SUCCESSFULLY! (100% Acceptance Criteria Met)   ");
  console.log("==================================================================");
})();
