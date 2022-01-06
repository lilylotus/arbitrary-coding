package cn.nihility.nio.pro;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ClientSocketHandler implements Closeable {

    private static final int MESSAGE_LEN_SIZE = 4;

    private byte[] globalReadBuffer = new byte[10240];
    private int globalBufferPosition = 0;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(2048);

    private Selector selector;
    private SocketChannel socketChannel;

    public ClientSocketHandler(Selector selector, SocketChannel socketChannel) {
        this.selector = selector;
        this.socketChannel = socketChannel;
    }

    public void dispatchSelectKey(SelectionKey selectionKey) throws IOException {
        SocketChannel sc = null;
        try {
            if (selectionKey.isReadable()) {
                sc = (SocketChannel) selectionKey.channel();
                handleReadableEvent(sc, selectionKey);
            } else if (selectionKey.isConnectable()) {
                sc = (SocketChannel) selectionKey.channel();
                // 连接完成（与服务端的三次握手完成）
                if (sc.finishConnect()) {
                    System.out.println("连接到服务器 [" + sc.getRemoteAddress() + "]");
                }
                //sc.configureBlocking(true);
                sc.register(selector, SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            System.out.println(String.format("处理 SelectionKey 异常, socket<%s> 退出", Integer.toString(sc.hashCode())));

            try {
                selectionKey.cancel();
                sc.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void handleReadableEvent(SocketChannel socketChannel, SelectionKey selectionKey) throws IOException {

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
                int msgLen = SocketUtil.byte2int(intBytes);
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
                    if (msg.contains("新客户端") || msg.contains("欢迎")) {
                        System.out.println(String.format("%d 接收消息内容: [%d]:[%s]", socketChannel.hashCode(), msgLen, msg));
                    }

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
            SocketUtil.sendData(socketChannel, byteBuffer,
                    "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");

            selectionKey.interestOps(SelectionKey.OP_READ);

        } else if (len < 0) {
            System.out.println("读取数据小于零 = " + len + ", 关闭该 socket = " + socketChannel.hashCode());
            throw new IOException("读取数据小于零，判断服务的关闭");
        }

    }


    @Override
    public void close() {
        if (null != socketChannel) {
            System.out.println("关闭 Socket = " + socketChannel.hashCode());
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
