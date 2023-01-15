
document.getElementById('submitBtn').addEventListener('click', () => submitTriple());

let newTriple;

function submitTriple() {
  let sub = document.getElementById('sub');
  let pred = document.getElementById('pred');
  let obj = document.getElementById('obj');
  let statement = [
      "http://ckg.de/default#" + (sub.value ? sub.value : "mainPerson"),
    "http://ckg.de/default#" + pred.value,
    obj.value
  ];
  newTriple = {
    normal: statement[1].split('#')[1] + "_" + statement[2],
    advanced: statement[0] + "_" + statement[1] + "_" + statement[2]
  };
  fetch('http://localhost:8080/api/v1/ontoengine/addNewStatement', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(statement)
  })
  .then(response => response.text())
  .then(response => {
    console.log("response:", response);
    fetchAllTriples();
    sub.value = '';
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
    let triplesSorted = response.triples.sort((a, b) => a.predicate.split('#')[1] > b.predicate.split('#')[1] ? 1 : -1);
    let dataRows = triplesSorted.map(triple => buildDataRow(triple));
    buildTableSection(table, "", dataRows, false, newTriple);
    newTriple = undefined;
  })
  .catch(error => console.error(error))
}

fetchAllTriples();

let mode = "normal";

document.getElementById("advancedMode").addEventListener("click", function(e) {
  mode = mode === "normal" ? "advanced" : "normal";
  if (mode === "advanced") {
    document.getElementById("advancedMode").innerHTML = "Switch to normal mode";
    document.getElementById("sub").style.display = "inline";
    document.getElementById("pred").placeholder = "predicate";
    document.getElementById("obj").placeholder = "object";
  } else {
    document.getElementById("advancedMode").innerHTML = "Switch to advanced mode";
    document.getElementById("sub").style.display = "none";
    document.getElementById("pred").placeholder = "field name";
    document.getElementById("obj").placeholder = "value";
  }
  fetchAllTriples();
});
