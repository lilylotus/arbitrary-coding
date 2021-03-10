package cn.nihility.nettry;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.time.LocalDateTime;

public class NettyHttpUtil {

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

    public static FullHttpResponse notAllowedMethod405(String method, String path) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.METHOD_NOT_ALLOWED,
                Unpooled.copiedBuffer("{\n" +
                        "    \"timestamp\": \"" + (LocalDateTime.now().toString()) + "\",\n" +
                        "    \"code\": 405,\n" +
                        "    \"error\": \"请求方法不允许\",\n" +
                        "    \"message\": \"请求方法 '" + method + "'不支持\",\n" +
                        "    \"path\": \"" + getOriginUri(path) + "\"\n" +
                        "}", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        return response;
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

}
