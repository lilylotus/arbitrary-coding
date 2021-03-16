package cn.nihility.nettry;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * Netty Http Server 针对不同 request 方法的处理器
 */
public interface NettyRequestHandler {

    /**
     * 处理对应 uri 请求路径请求的处理器
     * @param ctx Netty Http 请求上下文
     * @param httpRequest 请求
     */
    void handle(ChannelHandlerContext ctx, FullHttpRequest httpRequest);

    /**
     * 该请求处理器对应要处理的 uri
     * @return 该请求处理器对应的 uri 路径
     */
    String uri();

    void doGet(FullHttpRequest httpRequest, FullHttpResponse response);

    void doPost(FullHttpRequest httpRequest, FullHttpResponse response);

    void doPut(FullHttpRequest httpRequest, FullHttpResponse response);

    void doDelete(FullHttpRequest httpRequest, FullHttpResponse response);

    void doOther(FullHttpRequest httpRequest, FullHttpResponse response);

}
