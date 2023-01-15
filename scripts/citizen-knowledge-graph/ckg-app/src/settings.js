const rdflib = require('rdflib')

document.getElementById("rdfExport").addEventListener("click", e => {
  fetch('http://localhost:8080/api/v1/ontoengine/getAllTriples', {
    method: 'GET'
  })
  .then(response => response.json())
  .then(data => {
    console.log(data);
    let store = rdflib.graph();
    store.setPrefixForURI("ckg", "http://ckg.de/default#");
    store.setPrefixForURI("vcard", "http://www.w3.org/2006/vcard/ns#");
    store.setPrefixForURI("foaf", "http://xmlns.com/foaf/0.1#"); // remains n0 for some reason
    data.triples.forEach(triple => {
      store.add(rdflib.st(rdflib.sym(triple.subject), rdflib.sym(triple.predicate), rdflib.lit(triple.object)));
    });
    let ttl = rdflib.serialize(undefined, store, undefined, 'text/turtle');
    let blob = new Blob([ttl], { type: "text/turtle" });
    let url = URL.createObjectURL(blob);
    let link = document.createElement('a');
    link.href = url;
    link.download = "ckg_export.ttl";
    link.setAttribute('Content-Type', "text/turtle" );
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  });
});
