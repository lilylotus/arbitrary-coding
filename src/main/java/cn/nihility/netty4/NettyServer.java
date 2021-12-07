package cn.nihility.netty4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    /**
     * netty server bootstrap.
     */
    private ServerBootstrap bootstrap;
    /**
     * the boss channel that receive connections and dispatch these to worker channel.
     */
    private Channel channel;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Map<String, Channel> channels;

    /**
     * ip:port
     */
    private final String url;

    public NettyServer(String url) {
        this.url = url;
    }


    protected void doOpen() throws Throwable {
        bootstrap = new ServerBootstrap();

        bossGroup = NettyEventLoopFactory.eventLoopGroup(1, "NettyServerBoss");
        workerGroup = NettyEventLoopFactory.eventLoopGroup(
            Math.min(Runtime.getRuntime().availableProcessors() + 1, 32),
            "NettyServerWorker");

        final NettyServerHandler serverHandler = new NettyServerHandler(url);
        channels = serverHandler.getChannels();

        boolean keepalive = Boolean.parseBoolean(Objects.toString(System.getenv(CommonConstant.KEEP_ALIVE_KEY), "false"));

        bootstrap.group(bossGroup, workerGroup)
            .channel(NettyEventLoopFactory.serverSocketChannelClass())
            .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
            .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
            .childOption(ChannelOption.SO_KEEPALIVE, keepalive)
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {

                    NettyCodecAdapter adapter = new NettyCodecAdapter(new JacksonCodec());

                    ch.pipeline()
                        .addLast("decoder", adapter.getDecoder())
                        .addLast("encoder", adapter.getEncoder())
                        .addLast("server-idle-handler", new IdleStateHandler(0, 0,
                            CommonConstant.DEFAULT_HEARTBEAT * 3, MILLISECONDS))
                        .addLast("handler", serverHandler);

                }
            });

        String[] address = url.split(":");
        ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(address[0], Integer.parseInt(address[1])));
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();

    }

    protected void doClose() {
        try {
            if (channel != null) {
                // unbind.
                channel.close();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (bootstrap != null) {
                bossGroup.shutdownGracefully().syncUninterruptibly();
                workerGroup.shutdownGracefully().syncUninterruptibly();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (channels != null) {
                channels.forEach((k, v) -> v.close());
                channels.clear();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }


}
