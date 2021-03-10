package cn.nihility.nettry;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.HashMap;

public class NettyRequestHandlerFactory {

    public static final java.util.Map<String, NettyRequestHandler> REQUEST_HANDLERS = new HashMap<>();

    static {
        REQUEST_HANDLERS.put("/test", new NettyTestUriHandler());
    }

    public static NettyRequestHandler create(String uri) {
        NettyRequestHandler handler = REQUEST_HANDLERS.get(NettyHttpUtil.getOriginUri(uri));
        if (null == handler) {
            handler = new NettyRequestHandler() {
                @Override
                public FullHttpResponse handle(FullHttpRequest httpRequest) {
                    return NettyHttpUtil.notFound404(httpRequest.uri());
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
