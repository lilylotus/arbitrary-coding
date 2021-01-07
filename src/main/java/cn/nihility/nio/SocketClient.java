package cn.nihility.nio;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketClient {

    private SocketChannel socketChannel;
    private int port;
    private String name;

    public SocketClient(int port) {
        this.port = port;
    }

    public SocketClient(int port, String name) {
        this.port = port;
        this.name = name;
    }

    public static void main(String[] args) {
        /*SocketClient client = new SocketClient(4000);
        client.start();*/

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 4; i++) {
            final String index = Integer.toString(i);
            executorService.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new SocketClient(4000, ("index:" + index)).start();
            });
        }
        executorService.shutdown();
    }

    public void shutdown() {
        if (null != socketChannel) {
            try {
                socketChannel.close();
                System.out.println("Shutdown Client Socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        try {
//            socketChannel = SocketChannel.open();
            socketChannel = SocketChannel.open(new InetSocketAddress(port));
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            byteBuffer.put(("Hello : " + name).getBytes());
            byteBuffer.flip();

            socketChannel.write(byteBuffer);
            byteBuffer.clear();

            String file = "D:\\tmp\\rolling-file-warn.log";

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer =new byte[1024];
            int len;
            int sum = 0, count = 0;

            while ((len = bis.read(buffer)) != -1) {
                byteBuffer.put(buffer, 0, len);
                byteBuffer.flip();

                sum += len;
                count++;

                System.out.println("name : [" + name + "] Read len [" + len + "]");
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
            }
            bis.close();
            System.out.println("name : [" + name + "] Sum [" + (sum / 1024) + "] KB, Count [" + count + "]");

            System.out.println("Starting Read");
            len = socketChannel.read(byteBuffer);
            byteBuffer.flip();
            byte[] array = byteBuffer.array();
            String rec = new String(array, 0, len);
            System.out.println("name : [" + name + "] Rec [" + rec + "] len [" + len + "]");

            byteBuffer.clear();


            if (len == -1) {
                System.out.println("Close Client");
                socketChannel.close();
            }
            System.out.println("Starting Read Over");

            shutdown();


//            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            // 发起连接
//            socketChannel.connect(new InetSocketAddress(port));

            /*int loop = 0;

            while (true) {
                int select = selector.select(500);
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

                loop++;

                if (loop == 50) {
                    shutdown();
                }
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handle(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isValid() && selectionKey.isConnectable()) {
            handleConnection(selectionKey);
        }

        if (selectionKey.isValid() && selectionKey.isReadable()) {
            handleRead(selectionKey);
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        System.out.println("Starting Read ..., key Hash [" + key.hashCode() + "]");

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        SocketChannel client = (SocketChannel) key.channel();

        int len;

        while ((len = client.read(byteBuffer)) > 0) {
            byteBuffer.flip();
            byte[] array = byteBuffer.array();
            String rec = new String(array, 0, len);
            System.out.println("Rec [" + rec + "] len [" + len + "]");

            String rep = "REP: [" + rec + "]";
            client.write(ByteBuffer.wrap(rep.getBytes()));

            byteBuffer.clear();
        }

        if (len == -1) {
            System.out.println("Close Client");
            client.close();
        }

        System.out.println("Starting Read Over");

        //shutdown();
    }

    private void handleConnection(SelectionKey selectionKey) throws IOException {
        System.out.println("handle connection");

        // 完成连接
        if (socketChannel.isConnectionPending()) {
            System.out.println("Connection is Pending");
            if (socketChannel.finishConnect()) {
                System.out.println("Finished Connection.");

                socketChannel.write(ByteBuffer.wrap("Hello".getBytes()));

                /*doResponseContent(sc, "Connect To Multiplex Server.");*/
//                socketChannel.register(selector, SelectionKey.OP_READ);
            } else {
                System.out.println("Connection To Multiplex Server Error.");
                System.exit(1);
            }
        }

        System.out.println("handle connection over");
    }
}
