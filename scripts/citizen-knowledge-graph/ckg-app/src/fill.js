
let message;

ipcRenderer.on('main-to-site', (event, msg) => {
  console.log("fill.js, main-to-site:", msg);
  message = msg;
  sendQuery(msg.query);
})

function sendQuery(query) {
  fetch('http://localhost:8080/api/v1/ontoengine/query', {
    method: 'PUT',
    body: query
  })
  .then(response => response.json())
  .then(data => {
    let reportDiv = document.getElementById('reportDiv');
    clearDiv(reportDiv);
    let table = document.createElement('table');
    reportDiv.appendChild(table);
    if (data.valuesNotFound.length > 0) {
      buildTableSection(table, "Values not found", data.valuesNotFound.map(val => [val, '?']));
    }
    if (Object.keys(data.valuesFound).length > 0) {
      buildTableSection(table, "Values found",
          Object.keys(data.valuesFound).map(key => [key, data.valuesFound[key]]));
      let queryParams = {};
      for (let key of Object.keys(data.valuesFound)) {
        queryParams[key] = data.valuesFound[key];
      }
      let btn = buildActionBtn('Fill values on website', () => {
        openInExternalBrowser(message.responseUrl, queryParams);
      });
      btn.disabled = "disabled";
      btn.style.backgroundColor = "gray";
      btn.style.color = "silver";

      reportDiv.appendChild(document.createElement('br'));
      reportDiv.appendChild(document.createElement('br'));

      let warningCheckbox = document.createElement('input');
      warningCheckbox.type = "checkbox";
      warningCheckbox.style.marginRight = "7px";
      warningCheckbox.addEventListener('change', () => {
        if (warningCheckbox.checked) {
          btn.disabled = "";
          btn.style.backgroundColor = "dimgray";
          btn.style.color = "navajowhite";
        } else {
          btn.disabled = "disabled";
          btn.style.backgroundColor = "gray";
          btn.style.color = "silver";
        }
      });
      reportDiv.appendChild(warningCheckbox);

      let warningLabel = document.createElement('label');
      warningLabel.innerHTML = "I understand that this data will leave my system and I have no control over what happens to it there";
      warningLabel.style.color = "silver";
      reportDiv.appendChild(warningLabel);

      reportDiv.appendChild(btn);
    }
  })
  .catch(error => console.error(error))
}
