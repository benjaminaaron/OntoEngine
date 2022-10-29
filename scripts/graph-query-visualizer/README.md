# graph-query-visualizer
Build a SPARQL query by constructing a visual graph and vice versa. I moved this out of my [OntoEngine](https://github.com/benjaminaaron/OntoEngine) coding project to make it also useful independent of it.

📺 Demo [from 18:24](https://youtu.be/-AwxUrsVxsI?t=1104) in the [42min presentation video](https://github.com/benjaminaaron/OntoEngine#-video-presentation-of-the-project) of the OntoEngine project.

## Tech stack

- [force-graph](https://github.com/vasturiano/force-graph/) for the graph visualization
- [Yasgui](https://github.com/TriplyDB/Yasgui) for the SPARQL editor
- [SPARQL.js](https://github.com/RubenVerborgh/SPARQL.js) for parsing and generating the SPARQL query

## Usage

### Setup this repo for development

`npm install`

`npm run build` creates the `bundle.js` (via webpack) in `dist/` which is used by `example/index.html`

### Use this as dependency in your project

I developed this for usage in HTML. I might see later how to make it useful as `import` and `require`.

`npm i --save graph-query-visualizer`

Import `bundle.js` and `yasgui.min.css` into your `.html` file:

```html
<script src="./node_modules/graph-query-visualizer/dist/bundle.js"></script>
<link href="./node_modules/graph-query-visualizer/dist/yasgui.min.css" rel="stylesheet" />
```
