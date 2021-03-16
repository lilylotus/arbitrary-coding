package cn.nihility.nettry;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty Http Server 针对各个 URL 请求的处理器
 */
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(NettyHttpServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        NettyRequestHandler handler = NettyRequestHandlerFactory.create(request.uri());
        handler.handle(ctx, request);

        /*ctx.write(response).addListener(ChannelFutureListener.CLOSE);*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Netty Http Server 捕获异常", cause);
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}
