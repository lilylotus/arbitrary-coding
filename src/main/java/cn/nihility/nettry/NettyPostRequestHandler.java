package cn.nihility.nettry;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;

import java.nio.charset.StandardCharsets;

/**
 * Netty Http Server 对与 POST 请求的处理器
 */
public class NettyPostRequestHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NettyPostRequestHandler.class);

    public FullHttpResponse handle(FullHttpRequest fullHttpRequest) {
        String requestUri = fullHttpRequest.uri();
        if (log.isDebugEnabled()) {
            log.debug("Netty Http Request Uri [{}]", requestUri);
        }

        FullHttpResponse response;
        String contentType = getContentType(fullHttpRequest.headers());
        if ("application/json".equals(contentType)) {
            String content = fullHttpRequest.content().toString(Charsets.toCharset(CharEncoding.UTF_8));
            if (log.isDebugEnabled()) {
                log.debug("Netty Http Post 接收内容 [{}]", content);
            }
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.copiedBuffer(content, StandardCharsets.UTF_8));
        } else {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    Unpooled.copiedBuffer("{\"message\": \"不支持的请求内容类型 [" + contentType + "]\"}", StandardCharsets.UTF_8));
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        return response;
    }

    private String getContentType(HttpHeaders headers) {
        String typeStr = headers.get("Content-Type");
        String[] list = typeStr.split(";");
        return list[0];
    }

}
