import { SparqlEndpointFetcher } from "fetch-sparql-endpoint"
import { decode } from "js-base64";

const sparql = new SparqlEndpointFetcher()
const ENDPOINT = "http://localhost:7200/repositories/dev"

async function fetch() {
  const query = "SELECT * WHERE { ?s ?p ?o }"
  const bindingsStream = await sparql.fetchBindings(ENDPOINT, query)
  bindingsStream.on("data", (resultRow) => {
    let sub = resultRow["s"].value
    if (sub.startsWith("urn:rdf4j:triple:")) sub = decode(sub.substring(17))
    let pred = resultRow["p"].value
    let obj = resultRow["o"].value

    console.log(resultRow)
    console.log(sub, pred, obj)
  })
}

function clearElement(el) {
  while (el.firstChild) el.removeChild(el.lastChild);
}

function showExample(idx) {
  clearElement(document.getElementById("root"))
  //  fetch().then(r => {})
}

function registerClickListener(id, idx) {
  document.getElementById(id).addEventListener("click", () => showExample(idx))
}

registerClickListener("ex1link", 1)
registerClickListener("ex2link", 2)
registerClickListener("ex3link", 3)
registerClickListener("ex4link", 4)
