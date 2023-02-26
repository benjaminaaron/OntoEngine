const config = require("./config.json")
const fs = require("fs")
const path = require("path")
const { MiroApi } = require("@mirohq/miro-api")
const N3 = require("n3"); // using this version that already has RDF-star support, built locally https://github.com/rdfjs/N3.js/pull/311
const { DataFactory } = N3;
const { namedNode, literal, quad } = DataFactory;
const slugify = require("slugify")
const api = new MiroApi(config.ACCESS_TOKEN)

let board
const nodes = {}
const edges = []

async function processEdge(edge) {
  if (!(edge.startItem && edge.endItem && edge.captions)) return false
  const startItem = await board.getItem(edge.startItem.id)
  const endItem = await board.getItem(edge.endItem.id)
  if (startItem.type === "sticky_note" &&
      endItem.type === "sticky_note" &&
      startItem.data.content &&
      endItem.data.content
  ) {
    let edgeObj = {
      from: edge.startItem.id,
      to: edge.endItem.id,
      label: undefined,
      keyValuePairs: []
    }
    let edgeLabel = removeHtml(edge.captions[0].content)
    let parts = edgeLabel.split(",")
    if (parts.length > 1) {
      edgeLabel = parts[0]
      parts.slice(1).forEach(pair => edgeObj.keyValuePairs.push(pair.split(":")))
    }
    edgeObj.label = toCamelCase(edgeLabel, true)
    edges.push(edgeObj)

    let [fromIsLiteral, fromLabel] = processNodeLabel(startItem.data.content)
    nodes[edge.startItem.id] = {
      id: undefined,
      label: fromLabel,
      isLiteral: fromIsLiteral
    }
    let [toIsLiteral, toLabel] = processNodeLabel(endItem.data.content)
    nodes[edge.endItem.id] = {
      id: undefined,
      label: toLabel,
      isLiteral: toIsLiteral
    }
  }
}

const removeHtml = txt => {
  return txt.replace(/<[^>]*>/g, "").trim()
}

const toCamelCase = (txt, isPredicate) => {
  txt = txt.replace(/\s+(\w)/g, (match, letter) => letter.toUpperCase()).trim()
  return isPredicate ? txt.charAt(0).toLowerCase() + txt.slice(1) : txt
}

const isLiteral = txt => {
  return (txt.startsWith("&#34;") && txt.endsWith("&#34;")) ||
      (txt.startsWith("\"") && txt.endsWith("\""))
}

const processLiteral = txt => {
  if (txt.startsWith("&#34;")) return txt.substring(5, txt.length - 5)
  return txt.substring(1, txt.length - 1)
}

const processNodeLabel = txt => {
  txt = removeHtml(txt)
  if (isLiteral(txt)) return [true, processLiteral(txt)]
  return [false, toCamelCase(txt, false)]
}

const uri = localName => {
  return config.BASE_URI + localName
}

(async function () {
  board = await api.getBoard(config.BOARD_ID)

  // let tgf = ""
  let counter = 0
  let triples = []
  let quads = []

  for await (const edge of board.getAllConnectors()) {
    await processEdge(edge)
  }

  Object.entries(nodes).forEach(([id, node]) => {
    node.id = ++ counter
    // tgf += node.id + " " + node.label + "\n"
  })

  // tgf += "#\n"

  for (const edge of edges) {
    let from = nodes[edge.from]
    let to = nodes[edge.to]
    // tgf += from.id + " " + to.id + " " + edge.label + "\n"
    triples.push([from.label, edge.label, to.label])
    let triple = quad(
        namedNode(uri(from.label)),
        namedNode(uri(edge.label)),
        to.isLiteral ? literal(to.label) : namedNode(uri(to.label)),
    )
    quads.push(triple)
    for (const pair of edge.keyValuePairs) { // RDF-star
      let pred = toCamelCase(pair[0], true)
      let [objIsLiteral, objLabel] = processNodeLabel(pair[1])
      triples.push(["<<" + from.label + " " + edge.label + " " + to.label + ">>", pred, objLabel])
      quads.push(quad(
          triple,
          namedNode(uri(pred)),
          objIsLiteral ? literal(objLabel) : namedNode(uri(objLabel))
      ))
    }
  }

  console.log(triples)

  const filename = "miro_" + slugify(board.name) + "_" + getTimestamp() + ".ttl"
  const writer = new N3.Writer({ prefixes: { [config.BASE_URI_PREFIX]: config.BASE_URI } });
  quads.forEach(quad => writer.addQuad(quad))
  writer.end((error, result) => {
    if (error) { console.error(error); return; }
    console.log(result);
    fs.mkdir(path.join(__dirname, config.EXPORT_DIR), () => {})
    fs.writeFile(path.join(__dirname, config.EXPORT_DIR + "/" + filename), result, () => {})
  });
})()

function getTimestamp() {
  const pad = num => String(num).padStart(2, "0")
  let date = new Date();
  return date.getFullYear() + pad(date.getMonth() + 1) + pad(date.getDate())
      + "-" +  pad(date.getHours()) + pad(date.getMinutes()) + pad(date.getSeconds());
}
