package cn.nihility.nettry;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.AsciiString;

public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final AsciiString CONNECTION = AsciiString.cached("Connection");
    public static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");
    public static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    public static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NettyHttpServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        NettyRequestHandler handler = NettyRequestHandlerFactory.create(request.uri());
        FullHttpResponse response = handler.handle(request);

        ctx.write(response).addListener(ChannelFutureListener.CLOSE);

        /*boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }*/
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
