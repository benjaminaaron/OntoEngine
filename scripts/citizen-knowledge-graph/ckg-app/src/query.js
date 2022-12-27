
let message;

ipcRenderer.on('main-to-site', (event, msg) => {
  console.log("query.js, main-to-site:", msg);
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
      reportDiv.appendChild(btn);
    }
  })
  .catch(error => console.error(error))
}
