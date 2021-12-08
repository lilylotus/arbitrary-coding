package cn.nihility.nio.multi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class MultiReadHandler implements Runnable {

    private final Selector selector;
    private final SelectionKey selectionKey;

    MultiReadHandler(Selector selector, SelectionKey selectionKey) {
        this.selector = selector;
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        if (null != socketChannel) {
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            try {
                // read方法结束，意味着本次"读就绪"变为"读完毕"，标记着一次就绪事件的结束
                int count = socketChannel.read(readBuffer);
                if (count > 0) {
                    readBuffer.flip();
                    String readMessage = new String(readBuffer.array(), readBuffer.position(),
                            readBuffer.limit(), StandardCharsets.UTF_8);

                    System.out.println(String.format("收到来自 %s 的消息: [%s]",
                            socketChannel.getRemoteAddress(), readMessage));
                    socketChannel.register(selector, SelectionKey.OP_WRITE, readMessage);
                } else if (count < 0) {
                    // 读模式下拿到的值是 -1，说明客户端已经断开连接，那么将对应的 selectKey 从 selector 里清除
                    // 否则下次还会 select 到，因为断开连接意味着读就绪不会变成读完毕，也不 cancel，下次 select 会不停收到该事件
                    // 所以在这种场景下，（服务器程序）需要关闭 socketChannel 并且取消 key，最好是退出当前函数。
                    // 注意，这个时候服务端要是继续使用该 socketChannel 进行读操作的话，就会抛出“远程主机强迫关闭一个现有的连接”的IO异常。

                    System.out.println("read 时 " + socketChannel.getRemoteAddress() + " 连接关闭");
                    selectionKey.cancel();
                    socketChannel.close();
                }
            } catch (IOException e) {
                System.out.println("========== 读取消息时异常连接关闭 ==========");
                System.out.println("读取消息时异常 " + e.getMessage());
                selectionKey.cancel();
                try {
                    socketChannel.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
