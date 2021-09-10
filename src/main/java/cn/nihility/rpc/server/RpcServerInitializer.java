package cn.nihility.rpc.server;

import cn.nihility.rpc.common.Beat;
import cn.nihility.rpc.common.codec.RpcDecoder;
import cn.nihility.rpc.common.codec.RpcEncoder;
import cn.nihility.rpc.common.codec.RpcRequest;
import cn.nihility.rpc.common.codec.RpcResponse;
import cn.nihility.rpc.common.serialize.jackson.JacksonSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        pipeline.addLast(new RpcDecoder(RpcRequest.class, new JacksonSerializer()));
        pipeline.addLast(new RpcEncoder(RpcResponse.class, new JacksonSerializer()));
        pipeline.addLast(new RpcServerHandler());
    }

}
