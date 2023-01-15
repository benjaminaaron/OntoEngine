
document.getElementById('submitBtn').addEventListener('click', () => submitTriple());

function submitTriple() {
  let sub = "http://onto.de/default#mainPerson";
  let pred = document.getElementById('pred');
  let obj = document.getElementById('obj');
  let statementParts = [sub, pred.value, obj.value];
  fetch('http://localhost:8080/api/v1/ontoengine/addLocalNamesStatement', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(statementParts)
  })
  .then(response => response.text())
  .then(response => {
    console.log("response:", response);
    fetchAllTriples();
    pred.value = '';
    obj.value = '';
  })
  .catch(error => console.error(error))
}

document.getElementById("sub").addEventListener("keyup", function(event) {
  if (event.code === "Enter") submitTriple();
});

document.getElementById("pred").addEventListener("keyup", function(event) {
  if (event.code === "Enter") submitTriple();
});

document.getElementById("obj").addEventListener("keyup", function(event) {
  if (event.code === "Enter") submitTriple();
});

function fetchAllTriples() {
  fetch('http://localhost:8080/api/v1/ontoengine/getAllTriples')
  .then(response => response.json())
  .then(response => {
    console.log("response:", response);
    let triplesDiv = document.getElementById('triples');
    clearDiv(triplesDiv);
    let table = document.createElement('table');
    triplesDiv.appendChild(table);
    const buildDataRow = (triple) => {
      if (mode === "normal") {
        return [triple.predicate.split('#')[1], triple.object]
      }
      return [triple.subject, triple.predicate, triple.object]
    };
    let dataRows = response.triples.map(triple => buildDataRow(triple));
    buildTableSection(table, "", dataRows);
  })
  .catch(error => console.error(error))
}

fetchAllTriples();

let mode = "normal";

document.getElementById("advancedMode").addEventListener("click", function(e) {
  mode = mode === "normal" ? "advanced" : "normal";
  if (mode === "advanced") {
    document.getElementById("sub").style.display = "inline";
    document.getElementById("pred").placeholder = "predicate";
    document.getElementById("obj").placeholder = "object";
  } else {
    document.getElementById("sub").style.display = "none";
    document.getElementById("pred").placeholder = "field name";
    document.getElementById("obj").placeholder = "value";
  }
  fetchAllTriples();
});
