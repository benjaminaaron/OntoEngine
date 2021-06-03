package de.benjaminaaron.ontoclientjavafx.websocket;

import de.benjaminaaron.ontoclientjavafx.websocket.messages.AddStatementMessage;
import de.benjaminaaron.ontoclientjavafx.websocket.messages.ServerToClientMessage;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Component
public class WebSocketController {

    @Value("${websocket.server.endpoint}")
    private String WEBSOCKET_ENDPOINT;
    private StompSession session;

    @SneakyThrows
    @PostConstruct
    private void init() {
        WebSocketClient simpleWebSocketClient = new StandardWebSocketClient();
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(simpleWebSocketClient));

        SockJsClient sockJsClient = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        session = stompClient.connect(WEBSOCKET_ENDPOINT, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("StompSession connected: " + session.getSessionId());
            }
        }).get();

        session.subscribe("/topic/serverBroadcasting", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders stompHeaders) {
                return ServerToClientMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders stompHeaders, Object payload) {
                ServerToClientMessage msg = (ServerToClientMessage) payload;
                System.out.println("Server says: " + msg.getMessage());
            }
        });
    }

    public void sendAddStatement(String subject, String predicate, String object) {
        AddStatementMessage msg = new AddStatementMessage();
        msg.setSubject(subject);
        msg.setPredicate(predicate);
        msg.setObject(object);
        session.send("/app/serverReceiveAddStatements", msg);
    }
}
