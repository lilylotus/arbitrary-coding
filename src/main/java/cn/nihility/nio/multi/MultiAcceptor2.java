package cn.nihility.nio.multi;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class MultiAcceptor2 implements Runnable {

    private final Selector selector;
    private final SelectionKey selectionKey;

    MultiAcceptor2(Selector selector, SelectionKey selectionKey) {
        this.selector = selector;
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            // 接收客户端连接
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (null != socketChannel) {
                // 置为非阻塞模式（selector 仅允非阻塞模式）
                socketChannel.configureBlocking(false);
                // 注册读事件
                socketChannel.register(selector, SelectionKey.OP_READ);

                System.out.println(String.format("收到来自 %s 的连接",
                        socketChannel.getRemoteAddress()));
            } else {
                System.out.println("建立连接失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
