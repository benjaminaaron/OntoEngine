
ipcRenderer.on('main-to-site', (event, message) => {
  console.log("query.js, main-to-site:", message);
  sendQuery(message.query);
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
    if (Object.keys(data.valuesFound).length > 0) {
      buildTableSection(table, "Values found",
          Object.keys(data.valuesFound).map(key => [key, data.valuesFound[key]]));
    }
    if (data.valuesNotFound.length > 0) {
      buildTableSection(table, "Values not found", data.valuesNotFound.map(val => [val, '?']));
    }
    // send back to site via deep link TODO
  })
  .catch(error => console.error(error))
}
