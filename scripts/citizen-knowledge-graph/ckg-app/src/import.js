const { ipcRenderer } = require('electron');

ipcRenderer.on('main-to-site', (event, msg) => {
  console.log("main-to-site", msg);
  importTurtle(msg);
})

function importTurtle(data) {
  fetch('http://localhost:8080/api/v1/ontoengine/importTurtle', {
    method: 'POST',
    body: data
  })
  .then(response => response.json())
  .then(data => {
    // ...
    console.log(data);
  })
  .catch(error => console.error(error))
}
