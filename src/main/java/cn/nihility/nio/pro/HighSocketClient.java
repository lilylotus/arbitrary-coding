package cn.nihility.nio.pro;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class HighSocketClient implements Runnable {

    private int serverPort;
    private String serverHost;

    private AtomicBoolean running = new AtomicBoolean(true);

    public HighSocketClient(String serverHost, int serverPort) {
        this.serverPort = serverPort;
        this.serverHost = serverHost;
    }

    public static void multiClientStart(int clientCount) throws IOException {

        final Selector selector = Selector.open();
        final AtomicBoolean running = new AtomicBoolean(true);
        Thread exitThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                if (scanner.hasNextLine()) {
                    String next = scanner.next();
                    System.out.println("输入 = " + next);
                    if ("exit".equals(next) || "e".equals(next)) {
                        running.compareAndSet(true, false);
                        try {
                            selector.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        exitThread.setDaemon(true);
        exitThread.start();

        ExecutorService service = Executors.newFixedThreadPool(100);
        for (int i = 0; i < clientCount; i++) {
            service.submit(new HighSocketClient("127.0.0.1", 2333));
            try {
                Thread.sleep(20L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        service.shutdown();

    }

    public static void main(String[] args) {
        try {
            multiClientStart(10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main2(String[] args) {
        new HighSocketClient("127.0.0.1", 2333).run();
        //String sendMsg = "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678|";
        /*String sendMsg = "0123456789|";
        byte[] array = new byte[60];
        byte[] bs = sendMsg.getBytes();
        int len = bs.length;

        array[59] = ':';

        int posit = 0;
        System.arraycopy(bs, 0, array, posit, len);
        posit += len;
        System.arraycopy(bs, 0, array, posit, len);
        int posit1 = posit += len;

        for (int i = 0; i < len; i++) {
            array[posit1 + i] = '*';
        }
        int posit2 = posit1 + len;

        System.out.println(new String(array, 0, posit));
        System.out.println(new String(array));

        System.out.println("p2 = " + posit2);
        System.arraycopy(array, posit1, array, 0, posit2 - posit1);
        System.out.println(new String(array));


        byte[] bs2 = "12345678910111213141516171819202122232425262728293031323334353637383940".getBytes();
        System.arraycopy(bs2, 25, bs2, 0, 46);
        System.out.println(new String(bs2));*/


        /*String sendMsg = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
        byte[] sendBytes = sendMsg.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        try {
            os.write(int2byte(sendBytes.length));
            os.write(sendBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] bytes = os.toByteArray();
        System.out.println(new String(bytes));*/

    }

    @Override
    public void run() {
        SocketChannel socketChannel;
        ClientSocketHandler clientSocketHandler;
        Selector selector;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            clientSocketHandler = new ClientSocketHandler(selector, socketChannel);

            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            System.out.println("连接到服务器 [" + serverHost + ":" + serverPort + "] 异常");
            e.printStackTrace();
            return;
        }

        try {
            int selectCount;
            SelectionKey selectionKey;

            while (running.get()) {
                selectCount = selector.select(500L);
                if (selectCount > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        selectionKey = iterator.next();
                        iterator.remove();
                        if (selectionKey.isValid()) {
                            clientSocketHandler.dispatchSelectKey(selectionKey);
                        }
                    }
                    selectionKeys.clear();
                }
            }

            selector.close();
            socketChannel.close();

        } catch (IOException e) {
            System.out.println("select 出现异常，socket 关闭");
            e.printStackTrace();
        }
        System.out.println(socketChannel.hashCode() + " 服务关闭");

    }

}
