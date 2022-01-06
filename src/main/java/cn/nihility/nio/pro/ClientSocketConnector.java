package cn.nihility.nio.pro;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ClientSocketConnector implements Runnable {

    private String serverHost;
    private int serverPort;
    private Selector selector;
    private int clientCount;

    public ClientSocketConnector(String serverHost, int serverPort,
                                 Selector selector, int clientCount) {
        this.serverPort = serverPort;
        this.serverHost = serverHost;
        this.selector = selector;
        this.clientCount = clientCount;
    }

    @Override
    public void run() {
        for (int i = 0; i < clientCount; i++) {
            socketConnector(i);
            try {
                Thread.sleep(10L);
            } catch (InterruptedException ignore) {
            }
        }
    }

    public void socketConnector(int index) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            System.out.println(index + " : 连接到服务器 [" + serverHost + ":" + serverPort + "] 异常");
            e.printStackTrace();
        }
    }

}
