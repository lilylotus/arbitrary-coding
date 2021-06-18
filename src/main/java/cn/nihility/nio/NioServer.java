package cn.nihility.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NioServer {

    private static final Logger log = LoggerFactory.getLogger(NioServer.class);
    private static ServerSocketChannel serverSocketChannel;
    private static Selector selector;

    private static volatile boolean run = true;

    static {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            // 设置非阻塞
            serverSocketChannel.configureBlocking(false);

            serverSocketChannel.bind(new InetSocketAddress(40000));
            final ServerSocket serverSocket = serverSocketChannel.socket();
            serverSocket.bind(new InetSocketAddress(40000));

            /* 多路复用器 */
            Selector selector = Selector.open();
            // 注册 serverSocketChannel 到 多路复用器
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.info("Initialization Nio Server Success.");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void listen() {
        while (run) {
            int selectCnt = 0;
            try {
                selectCnt = selector.select(1000L);
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("Selector count [{}]", selectCnt);
            if (selectCnt > 0) {
                final Set<SelectionKey> selectionKeys = selector.selectedKeys();
                final Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    final SelectionKey key = iterator.next();
                    // 移除事件，避免重复处理
                    iterator.remove();
                    try {
                        handleKey(key);
                    } catch (Exception ex) {
                        if (null != key) {
                            key.cancel();
                            if (key.channel() != null) {
                                try {
                                    key.channel().close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private static void handleKey(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isValid()) {
            // 可连接
            if (selectionKey.isAcceptable()) {
                log.info("Acceptable SelectionKey");
                final ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
                // 三次握手
                final SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);

                // 返回信息
                if (sc.isConnected()) {
                    sc.write(StandardCharsets.UTF_8.encode("Welcome To Nio Server."));
                    log.info("Echo Connected Client Info.");
                }

                sc.register(selector, SelectionKey.OP_READ);
            }

            if (selectionKey.isReadable()) {

            }
        }
    }


    private static void stop() {
        run = false;
    }

}
