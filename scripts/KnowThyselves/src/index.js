import { SparqlEndpointFetcher } from "fetch-sparql-endpoint"
import { decode } from "js-base64"
import { tailoredQueryObjects } from "./tailoredQueryObjects"

const sparql = new SparqlEndpointFetcher()
const ENDPOINT = "http://localhost:7200/repositories/dev"

async function fetch(tailoredQueryObj, table) {
  const bindingsStream = await sparql.fetchBindings(ENDPOINT, tailoredQueryObj.query)
  bindingsStream.on("data", resultRow => {
    console.log(resultRow)
    let tr = document.createElement("tr")
    table.appendChild(tr)
    for (let header of tailoredQueryObj.tableHeaders) {
      let value = resultRow[header].value
      if (value.startsWith("urn:rdf4j:triple:")) value = decode(value.substring(17))
      let localName = value.split("#").pop()
      let td = document.createElement("td")
      td.innerHTML = localName
      tr.appendChild(td)
    }
  })
}

function clearElement(el) {
  while (el.firstChild) el.removeChild(el.lastChild)
}

async function renderTailoredQueryObj(idx) {
  let tailoredQueryObj = tailoredQueryObjects[idx]
  let root = document.getElementById("root")
  clearElement(root)
  let h3 = document.createElement("h3")
  h3.innerHTML = tailoredQueryObjects[idx].name
  root.appendChild(h3)
  let p = document.createElement("p")
  p.innerHTML = tailoredQueryObjects[idx].description
  root.appendChild(p)
  let table = document.createElement("table")
  table.style.margin = "0 auto"
  for (let header of tailoredQueryObj.tableHeaders) {
    let th = document.createElement("th")
    th.style.backgroundColor = "lightgray"
    th.innerHTML = header
    table.appendChild(th)
  }
  root.appendChild(table)
  await fetch(tailoredQueryObj, table)
}

function registerClickListener(id, idx) {
  document.getElementById(id).addEventListener("click", () => renderTailoredQueryObj(idx))
}

registerClickListener("ex1link", 1)
registerClickListener("ex2link", 2)
registerClickListener("ex3link", 3)
registerClickListener("ex4link", 4)
