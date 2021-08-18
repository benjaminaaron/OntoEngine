let stompClient = null;

const connect = () => {
    let socket = new SockJS('/onto-engine-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, frame => {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/app/subscribe', messageObj => {
            console.log("Server says: ", JSON.parse(messageObj.body).message);
        });
        stompClient.subscribe('/topic/serverBroadcasting', messageObj => {
            console.log("Server says: ", JSON.parse(messageObj.body).message);
        });
        stompClient.subscribe('/topic/serverAddStatementResponse', messageObj => {
            console.log("serverAddStatementResponse: ", JSON.parse(messageObj.body));
        });
        stompClient.subscribe('/topic/serverSuggestions', messageObj => {
            console.log("serverSuggestions: ", JSON.parse(messageObj.body));
        });
        stompClient.subscribe('/topic/whileTypingSuggestionsResponse', messageObj => {
            console.log("whileTypingSuggestionsResponse: ", JSON.parse(messageObj.body));
        });
        stompClient.subscribe('/app/initial-triples', messageObj => {
            for (let triple of JSON.parse(messageObj.body).triples) {
                addNewTripleToGraph(triple.subjectUri, triple.predicateUri, triple.objectUriOrLiteralValue, triple.objectIsLiteral);
            }
            updateOutputGraph();
        });
        stompClient.subscribe('/topic/new-triple-event', messageObj => {
            let json = JSON.parse(messageObj.body);
            addNewTripleToGraph(json.subjectUri, json.predicateUri, json.objectUriOrLiteralValue, json.objectIsLiteral);
            updateOutputGraph();
        });
    });
    // stompClient.disconnect();
};

const addStatement = () => {
    let statement = {
        subject: $("#subjectTextField").val(),
        predicate: $("#predicateTextField").val(),
        object: $("#objectTextField").val(),
        objectIsLiteral: $("#literalCheckBox").prop('checked')
    };
    console.log("statement: ", statement);
    stompClient.send("/app/serverReceiveAddStatement", {}, JSON.stringify(statement));
};

const sendCommand = () => {
    let command = {
        command: $("#commandTextField").val()
    }
    stompClient.send("/app/serverReceiveCommand", {}, JSON.stringify(command));
};

const onKeypress = (element, onEnter, resourceType) => {
    let el = $("#" + element);
    el.on('keyup', e => {
        if (e.which === 13) {
            onEnter();
        } else if (resourceType !== null) {
            let message = {
                resourceType: resourceType,
                value: el.val()
            };
            // stompClient.send("/app/requestWhileTypingSuggestions", {}, JSON.stringify(message));
        }
    });
};

const buildOutputGraph = visuType => {
    let outputGraphDiv = document.getElementById("graphOutput");
    while (outputGraphDiv.firstChild) outputGraphDiv.removeChild(outputGraphDiv.lastChild);
    switch (visuType) {
        case "None":
            return;
        case "2D":
            outputGraph = ForceGraph()(outputGraphDiv);
            break;
        case "3D":
            outputGraph = ForceGraph3D()(outputGraphDiv);
            break;
    }
    outputGraph.graphData({ nodes: [], links: [] })
        .width(1200)
        .height(600)
        .nodeLabel('label')
        .linkLabel('label')
        .linkDirectionalArrowLength(6)
        .linkDirectionalArrowRelPos(1)
        .linkCurvature('curvature');
    let canvasEl = outputGraphDiv.firstChild.firstChild;
    canvasEl.style.border = "1px solid silver";
    updateOutputGraph();
};

let outputGraph;
let outputNodesMap = {};
let outputEdgesArr = [];
const curvatureMinMax = 0.5;

const updateOutputGraph = () => {
    let nodes = [];
    Object.keys(outputNodesMap).forEach(key => nodes.push(outputNodesMap[key]));

    let edges = [];
    let selfLoopEdges = {};
    let vertexPairEdges = {}; // edges between the same two vertices, indifferent of their direction

    const add = (map, edge) => {
        if (!map[edge.vertexPairId]) {
            map[edge.vertexPairId] = [];
        }
        map[edge.vertexPairId].push(edge);
    };

    outputEdgesArr.forEach(edge => {
        edges.push(edge);
        add(edge.sourceId === edge.targetId ? selfLoopEdges : vertexPairEdges, edge);
    });

    // self loops
    Object.keys(selfLoopEdges).forEach(vpId => {
        let edges = selfLoopEdges[vpId];
        edges[edges.length - 1].curvature = 1;
        for (let i = 0; i < edges.length - 1; i++) {
            edges[i].curvature = curvatureMinMax + ((1 - curvatureMinMax) / (edges.length - 1)) * i;
        }
    });

    // multiple edges in either direction between two vertices
    Object.keys(vertexPairEdges).filter(vpId => vertexPairEdges[vpId].length > 1).forEach(vpId => {
        let vpEdges = vertexPairEdges[vpId];
        vpEdges[vpEdges.length - 1].curvature = curvatureMinMax;
        let refSourceId = vpEdges[vpEdges.length - 1].sourceId;
        for (let i = 0; i < vpEdges.length - 1; i++) {
            vpEdges[i].curvature = - curvatureMinMax + i * (2 * curvatureMinMax / (vpEdges.length - 1));
            if (refSourceId !== vpEdges[i].sourceId) {
                vpEdges[i].curvature *= -1; // flip it
            }
        }
    });

    outputGraph.graphData({ nodes: nodes, links: edges });
};

const nextId = () => {
  return Object.keys(outputNodesMap).length;
};

const addNewTripleToGraph = (subjectUri, predicateUri, object, objectIsLiteral) => {
    console.log("addNewTripleToGraph", subjectUri, predicateUri, object, objectIsLiteral);

    if (!outputNodesMap[subjectUri]) {
        outputNodesMap[subjectUri] = {id: nextId(), label: subjectUri};
    }
    let sVertex = outputNodesMap[subjectUri];

    let oVertex;
    if (objectIsLiteral) {
        oVertex = {id: nextId(), label: object};
        outputNodesMap[appendRandomStr(object)] = oVertex;
    } else {
        let objectUri = object;
        if (!outputNodesMap[objectUri]) {
            outputNodesMap[objectUri] = {id: nextId(), label: objectUri};
        }
        oVertex = outputNodesMap[objectUri];
    }

    let vertexPairId = sVertex.id <= oVertex.id ? (sVertex.id + "_" + oVertex.id) : (oVertex.id + "_" + sVertex.id); // indifferent to the direction of an edge

    outputEdgesArr.push({
        source: sVertex,
        target: oVertex,
        sourceId: sVertex.id,
        targetId: oVertex.id,
        label: predicateUri,
        vertexPairId: vertexPairId,
        curvature: 0
    });
};

const appendRandomStr = str => {
    return str + "_" + Math.random().toString(36).substr(2, 5);
};

const visuChange = visuType => {
    buildOutputGraph(visuType);
};

$(() => {
    connect();
    $("#addStatementBtn").click(() => { addStatement(); });
    $("#sendCommandBtn").click(() => { sendCommand(); });
    onKeypress("subjectTextField", () => $("#predicateTextField").focus(), "SUBJECT");
    onKeypress("predicateTextField", () => $("#objectTextField").focus(), "PREDICATE");
    onKeypress("objectTextField", addStatement, "OBJECT");
    onKeypress("commandTextField", sendCommand, null);
    $("#subjectTextField").focus();
    $("#visu-2d").prop("checked", true);
    buildOutputGraph("2D");
});
