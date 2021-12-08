package cn.nihility.nio.single.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端的处理器
 */
public class ReactorClientHandler implements Runnable {

    private final SelectionKey selectionKey;
    private final SocketChannel socketChannel;

    private ByteBuffer readBuffer = ByteBuffer.allocate(2048);
    private ByteBuffer sendBuffer = ByteBuffer.allocate(1024);

    private static final int READ = 0;
    private static final int SEND = 1;

    private volatile int status = SEND; //与服务端不同，默认最开始是发送数据

    private AtomicInteger counter = new AtomicInteger();

    ReactorClientHandler(SocketChannel socketChannel, Selector selector) throws IOException {
        this.socketChannel = socketChannel; //接收客户端连接
        this.socketChannel.configureBlocking(false); //置为非阻塞模式（selector仅允非阻塞模式）
        // 将该客户端注册到 selector，得到一个 SelectionKey，以后的 select 到的就绪动作全都是由该对象进行封装
        selectionKey = socketChannel.register(selector, 0);
        // 附加处理对象，当前是 Handler 对象，run 是对象处理业务的方法
        selectionKey.attach(this);
        // 说明之前 Connect 已完成，那么接下来就是发送数据，因此这里首先将写事件标记为“感兴趣”事件
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup(); // 唤起 select 阻塞
    }

    @Override
    public void run() {
        try {
            switch (status) {
                case SEND:
                    send();
                    break;
                case READ:
                    read();
                    break;
                default:
            }
        } catch (IOException e) {
            // 这里的异常处理是做了汇总，同样的
            // 客户端也面临着正在与服务端进行写/读数据时，突然因为网络等原因
            // 服务端直接断掉连接，这个时候客户端需要关闭自己并退出程序
            System.err.println("send或read时发生异常！异常信息：" + e.getMessage());
            selectionKey.cancel();
            try {
                socketChannel.close();
            } catch (IOException e2) {
                System.err.println("关闭通道时发生异常！异常信息：" + e2.getMessage());
                e2.printStackTrace();
            }
        }
        //System.out.println("ReactorClientHandler 线程关闭");
    }

    void send() throws IOException {
        if (selectionKey.isValid()) {
            int count = counter.incrementAndGet();
            if (count <= 10) {
                final byte[] buffer = ("客户端发送的第 " + hashCode() + ":" + count + " 条消息").getBytes(StandardCharsets.UTF_8);

                sendBuffer.clear();
                sendBuffer.put(buffer);
                // 切换到读模式，用于让通道读到 buffer 里的数据
                sendBuffer.flip();

                socketChannel.write(sendBuffer);

                // 则再次切换到读，用以接收服务端的响应
                status = READ;
                selectionKey.interestOps(SelectionKey.OP_READ);
            } else {
                selectionKey.cancel();
                socketChannel.close();
            }
        }
    }

    private void read() throws IOException {
        // 模拟业务操作延迟
        /*try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        if (selectionKey.isValid()) {
            // 切换成 buffer 的写模式，用于让通道将自己的内容写入到 buffer 里
            readBuffer.clear();
            socketChannel.read(readBuffer);
            readBuffer.flip();
            System.out.println(String.format("收到来自服务端的消息: [%s]",
                    new String(readBuffer.array(), readBuffer.position(), readBuffer.limit(), StandardCharsets.UTF_8)));

            //收到服务端的响应后，再继续往服务端发送数据
            status = SEND;
            selectionKey.interestOps(SelectionKey.OP_WRITE); //注册写事件
        }
    }

}
