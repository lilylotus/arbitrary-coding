package cn.nihility.nio.pro;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class HighSocketClient implements Runnable {

    private static final int MESSAGE_LEN_SIZE = 4;

    private int serverPort;
    private String serverHost;

    private byte[] globalReadBuffer = new byte[102400];
    private int globalBufferPosition = 0;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(2048);

    private java.nio.channels.Selector selector;
    private AtomicBoolean running = new AtomicBoolean(true);

    public HighSocketClient(String serverHost, int serverPort) {
        this.serverPort = serverPort;
        this.serverHost = serverHost;
    }

    /**
     * 将 byte[] 转换为 int 类型
     */
    public static int byte2int(byte[] res) {
        // 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000
        return (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位或
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
    }

    /**
     * 将 int 类型装换为 byte[] 数组
     */
    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }

    public static void main(String[] args) {
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
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            System.out.println("连接到服务器 [" + serverHost + ":" + serverPort + "] 异常");
            e.printStackTrace();
            return;
        }

        Thread exitThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                if (scanner.hasNextLine()) {
                    String next = scanner.next();
                    System.out.println("输入 = " + next);
                    if ("exit".equals(next)) {
                        running.compareAndSet(true, false);
                    }
                }
            }
        });
        exitThread.setDaemon(true);
        exitThread.start();

        try {
            int selectCount;
            SelectionKey selectionKey;

            while (running.get()) {
                selectCount = selector.select(500L);
                if (selectCount > 0) {
                    java.util.Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        selectionKey = iterator.next();
                        iterator.remove();
                        if (selectionKey.isValid()) {
                            dispatchSelectKey(selectionKey);
                        }
                    }
                    selectionKeys.clear();
                }
            }

            socketChannel.close();
            selector.close();

        } catch (IOException e) {
            System.out.println("select 出现异常，socket 关闭");
            e.printStackTrace();
        }
        System.out.println("服务关闭");

    }

    private void dispatchSelectKey(SelectionKey selectionKey) {
        try {
            if (selectionKey.isReadable()) {
                SocketChannel sc = (SocketChannel) selectionKey.channel();
                handleReadableEvent(sc);
                selectionKey.interestOps(SelectionKey.OP_READ);
            } else if (selectionKey.isConnectable()) {
                SocketChannel sc = (SocketChannel) selectionKey.channel();
                // 连接完成（与服务端的三次握手完成）
                if (sc.finishConnect()) {
                    System.out.println("连接到服务器 [" + sc.getRemoteAddress() + "]");
                }
                //sc.configureBlocking(true);
                sc.register(selector, SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            System.out.println("处理 Socket 事件异常");
            running.compareAndSet(true, false);
            e.printStackTrace();
        }
    }

    private void handleReadableEvent(SocketChannel socketChannel) throws IOException {

        while (running.get()) {
            // 切换成 buffer 的写模式，用于让通道将自己的内容写入到 buffer 里
            byteBuffer.clear();
            int len = socketChannel.read(byteBuffer);
            byteBuffer.flip();

            if (len > 0) {
                /*if (len % 104 != 0) {
                    System.out.println("缓存中数据 = " + new String(globalReadBuffer, 0, globalBufferPosition));
                    System.out.println("数据缓存中的数据长度 = " + globalBufferPosition + ", 本次消息 = " +
                            new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit()));
                }*/

                // 放到读取的 2 级缓存当中
                System.arraycopy(byteBuffer.array(), byteBuffer.position(), globalReadBuffer, globalBufferPosition, byteBuffer.limit());
                globalBufferPosition = byteBuffer.limit() + globalBufferPosition;

                while (globalBufferPosition >= MESSAGE_LEN_SIZE) {
                    // 获取本次消息的数据长度
                    byte[] intBytes = new byte[MESSAGE_LEN_SIZE];
                    System.arraycopy(globalReadBuffer, 0, intBytes, 0, MESSAGE_LEN_SIZE);
                    int msgLen = byte2int(intBytes);
                    //System.out.println("消息体 bytes 长度 = " + msgLen);
                    if (msgLen == 0) {
                        for (int i = 0; i < MESSAGE_LEN_SIZE; i++) {
                            globalReadBuffer[i] = globalReadBuffer[MESSAGE_LEN_SIZE + i];
                        }
                        globalBufferPosition = globalBufferPosition - MESSAGE_LEN_SIZE;
                        continue;
                    }

                    int dataBytesLen = msgLen + MESSAGE_LEN_SIZE;
                    if (dataBytesLen <= globalBufferPosition) {
                        byte[] content = new byte[msgLen];
                        System.arraycopy(globalReadBuffer, MESSAGE_LEN_SIZE, content, 0, msgLen);

                        String msg = new String(content);
                        //System.out.println(String.format("接收消息内容: [%d]:[%s]", msgLen, msg));

                        // 重置二级缓存数据，还剩余的字节数量
                        globalBufferPosition = globalBufferPosition - dataBytesLen;
                        if (globalBufferPosition > 0) {
                            for (int i = 0; i < globalBufferPosition; i++) {
                                globalReadBuffer[i] = globalReadBuffer[dataBytesLen + i];
                            }
                            /*int newPosition = globalBufferPosition - 1;
                            byte[] tmpArray = new byte[globalBufferPosition];
                            System.arraycopy(globalReadBuffer, newPosition , tmpArray, 0, globalBufferPosition);
                            System.arraycopy(tmpArray, 0, globalReadBuffer, 0, globalBufferPosition);*/
                        }
                    } else {
                        System.out.println("数据长度不够， 需要数据长度 = " + dataBytesLen + ", 当前数据长度 = " +
                                globalBufferPosition + " buffer = " + new String(globalReadBuffer, 0, globalBufferPosition));
                        break;
                    }
                }
                HighSocketServer.sendData(socketChannel, byteBuffer, "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
            } else if (len < 0) {
                System.out.println("读取数据小于零 = " + len + ", 关闭该 socket = " + socketChannel.hashCode());
                running.compareAndSet(true, false);
            }

        }

        // 再次注册到 selector 上
        //socketChannel.register(selector, SelectionKey.OP_READ);
    }

}
