package cn.nihility.nio.single.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 模块只负责处理连接就绪事件，有了这个事件就可以拿到客户单的 SocketChannel，就可以继续完成接下来的读写任务了
 */
public class Acceptor implements Runnable {

    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;

    Acceptor(ServerSocketChannel serverSocketChannel, Selector selector) {
        this.serverSocketChannel = serverSocketChannel;
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                System.out.println(String.format("收到来自 %s 的连接",
                        socketChannel.getRemoteAddress()));
                // 这里把客户端通道传给 Handler，Handler 负责接下来的事件处理（除了连接事件以外的事件均可）
                new ReactorServerHandler(socketChannel, selector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
