package cn.nihility.nettry;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.List;

/**
 * Netty Http Server url 处理操作的工厂类，针对不同的 URI 调用不同的处理器
 */
public class NettyRequestHandlerFactory {

    private static List<NettyRequestHandler> cachedHandlerList;

    public static NettyRequestHandler create(String uri) {
        if (cachedHandlerList == null) {
            synchronized (NettyRequestHandlerFactory.class) {
                if (null == cachedHandlerList) {
                    cachedHandlerList = NettyHttpServerConfig.getBeansByType(NettyRequestHandler.class);
                }
            }
        }

        NettyRequestHandler handler = null;
        for (NettyRequestHandler reqHandler : cachedHandlerList) {
            if (reqHandler.uri().equals(NettyHttpUtil.getOriginUri(uri))) {
                handler = reqHandler;
                break;
            }
        }

        if (null == handler) {
            handler = new AbstractNettyRequestHandler() {
                @Override
                public void handle(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
                    FullHttpResponse response = NettyHttpUtil.notFound404(httpRequest.uri());
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                }

                @Override
                public String uri() {
                    return "";
                }
            };
        }

        return handler;
    }

}
