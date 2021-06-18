package cn.nihility.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty Http Server 的启动类
 */
public class NettyHttpServer {

    private static final Logger log = LoggerFactory.getLogger(NettyHttpServer.class);

    private int port;

    public NettyHttpServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        int port = 8089;
        try {
            new NettyHttpServer(port).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        /*
        * NioEventLoopGroup 是一个处理 I/O 操作的多线程事件循环，EventLoopGroup 负责接收传入连接
        * EventLoopGroup 只负责接收客户端的连接，不做复杂操作，为了减少资源占用，取值越小越好
        * Group：群组，Loop：循环，Event：事件，这几个东西联在一起，相比大家也大概明白它的用途了。
        * Netty 内部都是通过线程在处理各种数据
        * EventLoopGroup 就是用来管理调度它们
        * 注册 Channel，管理它们的生命周期
        * */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // workerGroup 作为 worker，处理 EventLoopGroup 接收的连接的流量和将接收的连接注册进入这个 worker
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // ServerBootstrap 负责建立服务端
            // 可以直接使用 Channel 去建立服务端，但是大多数情况下你无需做这种乏味的事情
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    // 指定使用 NioServerSocketChannel 产生一个 Channel 用来接收连接
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // ChannelInitializer 用于配置一个新的 Channel
                    // 用于向你的 Channel 当中添加 ChannelInboundHandler 的实现
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        public void initChannel(SocketChannel ch) throws Exception {
                            // server 端接收到的是 httpRequest，所以要使用 HttpRequestDecoder 进行解码
                            ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                            // server 端发送的是 httpResponse，所以要使用 HttpResponseEncoder 进行编码
                            ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                            // 将多个消息转换为单一的 FullHttpRequest 或 FullHttpResponse 对象
                            ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
                            // 解决大数据包传输问题，用于支持异步写大量数据流并且不需要消耗大量内存也不会导致内存溢出错误( OutOfMemoryError )
                            // 仅支持 ChunkedInput 类型的消息。也就是说，仅当消息类型是 ChunkedInput 时才能实现 ChunkedWriteHandler 提供的大数据包传输功能
                            // 解决大码流的问题
                            ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                            ch.pipeline().addLast("http-handler", new NettyHttpServerHandler());

                            // HTTPS 配置
                            /*SelfSignedCertificate ssc = new SelfSignedCertificate();
                            SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
                            ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));*/
                        }

                    })
                    // TCP 默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。
                    // TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，
                    // 服务器处理创建新连接较慢，可以适当调大这个参数
                    // 注意以下是 socket 的标准参数
                    // BACKLOG 用于构造服务端套接字 ServerSocket 对象，标识当服务器请求处理线程全满时，
                    // 用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于 1，Java 将使用默认值 50。
                    // Option 是为了 NioServerSocketChannel 设置的，用来接收传入连接的
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 是否开启 TCP 底层心跳机制 (是否启用心跳保活机制)
                    // 双方 TCP 套接字建立连接后（即都进入 ESTABLISHED 状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
                    // childOption 是用来给父级 ServerChannel 之下的 Channels 设置参数的
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定并开始接受传入的连接。b.bind(PORT).sync().channel();
            Channel channel = b.bind(port).sync().channel();
            log.info("Netty Http Server 启动，端口 [{}]", port);

            /*
            * 等到服务器 socket 关闭， 在这个例子中，这种情况不会发生，但你可以优雅的做到这一点关闭服务器
            * sync() 会同步等待连接操作结果，用户线程将在此 wait()，直到连接操作完成之后，线程被 notify(),用户代码继续执行
            * closeFuture() 当 Channel 关闭时返回一个 ChannelFuture,用于链路检测
            * */
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Netty Http Server 运行时遇到异常", e);
        } finally {
            log.info("Netty Http Server 关闭");
            // 资源优雅释放
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
