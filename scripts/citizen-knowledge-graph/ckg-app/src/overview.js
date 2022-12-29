
document.getElementById('submitBtn').addEventListener('click', () => {
  let statementParts = [
    document.getElementById('sub').value,
    document.getElementById('pred').value,
    document.getElementById('obj').value
  ];
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
  })
  .catch(error => console.error(error))
});
