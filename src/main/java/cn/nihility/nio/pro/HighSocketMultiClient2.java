package cn.nihility.nio.pro;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HighSocketMultiClient2 implements Runnable {

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicInteger atomicClientCount = new AtomicInteger(0);
    private final int serverPort;
    private final String serverHost;
    private final int clientCount;
    private Selector selector;

    public HighSocketMultiClient2(String serverHost, int serverPort, int clientCount) {
        this.serverPort = serverPort;
        this.serverHost = serverHost;
        this.clientCount = clientCount;
    }

    public static void main(String[] args) {

//        new Thread(new HighSocketMultiClient("8.217.47.93", 50080, 1)).start();
//        new Thread(new HighSocketMultiClient("192.168.10.6", 50080, 10)).start();
        new Thread(new HighSocketMultiClient2("127.0.0.1", 50080, 100)).start();

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
                    System.out.println("Client Count = " + atomicClientCount.get());
                }
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
                ClientSocketHandler handler = (ClientSocketHandler) selectionKey.attachment();
                handler.handleReadableEvent(sc, selectionKey);
            } else if (selectionKey.isConnectable()) {
                sc = (SocketChannel) selectionKey.channel();
                // 连接完成（与服务端的三次握手完成）
                if (sc.finishConnect()) {
                    System.out.println("连接到服务器 [" + sc.getRemoteAddress() + "]");
                }
                SelectionKey sk = sc.register(selector, SelectionKey.OP_READ);
                sk.attach(new ClientSocketHandler(selector, sc));
                int old = atomicClientCount.get();
                atomicClientCount.compareAndSet(old, old + 1);
            }
        } catch (IOException e) {
            System.out.println(String.format("处理 SelectionKey 异常, socket<%d> 退出", sc.hashCode()));

            int old = atomicClientCount.get();
            atomicClientCount.compareAndSet(old, old - 1);

            try {
                selectionKey.cancel();
                sc.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
