const { app, BrowserWindow, ipcMain } = require('electron');
const contextMenu = require('electron-context-menu');
const path = require('path');
try {
  require('electron-reloader')(module);
} catch {}
contextMenu();

if (process.defaultApp) {
  if (process.argv.length >= 2) {
    app.setAsDefaultProtocolClient('ckg-app', process.execPath, [path.resolve(process.argv[1])])
  }
} else {
  app.setAsDefaultProtocolClient('ckg-app')
}

let win;

const createWindow = () => {
  win = new BrowserWindow({
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false,
    },
    width: 800,
    height: 600,
  });

  win.loadFile('src/index.html');
  win.webContents.openDevTools()
};

app.whenReady().then(() => {
  createWindow();
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('open-url', (event, url) => {
  console.log(`You arrived from: ${url}`);
  let content = decodeURIComponent(url.substring("ckg-app://".length));
  win.webContents.send('main-to-site', content);
})

ipcMain.on('site-to-main', (event, arg) => {
  console.log(arg);
  event.sender.send('main-to-site', 'async pong');
})
