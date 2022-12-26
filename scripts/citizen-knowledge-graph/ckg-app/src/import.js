const { ipcRenderer } = require('electron');

ipcRenderer.on('main-to-site', (event, msg) => {
  console.log("main-to-site", msg);
  importTurtle(msg);
})

function clearDiv(div) {
  while (div.firstChild) {
    div.removeChild(div.lastChild);
  }
}

function buildTable(triples) {
  let table = document.createElement('table');
  triples.forEach(triple => {
    let tr = document.createElement('tr');
    let td = document.createElement('td');
    td.innerHTML = triple.predicate.split('#')[1] + " ";
    tr.appendChild(td);
    td = document.createElement('td');
    td.innerHTML = triple.object;
    tr.appendChild(td);
    table.appendChild(tr);
  });
  return table;
}

function importTurtle(data) {
  fetch('http://localhost:8080/api/v1/ontoengine/importTurtle', {
    method: 'POST',
    body: data
  })
  .then(response => response.json())
  .then(data => {
    console.log(data);

    let reportDiv = document.getElementById('reportDiv');
    clearDiv(reportDiv);

    let alreadyPresentHeadline = document.createElement('h4');
    alreadyPresentHeadline.innerHTML = "Not imported, already present";
    reportDiv.appendChild(alreadyPresentHeadline);
    reportDiv.appendChild(buildTable(data.alreadyPresent));

    let importedHeadline = document.createElement('h4');
    importedHeadline.innerHTML = "Newly imported";
    reportDiv.appendChild(importedHeadline);
    reportDiv.appendChild(buildTable(data.imported));
  })
  .catch(error => console.error(error))
}
