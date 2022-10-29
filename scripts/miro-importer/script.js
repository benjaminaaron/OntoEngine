const config = require('./config.json');
const { MiroApi } = require("@mirohq/miro-api")
const api = new MiroApi(config.ACCESS_TOKEN)

const app = (async function () {
  const board = await api.getBoard(config.BOARD_ID);

  const nodesContent = {}
  const triples = [];

  for await (const item of board.getAllItems()) {
    nodesContent[item.id] = item.data.content
  }

  for await (const connector of board.getAllConnectors()) {
     if (connector.startItem && connector.endItem && connector.captions) {
        triples.push([
            clean(nodesContent[connector.startItem.id]),
            clean(connector.captions[0].content),
            clean(nodesContent[connector.endItem.id])
        ])
     }
  }

  console.log(triples)
  // TODO
})()

const clean = content => {
  return content.substring(3, content.length - 4)
}
