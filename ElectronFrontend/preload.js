const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("efeApi", {
  request: (request) => ipcRenderer.invoke("api:request", request)
});

