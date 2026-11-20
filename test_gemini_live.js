const https = require('https');

const key = process.env.GEMINI_API_KEY || "AQ.Ab8RN6_MOCK_TEST_KEY_FOR_LOCAL_RUN_ONLY";
const model = "gemini-3.5-flash-lite";
const prompt = "alo";

const postData = JSON.stringify({
  contents: [
    { parts: [{ text: prompt }] }
  ]
});

const options = {
  hostname: 'generativelanguage.googleapis.com',
  path: `/v1beta/models/${model}:generateContent?key=${key}`,
  method: 'POST',
  headers: {
    'Content-Type': 'application/json; charset=UTF-8',
    'X-goog-api-key': key,
    'Content-Length': Buffer.byteLength(postData)
  },
  timeout: 10000
};

console.log(`Sending live request to Gemini API (model: ${model}, prompt: "${prompt}")...`);

const req = https.request(options, (res) => {
  let data = '';
  res.on('data', chunk => { data += chunk; });
  res.on('end', () => {
    console.log(`Response HTTP Status: ${res.statusCode}`);
    if (res.statusCode === 200) {
      try {
        const json = JSON.parse(data);
        const text = json.candidates[0].content.parts[0].text;
        console.log("Gemini Response Text:\n", text);
        console.log("\n>>> LIVE GEMINI API TEST PASSED! HTTP 200 OK <<<");
        process.exit(0);
      } catch (e) {
        console.error("Failed to parse JSON response:", e.message);
        process.exit(1);
      }
    } else {
      console.error("Gemini API Error Response:", data);
      process.exit(1);
    }
  });
});

req.on('error', (e) => {
  console.error("Network request error:", e.message);
  process.exit(1);
});

req.on('timeout', () => {
  req.destroy(new Error("Request timed out after 10s"));
});

req.write(postData);
req.end();
