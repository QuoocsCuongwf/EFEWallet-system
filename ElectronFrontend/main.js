const { app, BrowserWindow, ipcMain } = require("electron");
const path = require("path");

function parseBody(rawText, contentType) {
  if (!rawText) return null;
  if (contentType && contentType.includes("application/json")) {
    try {
      return JSON.parse(rawText);
    } catch {
      return { raw: rawText };
    }
  }
  return rawText;
}

async function handleApiRequest(_, request) {
  const { method, url, headers, body } = request ?? {};
  if (!url || typeof url !== "string") {
    throw new Error("Invalid request URL");
  }

  const parsed = new URL(url);
  if (!["http:", "https:"].includes(parsed.protocol)) {
    throw new Error("Only HTTP/HTTPS URLs are allowed");
  }

  const response = await fetch(parsed, {
    method: method || "GET",
    headers: headers || {},
    body: body ? JSON.stringify(body) : undefined
  });

  const rawText = await response.text();
  const data = parseBody(rawText, response.headers.get("content-type"));
  return {
    ok: response.ok,
    status: response.status,
    data
  };
}

function createWindow() {
  const window = new BrowserWindow({
    width: 1200,
    height: 900,
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false
    }
  });

  window.loadFile(path.join(__dirname, "src", "index.html"));
}

app.whenReady().then(() => {
  ipcMain.handle("api:request", handleApiRequest);
  createWindow();

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});

