const { SparqlEndpointFetcher } = require('fetch-sparql-endpoint')
const fetcher = new SparqlEndpointFetcher();

const url = 'http://localhost:3330/sparql';
let query = 'SELECT * WHERE { GRAPH <http://onto.de/model#main> { ?s ?p ?o } } LIMIT 5';

let columns = {};
runSelectQuery(query);

async function runSelectQuery(query) {
   const bindingsStream = await fetcher.fetchBindings(url, query);
   bindingsStream.on('data', bindings => {
       Object.keys(bindings).map(v => {
           if (!columns[v]) {
               columns[v] = [];
           }
           columns[v].push(bindings[v].value);
       });
   });
   bindingsStream.on('prefinish', () => {
       console.log(columns);
   });
}
