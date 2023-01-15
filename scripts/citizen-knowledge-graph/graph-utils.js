const rdflib = $rdf;
const baseUri = "http://ckg.de/default#";

function createStore() {
  return rdflib.graph();
}

function namedNode(localNameOrFullUri) {
  if (!localNameOrFullUri.startsWith("http")) {
    localNameOrFullUri = baseUri + localNameOrFullUri;
  }
  return rdflib.namedNode(localNameOrFullUri);
}

function statement(sub, pred, obj, isLiteral) {
  return rdflib.st(namedNode(sub), namedNode(pred),
      isLiteral ? rdflib.literal(obj) : namedNode(obj));
}

function downloadAsTurtleFile(store, filename) {
  download(rdflib.serialize(undefined, store, undefined, 'text/turtle'),
      'text/turtle', filename);
}

function download(content, type, filename) {
  let blob = new Blob([content], { type: type });
  let url = URL.createObjectURL(blob);
  let link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.setAttribute('Content-Type', type);
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

function deepLinkHandoverToElectronApp(message) {
  let url = "ckg-app://" + encodeURIComponent(JSON.stringify(message));
  window.open(url, "_self");
}
