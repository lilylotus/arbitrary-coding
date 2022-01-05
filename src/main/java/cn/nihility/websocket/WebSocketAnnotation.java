package cn.nihility.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * {@link javax.websocket.Endpoint}
 */
@Component
@ServerEndpoint(value = "/ws/{name}", configurator = WebSocketConfigurerImpl.class)
public class WebSocketAnnotation {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAnnotation.class);
    private Session session;

    @OnOpen
    public void onOpen(Session session, @PathParam("name") String name, EndpointConfig config) throws IOException {
        log.info("on open, session id [{}], path param [{}]", session.getId(), name);
        Object configValue = config.getUserProperties().get("timestamp");
        log.info("user property timestamp [{}]", configValue);
        this.session = session;
        sendMessage("connection success.");
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) throws IOException {
        log.info("session close id [{}], reason [{}]", session.getId(), closeReason);
        sendMessage("ready close session");
        if (session.isOpen()) {
            session.close();
            log.info("session closed, [{}]", session.getId());
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        log.info("on the message, session id [{}], message [{}]", session.getId(), message);
        sendMessage("receive: " + message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        log.info("on error, session id [{}], error message [{}]", session.getId(), throwable.getMessage());
        sendMessage("encounter trouble:" + throwable.getMessage());
    }

    private void sendMessage(String message) throws IOException {
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText("sever:" + message);
        }
    }

}
