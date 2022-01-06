package cn.nihility.nio.pro;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class HighSocketMultiClient implements Runnable {

    private final Map<String, ClientSocketHandler> socketContainerMap = new HashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final int serverPort;
    private final String serverHost;
    private final int clientCount;
    private Selector selector;

    public HighSocketMultiClient(String serverHost, int serverPort, int clientCount) {
        this.serverPort = serverPort;
        this.serverHost = serverHost;
        this.clientCount = clientCount;
    }

    public static void main(String[] args) {

        new Thread(new HighSocketMultiClient("127.0.0.1", 2333, 30)).start();

    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            System.out.println("创建 Selector 异常");
            e.printStackTrace();
            return;
        }

        Thread exitThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                if (scanner.hasNextLine()) {
                    String next = scanner.next();
                    System.out.println("输入 = " + next);
                    if ("exit".equals(next) || "e".equals(next)) {
                        running.compareAndSet(true, false);
                    }
                }
            }
        });
        exitThread.setDaemon(true);
        exitThread.start();

        new Thread(new ClientSocketConnector(serverHost, serverPort, selector, clientCount)).start();

        try {
            int selectCount;
            SelectionKey selectionKey;

            while (running.get()) {
                selectCount = selector.select(500L);
                if (selectCount > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    for (SelectionKey key : selectionKeys) {
                        selectionKey = key;
                        if (selectionKey.isValid()) {
                            dispatchSelectKey(selectionKey);
                        }
                    }
                    selectionKeys.clear();
                } else {
                    System.out.println("Client Count = " + socketContainerMap.size());
                }
            }

            for (ClientSocketHandler handler : socketContainerMap.values()) {
                handler.close();
            }

            selector.close();
        } catch (IOException e) {
            System.out.println("select 出现异常，socket 关闭");
            e.printStackTrace();
        }

        System.out.println("服务关闭");

    }

    public void dispatchSelectKey(SelectionKey selectionKey) {
        SocketChannel sc = null;
        try {
            if (selectionKey.isReadable()) {
                sc = (SocketChannel) selectionKey.channel();
                final SocketChannel socketChannel = sc;
                ClientSocketHandler handler = socketContainerMap.computeIfAbsent(Integer.toString(sc.hashCode()),
                        key -> new ClientSocketHandler(selector, socketChannel));
                handler.handleReadableEvent(sc, selectionKey);
            } else if (selectionKey.isConnectable()) {
                sc = (SocketChannel) selectionKey.channel();
                // 连接完成（与服务端的三次握手完成）
                if (sc.finishConnect()) {
                    System.out.println("连接到服务器 [" + sc.getRemoteAddress() + "]");
                }
                //sc.configureBlocking(true);
                sc.register(selector, SelectionKey.OP_READ);
                socketContainerMap.put(Integer.toString(sc.hashCode()), new ClientSocketHandler(selector, sc));
            }
        } catch (IOException e) {
            String sk = Integer.toString(sc.hashCode());
            System.out.println(String.format("处理 SelectionKey 异常, socket<%s> 退出", sk));
            socketContainerMap.remove(Integer.toString(sc.hashCode()));

            try {
                selectionKey.cancel();
                sc.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    public void socketConnector() {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            System.out.println("连接到服务器 [" + serverHost + ":" + serverPort + "] 异常");
            e.printStackTrace();
        }
    }


}
