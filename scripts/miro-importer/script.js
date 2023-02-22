const config = require('./config.json')
const fs = require('fs')
const path = require('path')
const { MiroApi } = require("@mirohq/miro-api")
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

  let tgf = ""
  let counter = 0
  let triples = []

  for await (const edge of board.getAllConnectors()) {
    await processEdge(edge)
  }

  Object.entries(nodes).forEach(([id, node]) => {
    node.id = ++ counter
    tgf += node.id + " " + node.label + "\n"
  })

  tgf += "#\n"

  edges.forEach(edge => {
    tgf += nodes[edge.from].id + " " + nodes[edge.to].id + " " + edge.label + "\n"
    triples.push([nodes[edge.from].label, edge.label, nodes[edge.to].label])
  })

  console.log(triples)

  fs.mkdir(path.join(__dirname, config.TGF_DIR), () => {})
  fs.writeFile(path.join(__dirname, config.TGF_DIR + "/graph.tgf"), tgf, () => {})
})()
