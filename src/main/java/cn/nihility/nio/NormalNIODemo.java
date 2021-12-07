package cn.nihility.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NormalNIODemo {

    public static void normal() throws IOException {
        Selector selector = Selector.open(); //打开选择器

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); //打开通道
        serverSocketChannel.configureBlocking(false); //设置通道为非阻塞模式
        serverSocketChannel.bind(new InetSocketAddress(2333)); //绑定端口
        //注册 channel 到选择器，指定监听该 Channel 的哪些事件，初始化都是对连接事件监听（因为是入口）
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (selector.select() > 0) { // 若收到就绪事件select返回“感兴趣”事件集合，否则阻塞当前线程
            Set<SelectionKey> keys = selector.selectedKeys(); //获取本次拿到的事件集合
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) { //当前就绪事件为连接事件
                    //连接就绪触发，说明已经有客户端通道连了过来，这里需要拿服务端通道去获取客户端通道
                    ServerSocketChannel skc = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = skc.accept(); //获取客户端通道（连接就绪，说明客户端接下来可能还有别的动作，比如读和写）
                    socketChannel.configureBlocking(false); //同样的需要设置非阻塞模式
                    System.out.println(String.format("收到来自 %s 的连接", socketChannel.getRemoteAddress()));
                    //将该客户端注册到选择器，感兴趣事件设置为读（客户端连接完毕，很肯能会往服务端写数据，因此这里要注册读事件用以接收这些数据）
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) { //当前事件为读就绪
                    //能触发读就绪，说明客户端已经开始往服务端写数据，通过 SelectionKey 拿到当前客户端通道
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int count = socketChannel.read(buffer); //从通道读入数据
                    if (count < 0) { // 若本次读就绪拿到-1，则认为客户端主动断开了连接
                        socketChannel.close(); //服务端关闭客户端通道
                        //断连后就将该事件从选择器的 SelectionKey 集合中移除
                        //这里说一下，这里不是真正意义上的移除，这里是取消，会将该 key 放入取消队列里，在下次 select 函数调用时才负责清空
                        key.cancel();
                        System.out.println("连接关闭");
                        continue;
                    }
                    System.out.println(String.format("收到来自 %s 的消息: %s",
                            socketChannel.getRemoteAddress(),
                            new String(buffer.array())));
                }
                keys.remove(key);
            }
        }
    }
}
