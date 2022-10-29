import { onValidSparqlChange, setSparqlQuery, getQuery } from './panels/sparql-editor'
import { setGraphBuilderData, onValidGraphChange } from './panels/graph-builder';
import { getEditorValue, onEditorChange, updateLanguageEditor } from "./panels/language-interpreter";
import { querySparqlEndpoint, fetchAllTriplesFromEndpoint, extractTriplesFromQuery, insertResultForVariable, orderNodesArray, extractWordFromUri, SPARQL_ENDPOINT } from "./utils";
import { highlightGraphOutputSubset, setGraphOutputData } from "./panels/graph-output";
import { buildTable } from "./panels/results-table";

const parser = new require('sparqljs').Parser();
const generator = new require('sparqljs').Generator();
let currentSparqlModel;
let outputElements;

const Domain = {
    SPARQL: 1, GRAPH: 2, LANGUAGE: 3
};
let acceptingChanges = true; // to avoid changes triggering circular onChange-calls

const initModel = _outputElements => {
    onValidSparqlChange(data => acceptingChanges && translateToOtherDomains(Domain.SPARQL, data));
    onValidGraphChange(data => acceptingChanges && translateToOtherDomains(Domain.GRAPH, data));
    onEditorChange(data => acceptingChanges && translateToOtherDomains(Domain.LANGUAGE, data));

    outputElements = _outputElements;
    document.getElementById(outputElements.submitButtonId).addEventListener('click', () => submitSparqlQuery());

    let emptyQuery = "PREFIX : <http://onto.de/default#>\n" +
        "SELECT * WHERE {\n" +
        "  ?s ?p ?o ;\n" +
        "}";

    let exampleQuery = "PREFIX : <http://onto.de/default#>\n" +
        "SELECT * WHERE {\n" +
        "  ?someone :isA :Human ;\n" +
        "  \t:rentsA ?flat .\n" +
        "  ?flat :isLocatedIn :Hamburg .\n" +
        "  ?someone ?opinion :DowntownAbbey .\n" +
        "  ?flat :isOnFloor 2 .\n" +
        "}";
    /*let query = "PREFIX : <http://onto.de/default#> \n" +
        "CONSTRUCT { \n" +
        "  ?someone :livesIn ?location . \n" +
        "} WHERE { \n" +
        "    ?someone :isA :Human . \n" +
        "    ?someone :likes :iceCream . \n" +
        "    ?someone :rentsA ?flat . \n" +
        "    ?flat :isLocatedIn ?location . \n" +
        "}";*/
    setSparqlQuery(SPARQL_ENDPOINT ? emptyQuery : exampleQuery);
};

const submitSparqlQuery = () => {
    outputElements.outputWrapperDiv.style.display = 'flex';
    let prefixes = currentSparqlModel.prefixes;
    fetchAllTriplesFromEndpoint(prefixes, allGraphData => {
        allGraphData.nodes = orderNodesArray(Object.values(allGraphData.nodes));
        setGraphOutputData(allGraphData);
        querySparqlEndpoint(getQuery(), false, false, (variables, rows) => {
            console.log("query result:", variables, rows);
            buildTable(variables, rows, prefixes, selectedRow => {
                let queryGraphData = null;
                let filledSentence = "";
                if (selectedRow) {
                    queryGraphData = extractTriplesFromQuery(currentSparqlModel, true, true);
                    queryGraphData.nodes = orderNodesArray(Object.values(queryGraphData.nodes));
                    queryGraphData.nodes.forEach(node => insertResultForVariable(node, selectedRow))
                    queryGraphData.edges.forEach(edge => insertResultForVariable(edge, selectedRow));
                    console.log("queryGraphData", queryGraphData);
                    let sentence = getEditorValue(); // sentence with variables in it
                    Object.keys(selectedRow).forEach(key => {
                        if (key === "tr") return;
                        let filledVar = "<b>" + extractWordFromUri(selectedRow[key].value) + "</b>";
                        // get the editor value as JSON object instead to avoid having to parse it from raw text? TODO
                        sentence = sentence.replace("<" + key + ">", filledVar);
                    });
                    filledSentence = "--> " + sentence;
                }
                outputElements.queryResultsSentenceDiv.innerHTML = filledSentence;
                highlightGraphOutputSubset(queryGraphData);
            });
        }).then();
    });
};

const translateToOtherDomains = (sourceDomain, data) => {
    acceptingChanges = false;
    switch (sourceDomain) {
        case Domain.SPARQL:
            currentSparqlModel = parser.parse(data);
            updateLanguageEditor(currentSparqlModel);
            setGraphBuilderData(extractTriplesFromQuery(currentSparqlModel, true, true)); // edge.source/target will be made the node objects instead of just ids
            break;
        case Domain.GRAPH:
            currentSparqlModel = constructSparqlModelFromGraphBuilderData(data);
            setSparqlQuery(generator.stringify(currentSparqlModel));
            updateLanguageEditor(currentSparqlModel);
            break;
        case Domain.LANGUAGE:
            // not supported (yet)
            break;
    }
    acceptingChanges = true;
};

const constructSparqlModelFromGraphBuilderData = data => {
    let isConstructQuery = data.constructTriples.length > 0;
    let constructedSparqlModel = {
        prefixes: data.prefixes,
        queryType: isConstructQuery ? "CONSTRUCT" : "SELECT",
        type: "query",
        where: [{
            type: "bgp",
            triples: data.whereTriples
        }]
    };
    if (isConstructQuery) {
        constructedSparqlModel.template = data.constructTriples;
    } else {
        constructedSparqlModel.variables = [{
            termType: "Wildcard",
            value: "*"
        }];
    }
    return constructedSparqlModel;
};

export { initModel }
