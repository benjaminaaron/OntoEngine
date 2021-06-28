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
let nodes = [{id: 0}, {id: 1}, {id: 2}];
let edges = [{source: 0, target: 1, label: "edge1"}, {source: 0, target:2, label: "edge2"}];

const buildGraph = () => {
    graph = ForceGraph()(document.getElementById('graph'))
        .graphData({ nodes: nodes, links: edges })
        .nodeLabel('id')
        .linkLabel('label')
        .linkDirectionalArrowLength(6)
        .linkDirectionalArrowRelPos(1);
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
    buildGraph();
});
