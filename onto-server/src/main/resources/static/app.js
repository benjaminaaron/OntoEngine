let stompClient = null;

const connect = () => {
    let socket = new SockJS('/onto-engine-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, frame => {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/serverBroadcasting', messageObj => {
            console.log("got the message: ", messageObj, JSON.parse(messageObj.body).message);
        });
    });
    // stompClient.disconnect();
};

const send = () => {
    stompClient.send("/app/routeServerListening", {}, JSON.stringify({'message': 'the browser client says hello'}));
};

const addStatement = () => {
    let statement = {
        subject: 'browser',
        predicate: 'says',
        object: 'hi'
    };
    stompClient.send("/app/serverReceiveAddStatements", {}, JSON.stringify(statement));
};

$(() => {
    connect();
    $("#sendBtn").click(() => { send(); });
    $("#addStatementBtn").click(() => { addStatement(); });
});
