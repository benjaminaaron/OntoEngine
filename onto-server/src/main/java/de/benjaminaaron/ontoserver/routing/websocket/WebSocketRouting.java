package de.benjaminaaron.ontoserver.routing.websocket;

import de.benjaminaaron.ontoserver.routing.BaseRouting;
import de.benjaminaaron.ontoserver.routing.websocket.messages.*;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.SuggestionBaseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketRouting extends BaseRouting {

    private final Logger logger = LogManager.getLogger(WebSocketRouting.class);

    @Autowired
    private SimpMessagingTemplate template;

    @SubscribeMapping("/subscribe")
    public ServerToClientMessage oneTimeMessageUponSubscribe() {
        logger.info("New WebSocket subscriber");
        ServerToClientMessage serverMsg = new ServerToClientMessage();
        serverMsg.setMessage("One-time message from the server upon subscribing");
        return serverMsg;
    }

    @MessageMapping("/routeServerListening")
    @SendTo("/topic/serverBroadcasting")
    public ServerToClientMessage incoming(ClientToServerMessage clientMsg) {
        logger.info("Received message: " + clientMsg);
        ServerToClientMessage serverMsg = new ServerToClientMessage();
        serverMsg.setMessage("Server got the message");
        return serverMsg;
    }

    @MessageMapping("/serverReceiveAddStatement")
    @SendTo("/topic/serverAddStatementResponse")
    public AddStatementResponse addStatementWebSocket(AddStatementMessage statementMsg) {
        logger.info("AddStatement via WebSocket received: " + statementMsg);
        return addStatement(statementMsg);
    }

    @MessageMapping("/serverReceiveCommand")
    @SendTo("/topic/serverBroadcasting")
    public ServerToClientMessage receiveCommand(CommandMessage commandMessage) {
        logger.info("Received command " + commandMessage);
        String response = handleCommand(commandMessage);
        ServerToClientMessage responseMessage = new ServerToClientMessage();
        if (response == null) {
            responseMessage.setMessage("Command received");
        } else {
            responseMessage.setMessage("Command failed: " + response);
        }
        return responseMessage;
    }

    public void sendSuggestion(SuggestionBaseMessage message) {
        this.template.convertAndSend("/topic/serverSuggestions", message);
        logger.info("Suggestion " + message.getSuggestionId() + " sent");
    }
}
