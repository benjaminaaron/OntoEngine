const rdflib = $rdf;
const baseUri = "http://onto.de/default#";

function createStore() {
  return rdflib.graph();
}

function namedNode(localName) {
  return rdflib.namedNode(baseUri + localName);
}

function statement(sub, pred, obj, isLiteral) {
  return rdflib.st(namedNode(sub), namedNode(pred),
      isLiteral ? rdflib.literal(obj) : namedNode(obj));
}

function downloadAsTurtleFile(store, filename) {
  let content = rdflib.serialize(undefined, store, undefined, 'text/turtle');
  let blob = new Blob([content], { type: 'text/turtle' });
  let url = URL.createObjectURL(blob);
  let link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.setAttribute('Content-Type', 'text/turtle');
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function getTimestamp() {
  const date = new Date();
  return date.toLocaleString('de-DE', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
}

function openDeepLinkToElectronApp(message) {
  let url = "ckg-app://" + encodeURIComponent(JSON.stringify(message));
  window.open(url, "_self");
}
