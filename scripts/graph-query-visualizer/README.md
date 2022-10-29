# graph-query-visualizer
Build a SPARQL query by constructing a visual graph and vice versa.

📺 Demo [from 18:24](https://youtu.be/-AwxUrsVxsI?t=1104) in the [42min presentation video](https://github.com/benjaminaaron/OntoEngine#-video-presentation-of-the-project).

## Tech stack

- [force-graph](https://github.com/vasturiano/force-graph/) for the graph visualization
- [Yasgui](https://github.com/TriplyDB/Yasgui) for the SPARQL editor
- [SPARQL.js](https://github.com/RubenVerborgh/SPARQL.js) for parsing and generating the SPARQL query

## Usage

`npm install`

`npm run build` creates the `bundle.js` (via webpack) in `dist/` which is used by `example/index.html`
