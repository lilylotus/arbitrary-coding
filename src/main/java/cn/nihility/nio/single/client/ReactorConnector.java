package cn.nihility.nio.single.client;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * 负责建立连接
 */
public class ReactorConnector implements Runnable {

    private final Selector selector;
    private final SocketChannel socketChannel;

    ReactorConnector(SocketChannel socketChannel, Selector selector) {
        this.socketChannel = socketChannel;
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            if (socketChannel.finishConnect()) { //这里连接完成（与服务端的三次握手完成）
                System.out.println(String.format("已完成 %s 的连接",
                        socketChannel.getRemoteAddress()));
                // 连接建立完成后，接下来的动作交给Handler去处理（读写等）
                new ReactorClientHandler(socketChannel, selector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("ReactorConnector 线程关闭");
    }
}
