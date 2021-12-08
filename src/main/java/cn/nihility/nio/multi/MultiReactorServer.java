package cn.nihility.nio.multi;

import cn.nihility.nio.ReactorThreadPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 多线程版本
 */
public class MultiReactorServer implements Runnable {

    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;

    private volatile boolean running = true;

    public MultiReactorServer(int port) throws IOException { // Reactor 初始化
        selector = Selector.open(); //打开一个 Selector
        serverSocketChannel = ServerSocketChannel.open(); //建立一个 Server 端通道
        serverSocketChannel.socket().bind(new InetSocketAddress(port)); //绑定服务端口
        // selector 模式下，所有通道必须是非阻塞的
        serverSocketChannel.configureBlocking(false);
        // Reactor 是入口，最初给一个 channel 注册上去的事件都是 accept
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // key.attach(new MultiAcceptor(selector, serverSocketChannel));
    }

    @Override
    public void run() {
        try {
            while (running && !Thread.interrupted()) {
                int selectCount = selector.select(500L);//就绪事件到达之前，阻塞
                if (selectCount > 0) {
                    //拿到本次 select 获取的就绪事件
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    SelectionKey key;
                    while (iterator.hasNext()) {
                        key = iterator.next();
                        iterator.remove();
                        // 这里进行任务分发
                        dispatch2(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null != serverSocketChannel) {
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (null != selector) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
    }

    private void dispatch(SelectionKey key) {
        try {
            if (key.isValid() && key.isAcceptable()) {
                System.out.println("处理 Acceptable 事件");
                new MultiAcceptor2(selector, key).run();
            } else if (key.isValid() && key.isReadable()) {
                System.out.println("处理 Readable 事件");
                new MultiReadHandler(selector, key).run();
            } else if (key.isValid() && key.isWritable()) {
                System.out.println("处理 Writable 事件");
                new MultiWriteHandler(selector, key).run();
            }
        } catch (Exception e) {
            key.cancel();
            if (key.channel() != null) {
                try {
                    key.channel().close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void dispatch2(SelectionKey key) {
        try {
            if (key.isValid()) {
                if (key.isAcceptable()) {
                    System.out.println("处理 Acceptable 事件");
//                    new MultiAcceptor2(selector, key).run();
                    new MultiAcceptor(serverSocketChannel, selector).run();
                }
                if (key.isWritable()) {
                    System.out.println("处理 Writable 事件");
                    // 改变感兴趣的事件，防止重复处理
                    key.interestOps(0);
                    ReactorThreadPool.submit(new MultiWriteHandler2(selector, key));
                }
                if (key.isReadable()) {
                    System.out.println("处理 Readable 事件");
                    // 改变感兴趣的事件，防止重复处理
                    key.interestOps(0);
                    ReactorThreadPool.submit(new MultiReadHandler2(selector, key));
                }
            }
        } catch (Exception e) {
            key.cancel();
            if (key.channel() != null) {
                try {
                    key.channel().close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
