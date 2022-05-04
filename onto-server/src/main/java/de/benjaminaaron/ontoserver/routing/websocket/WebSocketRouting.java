package de.benjaminaaron.ontoserver.routing.websocket;

import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.model.Utils;
import de.benjaminaaron.ontoserver.routing.BaseRouting;
import de.benjaminaaron.ontoserver.routing.websocket.messages.*;
import de.benjaminaaron.ontoserver.routing.websocket.messages.suggestion.SuggestionBaseMessage;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class WebSocketRouting {

    private final Logger logger = LogManager.getLogger(WebSocketRouting.class);

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private ModelController modelController;

    @Autowired
    private BaseRouting baseRouting;

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
        return baseRouting.addStatement(statementMsg);
    }

    @MessageMapping("/serverReceiveCommand")
    @SendTo("/topic/serverBroadcasting")
    public ServerToClientMessage receiveCommand(CommandMessage commandMessage) {
        logger.info("Received " + commandMessage);
        String response = baseRouting.handleCommand(commandMessage);
        ServerToClientMessage responseMessage = new ServerToClientMessage();
        if (response == null) {
            responseMessage.setMessage("Command received");
        } else {
            responseMessage.setMessage("Command failed: " + response);
        }
        return responseMessage;
    }

    @MessageMapping("/requestWhileTypingSuggestions")
    @SendTo("/topic/whileTypingSuggestionsResponse")
    public WhileTypingSuggestionsMessage whileTypingSuggestions(WhileTypingSuggestionsMessage message) {
        // suggestionEngine.generateWhileTypingSuggestions(message); TODO
        return message;
    }

    public void sendSuggestion(SuggestionBaseMessage message) {
        this.template.convertAndSend("/topic/serverSuggestions", message);
        logger.info("Suggestion " + message.getSuggestionId() + " sent");
    }

    public void sendMessage(String str) {
        ServerToClientMessage message = new ServerToClientMessage();
        message.setMessage(str);
        this.template.convertAndSend("/topic/serverBroadcasting", message);
    }

    // Broadcasting of initial and new triples

    @SubscribeMapping("/initial-triples")
    public InitialTriplesMessage initialTriples() {
        logger.info("Transferring initial-triples");
        InitialTriplesMessage initialTriplesMessage = new InitialTriplesMessage();
        List<TripleMessage> triples = new ArrayList<>();
        StmtIterator iter = modelController.getMainModel().listStatements();
        while (iter.hasNext()) {
            triples.add(Utils.buildTripleMessageFromStatement(iter.nextStatement()));
        }
        initialTriplesMessage.setTriples(triples);
        return initialTriplesMessage;
    }

    public void sendNewTripleEvent(Statement statement) {
        this.template.convertAndSend("/topic/new-triple-event", Utils.buildTripleMessageFromStatement(statement));
    }
}
