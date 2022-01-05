package cn.nihility.nio.pro;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class HighSocketServer implements Runnable {

    private static final int MESSAGE_LEN_SIZE = 4;
    private static final int SECOND_BUFFER_LENGTH = 10240;

    private int serverPort;
    private Selector selector;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final java.util.Map<String, SocketContainer> socketContainerMap = new ConcurrentHashMap<>();
    private final SocketTiming socketTiming = new SocketTiming();

    public HighSocketServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public static void main(String[] args) {
        new HighSocketServer(2333).run();
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


        try {

            int selectCount;
            SelectionKey selectionKey;
            while (running.get()) {
                selectCount = selector.select(500L);
                if (selectCount > 0) {
                    java.util.Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    for (SelectionKey key : selectionKeys) {
                        selectionKey = key;
                        if (selectionKey.isValid()) {
                            dispatchEvent(selectionKey);
                        }
                    }
                    selectionKeys.clear();
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
                SocketContainer socketContainer = socketContainerMap.computeIfAbsent(Integer.toString(socketChannel.hashCode()),
                        key -> new SocketContainer(SECOND_BUFFER_LENGTH));
                handleReadableEvent(socketChannel, socketContainer);
            } else if (selectionKey.isAcceptable()) {
                //连接就绪触发，说明已经有客户端通道连了过来，这里需要拿服务端通道去获取客户端通道
                ServerSocketChannel skc = (ServerSocketChannel) selectionKey.channel();
                System.out.println("Accept ServerSocketChannel = " + skc.hashCode());
                //获取客户端通道（连接就绪，说明客户端接下来可能还有别的动作，比如读和写）
                socketChannel = skc.accept();
                //同样的需要设置非阻塞模式
                socketChannel.configureBlocking(false);
                System.out.println(String.format("收到来自 %s 的连接, SocketChannel = %d",
                        socketChannel.getRemoteAddress(), socketChannel.hashCode()));

                String sendMsg = String.format("欢迎 %s 连接", socketChannel.getRemoteAddress());
                SocketContainer socketContainer = new SocketContainer(SECOND_BUFFER_LENGTH);
                socketContainerMap.put(Integer.toString(socketChannel.hashCode()), socketContainer);
                sendData(socketChannel, socketContainer.getByteBuffer(), sendMsg);


                //将该客户端注册到选择器，感兴趣事件设置为读（客户端连接完毕，很肯能会往服务端写数据，因此这里要注册读事件用以接收这些数据）
                socketChannel.register(selector, SelectionKey.OP_READ);
            }

        } catch (IOException e) {
            System.out.println("处理 SelectionKey 异常");

            selectionKey.cancel();
            if (selectionKey.channel() != null) {
                try {
                    selectionKey.channel().close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (null != socketChannel) {
                socketContainerMap.remove(Integer.toString(socketChannel.hashCode()));
            }

            e.printStackTrace();
        }
    }

    private void handleReadableEvent(final SocketChannel channel, final SocketContainer socketContainer) throws IOException {
        // 切换成 buffer 的写模式，用于让通道将自己的内容写入到 buffer 里
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
                System.out.println(String.format("time<%s>, package<%d>, packageSize<%d>",
                        socketTiming.toSecond(), socketTiming.getPackageCounter(), socketTiming.getPackageSize()));
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
                int msgLen = HighSocketClient.byte2int(intBytes);
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

            sendData(channel, byteBuffer, "服务端 = " + hashCode() + " 发送的消息");
        } else if (len < 0) {
            System.out.println("读取数据小于零 = " + len + ", 关闭该 socket = " + channel.hashCode());
            socketContainerMap.remove(Integer.toString(channel.hashCode()));
        }

    }

    static void sendData(SocketChannel channel, ByteBuffer byteBuffer, String data) throws IOException {
        byte[] sendBytes = data.getBytes(StandardCharsets.UTF_8);
        byteBuffer.clear();
        byteBuffer.put(HighSocketClient.int2byte(sendBytes.length));
        byteBuffer.put(sendBytes);
        byteBuffer.flip();
        channel.write(byteBuffer);
    }

    static class SocketContainer {

        private int bufferPosition;
        private final byte[] secondBuffer;
        private final ByteBuffer byteBuffer;

        public SocketContainer(int bufferLength) {
            secondBuffer = new byte[bufferLength];
            byteBuffer = ByteBuffer.allocate(1024);
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
            timePoint = System.nanoTime();
        }

        public long toNano() {
            return System.nanoTime() - timePoint;
        }

        public double toMillis() {
            return (System.nanoTime() - timePoint) * 0.001D;
        }

        public double toSecond() {
            return (System.nanoTime() - timePoint) * 0.000001D;
        }

    }

}
