package cn.nihility.nettry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;

import java.util.*;
import java.util.Map;

/**
 * Netty Http Server 对与 GET 请求的处理器
 */
public class NettyGetRequestHandler {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NettyGetRequestHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    public FullHttpResponse handle(FullHttpRequest fullHttpRequest) {
        String requestUri = fullHttpRequest.uri();
        if (log.isDebugEnabled()) {
            log.debug("Netty Http Get Uri [{}]", requestUri);
        }
        java.util.Map<String, String> queryParameterMappings = getQueryParams(requestUri);

        FullHttpResponse response;
        try {
            String content = objectMapper.writeValueAsString(queryParameterMappings);
            if (log.isDebugEnabled()) {
                log.debug("Netty Http Get 接收参数 [{}]", content);
            }
            response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        } catch (JsonProcessingException e) {
            log.error("处理 Netty Http Get 请求数据异常", e);

            response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    Unpooled.copiedBuffer("{\"message\": \"请求参数处理异常\"}", CharsetUtil.UTF_8));
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        return response;
    }

    private Map<String, String> getQueryParams(String uri) {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, Charsets.toCharset(CharEncoding.UTF_8));
        Map<String, List<String>> parameters = queryDecoder.parameters();
        Map<String, String> queryParams = new HashMap<>();
        for (Map.Entry<String, List<String>> attr : parameters.entrySet()) {
            for (String attrVal : attr.getValue()) {
                queryParams.put(attr.getKey(), attrVal);
            }
        }
        return queryParams;
    }

}
