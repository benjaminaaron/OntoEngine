package de.benjaminaaron.ontoserver.websocket;

import de.benjaminaaron.ontoserver.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoserver.websocket.messages.ClientToServerMessage;
import de.benjaminaaron.ontoserver.websocket.messages.ServerToClientMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketRouting {

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
    public ServerToClientMessage addStatement(AddStatementMessage statement) {
        System.out.println("Received: " + statement);
        // TODO
        ServerToClientMessage response = new ServerToClientMessage();
        response.setMessage("Statement received and added");
        return response;
    }
}
