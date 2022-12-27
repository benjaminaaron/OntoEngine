const { ipcRenderer } = require('electron');

document.getElementById('headline').addEventListener('click', () => {
  ipcRenderer.send('site-to-main', 'navigate-to-home');
});

function clearDiv(div) {
  while (div.firstChild) div.removeChild(div.lastChild);
}

function buildTableSection(table, text, dataRows, addPlus = false) {
  let tr = document.createElement('tr');
  tr.className = 'headline-row';
  tr.style.color = 'navajowhite';
  let td = document.createElement('td');
  td.colSpan = 3;
  td.innerHTML = "<b style='color: navajowhite'>" + text + "</b>";
  tr.appendChild(td);
  table.appendChild(tr);
  dataRows.forEach(dataRow => {
    let tr = document.createElement('tr');
    let td = document.createElement('td');
    if (addPlus) td.innerHTML = '+&nbsp;';
    tr.appendChild(td);
    td = document.createElement('td');
    td.style.color = 'darkgray';
    td.innerHTML = dataRow[0];
    tr.appendChild(td);
    td = document.createElement('td');
    td.innerHTML = '&nbsp;&nbsp;&nbsp;' +  dataRow[1];
    tr.appendChild(td);
    table.appendChild(tr);
  });
}
