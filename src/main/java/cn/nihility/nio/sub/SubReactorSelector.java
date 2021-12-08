package cn.nihility.nio.sub;

import cn.nihility.nio.ReactorThreadPool;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class SubReactorSelector implements Runnable, Closeable {

    private final Selector selector;
    private volatile boolean running = true;

    public SubReactorSelector() throws IOException {
        selector = Selector.open();
    }

    public void register(SelectionKey key, int ops) throws ClosedChannelException {
        if (key != null) {
            SelectableChannel channel = key.channel();
            if (channel != null) {
                channel.register(selector, ops);
            }
        }
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public void run() {
        try {
            while (running && !Thread.interrupted()) {
                int cnt = selector.select(500L);
                if (cnt > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isValid()) {
                            dispatch(key);
                        }
                    }
                    selectionKeys.clear();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Sub Select 关闭");
        if (null != selector) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        this.running = false;
    }

    private void dispatch(final SelectionKey key) {
        if (key.isAcceptable()) {
            System.out.println("子 Selector 处理 Acceptable 事件");
            new ReactorAcceptor(selector, key).run();
        }
        if (key.isWritable()) {
            System.out.println("子 Selector 处理 Writable 事件");
            // 改变感兴趣的事件，防止重复处理
            key.interestOps(0);
            ReactorThreadPool.submit(new ReactorWriteHandler(selector, key));
        }
        if (key.isReadable()) {
            System.out.println("子 Selector 处理 Readable 事件");
            // 改变感兴趣的事件，防止重复处理
            key.interestOps(0);
            ReactorThreadPool.submit(new ReactorReadHandler(selector, key));
        }
    }
}
