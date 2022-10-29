import { buildGraph, updateGraphData, getColorForType, renderNode, renderEdge } from '../graph-shared';
import { buildShortFormIfPrefixExists } from "../utils";

// possible feature-rich alternative: https://github.com/wbkd/react-flow --> https://www.npmjs.com/package/react-flow-renderer

let graph;
let nodeIdCounter = 0, edgeIdCounter = 0;
let nodes = [], edges = [];
let prefixes = {};
let dragSourceNode = null, interimEdge = null;
const SNAP_IN_DISTANCE = 15;
const SNAP_OUT_DISTANCE = 40;
const EntityType = {
    VARIABLE: 1, NAMED_NODE_SHORT: 2, NAMED_NODE: 3, LITERAL: 4
};

const setGraphBuilderData = graphData => {
    nodes = Object.values(graphData.nodes);
    edges = graphData.edges;
    prefixes = graphData.prefixes;
    nodes.forEach(node => interpretFromModel(node));
    edges.forEach(edge => interpretFromModel(edge));
    nodeIdCounter = nodes.length;
    edgeIdCounter = edges.length;
    update();
};

const interpretFromModel = nodeOrEdge => {
    switch (nodeOrEdge.type) {
        case 'NamedNode':
            nodeOrEdge.label = buildShortFormIfPrefixExists(prefixes, nodeOrEdge.value);
            nodeOrEdge.tooltip = nodeOrEdge.value;
            break;
        case 'Literal':
            nodeOrEdge.label = nodeOrEdge.value;
            nodeOrEdge.tooltip = null;
            break;
        case 'Variable':
            nodeOrEdge.label = "?" + nodeOrEdge.value;
            nodeOrEdge.tooltip = null;
            break;
    }
}

const expandShortForm = shortForm => {
    let baseUri = prefixes[shortForm.split(':')[0]];
    return baseUri + (baseUri.endsWith("#") || baseUri.endsWith("/") ? "" : "#") + shortForm.split(':')[1];
};

const determineTypeFromInput = input => {
    if (input.startsWith("?")) return EntityType.VARIABLE;
    if (input.startsWith("http")) return EntityType.NAMED_NODE;
    if (input.includes(":")) return EntityType.NAMED_NODE_SHORT;
    // if we reach here, it must be a Literal
    return EntityType.LITERAL;
};

const interpretInput = (nodeOrEdge, input) => {
    switch (determineTypeFromInput(input)) {
        case EntityType.VARIABLE:
            nodeOrEdge.type = "Variable";
            nodeOrEdge.value = input.substr(1);
            nodeOrEdge.label = input;
            nodeOrEdge.tooltip = null;
            break;
        case EntityType.NAMED_NODE_SHORT:
            nodeOrEdge.type = "NamedNode";
            let fullUri = expandShortForm(input);
            nodeOrEdge.value = fullUri;
            nodeOrEdge.label = input;
            nodeOrEdge.tooltip = fullUri;
            break;
        case EntityType.NAMED_NODE:
            nodeOrEdge.type = "NamedNode";
            nodeOrEdge.value = input;
            nodeOrEdge.label = buildShortFormIfPrefixExists(prefixes, input);
            nodeOrEdge.tooltip = input;
            break;
        case EntityType.LITERAL:
            nodeOrEdge.type = "Literal";
            nodeOrEdge.value = input;
            nodeOrEdge.label = input;
            nodeOrEdge.tooltip = null;
            break;
    }
    return nodeOrEdge;
};

const prefixCreatedIfUnknownShortFormUsed = input => {
    if (input.startsWith("http") || !input.includes(":")) {
        return true;
    }
    let shortForm = input.split(":")[0];
    if (prefixes[shortForm]) {
        return true;
    }
    let fullUri = prompt("New prefix: which URI does " + shortForm + " stand for:", "http://onto.de/default/");
    if (!fullUri) {
        return false;
    }
    prefixes[shortForm] = fullUri;
    return true;
};

const update = () => {
    updateGraphData(graph, nodes, edges);
};

const distance = (node1, node2) => {
    return Math.sqrt(Math.pow(node1.x - node2.x, 2) + Math.pow(node1.y - node2.y, 2));
};

const getNodePosByValue = value => {
  let node = nodes.find(node => node.value === value);
  if (!node) return {};
  return {
      x: node.x,
      y: node.y
  }
};

const getInput = (nodeOrEdge, isNode, callUpdates = true) => {
    let input = prompt('Set a value for this ' + (isNode ? 'node' : 'edge') + ':', (nodeOrEdge.isNewInConstruct ? '+' : '') + nodeOrEdge.label);
    if (!input) return false;
    let isNewInConstruct = false;
    if (input.startsWith("+")) {
        isNewInConstruct = true;
        input = input.substr(1);
    }
    if (!input.trim()) return false;
    if (EntityType.LITERAL === determineTypeFromInput(input)) {
        if (!isNode) {
            alert("Predicates (edges) can't be literals");
            return false;
        } else if (edges.filter(edge => edge.source === nodeOrEdge).length > 0) {
            alert("Subjects (nodes with outgoing edges) can't be literals");
            return false;
        }
    }
    if (!isNode && nodeOrEdge.source.type === "Literal") {
        alert("This would turn the literal-node \"" + nodeOrEdge.source.label + "\" into a subject, and subjects can't be literals");
        return false;
    }
    if (!prefixCreatedIfUnknownShortFormUsed(input)) {
        return false;
    }
    interpretInput(nodeOrEdge, input);
    nodeOrEdge.isNewInConstruct = isNewInConstruct; // also need to be able to turn it off in another rename
    if (callUpdates) {
        graphChanged();
        update();
    }
    return true;
};

const setInterimEdge = (source, target) => {
    let edgeId = edgeIdCounter ++; // this raises the ID with every snapIn-snapOut, maybe find a less "Id-wasteful" approach? TODO
    interimEdge = { id: edgeId, source: source, target: target, label: '?pred' + edgeId, type: "IN_DRAGGING" };
    edges.push(interimEdge);
    update();
};

const removeEdge = edge => {
    edges.splice(edges.indexOf(edge), 1);
};

const removeInterimEdgeWithoutAddingIt = () => {
    removeEdge(interimEdge);
    interimEdge = null;
    update();
};

const removeNode = node => {
    edges.filter(edge => edge.source === node || edge.target === node).forEach(edge => removeEdge(edge));
    nodes.splice(nodes.indexOf(node), 1);
    graphChanged();
};

const initGraphBuilder = config => {
    graph = buildGraph(config)
        .onNodeDrag(dragNode => {
            dragSourceNode = dragNode;
            for (let node of nodes) {
                if (dragNode === node) {
                    continue;
                }
                // close enough: snap onto node as target for suggested edge
                if (!interimEdge && distance(dragNode, node) < SNAP_IN_DISTANCE) {
                    setInterimEdge(dragSourceNode, node);
                }
                // close enough to other node: snap over to other node as target for suggested edge
                if (interimEdge && node !== interimEdge.target && distance(dragNode, node) < SNAP_IN_DISTANCE) {
                    removeEdge(interimEdge);
                    setInterimEdge(dragSourceNode, node);
                }
            }
            // far away enough: snap out of the current target node
            if (interimEdge && distance(dragNode, interimEdge.target) > SNAP_OUT_DISTANCE) {
                removeInterimEdgeWithoutAddingIt();
            }
        })
        .onNodeDragEnd(() => {
            dragSourceNode = null;
            if (interimEdge && !getInput(interimEdge, false)) {
                removeEdge(interimEdge);
            }
            interimEdge = null;
            update();
        })
        .linkColor(edge => getColorForType(edge.type))
        .linkLineDash(edge => edge === interimEdge ? [2, 2] : [])
        .onNodeClick((node, event) => getInput(node, true))
        .onNodeRightClick((node, event) => removeNode(node))
        .onLinkClick((edge, event) => getInput(edge, false))
        .onLinkRightClick((edge, event) => {
            removeEdge(edge);
            graphChanged();
        })
        .onBackgroundClick(event => {
            let coords = graph.screen2GraphCoords(event.layerX, event.layerY);
            let nodeId = nodeIdCounter ++;
            let node = { id: nodeId, x: coords.x, y: coords.y, label: '?var' + nodeId };
            if (getInput(node, true, false)) {
                nodes.push(node);
                update();
            }
        })
        .nodeCanvasObject((node, ctx, globalScale) =>
            renderNode(node, ctx, globalScale, () => {
                return node === dragSourceNode || (interimEdge && (node === interimEdge.source || node === interimEdge.target)) ? 'IN_DRAGGING' : node.type;
            })
        )
        .linkCanvasObjectMode(() => 'after')
        .linkCanvasObject((edge, ctx, globalScale) =>
            edge !== interimEdge && renderEdge(edge, ctx, globalScale, () => {
                return edge.type;
            })
        );
    update();
    setTimeout(() => graph.zoomToFit(400, 60), 600);
};

const graphChanged = () => {
    if (!graphChangeCallback) {
        return;
    }
    let whereTriples = [];
    let constructTriples = [];
    edges.forEach(edge => {
        (edge.isNewInConstruct || edge.source.isNewInConstruct || edge.target.isNewInConstruct ? constructTriples : whereTriples).push({
            subject: {
                termType: edge.source.type,
                value: edge.source.value
            },
            predicate: {
                termType: edge.type,
                value: edge.value
            },
            object: {
                termType: edge.target.type,
                value: edge.target.value
            }
        });
    });

    // prune unused prefixes --> doesn't seem necessary, the editor drops them automatically
    // let allShortFormLabels = new Set(nodes.map(node => node.label).concat(edges.map(edge => edge.label))
    //    .filter(label => !label.startsWith("http") && label.includes(":")).map(shortLabel => shortLabel.split(":")[0]));
    // let unusedPrefixKeys = Object.keys(prefixes).filter(key => !allShortFormLabels.has(key));
    graphChangeCallback({ prefixes: prefixes, whereTriples: whereTriples, constructTriples: constructTriples });
};

let graphChangeCallback;

const onValidGraphChange = callback => {
    graphChangeCallback = callback;
};

export { initGraphBuilder, setGraphBuilderData, onValidGraphChange, getNodePosByValue }
