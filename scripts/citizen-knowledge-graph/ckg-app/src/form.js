
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
    let reportDiv = document.getElementById('reportDiv');
    let orderedFieldIds = Object.keys(data.fields).sort();
    for (let fieldId of orderedFieldIds) {
      let field = data.fields[fieldId];
      let label = document.createElement('label');
      label.setAttribute('for', fieldId);
      label.innerHTML = field.description;
      let input = document.createElement('input');
      input.setAttribute('type', 'text');
      input.setAttribute('id', fieldId);
      if (Object.keys(data.valuesFound).includes(field.hasPredicate)) {
        input.setAttribute('value', data.valuesFound[field.hasPredicate]);
      }
      reportDiv.appendChild(input);
      reportDiv.appendChild(label);
      reportDiv.appendChild(document.createElement('br'));
    }
  })
  .catch(error => console.error(error))
}
