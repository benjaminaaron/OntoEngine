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
            updateGraph();
        });
        stompClient.subscribe('/topic/new-triple-event', messageObj => {
            let json = JSON.parse(messageObj.body);
            addNewTripleToGraph(json.subjectUri, json.predicateUri, json.objectUriOrLiteralValue, json.objectIsLiteral);
            updateGraph();
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

let graph;

const buildGraph = visuType => {
    let graphDiv = document.getElementById("graph");
    while (graphDiv.firstChild) {
        graphDiv.removeChild(graphDiv.lastChild);
    }
    switch (visuType) {
        case "None":
            return;
        case "2D":
            graph = ForceGraph()(graphDiv);
            break;
        case "3D":
            graph = ForceGraph3D()(graphDiv);
            break;
    }
    graph.graphData({ nodes: [], links: [] })
        .nodeLabel('label')
        .linkLabel('label')
        .linkDirectionalArrowLength(6)
        .linkDirectionalArrowRelPos(1)
        .linkCurvature('curvature');
    updateGraph();
};

let nodesMap = {};
let edgesArr = [];
const curvatureMinMax = 0.5;

const updateGraph = () => {
    let nodes = [];
    Object.keys(nodesMap).forEach(key => nodes.push(nodesMap[key]));

    let edges = [];
    let selfLoopEdges = {};
    let vertexPairEdges = {}; // edges between the same two vertices, indifferent of their direction

    const add = (map, edge) => {
        if (!map[edge.vertexPairId]) {
            map[edge.vertexPairId] = [];
        }
        map[edge.vertexPairId].push(edge);
    };

    edgesArr.forEach(edge => {
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

    graph.graphData({ nodes: nodes, links: edges });
};

const nextId = () => {
  return Object.keys(nodesMap).length;
};

const addNewTripleToGraph = (subjectUri, predicateUri, object, objectIsLiteral) => {
    console.log("addNewTripleToGraph", subjectUri, predicateUri, object, objectIsLiteral);

    if (!nodesMap[subjectUri]) {
        nodesMap[subjectUri] = {id: nextId(), label: subjectUri};
    }
    let sVertex = nodesMap[subjectUri];

    let oVertex;
    if (objectIsLiteral) {
        oVertex = {id: nextId(), label: object};
        nodesMap[appendRandomStr(object)] = oVertex;
    } else {
        let objectUri = object;
        if (!nodesMap[objectUri]) {
            nodesMap[objectUri] = {id: nextId(), label: objectUri};
        }
        oVertex = nodesMap[objectUri];
    }

    let vertexPairId = sVertex.id <= oVertex.id ? (sVertex.id + "_" + oVertex.id) : (oVertex.id + "_" + sVertex.id); // indifferent to the direction of an edge

    edgesArr.push({
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
    buildGraph(visuType);
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
    buildGraph("2D");
});
