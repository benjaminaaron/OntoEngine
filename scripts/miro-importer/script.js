const config = require('./config.json')
const fs = require('fs')
const path = require('path')
const { MiroApi } = require("@mirohq/miro-api")
const N3 = require('n3'); // using this version that already has RDF-star support, built locally https://github.com/rdfjs/N3.js/pull/311
const { DataFactory } = N3;
const { namedNode, literal, quad } = DataFactory;
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
      edges.push({
        from: edge.startItem.id,
        to: edge.endItem.id,
        label: clean(edge.captions[0].content)
      })
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

const clean = txt => {
  return txt.substring(3, txt.length - 4)
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
    quads.push(quad(
      namedNode(config.BASE_URI + from.label),
      namedNode(config.BASE_URI + edge.label),
      namedNode(config.BASE_URI + to.label),
    ))
  })

  console.log(triples)

  const writer = new N3.Writer({ prefixes: { dev: 'http://dev.de/default#' } });
  quads.forEach(quad => writer.addQuad(quad))
  writer.end((error, result) => {
    if (error) { console.error(error); return; }
    console.log(result);
    fs.mkdir(path.join(__dirname, config.EXPORT_DIR), () => {})
    fs.writeFile(path.join(__dirname, config.EXPORT_DIR + "/miro.ttl"), result, () => {})
  });
})()
