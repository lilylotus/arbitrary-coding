package cn.nihility.nio.multi.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class MultiReadHandler2 implements Runnable {

    private final Selector selector;
    private final SelectionKey selectionKey;

    MultiReadHandler2(Selector selector, SelectionKey selectionKey) {
        this.selector = selector;
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        // 取消读事件的监控
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        if (null != socketChannel) {
            final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            try {
                final int len = socketChannel.read(byteBuffer);
                byteBuffer.flip();

                if (len > 0) {
                    String readMessage = new String(byteBuffer.array(), byteBuffer.position(),
                            byteBuffer.limit(), StandardCharsets.UTF_8);
                    System.out.println(String.format("收到来自 %s 的消息: [%s]",
                            socketChannel.getRemoteAddress(), readMessage));

                    // 切换感兴趣的事件
                    selectionKey.attach(readMessage);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                } else if (len < 0) {
                    System.out.println("读取消息 为空 [" + len + "] 连接关闭");
                    selectionKey.cancel();
                    socketChannel.close();
                }

            } catch (IOException e) {
                System.out.println("========== 读取消息时异常连接关闭 ==========");
                //System.out.println("读取消息时异常 " + e.getMessage());
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
