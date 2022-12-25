const { app, BrowserWindow } = require('electron');
const path = require('path')
try {
  require('electron-reloader')(module);
} catch {}

if (process.defaultApp) {
  if (process.argv.length >= 2) {
    app.setAsDefaultProtocolClient('ckg-app', process.execPath, [path.resolve(process.argv[1])])
  }
} else {
  app.setAsDefaultProtocolClient('ckg-app')
}

const createWindow = () => {
  const win = new BrowserWindow({
    width: 800,
    height: 600,
  });

  win.loadFile('index.html').then(() => {
    win.webContents.openDevTools()
  });
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
})
