const { ipcRenderer } = require('electron');

ipcRenderer.on('main-to-site', (event, msg) => {
  console.log("main-to-site", msg);
})

/*document.getElementById('devBtn').addEventListener('click', () => {
  ipcRenderer.send('site-to-main', 'async ping');
});*/
