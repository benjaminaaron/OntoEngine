const config = require('./config.json');
const fs = require('fs');
const path = require('path');
const { MiroApi } = require("@mirohq/miro-api")
const api = new MiroApi(config.ACCESS_TOKEN)

const app = (async function () {
  const board = await api.getBoard(config.BOARD_ID);

  const nodes = {}
  const edges = [];
  const idsOfNodesThatHaveEdges = {}
  let tgf = ""
  let counter = 0
  let triples = [] // just for console output

  for await (const edge of board.getAllConnectors()) {
    if (!(edge.startItem && edge.endItem && edge.captions)) continue
    idsOfNodesThatHaveEdges[edge.startItem.id] = true
    idsOfNodesThatHaveEdges[edge.endItem.id] = true
    edges.push(edge)
  }

  for await (const item of board.getAllItems()) {
    if (!idsOfNodesThatHaveEdges[item.id]) continue
    let node = {
      newId: ++ counter,
      content: clean(item.data.content)
    }
    tgf += node.newId + " " + node.content + "\n"
    nodes[item.id] = node
  }

  tgf += "#\n"

  for (let edge of edges) {
    const from = nodes[edge.startItem.id]
    const to = nodes[edge.endItem.id]
    const edgeContent = clean(edge.captions[0].content)
    tgf += from.newId + " " + to.newId + " " + edgeContent + "\n"
    triples.push([from.content, edgeContent, to.content])
  }

  console.log(triples)

  fs.mkdir(path.join(__dirname, config.TGF_DIR), () => {})
  fs.writeFile(path.join(__dirname, config.TGF_DIR + "/graph.tgf"), tgf, () => {})
})()

const clean = content => {
  return content.substring(3, content.length - 4)
}
