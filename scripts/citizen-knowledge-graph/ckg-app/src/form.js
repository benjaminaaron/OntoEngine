
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
      if (field.hasPredicate === 'http://ckg.de/default#eVBnumber') {
        label.innerHTML += ',<span style="color: yellow; font-weight: bold">&#9200; mehr Infos</span>';
      }
      let input = document.createElement('input');
      input.className = 'form-input-field';
      input.setAttribute('type', 'text');
      input.setAttribute('id', fieldId);
      if (Object.keys(data.valuesFound).includes(field.hasPredicate)) {
        input.className += ' form-input-field-already-known';
        input.setAttribute('value', data.valuesFound[field.hasPredicate]);
      }
      if (field.computableVia && data.valuesFound['http://www.w3.org/2006/vcard/ns#bday']) {
        input.className += ' form-input-field-computed';
        // hardwired for the demo
        let age = 2022 - data.valuesFound['http://www.w3.org/2006/vcard/ns#bday'].split('.')[2];
        input.setAttribute('value', age + '');
        label.innerHTML += ",<b style='color: yellow'>berechnet aus Geburtstag</b>";
      }
      reportDiv.appendChild(input);
      reportDiv.appendChild(label);
      reportDiv.appendChild(document.createElement('br'));
    }
    reportDiv.appendChild(document.createElement('br'));

    let btn = buildActionBtn('Submit form to endpoint');
    btn.style.margin = "18px";
    appendWarningCheckboxAndLabel(reportDiv, btn);

    reportDiv.appendChild(btn);
  })
  .catch(error => console.error(error))
}
