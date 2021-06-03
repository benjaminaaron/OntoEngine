let stompClient = null;

const connect = () => {
    let socket = new SockJS('/onto-engine-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, frame => {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/serverBroadcasting', messageObj => {
            console.log("Server says: ", JSON.parse(messageObj.body).message);
        });
    });
    // stompClient.disconnect();
};

const dev = () => {
    stompClient.send("/app/routeServerListening", {}, JSON.stringify({'message': 'the browser client says hello'}));
};

const addStatement = () => {
    let statement = {
        subject: $("#subjectTextField").val(),
        predicate: $("#predicateTextField").val(),
        object: $("#objectTextField").val()
    };
    stompClient.send("/app/serverReceiveAddStatements", {}, JSON.stringify(statement));
};

$(() => {
    connect();
    $("#devBtn").click(() => { dev(); });
    $("#addStatementBtn").click(() => { addStatement(); });
});
