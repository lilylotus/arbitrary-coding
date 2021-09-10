package cn.nihility.rpc.client;


import cn.nihility.rpc.common.Beat;
import cn.nihility.rpc.common.codec.RpcRequest;
import cn.nihility.rpc.common.codec.RpcResponse;
import cn.nihility.rpc.common.conn.ConnectionManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger log = LoggerFactory.getLogger(RpcClientHandler.class);

    private final Map<String, RpcFuture> pendingRpc = new ConcurrentHashMap<>();
    private SocketAddress remoteAddress;
    private Channel channel;
    private RpcProtocol rpcProtocol;

    public RpcFuture sendRequest(RpcRequest request) {
        RpcFuture rpcFuture = new RpcFuture(request);
        pendingRpc.put(request.getRequestId(), rpcFuture);
        rpcFuture.addCallback(new AsyncRpcCallbackLogger());
        try {
            ChannelFuture channelFuture = channel.writeAndFlush(request).sync();
            if (!channelFuture.isSuccess()) {
                log.error("Send request {} error", request.getRequestId());
            }
        } catch (InterruptedException e) {
            log.error("Send request [{}] exception", request, e);
        }
        return rpcFuture;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        log.info("response [{}]", response);

        final String id = response.getRequestId();
        final RpcFuture rpcFuture = pendingRpc.get(id);
        if (null != rpcFuture) {
            pendingRpc.remove(id);
            rpcFuture.done(response);
        } else {
            log.warn("Can not get pending response for request [{}]", id);
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        remoteAddress = ctx.channel().remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        channel = ctx.channel();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Client caught exception", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            //Send ping
            sendRequest(Beat.BEAT_PING);
            log.debug("Client send beat-ping to [{}]", remoteAddress);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connect to [{}] inactive", remoteAddress);
        super.channelInactive(ctx);
        ConnectionManager.getInstance().removeHandler(rpcProtocol);
    }

    public RpcProtocol getRpcProtocol() {
        return rpcProtocol;
    }

    public void setRpcProtocol(RpcProtocol rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }

}
