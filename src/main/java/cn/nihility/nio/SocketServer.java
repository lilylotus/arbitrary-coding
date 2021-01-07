package cn.nihility.nio;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SocketServer {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private static Map<Integer, String> PATH_MAP = new ConcurrentHashMap<>();

    private int port;

    public SocketServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        SocketServer server = new SocketServer(4000);
        server.start();
        server.shutdown();
    }

    public void start() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
//                System.out.println("Start Select");
                int select = selector.select();
//                System.out.println("End Select");

                if (select > 0) {
                    System.out.println();
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();

                        try {
                            handle(key);
                        } catch (Exception ex) {
                            ex.printStackTrace();

                            if (null != key) {
                                key.cancel();
                                if (key.channel() != null) {
                                    try {
                                        key.channel().close();
                                    } catch (IOException e) {
                                        System.out.println("key channel close ex " + e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    System.out.print(".");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handle(SelectionKey key) throws IOException {

        if (key.isValid() && key.isAcceptable()) {
            handleConnection(key);
        }

        if (key.isValid() && key.isReadable()) {
            handleRead(key);
        }


    }

    private void handleRead(SelectionKey key) throws IOException {
        System.out.println("-------------------------------------------");
        System.out.println("Starting Read ..., key Hash [" + key.hashCode() + "]");

        PATH_MAP.putIfAbsent(key.hashCode(), Long.toString(Instant.now().toEpochMilli()));

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        SocketChannel client = (SocketChannel) key.channel();

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("D:\\tmp\\nio\\" + PATH_MAP.get(key.hashCode()) + ".txt", true));
        int len;

        while ((len = client.read(byteBuffer)) > 0) {
            byteBuffer.flip();
            byte[] array = byteBuffer.array();
            String rec = new String(array, 0, len);
//            System.out.println("Rec [" + rec + "] len [" + len + "]");
            if (rec.contains("Hello")) {
                System.out.println("Rec [" + rec + "] len [" + len + "]");
            } else {
                System.out.println("Rec len [" + len + "]");
            }

            bos.write(array, 0, len);
            /*String rep = "REP: [" + rec + "]";
            client.write(ByteBuffer.wrap(rep.getBytes()));*/

            byteBuffer.clear();
        }

        bos.close();

        if (len == -1) {
            System.out.println("Close Client");
            client.close();
        }

        System.out.println("Starting Read Over");
    }

    private void handleConnection(SelectionKey key) throws IOException {
        System.out.println("Handle Connection, Key Hash [" + key.hashCode() + "]");

        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);

        if (client.isConnected()) {
            System.out.println("Connection Success.");
            client.write(ByteBuffer.wrap("Welcome".getBytes()));
        }

        System.out.println("Client Hash [" + client.hashCode() + "]");

        client.register(selector, SelectionKey.OP_READ);

        System.out.println("Handle Connection Over");
    }

    public void shutdown() {
        if (null != selector) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (null != serverSocketChannel) {
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
