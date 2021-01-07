package cn.nihility.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

public class WebSocketManual extends AbstractWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketManual.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("afterConnectionEstablished session [{}]", session.getId());
        session.sendMessage(new TextMessage("ConnectionEstablished"));
        WebsocketSessionManager.add(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("handleTextMessage session [{}]", session.getId());
        session.sendMessage(new TextMessage("handleTextMessage:" + message.getPayload()));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        log.info("handleBinaryMessage session [{}]", session.getId());
        session.sendMessage(new TextMessage("handleBinaryMessage"));
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.info("handlePongMessage session [{}]", session.getId());
        session.sendMessage(new TextMessage("handlePongMessage"));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("handleTransportError session [{}]", session.getId());
        session.close(CloseStatus.SERVER_ERROR.withReason(exception.getMessage()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("afterConnectionClosed session [{}]", session.getId());
        session.close(status);
        WebsocketSessionManager.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        log.info("supportsPartialMessages");
        return false;
    }

}
