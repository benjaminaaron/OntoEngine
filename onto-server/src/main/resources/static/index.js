let stompClient = null;

let serverSuggestionMessages = [];

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
            appendOutput(messageObj.body);
            console.log("serverAddStatementResponse: ", JSON.parse(messageObj.body));
        });
        stompClient.subscribe('/topic/serverSuggestions', messageObj => {
            appendOutput(messageObj.body);
            console.log("serverSuggestions: ", JSON.parse(messageObj.body));
        });
        stompClient.subscribe('/topic/whileTypingSuggestionsResponse', messageObj => {
            console.log("whileTypingSuggestionsResponse: ", JSON.parse(messageObj.body));
        });
    });
    // stompClient.disconnect();
};

const prependZero = value => {
  return (value + "").length === 1 ? "0" + value : value;
};

const appendOutput = text => {
    let date = new Date();
    let timestamp = prependZero(date.getHours()) + ":" + prependZero(date.getMinutes()) + ":" + prependZero(date.getSeconds());
    serverSuggestionMessages.push(timestamp + " " + text);
    let output = "";
    serverSuggestionMessages.forEach(msg => output += msg + "\n\n");
    let textarea = document.getElementById('outputField');
    textarea.value = output;
    textarea.scrollTop = textarea.scrollHeight; // scroll to bottom
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
