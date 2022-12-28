
let message;

ipcRenderer.on('main-to-site', (event, msg) => {
  console.log("form.js, main-to-site:", msg);
  message = msg;
})

