
ipcRenderer.on('main-to-site', (event, message) => {
  console.log("import.js, main-to-site", message);
  importTurtle(message.turtleData);
})

function importTurtle(data) {
  fetch('http://localhost:8080/api/v1/ontoengine/importTurtle', {
    method: 'POST',
    body: data
  })
  .then(response => response.json())
  .then(data => {
    let reportDiv = document.getElementById('reportDiv');
    clearDiv(reportDiv);
    let table = document.createElement('table');
    reportDiv.appendChild(table);
    const convertToDataRows = (triples => triples.map(triple => [triple.predicate.split('#')[1], triple.object]));
    if (data.imported.length > 0) {
      buildTableSection(table, "Newly imported", convertToDataRows(data.imported), true);
    }
    if (data.alreadyPresent.length > 0) {
      buildTableSection(table, "Not imported, already existing", convertToDataRows(data.alreadyPresent));
    }
  })
  .catch(error => console.error(error))
}
