const DEFAULT_CONFIG = {
  authBase: "http://localhost:8083/api/v1",
  walletBase: "http://localhost:8085/api/v1"
};

const outputEl = document.getElementById("output");
const tokenPreviewEl = document.getElementById("tokenPreview");
const authBaseEl = document.getElementById("authBase");
const walletBaseEl = document.getElementById("walletBase");

function readConfig() {
  const saved = localStorage.getItem("efe-config");
  if (!saved) return DEFAULT_CONFIG;
  try {
    return { ...DEFAULT_CONFIG, ...JSON.parse(saved) };
  } catch {
    return DEFAULT_CONFIG;
  }
}

function saveConfig(config) {
  localStorage.setItem("efe-config", JSON.stringify(config));
}

function getToken() {
  return localStorage.getItem("efe-token");
}

function setToken(token) {
  if (token) {
    localStorage.setItem("efe-token", token);
  } else {
    localStorage.removeItem("efe-token");
  }
  tokenPreviewEl.textContent = token
    ? `Token: ${token.slice(0, 24)}...`
    : "Token: (not logged in)";
}

function show(data) {
  outputEl.textContent = JSON.stringify(data, null, 2);
}

function jsonHeaders(withAuth = false, extraHeaders = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...extraHeaders
  };
  if (withAuth) {
    const token = getToken();
    if (!token) {
      throw new Error("You need to login first.");
    }
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
}

async function callApi({ method, url, headers, body }) {
  const result = await window.efeApi.request({ method, url, headers, body });
  if (!result.ok) {
    throw new Error(
      `HTTP ${result.status}: ${
        typeof result.data === "string" ? result.data : JSON.stringify(result.data)
      }`
    );
  }
  return result.data;
}

function initConfigSection() {
  const config = readConfig();
  authBaseEl.value = config.authBase;
  walletBaseEl.value = config.walletBase;

  document.getElementById("saveConfigBtn").addEventListener("click", () => {
    const nextConfig = {
      authBase: authBaseEl.value.trim(),
      walletBase: walletBaseEl.value.trim()
    };
    saveConfig(nextConfig);
    show({ message: "Config saved", ...nextConfig });
  });
}

function initAuthForms() {
  const registerForm = document.getElementById("registerForm");
  const loginForm = document.getElementById("loginForm");

  registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const config = readConfig();
    const formData = new FormData(registerForm);
    const payload = Object.fromEntries(formData.entries());

    try {
      const data = await callApi({
        method: "POST",
        url: `${config.authBase}/auth/register`,
        headers: jsonHeaders(false),
        body: payload
      });
      show(data);
    } catch (error) {
      show({ error: error.message });
    }
  });

  loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const config = readConfig();
    const formData = new FormData(loginForm);
    const payload = Object.fromEntries(formData.entries());

    try {
      const data = await callApi({
        method: "POST",
        url: `${config.authBase}/auth/login`,
        headers: jsonHeaders(false),
        body: payload
      });
      const token = data?.data?.token;
      setToken(token || null);
      show(data);
    } catch (error) {
      show({ error: error.message });
    }
  });
}

function initWalletActions() {
  const generateWalletBtn = document.getElementById("generateWalletBtn");
  const getWalletBtn = document.getElementById("getWalletBtn");
  const getBalanceBtn = document.getElementById("getBalanceBtn");
  const transferForm = document.getElementById("transferForm");

  generateWalletBtn.addEventListener("click", async () => {
    const config = readConfig();
    try {
      const data = await callApi({
        method: "POST",
        url: `${config.walletBase}/wallet/generation`,
        headers: jsonHeaders(true)
      });
      show(data);
    } catch (error) {
      show({ error: error.message });
    }
  });

  getWalletBtn.addEventListener("click", async () => {
    const config = readConfig();
    try {
      const data = await callApi({
        method: "GET",
        url: `${config.walletBase}/wallet/`,
        headers: jsonHeaders(true)
      });
      show(data);
    } catch (error) {
      show({ error: error.message });
    }
  });

  getBalanceBtn.addEventListener("click", async () => {
    const config = readConfig();
    try {
      const data = await callApi({
        method: "GET",
        url: `${config.walletBase}/wallet/balance`,
        headers: jsonHeaders(true)
      });
      show(data);
    } catch (error) {
      show({ error: error.message });
    }
  });

  transferForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const config = readConfig();
    const payload = Object.fromEntries(new FormData(transferForm).entries());
    payload.amount = Number(payload.amount);

    try {
      const data = await callApi({
        method: "POST",
        url: `${config.walletBase}/wallet/transfer`,
        headers: jsonHeaders(true, {
          "idempotency-Key": crypto.randomUUID()
        }),
        body: payload
      });
      show(data);
    } catch (error) {
      show({ error: error.message });
    }
  });
}

initConfigSection();
initAuthForms();
initWalletActions();
setToken(getToken());
