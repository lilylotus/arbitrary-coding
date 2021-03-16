package cn.nihility.nettry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

public class NettyHttpUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(NettyHttpUtil.class);

    public static FullHttpResponse notFound404(String path) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND,
                Unpooled.copiedBuffer("{\n" +
                        "    \"timestamp\": \"" + (LocalDateTime.now().toString()) + "\",\n" +
                        "    \"code\": 404,\n" +
                        "    \"error\": \"请求 uri 不存在\",\n" +
                        "    \"message\": \"请求 uri 不存在\",\n" +
                        "    \"path\": \"" + getOriginUri(path) + "\"\n" +
                        "}", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        return response;
    }

    public static void notFound404(FullHttpResponse response, String path) {
        response.setStatus(HttpResponseStatus.NOT_FOUND);
        String content = "{\n" +
                "    \"timestamp\": \"" + (LocalDateTime.now().toString()) + "\",\n" +
                "    \"code\": 404,\n" +
                "    \"error\": \"请求 uri 不存在\",\n" +
                "    \"message\": \"请求 uri 不存在\",\n" +
                "    \"path\": \"" + getOriginUri(path) + "\"\n" +
                "}";
        setResponseStringContent(response, content);
    }

    public static FullHttpResponse notAllowedMethod405(String method, String path) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.METHOD_NOT_ALLOWED,
                Unpooled.copiedBuffer("{\n" +
                        "    \"timestamp\": \"" + (LocalDateTime.now().toString()) + "\",\n" +
                        "    \"code\": 405,\n" +
                        "    \"error\": \"请求方法不允许\",\n" +
                        "    \"message\": \"请求方法 '" + method + "' 不支持\",\n" +
                        "    \"path\": \"" + getOriginUri(path) + "\"\n" +
                        "}", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        return response;
    }

    public static void notAllowedMethod405(FullHttpResponse response, String method, String path) {
        response.setStatus(HttpResponseStatus.METHOD_NOT_ALLOWED);
        String content = "{\n" +
                "    \"timestamp\": \"" + (LocalDateTime.now().toString()) + "\",\n" +
                "    \"code\": 405,\n" +
                "    \"error\": \"请求方法不允许\",\n" +
                "    \"message\": \"请求方法 '" + method + "' 不支持\",\n" +
                "    \"path\": \"" + getOriginUri(path) + "\"\n" +
                "}";
        setResponseStringContent(response, content);
    }

    public static void setResponseByteBufContent(FullHttpResponse response, ByteBuf buf) {
        response.content().clear().writeBytes(buf);
    }

    public static void setResponseStringContent(FullHttpResponse response, String content) {
        ByteBuf buf = Unpooled.wrappedBuffer(content.getBytes(StandardCharsets.UTF_8));
        setResponseByteBufContent(response, buf);
    }

    /**
     * 获取最原始的 uri 路径，去除掉 uri 路径中的参数部分
     * @param uri 请求 uri 路径
     * @return 没有参数部分的 uri 路径
     */
    public static String getOriginUri(String uri) {
        if (uri == null) {
            return null;
        }
        String originUri = uri;
        int index = originUri.indexOf("?");
        if (index != -1) {
            originUri = originUri.substring(0, index);
        }
        return originUri;
    }

    public static String writeValueAsString(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转 JSON 字符串出错", e);
        }
        return null;
    }

    /**
     * 获取 ObjectMapper.readValue(jsonStr, Map.class)，中 JSON 字符串转 Map 后的 key 值
     * 如：a.b.c
     * @param data JSON 字符串转 Map 后的格式数据
     * @param key 要查询的 key 值
     * @return key 值 对应的 value
     */
    public static Object getJsonMapValue(Map<String, Object> data, String key) {
        if (null == data) {
            return null;
        }
        StringBuilder keyStr = new StringBuilder(key);
        int dotIndex = keyStr.indexOf(".");
        if (-1 == dotIndex) {
            return data.get(key);
        } else {
            String curKey = keyStr.substring(0, dotIndex);
            String subKey = keyStr.substring(dotIndex + 1);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) data.get(curKey);
            if (null == map) {
                return null;
            } else {
                return getJsonMapValue(map, subKey);
            }
        }
    }

    public static String getJsonMapStringValue(Map<String, Object> data, String key) {
        return objectToString(getJsonMapValue(data, key));
    }

    public static String objectToString(Object obj) {
        return null == obj ? null : obj.toString();
    }

}
