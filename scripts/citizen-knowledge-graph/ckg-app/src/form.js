
let message;

ipcRenderer.on('main-to-site', (event, msg) => {
  console.log("form.js, main-to-site:", msg);
  message = msg;
  sendFormWorkflow(msg.turtleData);
})

function sendFormWorkflow(turtleData) {
  fetch('http://localhost:8080/api/v1/ontoengine/formWorkflow', {
    method: 'PUT',
    body: turtleData
  })
  .then(response => response.json())
  .then(data => {
    console.log("data:", data);
  })
  .catch(error => console.error(error))
}
