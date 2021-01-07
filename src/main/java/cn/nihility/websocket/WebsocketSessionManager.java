package cn.nihility.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketSessionManager {
    private static final Logger log = LoggerFactory.getLogger(WebsocketSessionManager.class);

    private static final ConcurrentHashMap<String, WebSocketSession> SESSION_POOL =
            new ConcurrentHashMap<>();

    /**
     * 添加 session
     */
    public static void add(String key, WebSocketSession session) {
        SESSION_POOL.put(key, session);
    }

    /**
     * 删除 session,会返回删除的 session
     */
    public static WebSocketSession remove(String key) {
        log.info("remove websocket session [{}]", key);
        return SESSION_POOL.remove(key);
    }

    /**
     * 删除并同步关闭连接
     */
    public static void removeAndClose(String key) {
        WebSocketSession session = remove(key);
        if (session != null) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("Close Websocket Session Error [{}]", e.getMessage(), e);
            }
        }
        log.info("remove and close websocket session [{}]", session);
    }

    /**
     * 获得 session
     */
    public static WebSocketSession get(String key) {
        return SESSION_POOL.get(key);
    }

}
