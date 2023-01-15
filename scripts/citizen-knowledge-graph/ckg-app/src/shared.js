const { ipcRenderer, shell } = require('electron');
const querystringify = require('querystringify');

document.getElementById('headline').addEventListener('click', () => {
  ipcRenderer.send('site-to-main', 'navigate-to-home');
});

function messageToMain(msg) {
  ipcRenderer.send('site-to-main', msg);
}

function clearDiv(div) {
  while (div.firstChild) div.removeChild(div.lastChild);
}

function buildTableSection(table, text, dataRows, addPlus = false) {
  let tr, td;
  if (text) {
    tr = document.createElement('tr');
    tr.className = 'headline-row';
    tr.style.color = 'navajowhite';
    td = document.createElement('td');
    td.colSpan = 3;
    td.innerHTML = "<b style='color: navajowhite'>" + text + "</b>";
    tr.appendChild(td);
    table.appendChild(tr);
  }
  let isAdvancedMode = dataRows.length > 1  && dataRows[0].length === 3;

  const formatUri = (uri) => {
    return "<small style='color: silver'>" + uri.split('#')[0] + "#"
        + "<strong>" + uri.split('#')[1] + "</strong></small>";
  };

  dataRows.forEach(dataRow => {
    let tr = document.createElement('tr');
    let col1 = "";
    let col2 = dataRow[0];
    let col3 = dataRow[1];
    if (isAdvancedMode) {
      col1 = dataRow[0];
      col2 = dataRow[1];
      col3 = dataRow[2];
    }
    let td = document.createElement('td');
    if (addPlus) td.innerHTML = '+&nbsp;';
    if (isAdvancedMode) td.innerHTML = formatUri(col1);
    tr.appendChild(td);
    td = document.createElement('td');
    td.style.color = 'darkgray';
    td.innerHTML = isAdvancedMode ? "&nbsp;&nbsp;&nbsp;" + formatUri(col2) : col2;
    tr.appendChild(td);
    td = document.createElement('td');
    td.innerHTML = '&nbsp;&nbsp;&nbsp;' +  col3;
    tr.appendChild(td);
    table.appendChild(tr);
  });
}

function buildActionBtn(text, callback = () => {}) {
  let btn = document.createElement('input');
  btn.type = 'button';
  btn.className = 'action-btn'
  btn.value = text;
  btn.onclick = () => callback();
  return btn;
}

function openInExternalBrowser(url, queryParams = undefined) {
  if (queryParams) {
    let queryString = querystringify.stringify(queryParams, '');
    url += '?' + queryString;
  }
  shell.openExternal(url).then(() => console.log("Opened in external browser: " + url));
}

let clickCount = 1;

document.addEventListener("click", (event) => {
  let elements = document.getElementsByClassName("order" + (clickCount ++));
  if (elements.length > 0) elements[0].classList.add("fade");
});

function appendWarningCheckboxAndLabel(reportDiv, btn) {
  btn.disabled = "disabled";
  btn.style.backgroundColor = "gray";
  btn.style.color = "silver";
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
  reportDiv.appendChild(document.createElement('br'));
}
