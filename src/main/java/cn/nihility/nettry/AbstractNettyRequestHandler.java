package cn.nihility.nettry;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Netty Http Server 针对请求的处理模板
 */
public abstract class AbstractNettyRequestHandler implements NettyRequestHandler {

    /**
     * Netty Http Server 针对 Request 的请求处理
     * 默认已经添加的参数：
     * 针对 Response:
     *      HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8"
     *      HttpResponseStatus -> HttpResponseStatus.OK
     *      HttpVersion -> HttpVersion.HTTP_1_1
     * @param ctx Netty Http 请求上下文
     * @param httpRequest 请求
     */
    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {

        String methodName = httpRequest.method().name();
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK);

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        //response.headers().set(HttpHeaderNames.CONNECTION, "close");
        response.headers().set(HttpHeaderNames.CONNECTION, "keep-alive");
        response.headers().set(HttpHeaderNames.DATE, LocalDateTime.now());
        //response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, "chunked");

        if (HttpMethod.POST.name().equals(methodName)) {
            doPost(httpRequest, response);
        } else if (HttpMethod.GET.name().equals(methodName)) {
            doGet(httpRequest, response);
        } else if (HttpMethod.PUT.name().equals(methodName)) {
            doPut(httpRequest, response);
        } else if (HttpMethod.DELETE.name().equals(methodName)) {
            doDelete(httpRequest, response);
        } else {
            doOther(httpRequest, response);
        }

        response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, "chunked");
        //response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        //ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        ctx.writeAndFlush(response);

    }

    @Override
    public void doGet(FullHttpRequest httpRequest, FullHttpResponse response) {

    }

    @Override
    public void doPost(FullHttpRequest httpRequest, FullHttpResponse response) {

    }

    @Override
    public void doPut(FullHttpRequest httpRequest, FullHttpResponse response) {

    }

    @Override
    public void doDelete(FullHttpRequest httpRequest, FullHttpResponse response) {

    }

    @Override
    public void doOther(FullHttpRequest httpRequest, FullHttpResponse response) {

    }

    /**
     * 获取最原始的 uri 路径，去除掉 uri 路径中的参数部分
     * @param uri 请求 uri 路径
     * @return 没有参数部分的 uri 路径
     */
    protected String getOriginUri(String uri) {
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

    protected String getContentType(HttpHeaders headers) {
        String typeStr = headers.get("Content-Type");
        if (!StringUtils.isEmpty(typeStr)) {
            String[] list = typeStr.split(";");
            typeStr = list[0];
        }
        return typeStr;
    }

}
