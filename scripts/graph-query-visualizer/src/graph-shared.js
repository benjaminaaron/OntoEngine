import ForceGraph from 'force-graph';

const FONT_SIZE = 16;

const buildGraph = config => {
    let graph = ForceGraph()(config.div)
        .width(config.width)
        .height(config.height)
        .nodeLabel('tooltip')
        .linkLabel('tooltip')
        .linkDirectionalArrowLength(4)
        .linkDirectionalArrowRelPos(0.9)
        .linkCurvature('curvature');
    let canvasEl = config.div.firstChild.firstChild;
    canvasEl.style.border = "1px solid silver";
    return graph;
};

const updateGraphData = (graph, nodes, edges) => {
    computeEdgeCurvatures(edges);
    graph.graphData({ nodes: nodes, links: edges });
};

const getColorForType = type => {
    switch (type) {
        case 'NamedNode':
            return '#e9591e';
        case 'Variable':
            return '#1d158b';
        case 'Literal':
            return '#912419';
        case 'IN_DRAGGING':
            return 'orange';
        default:
            return 'rgba(31, 120, 180, 0.92)';
    }
    // ForceGraph default colors: edge = rgba(255,255,255,0.2), node = rgba(31, 120, 180, 0.92)
    // Yasgui editor colors: NamedNode full #337a4d, NamedNode short #e9591e, Literal #912419, Variable #1d158b, curly brackets: #4aae23 (selected, otherwise black), keywords: #62167a
};

const freezeNodeAtPos = (node, pos) => {
    // node.x = pos.x;
    // node.y = pos.y;
    // make them go there animated using forces instead of jumping there? TODO
    node.fx = pos.x;
    node.fy = pos.y;
};

const showOutline = nodeOrEdge => {
    return nodeOrEdge.isNewInConstruct || (nodeOrEdge.highlightAsType && nodeOrEdge.highlightAsType === 'Variable');
};

const renderNode = (node, ctx, globalScale, determineNodeType) => {
    const fontSize = FONT_SIZE / globalScale;
    ctx.font = `${fontSize}px Sans-Serif`;
    const textWidth = ctx.measureText(node.label).width;
    const rectDim = [textWidth, fontSize].map(n => n + fontSize * 1.1); // padding
    ctx.fillStyle = getColorForType(determineNodeType());
    // ctx.fillRect(node.x - rectDim[0] / 2, node.y - rectDim[1] / 2, ...rectDim);
    roundedRect(node.x - rectDim[0] / 2, node.y - rectDim[1] / 2, rectDim[0], rectDim[1], 20, ctx);
    if (showOutline(node)) {
        ctx.lineWidth = 4;
        ctx.strokeStyle = 'yellow';
        ctx.stroke(); // roundedRect outline
    }
    ctx.fill(); // roundedRect background
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillStyle = 'white';
    ctx.fillText(node.label, node.x, node.y + fontSize * 0.1); // corrective factor to move text down a tiny bit within the rectangle
};

const renderEdge = (edge, ctx, globalScale, determineEdgeType) => {
    const source = edge.source;
    const target = edge.target;
    // calculate label positioning
    const textPos = Object.assign(...['x', 'y'].map(c => ({ [c]: source[c] + (target[c] - source[c]) / 2 }))); // calc middle point
    const relLink = { x: target.x - source.x, y: target.y - source.y };
    let textAngle = Math.atan2(relLink.y, relLink.x);
    // maintain label vertical orientation for legibility
    if (textAngle > Math.PI / 2) textAngle = - (Math.PI - textAngle);
    if (textAngle < - Math.PI / 2) textAngle = - (- Math.PI - textAngle);
    const fontSize = FONT_SIZE / globalScale;
    ctx.font = `${fontSize}px Sans-Serif`;
    const textWidth = ctx.measureText(edge.label).width;
    const rectDim = [textWidth, fontSize].map(n => n + fontSize * 0.8); // padding
    // draw text label (with background rect)
    ctx.save();
    ctx.translate(textPos.x, textPos.y);
    ctx.rotate(textAngle);
    ctx.fillStyle = showOutline(edge) ? 'yellow' : 'rgba(255, 255, 255, 0.8)';
    ctx.fillRect(- rectDim[0] / 2, - rectDim[1] / 2, ...rectDim)
    // ctx.strokeRect(- rectDim[0] / 2, - rectDim[1] / 2, ...rectDim);
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillStyle = getColorForType(determineEdgeType());
    ctx.fillText(edge.label, 0, 0);
    ctx.restore();
};

const roundedRect = (x, y, w, h, r, ctx) => {
    // from stackoverflow.com/a/7838871/2474159
    if (w < 2 * r) r = w / 2;
    if (h < 2 * r) r = h / 2;
    ctx.beginPath();
    ctx.moveTo(x + r, y);
    ctx.arcTo(x + w, y, x + w, y + h, r);
    ctx.arcTo(x + w, y + h, x, y + h, r);
    ctx.arcTo(x, y + h, x, y, r);
    ctx.arcTo(x, y, x + w, y, r);
    ctx.closePath();
};

const CURVATURE_MIN_MAX = 0.5;

const computeEdgeCurvatures = edges => {
    let selfLoopEdges = {};
    let sameNodesEdges = {};

    // 1. assign each edge a nodePairId that combines their source and target independent of the edge-direction
    // 2. group edges together that share the same two nodes or are self-loops
    edges.forEach(edge => {
        let sourceId = edge.source.id === undefined ? edge.source : edge.source.id;
        let targetId = edge.target.id === undefined ? edge.target : edge.target.id;
        edge.nodePairId = sourceId <= targetId ? (sourceId + "_" + targetId) : (targetId + "_" + sourceId);
        edge.curvature = null; // reset all in case they had values
        let map = edge.source === edge.target ? selfLoopEdges : sameNodesEdges;
        if (!map[edge.nodePairId]) {
            map[edge.nodePairId] = [];
        }
        map[edge.nodePairId].push(edge);
    });

    // TODO the graph-builder UI doesn't support creating self-loops yet
    // Compute the curvature for self-loop edges to avoid overlaps
    Object.keys(selfLoopEdges).forEach(id => {
        let edges = selfLoopEdges[id];
        let lastIndex = edges.length - 1;
        edges[lastIndex].curvature = 1;
        let delta = (1 - CURVATURE_MIN_MAX) / lastIndex;
        for (let i = 0; i < lastIndex; i++) {
            edges[i].curvature = CURVATURE_MIN_MAX + i * delta;
        }
    });

    // Compute the curvature for edges sharing the same two nodes to avoid overlaps
    Object.keys(sameNodesEdges).filter(nodePairId => sameNodesEdges[nodePairId].length > 1).forEach(nodePairId => {
        let edges = sameNodesEdges[nodePairId];
        let lastIndex = edges.length - 1;
        let lastEdge = edges[lastIndex];
        lastEdge.curvature = CURVATURE_MIN_MAX;
        let delta = 2 * CURVATURE_MIN_MAX / lastIndex;
        for (let i = 0; i < lastIndex; i++) {
            edges[i].curvature = - CURVATURE_MIN_MAX + i * delta;
            if (lastEdge.source !== edges[i].source) {
                edges[i].curvature *= -1; // flip it around, otherwise they overlap
            }
        }
    });
};

export { buildGraph, updateGraphData, getColorForType, freezeNodeAtPos, renderNode, renderEdge }
