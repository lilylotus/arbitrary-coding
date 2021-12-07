package cn.nihility.nio.single.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * 客户端需要做的事情就是发送消息到服务端，等待服务端响应，然后再次发送消息，发够 10 条消息断开连接
 */
public class ReactorClient implements Runnable {

    private Selector selector;
    private SocketChannel socketChannel;

    public ReactorClient(String ip, int port) {
        try {
            selector = Selector.open(); //打开一个Selector
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false); //设置为非阻塞模式
            socketChannel.connect(new InetSocketAddress(ip, port)); //连接服务
            //入口，最初给一个客户端channel注册上去的事件都是连接事件
            SelectionKey sk = socketChannel.register(selector, SelectionKey.OP_CONNECT);
            //附加处理类，第一次初始化放的是连接就绪处理类
            sk.attach(new ReactorConnector(socketChannel, selector));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select(); //就绪事件到达之前，阻塞
                Set<SelectionKey> selected = selector.selectedKeys(); //拿到本次select获取的就绪事件
                for (SelectionKey selectionKey : selected) {
                    //这里进行任务分发
                    dispatch(selectionKey);
                }
                selected.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ReactorClient 线程关闭");
    }

    void dispatch(SelectionKey k) {
        // 这里很关键，拿到每次 selectKey 里面附带的处理对象，然后调用其 run
        // 这个对象在具体的 Handler 里会进行创建，初始化的附带对象为 Connector（看上面构造器）
        Runnable r = (Runnable) (k.attachment());
        //调用之前注册的callback对象
        if (r != null) {
            r.run();
        }
    }

}
