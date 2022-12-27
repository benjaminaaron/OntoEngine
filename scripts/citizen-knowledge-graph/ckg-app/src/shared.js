const { ipcRenderer } = require('electron');

document.getElementById('headline').addEventListener('click', () => {
  ipcRenderer.send('site-to-main', 'navigate-to-home');
});
