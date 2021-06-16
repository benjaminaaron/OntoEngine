package de.benjaminaaron.ontoserver.routing.websocket;

import de.benjaminaaron.ontoserver.routing.BaseRouting;
import de.benjaminaaron.ontoserver.routing.websocket.messages.*;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.ReformulateUriSuggestionMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketRouting extends BaseRouting {

    @Autowired
    private SimpMessagingTemplate template;

    @SubscribeMapping("/subscribe")
    public ServerToClientMessage oneTimeMessageUponSubscribe() {
        ServerToClientMessage serverMsg = new ServerToClientMessage();
        serverMsg.setMessage("One-time message from the server upon subscribing");
        return serverMsg;
    }

    @MessageMapping("/routeServerListening")
    @SendTo("/topic/serverBroadcasting")
    public ServerToClientMessage incoming(ClientToServerMessage clientMsg) {
        System.out.println("Received: " + clientMsg);
        ServerToClientMessage serverMsg = new ServerToClientMessage();
        serverMsg.setMessage("Server got the message");
        return serverMsg;
    }

    @MessageMapping("/serverReceiveAddStatement")
    @SendTo("/topic/serverAddStatementResponse")
    public AddStatementResponse addStatementWebSocket(AddStatementMessage statementMsg) {
        System.out.println("AddStatement via websocket received: " + statementMsg);
        return addStatement(statementMsg);
    }

    @MessageMapping("/serverReceiveCommand")
    @SendTo("/topic/serverBroadcasting")
    public ServerToClientMessage receiveCommand(CommandMessage command) {
        System.out.println("Received: " + command);
        handleCommand(command);
        ServerToClientMessage response = new ServerToClientMessage();
        response.setMessage("Command received");
        return response;
    }

    public void sendSuggestion(ReformulateUriSuggestionMessage message) {
        this.template.convertAndSend("/topic/serverSuggestions", message);
    }
}
