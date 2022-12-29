
document.getElementById('submitBtn').addEventListener('click', () => {
  let sub = document.getElementById('sub');
  let pred = document.getElementById('pred');
  let obj = document.getElementById('obj');
  let statementParts = [sub.value, pred.value, obj.value];
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
    sub.value = '';
    pred.value = '';
    obj.value = '';
  })
  .catch(error => console.error(error))
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
    let dataRows = response.triples.map(triple => [triple.predicate, triple.object]);
    buildTableSection(table, "", dataRows);
  })
  .catch(error => console.error(error))
}

fetchAllTriples();
