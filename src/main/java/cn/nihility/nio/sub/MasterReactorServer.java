package cn.nihility.nio.sub;


import cn.nihility.nio.ReactorThreadPool;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MasterReactorServer implements Runnable, Closeable {

    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;

    private volatile boolean running = true;

    private SubReactorSelector subSelector;

    public MasterReactorServer(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void addSubSelector(SubReactorSelector subSelector) {
        if (null != subSelector) {
            this.subSelector = subSelector;
            ReactorThreadPool.submit(subSelector);
        }
    }

    @Override
    public void close() {
        this.running = false;
    }

    @Override
    public void run() {
        try {
            while (running && !Thread.interrupted()) {
                int select = selector.select(500L);
                if (select > 0) {
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
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serverSocketChannel != null) {
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (subSelector != null) {
            subSelector.close();
        }
    }

    private void dispatch(SelectionKey key) {
        if (key.isAcceptable()) {
            System.out.println("处理 Acceptable 事件");
            Selector sc = selector;
            if (subSelector != null) {
                sc = subSelector.getSelector();

            }
            //key.interestOps(0);
            //ReactorThreadPool.submit(new ReactorAcceptor(sc, key));
            new ReactorAcceptor(sc, key).run();
        }
        if (key.isReadable()) {
            System.out.println("处理 Readable 事件");
            key.interestOps(0);
            ReactorThreadPool.submit(new ReactorReadHandler(selector, key));
        }
        if (key.isWritable()) {
            System.out.println("处理 Writable 事件");
            key.interestOps(0);
            ReactorThreadPool.submit(new ReactorWriteHandler(selector, key));
        }
    }

}
