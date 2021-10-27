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

$(() => {
    connect();
    $("#addStatementBtn").click(() => { addStatement(); });
    $("#sendCommandBtn").click(() => { sendCommand(); });
    onKeypress("subjectTextField", () => $("#predicateTextField").focus(), "SUBJECT");
    onKeypress("predicateTextField", () => $("#objectTextField").focus(), "PREDICATE");
    onKeypress("objectTextField", addStatement, "OBJECT");
    onKeypress("commandTextField", sendCommand, null);
});
