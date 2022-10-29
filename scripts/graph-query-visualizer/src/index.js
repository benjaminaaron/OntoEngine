import { initSparqlEditor } from './panels/sparql-editor';
import { initGraphBuilder } from './panels/graph-builder';
import { initLanguageInterpreter } from "./panels/language-interpreter";
import { initResultsTable } from "./panels/results-table";
import { initGraphOutput } from './panels/graph-output';
import { initModel } from "./model";

window.init = config => {
    initSparqlEditor(config.sparqlEditor);
    initGraphBuilder(config.graphBuilder);
    initLanguageInterpreter(config.languageInterpreter);
    initResultsTable(config.resultsTable);
    initGraphOutput(config.graphOutput);
    initModel(config.outputElements);
};
