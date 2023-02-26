const config = require("./config.json")
const { MiroApi } = require("@mirohq/miro-api")
const api = new MiroApi(config.ACCESS_TOKEN)

async function extract() {
  let board = await api.getBoard(config.BOARD_ID)

  for await (const edge of board.getAllConnectors()) {}
  for await (const item of board.getAllItems()) {}
}

async function recreate() {
  let board = await api.getBoard(config.BOARD_ID)
}

let args = process.argv.slice(2);

if (args[0] === "extract") {
  extract().then(() => console.log("infos extracted"))
}

if (args[0] === "recreate") {
  recreate().then(() => console.log("recreate step # completed"))
}
