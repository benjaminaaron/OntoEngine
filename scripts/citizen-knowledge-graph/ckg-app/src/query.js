
ipcRenderer.on('main-to-site', (event, message) => {
  console.log("query.js, main-to-site:", message);
  sendQuery(message.query);
})

function sendQuery(query) {
  fetch('http://localhost:8080/api/v1/ontoengine/query', {
    method: 'PUT',
    body: query
  })
  .then(response => response.text())
  .then(data => {
    let reportDiv = document.getElementById('reportDiv');
    reportDiv.innerHTML = data;
    // send back to site via deep link TODO
  })
  .catch(error => console.error(error))
}
