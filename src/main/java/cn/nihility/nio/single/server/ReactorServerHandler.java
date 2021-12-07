package cn.nihility.nio.single.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * 负责接下来的读写操作
 */
public class ReactorServerHandler implements Runnable {

    private final SelectionKey selectionKey;
    private final SocketChannel socketChannel;

    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer sendBuffer = ByteBuffer.allocate(2048);

    private static final int READ = 0;
    private static final int SEND = 1;

    private int status = READ;

    ReactorServerHandler(SocketChannel socketChannel, Selector selector) throws IOException {
        this.socketChannel = socketChannel; //接收客户端连接
        this.socketChannel.configureBlocking(false); //置为非阻塞模式（selector仅允非阻塞模式）
        //将该客户端注册到 selector，得到一个 SelectionKey，以后的 select 到的就绪动作全都是由该对象进行封装
        selectionKey = socketChannel.register(selector, 0);
        selectionKey.attach(this); //附加处理对象，当前是Handler对象，run是对象处理业务的方法
        //说明之前 Acceptor 里的建连已完成，那么接下来就是读取动作，因此这里首先将读事件标记为“感兴趣”事件
        selectionKey.interestOps(SelectionKey.OP_READ);
        selector.wakeup(); //唤起 select 阻塞
    }

    @Override
    public void run() {
        try {
            switch (status) {
                case READ:
                    read();
                    break;
                case SEND:
                    send();
                    break;
                default:
            }
        } catch (IOException e) {
            //这里的异常处理是做了汇总
            // 常出的异常就是 server 端还有未读/写完的客户端消息，客户端就主动断开连接，
            // 这种情况下是不会触发返回 -1 的，这样下面 read 和 write 方法里的 cancel 和 close 就都无法触发
            // 这样会导致死循环异常（read/write 处理失败，事件又未被 cancel，因此会不断的被 select 到，不断的报异常）
            System.err.println("read或send时发生异常！异常信息：" + e.getMessage());
            selectionKey.cancel();
            try {
                socketChannel.close();
            } catch (IOException e2) {
                System.err.println("关闭通道时发生异常！异常信息：" + e2.getMessage());
                e2.printStackTrace();
            }
        }
    }

    private void read() throws IOException {
        if (selectionKey.isValid()) {
            readBuffer.clear();
            //read方法结束，意味着本次"读就绪"变为"读完毕"，标记着一次就绪事件的结束
            int count = socketChannel.read(readBuffer);
            readBuffer.flip();
            if (count > 0) {
                System.out.println(String.format("收到来自 %s 的消息: [%s]",
                        socketChannel.getRemoteAddress(),
                        new String(readBuffer.array(), readBuffer.position(),
                                readBuffer.limit(), StandardCharsets.UTF_8)));
                status = SEND;
                selectionKey.interestOps(SelectionKey.OP_WRITE); //注册写方法
            } else {
                // 读模式下拿到的值是 -1，说明客户端已经断开连接，那么将对应的 selectKey 从 selector 里清除
                // 否则下次还会 select 到，因为断开连接意味着读就绪不会变成读完毕，也不 cancel，下次 select 会不停收到该事件
                // 所以在这种场景下，（服务器程序）需要关闭 socketChannel 并且取消 key，最好是退出当前函数。
                // 注意，这个时候服务端要是继续使用该 socketChannel 进行读操作的话，就会抛出“远程主机强迫关闭一个现有的连接”的IO异常。
                selectionKey.cancel();
                socketChannel.close();
                System.out.println("read时-------连接关闭");
            }
        }
    }

    void send() throws IOException {
        // 模拟业务操作延迟，性能瓶颈
        /*try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        if (selectionKey.isValid()) {
            sendBuffer.clear();
            sendBuffer.put(String.format("我收到来自 %s 的信息辣：[%s] ok",
                    socketChannel.getRemoteAddress(),
                    new String(readBuffer.array(), readBuffer.position(),
                            readBuffer.limit(), StandardCharsets.UTF_8)).getBytes());
            sendBuffer.flip();
            //write 方法结束，意味着本次写就绪变为写完毕，标记着一次事件的结束
            int count = socketChannel.write(sendBuffer);
            if (count < 0) {
                //同上，write场景下，取到-1，也意味着客户端断开连接
                selectionKey.cancel();
                socketChannel.close();
                System.out.println("send时-------连接关闭");
            }

            //没断开连接，则再次切换到读
            status = READ;
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }
}
