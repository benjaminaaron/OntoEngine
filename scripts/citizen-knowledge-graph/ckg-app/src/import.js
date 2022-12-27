
ipcRenderer.on('main-to-site', (event, message) => {
  console.log("main-to-site", message);
  importTurtle(message.turtleData);
})

function clearDiv(div) {
  while (div.firstChild) div.removeChild(div.lastChild);
}

function buildTableSection(table, text, triples, addPlus = false) {
  let tr = document.createElement('tr');
  tr.className = 'headline-row';
  tr.style.color = 'navajowhite';
  let td = document.createElement('td');
  td.colSpan = 3;
  td.innerHTML = "<b style='color: navajowhite'>" + text + "</b>";
  tr.appendChild(td);
  table.appendChild(tr);
  triples.forEach(triple => {
    let tr = document.createElement('tr');
    let td = document.createElement('td');
    if (addPlus) td.innerHTML = '+&nbsp;';
    tr.appendChild(td);
    td = document.createElement('td');
    td.style.color = 'darkgray';
    td.innerHTML = triple.predicate.split('#')[1] + " ";
    tr.appendChild(td);
    td = document.createElement('td');
    td.innerHTML = '&nbsp;&nbsp;&nbsp;' + triple.object;
    tr.appendChild(td);
    table.appendChild(tr);
  });
}

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
    if (data.imported.length > 0) {
      buildTableSection(table, "Newly imported", data.imported, true);
    }
    if (data.alreadyPresent.length > 0) {
      buildTableSection(table, "Not imported, already existing", data.alreadyPresent);
    }
  })
  .catch(error => console.error(error))
}
