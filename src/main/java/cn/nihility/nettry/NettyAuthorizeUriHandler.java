package cn.nihility.nettry;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Netty Http Server /test uri 请求路径解析处理器
 */
@Service
public class NettyAuthorizeUriHandler extends AbstractNettyRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(NettyAuthorizeUriHandler.class);
    private static final String AUTH_KEY_PREFIX = "trp:auth:";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JedisPool gwJedisPool;

    public NettyAuthorizeUriHandler(JedisPool gwJedisPool) {
        this.gwJedisPool = gwJedisPool;
    }

    @Override
    public void doGet(FullHttpRequest httpRequest, FullHttpResponse response) {
        NettyHttpUtil.notAllowedMethod405(response, "GET", httpRequest.uri());
    }

    @Override
    public void doPost(FullHttpRequest httpRequest, FullHttpResponse response) {
        String requestUri = httpRequest.uri();
        if (log.isDebugEnabled()) {
            log.debug("Netty Http Request Uri [{}]", requestUri);
        }

        String contentType = getContentType(httpRequest.headers());
        if ("application/json".equals(contentType)) {
            String content = httpRequest.content().toString(StandardCharsets.UTF_8);
            if (log.isDebugEnabled()) {
                log.debug("Netty Http Post 接收内容 [{}]", content);
            }

            Map<String, Object> contentMapData = null;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = objectMapper.readValue(content, Map.class);
                contentMapData = data;
            } catch (IOException e) {
                log.error("IAM 鉴权数据格式错误 [{}]", content, e);
            }

            String userId = NettyHttpUtil.getJsonMapStringValue(contentMapData, "user.id");
            String appId = NettyHttpUtil.getJsonMapStringValue(contentMapData, "app.id");
            String authData = gwJedisPool.getResource().get(AUTH_KEY_PREFIX + userId + ":" + appId);

            String ct = "{\"data\": \"key [" + content + "] data [" + authData + "]\"}";
            NettyHttpUtil.setResponseStringContent(response, ct);

        } else {
            NettyHttpUtil.setResponseStringContent(response, "{\"message\": \"不支持的请求内容类型 [" + contentType + "]\"}");
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String uri() {
        return "/upm/v2/policies/authorize";
    }

}
