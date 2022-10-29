import CodeMirror from "codemirror";
import {} from "codemirror/addon/mode/simple";
import { extractTriplesFromQuery, extractWordFromUri } from "../utils";

let editor;
let keywords = { NamedNode: [], Variable: [], Literal: [] };

CodeMirror.defineSimpleMode("sparqlTermTypes", {
    start: [{
        regex: /\w+/, token: match => {
            if (keywords.NamedNode.includes(match[0].toLowerCase())) {
                return 'namedNodeShort';
            }
            if (keywords.Variable.includes(match[0].toLowerCase())) {
                return 'variable';
            }
            if (keywords.Literal.includes(match[0].toLowerCase())) {
                return 'literal';
            }
            return 'word'
        }}]
});

const initLanguageInterpreter = config => {
    config.div.style.border = "1px solid silver";
    editor = CodeMirror(config.div, {
        value: "",
        mode:  "sparqlTermTypes",
        readOnly: true,
        lineWrapping: true
    });
};

const setEditorValue = (value, _keywords = { NamedNode: [], Variable: [], Literal: [] }) => {
    keywords = _keywords;
    editor.setValue(value);
};

const getEditorValue = () => {
  return editor.getValue();
};

const onEditorChange = onChange => {
    editor.on("keyup", (obj, event) => {
        if (event.key !== "Enter") {
            alert("Translating the natural language domain to the SPARQL and graph domain is not supported yet... or ever. Quite tough to get this right I imagine :)")
        }
    });
};

const updateLanguageEditor = sparqlModel => {
    let keywords = { NamedNode: [], Variable: [], Literal: [] };

    let selectGraphData = extractTriplesFromQuery(sparqlModel, true, false);
    let fullContent = buildSentence(selectGraphData);
    extractKeywords(selectGraphData, keywords);

    if (sparqlModel.queryType === "CONSTRUCT") {
        let constructGraphData = extractTriplesFromQuery(sparqlModel, false, true);
        fullContent = "From this:\n\n" + fullContent + "\n\nWe infer that:\n\n" + buildSentence(constructGraphData);
        extractKeywords(constructGraphData, keywords);
    }

    setEditorValue(fullContent, keywords);
};

const extractKeywords = (graphData, keywords) => {
    Object.values(graphData.nodes).filter(node => node.wordNormal).forEach(node => addKeywords(node, keywords));
    graphData.edges.filter(edge => edge.wordNormal).forEach(edge => addKeywords(edge, keywords));
};

const buildSentence = graphData => {
    let longestPathNodeKeys = findLongestPath(graphData.nodes);
    let longestPath = expandNodeKeysToFullPath(longestPathNodeKeys, graphData);
    let sentence = "";
    let branchCount;
    for (let i = 0; i < longestPath.length; i++) {
        let element = longestPath[i];
        setWord(element);
        sentence += " " + element.wordNormal;
        // only nodes have paths
        branchCount = 0;
        element.paths && element.paths.filter(path => isSideBranch(longestPathNodeKeys, path)).forEach(path => {
            let expandedPath = expandNodeKeysToFullPath(path, graphData).slice(1); // skip the first
            expandedPath.forEach(branchElement => setWord(branchElement));
            let subSentenceStarter = ["human", "person"].some(word => expandedPath[1].wordNormal.includes(word)) ? "who" : "which";
            sentence += branchCount === 0 ? ", " + subSentenceStarter : " and";
            expandedPath.forEach(branchElement => sentence += " " + branchElement.wordNormal);
            branchCount ++;
        });
        if ((i + 1) % 3 === 0) {
            sentence += " that";
        } else if (branchCount > 0) {
            sentence += ",";
        }
    }
    if (sentence.endsWith(" that")) { // TODO streamline these conditions
        sentence = sentence.substr(0, sentence.length - " that".length);
    }
    return sentence.substr(1) + ".";
};

const addKeywords = (element, keywords) => {
    // this is a hack to get the highlighting going in the editor, the proper way would be to get the regex right though
    if (element.type === "Variable") {
        keywords.Variable.push(element.word);
        return;
    }
    let words = element.wordNormal.split(" ");
    if (words.length > 1) {
        words.forEach(word => keywords[element.type].push(word));
    }
    keywords[element.type].push(element.wordNormal);
};

const isSideBranch = (longestPath, testPath) => {
    for (let i = 0; i < longestPath.length; i++) {
        if (longestPath[i] === testPath[0]) {
            return longestPath[i + 1] !== testPath[1];
        }
    }
    return true;
};

const setWord = entity => {
    let value = entity.value;
    if (entity.type === "NamedNode") {
        value = extractWordFromUri(value);
    }
    entity.word = value;
    // TODO for literal strings with spaces, this makes those spaces wider - make sure it's just one space
    entity.wordNormal = value.replace(/([A-Z])/g, " $1").toLowerCase().trim(); // via stackoverflow.com/a/7225450/2474159
    if (entity.type === "Variable") {
        entity.wordNormal = "<" + entity.wordNormal + ">";
    }
};

const findLongestPath = nodes => {
    let allPathsFromAllNodes = [];
    Object.values(nodes).forEach(node => {
        let allPathsFromThisNode = [];
        walkFromHere(node, [], allPathsFromThisNode, nodes);
        allPathsFromAllNodes.push.apply(allPathsFromAllNodes, allPathsFromThisNode);
    });
    return allPathsFromAllNodes.reduce((prev, current) => {
        return (prev.length > current.length) ? prev : current
    });
};

const expandNodeKeysToFullPath = (pathNodeKeys, graph) => {
    let path = [graph.nodes[pathNodeKeys[0]]];
    for (let i = 0; i < pathNodeKeys.length - 1; i++) {
        let node = graph.nodes[pathNodeKeys[i]];
        let nextNode = graph.nodes[pathNodeKeys[i + 1]];
        let edgeBetween = graph.edges.filter(edge => edge.source === node.id && edge.target === nextNode.id)[0];
        path.push(edgeBetween);
        path.push(nextNode);
    }
    return path;
};

const walkFromHere = (node, path, allPaths, nodes) => {
    let alreadyOnPath = path.includes(node.value);
    path.push(node.value);
    if (alreadyOnPath || node.children.length === 0) {
        allPaths.push(path);
        if (path.length > 1) { // that's only the root node then
            nodes[path[0]].paths.push(path);
        }
        return;
    }
    node.children.forEach(child => walkFromHere(child, path.slice(0), allPaths, nodes));
};

export { initLanguageInterpreter, onEditorChange, setEditorValue, updateLanguageEditor, getEditorValue }
