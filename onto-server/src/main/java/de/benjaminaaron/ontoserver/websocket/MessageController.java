package de.benjaminaaron.ontoserver.websocket;

import de.benjaminaaron.ontoserver.websocket.messages.ClientToServerMessage;
import de.benjaminaaron.ontoserver.websocket.messages.ServerToClientMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    @MessageMapping("/routeServerListening")
    @SendTo("/topic/serverBroadcasting")
    public ServerToClientMessage incoming(ClientToServerMessage clientMsg) {
        System.out.println("Received message from client: " + clientMsg.getMessage());
        ServerToClientMessage serverMsg = new ServerToClientMessage();
        serverMsg.setMessage("Server got the message");
        return serverMsg;
    }

}
