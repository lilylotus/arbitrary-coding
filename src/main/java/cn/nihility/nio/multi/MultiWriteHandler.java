package cn.nihility.nio.multi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class MultiWriteHandler implements Runnable {

    private final Selector selector;
    private final SelectionKey selectionKey;

    MultiWriteHandler(Selector selector, SelectionKey selectionKey) {
        this.selector = selector;
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        if (null != socketChannel) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(2048);

            try {
                String readMsg = (String) selectionKey.attachment();
                String sendMsg = String.format("我收到来自 %s 的信息辣：[%s] ok",
                        socketChannel.getRemoteAddress(), readMsg == null ? "空" : readMsg);

                byteBuffer.put(sendMsg.getBytes(StandardCharsets.UTF_8));
                byteBuffer.flip();

                //write 方法结束，意味着本次写就绪变为写完毕，标记着一次事件的结束
                int count = socketChannel.write(byteBuffer);
                if (count < 0) {
                    // 同上，write场景下，取到 -1，也意味着客户端断开连接
                    System.out.println("发送消息时 " + socketChannel.getRemoteAddress() + " 连接关闭");
                    selectionKey.cancel();
                    socketChannel.close();
                }

                socketChannel.register(selector, SelectionKey.OP_READ);
            } catch (IOException e) {
                System.out.println("========== 发送消息时异常连接关闭 ==========");
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
