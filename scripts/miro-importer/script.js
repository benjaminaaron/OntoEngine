const config = require('./config.json')
const fs = require('fs')
const path = require('path')
const { MiroApi } = require("@mirohq/miro-api")
const N3 = require('n3'); // using this version that already has RDF-star support, built locally https://github.com/rdfjs/N3.js/pull/311
const { DataFactory } = N3;
const { namedNode, literal, quad } = DataFactory;
const slugify = require('slugify')
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
    if (edgeLabel.includes(",")) {
      edgeLabel.split(",").slice(1).forEach(pair => edgeObj.keyValuePairs.push(pair.split(":")))
      edgeLabel = edgeLabel.split(",")[0]
    }
    edgeObj.label = clean(edgeLabel)
    edges.push(edgeObj)

    nodes[edge.startItem.id] = {
      id: undefined,
      label: clean(startItem.data.content)
    }
    nodes[edge.endItem.id] = {
      id: undefined,
      label: clean(endItem.data.content)
    }
  }
}

const removeHtml = txt => txt.replace(/<[^>]*>/g, '').trim()

const clean = txt => {
  return removeHtml(txt).replace(/\s+(\w)/g, (match, letter) => letter.toUpperCase()); // camelCase
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

  edges.forEach(edge => {
    let from = nodes[edge.from]
    let to = nodes[edge.to]
    // tgf += from.id + " " + to.id + " " + edge.label + "\n"
    triples.push([from.label, edge.label, to.label])
    let triple = quad(
      namedNode(uri(from.label)),
      namedNode(uri(edge.label)),
      namedNode(uri(to.label)),
    )
    quads.push(triple)
    edge.keyValuePairs.forEach(pair => { // RDF-star
      let pred = clean(pair[0])
      let obj = clean(pair[1])
      triples.push(["<<" + from.label + " " + edge.label + " " + to.label + ">>", pred, obj])
      quads.push(quad(
          triple,
          namedNode(uri(pred)),
          namedNode(uri(obj)),
      ))
    });
  })

  console.log(triples)

  const filename = "miro_" + slugify(board.name) + "_" + getTimestamp() + ".ttl"
  const writer = new N3.Writer({ prefixes: { dev: 'http://dev.de/default#' } });
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
