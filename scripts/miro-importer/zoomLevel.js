const config = require('./config.json');
const { MiroApi } = require("@mirohq/miro-api")
const api = new MiroApi(config.ACCESS_TOKEN)

async function getZoomLevel(boardId) {
  const view = await api.boards.view.get(boardId);
  return view.zoom;
}

getZoomLevel(config.BOARD_ID).then(zoom => {
  console.log(`Current zoom level: ${zoom}`);
});
