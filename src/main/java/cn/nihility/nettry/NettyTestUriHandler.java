package cn.nihility.nettry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map;

/**
 * Netty Http Server /test uri 请求路径解析处理器
 */
public class NettyTestUriHandler implements NettyRequestHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NettyTestUriHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FullHttpResponse handle(FullHttpRequest httpRequest) {

        String methodName = httpRequest.method().name();
        FullHttpResponse response;

        if (HttpMethod.POST.name().equals(methodName)) {
            response = doPost(httpRequest);
        } else if (HttpMethod.GET.name().equals(methodName)) {
            response = doGet(httpRequest);
        } else {
            response = NettyHttpUtil.notAllowedMethod405(methodName, httpRequest.uri());
        }

        return response;

    }

    private FullHttpResponse doGet(FullHttpRequest request) {
        String requestUri = request.uri();
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

    private FullHttpResponse doPost(FullHttpRequest request) {
        String requestUri = request.uri();
        if (log.isDebugEnabled()) {
            log.debug("Netty Http Request Uri [{}]", requestUri);
        }

        FullHttpResponse response;
        String contentType = getContentType(request.headers());
        if ("application/json".equals(contentType)) {
            String content = request.content().toString(Charsets.toCharset(CharEncoding.UTF_8));
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
        if (!StringUtils.isEmpty(typeStr)) {
            String[] list = typeStr.split(";");
            typeStr = list[0];
        }
        return typeStr;
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

    @Override
    public String uri() {
        return "/test";
    }

}
