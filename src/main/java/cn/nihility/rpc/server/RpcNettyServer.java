package cn.nihility.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcNettyServer implements IRpcServer {

    private static final Logger log = LoggerFactory.getLogger(RpcNettyServer.class);

    private Thread serverThread;
    private final RpcNettyInnerServer rpcServer;

    public RpcNettyServer(String serverAddress) {
        this.rpcServer = new RpcNettyInnerServer(serverAddress);
    }

    @Override
    public void start() {
        serverThread = new Thread(rpcServer::start);
        serverThread.start();
    }

    @Override
    public void shutdown() {
        rpcServer.shutdown();
        // 等待 10 秒，还没关闭完成，强制关闭
        if (null != serverThread && serverThread.isAlive()) {
            log.info("强制关闭 netty server");
            serverThread.interrupt();
        }
    }

    static class RpcNettyInnerServer implements IRpcServer {

        /**
         * server address - ip:port
         */
        private final String serverAddress;
        private ChannelFuture serverChannelFuture;

        RpcNettyInnerServer(String serverAddress) {
            this.serverAddress = serverAddress;
        }

        @Override
        public void start() {
            final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            final EventLoopGroup workerGroup = new NioEventLoopGroup(4);

            final ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new RpcServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            final String[] split = serverAddress.split(":");
            final String ip = split[0];
            final int port = Integer.parseInt(split[1]);

            try {
                serverChannelFuture = serverBootstrap.bind(ip, port).sync();
                log.info("RPC Netty Server Start on [{}]", serverAddress);
                serverChannelFuture.channel().closeFuture().sync();
                log.info("RPC Netty Server prepare shutdown.");
            } catch (InterruptedException e) {
                log.error("Start RPC Netty Server [{}] stopped", serverAddress, e);
            } catch (Exception ex) {
                log.error("Start RPC Netty Server [{}] case exception", serverAddress, ex);
            } finally {
                try {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }

        @Override
        public void shutdown() {
            if (serverChannelFuture != null) {
                serverChannelFuture.channel().close()
                        .addListener(future -> log.info("shutdown netty server [{}] success.", serverAddress));
            }
        }
    }

    public static void main(String[] args) {
        final RpcNettyServer server = new RpcNettyServer("10.0.41.80:50090");
        server.start();
        System.out.println("Server running");
    }

}
