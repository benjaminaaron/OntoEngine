
document.getElementById('submitBtn').addEventListener('click', () => {
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
    let dataRows = response.triples.map(triple => [triple.predicate.split('#')[1], triple.object]);
    buildTableSection(table, "", dataRows);
  })
  .catch(error => console.error(error))
}

fetchAllTriples();
