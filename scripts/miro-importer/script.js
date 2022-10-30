const config = require('./config.json');
const fs = require('fs');
const path = require('path');
const { MiroApi } = require("@mirohq/miro-api")
const api = new MiroApi(config.ACCESS_TOKEN)

const app = (async function () {
  const board = await api.getBoard(config.BOARD_ID);

  const ids = {}
  let tgf = ""
  let counter = 0
  const validEdges = [];
  const connectedNodes = {} // have at least one edge connected

  for await (const connector of board.getAllConnectors()) {
    if (!(connector.startItem && connector.endItem && connector.captions)) continue
    connectedNodes[connector.startItem.id] = true
    connectedNodes[connector.endItem.id] = true
    validEdges.push(connector)
  }

  for await (const item of board.getAllItems()) {
    if (!connectedNodes[item.id]) continue
    let newId = ++ counter
    ids[item.id] = newId
    tgf += newId + " " + clean(item.data.content) + "\n"
  }

  tgf += "#\n"

  for (let edge of validEdges) {
    tgf += ids[edge.startItem.id] + " " + ids[edge.endItem.id]
           + " " + clean(edge.captions[0].content) + "\n"
  }

  console.log(tgf)

  fs.mkdir(path.join(__dirname, config.TGF_DIR), () => {})
  fs.writeFile(path.join(__dirname, config.TGF_DIR + "/graph.tgf"), tgf, () => {})
})()

const clean = content => {
  return content.substring(3, content.length - 4)
}
