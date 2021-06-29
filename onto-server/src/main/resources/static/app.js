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
            console.log("AddStatementResponse from server: ", JSON.parse(messageObj.body));
        });
        stompClient.subscribe('/topic/serverSuggestions', messageObj => {
            console.log("serverSuggestions from server: ", JSON.parse(messageObj.body));
        });
        stompClient.subscribe('/app/initial-triples', messageObj => {
            for (let triple of JSON.parse(messageObj.body).triples) {
                addNewTripleToGraph(triple.subjectUri, triple.predicateUri, triple.objectUriOrLiteralValue, triple.objectIsLiteral);
            }
        });
        stompClient.subscribe('/topic/new-triple-event', messageObj => {
            let json = JSON.parse(messageObj.body);
            addNewTripleToGraph(json.subjectUri, json.predicateUri, json.objectUriOrLiteralValue, json.objectIsLiteral);
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

const onEnter = (element, func) => {
    $("#" + element).on('keypress', e => {
        if (e.which === 13) {
            func();
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
        .nodeLabel('id')
        .linkLabel('label')
        .linkDirectionalArrowLength(6)
        .linkDirectionalArrowRelPos(1);
};

let nodesMap = {};
let edgesMap = {};

const addNewTripleToGraph = (subject, predicate, object, objectIsLiteral) => {
    console.log("addNewTripleToGraph", subject, predicate, object, objectIsLiteral);
    if (!nodesMap[subject]) {
        nodesMap[subject] = {id: subject};
    }
    let objectId = object;
    if (objectIsLiteral) {
        objectId =  appendRandomStr(object);
        nodesMap[objectId] = {id: objectId};
    } else if (!nodesMap[object]) {
        nodesMap[object] = {id: object};
    }
    edgesMap[appendRandomStr(predicate)] = {source: subject, target: objectId, label: predicate};
    let nodes = [];
    let edges = [];
    Object.keys(nodesMap).forEach(key => nodes.push(nodesMap[key]));
    Object.keys(edgesMap).forEach(key => edges.push(edgesMap[key]));
    // TODO build initial load and add them all at once instead of updating the graph for each
    graph.graphData({ nodes: nodes, links: edges });
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
    onEnter("subjectTextField", () => $("#predicateTextField").focus());
    onEnter("predicateTextField", () => $("#objectTextField").focus());
    onEnter("objectTextField", addStatement);
    onEnter("commandTextField", sendCommand);
    $("#subjectTextField").focus();
    $("#visu-2d").prop("checked", true);
    buildGraph("2D");
});
