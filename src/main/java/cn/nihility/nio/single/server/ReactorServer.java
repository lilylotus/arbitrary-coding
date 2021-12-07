package cn.nihility.nio.single.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 该模块负责监听就绪事件和对事件的分发处理
 * <p>
 * 在响应客户端之前睡眠 2s，当做是性能瓶颈点，同样的再次开两个客户端同时访问服务端，每个客户端发送10条消息，会发现，程序直接运行了40s
 * 这是大多数情况下不愿意看到的，因此，就有了多线程 Reactor 模式，跟 BIO 为了提高性能将读操作放到一个独立线程处理一样
 * Reactor 这样做，也是为了解决上面提到的性能问题，只不过 NIO 比 BIO 做异步有个最大的优势就是 NIO 不会阻塞一个线程
 * 类似 read 这种操作状态都是由 selector 负责监听的，不像 BIO 里都是阻塞的
 * 只要被异步出去，那么一定是非阻塞的业务代码（除非是人为将代码搞成阻塞），而 BIO 由于 read 本身阻塞
 * 因此会阻塞掉整个线程，这也是同样是异步为什么 NIO 可以更加高效的原因之一。
 * <p>
 * 那么单线程 Reactor 适用于什么情况呢？适用于那种程序复杂度很低的系统
 * 例如 redis，其大部分操作都是非常高效的，很多命令的时间复杂度直接为 O(1)，这种情况下适合这种简单的 Reactor 模型实现服务端。
 */
public class ReactorServer implements Runnable {

    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;

    public ReactorServer(int port) throws IOException { // Reactor 初始化
        selector = Selector.open(); //打开一个 Selector
        serverSocketChannel = ServerSocketChannel.open(); //建立一个 Server 端通道
        serverSocketChannel.socket().bind(new InetSocketAddress(port)); //绑定服务端口
        // selector 模式下，所有通道必须是非阻塞的
        serverSocketChannel.configureBlocking(false);
        // Reactor 是入口，最初给一个 channel 注册上去的事件都是 accept
        SelectionKey sk = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // attach callback object, Acceptor
        sk.attach(new Acceptor(serverSocketChannel, selector));
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select(); //就绪事件到达之前，阻塞
                // 拿到本次select获取的就绪事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isValid()) {
                        //这里进行任务分发
                        dispatch(key);
                    }
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void dispatch(SelectionKey k) {
        // 这里很关键，拿到每次 selectKey 里面附带的处理对象
        // 然后调用其 run，这个对象在具体的 Handler 里会进行创建，初始化的附带对象为 Acceptor（看上面构造器）
        Runnable r = (Runnable) (k.attachment());
        //调用之前注册的 callback 对象
        if (r != null) {
            r.run();
        }
    }

}
