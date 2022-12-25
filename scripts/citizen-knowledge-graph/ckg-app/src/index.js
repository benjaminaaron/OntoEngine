const { ipcRenderer } = require('electron');

ipcRenderer.on('main-to-site', (event, arg) => {
  console.log(arg);
})

document.getElementById('devBtn').addEventListener('click', () => {
  ipcRenderer.send('site-to-main', 'async ping');
});
