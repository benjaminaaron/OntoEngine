import { SparqlEndpointFetcher } from "fetch-sparql-endpoint";
import allTriplesJson from './data/mock-data/all-triples.json';
import sparqlResultsJson from './data/mock-data/sparql-results.json';

const sparqlEndpointFetcher = new SparqlEndpointFetcher();
const SPARQL_ENDPOINT = "http://localhost:7200/repositories/onto-engine_main_empty"; // null

async function querySparqlEndpoint(query, doSave, isAllTriplesQuery, onResults) {
    if (!SPARQL_ENDPOINT) {
        let mockData = isAllTriplesQuery ? allTriplesJson : sparqlResultsJson;
        if (mockData.query !== query) {
            throw new Error("No mock data for this query");
        }
        onResults(mockData.variables, mockData.data);
        return;
    }
    const bindingsStream = await sparqlEndpointFetcher.fetchBindings(SPARQL_ENDPOINT, query);
    let variables;
    let data = [];
    bindingsStream.on('variables', vars => variables = vars);
    bindingsStream.on('data', bindings => data.push(bindings));
    bindingsStream.on('end', () => {
        if (doSave) saveData(query, variables, data, "out.json");
        onResults(variables, data);
    });
}

const saveData = (query, variables, data, filename) => {
    let output = {
        query: query,
        variables: variables,
        data: data
    };
    let blob = new Blob([JSON.stringify(output, null, 4)], {type: "application/json;charset=utf-8"});
    FileSaver.saveAs(blob, filename);
};

const fetchAllTriplesFromEndpoint = (prefixes, done) => {
    querySparqlEndpoint("SELECT * WHERE { ?s ?p ?o }", false, true, (variables, data) => {
        let nodes = {};
        let edges = [];
        data.forEach(triple => {
            let subNode = addOrGetNode(nodes, triple.s);
            let objNode = addOrGetNode(nodes, triple.o);
            addEdge(edges, triple.p, subNode.id, objNode.id);
        });
        done({ prefixes: prefixes, nodes: nodes, edges: edges });
    }).then();
};

const extractTriplesFromQuery = (sparqlModel, extractFromSelect, extractFromConstruct) => {
    let nodes = {};
    let edges = [];
    if (extractFromSelect) {
        parseTriples(sparqlModel.where[0].triples, nodes, edges, false);
    }
    if (extractFromConstruct) {
        parseTriples(sparqlModel.template, nodes, edges, true);
    }
    return { prefixes: sparqlModel.prefixes, nodes: nodes, edges: edges };
};


const parseTriples = (triplesJson, nodes, edges, markNew) => {
    triplesJson && triplesJson.forEach(triple => {
        let subNode = addOrGetNode(nodes, triple.subject, markNew);
        let objNode = addOrGetNode(nodes, triple.object, markNew);
        addEdge(edges, triple.predicate, subNode.id, objNode.id, markNew);
        // in this way from multiple same-direction edges between nodes, only one will be taken into account for computing the longest path
        // opposite-direction edges between same nodes lead to not-well defined behaviour as the alreadyOnPath-stopper kicks in, but not well defined TODO
        if (!subNode.children.includes(objNode)) {
            subNode.children.push(objNode);
        }
    });
};

const addOrGetNode = (nodes, subOrObj, markNew = false) => {
    let value = subOrObj.value;
    if (!nodes[value]) {
        nodes[value] = { id: Object.keys(nodes).length, value: value, type: subOrObj.termType, children: [], paths: [] };
        if (markNew) nodes[value].isNewInConstruct = true;
    }
    return nodes[value];
};

const addEdge = (edges, predicate, subNodeId, objNodeId, markNew = false) => {
    let value = predicate.value;
    let edge = { id: edges.length, source: subNodeId, target: objNodeId, value: value, type: predicate.termType };
    if (markNew) edge.isNewInConstruct = true;
    edges.push(edge);
};

const insertResultForVariable = (nodeOrEdge, resultRow) => {
    if (nodeOrEdge.type !== "Variable") return;
    nodeOrEdge.type = resultRow[nodeOrEdge.value].termType;
    nodeOrEdge.valueAsVariable = nodeOrEdge.value;
    nodeOrEdge.value = resultRow[nodeOrEdge.value].value;
    nodeOrEdge.wasVariable = true;
};

const extractWordFromUri = uri => {
    if (uri.includes('#')) {
        return uri.split('#')[1];
    }
    let parts = uri.split('/');
    return parts[parts.length - 1];
};

const buildShortFormIfPrefixExists = (prefixes, fullUri) => {
    let ret = fullUri;
    Object.entries(prefixes).forEach(([short, uri]) => {
        if (fullUri.startsWith(uri)) {
            ret = short + ":" + fullUri.substr(uri.length);
        }
    });
    return ret;
};

const orderNodesArray = unordered => {
    // are there less costly ways of doing this?
    let ordered = [];
    for (let i = 0; i < unordered.length; i++) {
        ordered.push(unordered.find(node => node.id === i));
    }
    return ordered;
};

export { querySparqlEndpoint, fetchAllTriplesFromEndpoint, extractTriplesFromQuery, insertResultForVariable, extractWordFromUri, buildShortFormIfPrefixExists, orderNodesArray, SPARQL_ENDPOINT }
