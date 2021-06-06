package de.benjaminaaron.ontoserver.routing.websocket;

import de.benjaminaaron.ontoserver.routing.BaseRouting;
import de.benjaminaaron.ontoserver.routing.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.ClientToServerMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.CommandMessage;
import de.benjaminaaron.ontoserver.routing.websocket.messages.ServerToClientMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketRouting extends BaseRouting {

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

    @MessageMapping("/serverReceiveAddStatements")
    @SendTo("/topic/serverBroadcasting")
    public ServerToClientMessage addStatement(AddStatementMessage statementMsg) {
        System.out.println("addStatement via websocket received: " + statementMsg);
        ServerToClientMessage response = new ServerToClientMessage();
        response.setMessage(addStatement(
                statementMsg.getSubject(), statementMsg.getPredicate(), statementMsg.getObject(),
                statementMsg.isObjectIsLiteral()));
        return response;
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
}
