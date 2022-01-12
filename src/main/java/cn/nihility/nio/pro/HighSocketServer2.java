package cn.nihility.nio.pro;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HighSocketServer2 implements Runnable {

    private static final int MESSAGE_LEN_SIZE = 4;
    private static final int SECOND_BUFFER_LENGTH = 10240;

    private int serverPort;
    private Selector selector;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicInteger clientCount = new AtomicInteger(0);
    private final SocketTiming socketTiming = new SocketTiming();

    public HighSocketServer2(int serverPort) {
        this.serverPort = serverPort;
    }

    public static void main(String[] args) {
        new HighSocketServer2(50080).run();
    }

    @Override
    public void run() {

        ServerSocketChannel serverSocketChannel;
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(serverPort));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务启动成功，ServerSocketChannel = " + serverSocketChannel.hashCode());
        } catch (IOException e) {
            System.out.println("服务端启动失败");
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
                            dispatchEvent(selectionKey);
                        }
                    }
                    selectionKeys.clear();
                } else {
                    if (socketTiming.toSecond() > 1.0D) {
                        System.out.println(String.format("time<%s>, clients<%d>, package<%d>, packageSize<%d>",
                            socketTiming.toSecond(), clientCount.get(),
                            socketTiming.getPackageCounter(), socketTiming.getPackageSize()));
                        socketTiming.update();
                    }
                }
            }

            serverSocketChannel.close();
            selector.close();
        } catch (IOException e) {
            System.out.println("服务端运行时发送异常");
            e.printStackTrace();
        }
        System.out.println("服务退出");

    }


    private void dispatchEvent(SelectionKey selectionKey) {

        SocketChannel socketChannel = null;
        try {
            if (selectionKey.isReadable()) {
                socketChannel = (SocketChannel) selectionKey.channel();
                handleReadableEvent(socketChannel, selectionKey);
            } else if (selectionKey.isAcceptable()) {
                //连接就绪触发，说明已经有客户端通道连了过来，这里需要拿服务端通道去获取客户端通道
                ServerSocketChannel skc = (ServerSocketChannel) selectionKey.channel();
                System.out.println("Accept ServerSocketChannel = " + skc.hashCode());
                //获取客户端通道（连接就绪，说明客户端接下来可能还有别的动作，比如读和写）
                socketChannel = skc.accept();
                //同样的需要设置非阻塞模式
                InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                socketChannel.configureBlocking(false);
                System.out.println(String.format("收到来自 %s 的连接, SocketChannel = %d",
                    remoteAddress, socketChannel.hashCode()));

                String sendMsg = String.format("欢迎 %s 连接", remoteAddress);
                SocketContainer socketContainer = new SocketContainer(SECOND_BUFFER_LENGTH, socketChannel);
                sendData(socketChannel, socketContainer.getByteBuffer(), sendMsg);

                /*for (SocketContainer socket : socketContainerMap.values()) {
                    sendData(socket, "新客户端 [" + remoteAddress + "] 进入");
                }
                socketContainerMap.put(remoteAddress.toString(), socketContainer);*/


                //将该客户端注册到选择器，感兴趣事件设置为读（客户端连接完毕，很肯能会往服务端写数据，因此这里要注册读事件用以接收这些数据）
                SelectionKey sk = socketChannel.register(selector, SelectionKey.OP_READ);
                sk.attach(socketContainer);
                int old = clientCount.get();
                clientCount.compareAndSet(old, old + 1);

            }

        } catch (IOException e) {

            try {
                selectionKey.cancel();
                if (null != socketChannel) {
                    System.out.println(String.format("处理 SelectionKey 异常, socket<%s> 退出", socketChannel.hashCode()));
                    socketChannel.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            int old = clientCount.get();
            clientCount.compareAndSet(old, old - 1);

            //e.printStackTrace();
        }
    }

    private void handleReadableEvent(final SocketChannel channel, SelectionKey selectionKey) throws IOException {
        // 切换成 buffer 的写模式，用于让通道将自己的内容写入到 buffer 里
        final SocketContainer socketContainer = (SocketContainer) selectionKey.attachment();
        ByteBuffer byteBuffer = socketContainer.getByteBuffer();
        byte[] globalReadBuffer = socketContainer.getSecondBuffer();
        int globalBufferPosition = socketContainer.getBufferPosition();
        byteBuffer.clear();
        int len = channel.read(byteBuffer);
        byteBuffer.flip();


        if (len > 0) {
            socketTiming.plusPackageCounter();
            socketTiming.plushPackageSize(len);
            if (socketTiming.toSecond() > 1.0D) {
                System.out.println(String.format("time<%s>, clients<%d>, package<%d>, packageSize<%d>",
                    socketTiming.toSecond(), clientCount.get(), socketTiming.getPackageCounter(), socketTiming.getPackageSize()));
                socketTiming.update();
            }
            if (len % 104 != 0) {
                System.out.println("缓存中数据 = " + new String(globalReadBuffer, 0, globalBufferPosition));
                System.out.println("数据缓存中的数据长度 = " + globalBufferPosition + ", 本次消息 = " +
                    new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit()));
            }

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
                    socketContainer.setBufferPosition(globalBufferPosition - MESSAGE_LEN_SIZE);
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
                    socketContainer.setBufferPosition(globalBufferPosition);
                    if (globalBufferPosition > 0) {
                        for (int i = 0; i < globalBufferPosition; i++) {
                            globalReadBuffer[i] = globalReadBuffer[dataBytesLen + i];
                        }
                    }
                } else {
                    System.out.println("数据长度不够， 需要数据长度 = " + dataBytesLen + ", 当前数据长度 = " +
                        globalBufferPosition + " buffer = " + new String(globalReadBuffer, 0, globalBufferPosition));
                    break;
                }
            }

            sendData(channel, byteBuffer, "服务端 = " + hashCode() + " 发送消息 = 0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");

            //channel.register(selector, SelectionKey.OP_READ, socketContainer);
            selectionKey.interestOps(SelectionKey.OP_READ);
        } else if (len < 0) {
            System.out.println("读取数据小于零 = " + len + ", 关闭该 socket = " + channel.hashCode());
            throw new IOException("读取数据小于零 = " + len + ", 关闭该 socket = " + channel.hashCode());
        }

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

    static void sendData(SocketChannel channel, ByteBuffer byteBuffer, String data) throws IOException {
        byte[] sendBytes = data.getBytes(StandardCharsets.UTF_8);
        byteBuffer.clear();
        byteBuffer.put(int2byte(sendBytes.length));
        byteBuffer.put(sendBytes);
        byteBuffer.flip();
        channel.write(byteBuffer);
    }

    static void sendData(SocketContainer socketContainer, String data) throws IOException {
        sendData(socketContainer.getSocketChannel(), socketContainer.getByteBuffer(), data);
    }

    static class SocketContainer implements Closeable {

        private final SocketChannel socketChannel;
        private int bufferPosition;
        private final byte[] secondBuffer;
        private final ByteBuffer byteBuffer;

        public SocketContainer(int bufferLength, SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
            secondBuffer = new byte[bufferLength];
            byteBuffer = ByteBuffer.allocate(1024);
        }

        public SocketChannel getSocketChannel() {
            return socketChannel;
        }

        public int getBufferPosition() {
            return bufferPosition;
        }

        public void setBufferPosition(int bufferPosition) {
            this.bufferPosition = bufferPosition;
        }

        public ByteBuffer getByteBuffer() {
            return byteBuffer;
        }

        public byte[] getSecondBuffer() {
            return secondBuffer;
        }

        @Override
        public void close() throws IOException {
            if (null != socketChannel) {
                System.out.println("关闭 ClientSocket = " + socketChannel.hashCode());
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class SocketTiming {

        private long timePoint;

        private long packageCounter;

        private long packageSize;

        public SocketTiming() {
            update();
            packageCounter = 0;
        }

        public synchronized void plusPackageCounter() {
            packageCounter += 1;
        }

        public void plushPackageSize(int size) {
            packageSize += size;
        }

        public long getPackageCounter() {
            return packageCounter;
        }

        public long getPackageSize() {
            return packageSize;
        }

        public synchronized void update() {
            timePoint = System.currentTimeMillis();
            packageSize = 0;
            packageCounter = 0;
        }

        public long toNano() {
            return System.currentTimeMillis() - timePoint;
        }

        public double toMillis() {
            return toNano() * 1D;
        }

        public double toSecond() {
            return toMillis() * 0.001D;
        }

    }

}
