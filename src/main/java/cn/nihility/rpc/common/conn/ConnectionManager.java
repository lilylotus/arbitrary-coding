package cn.nihility.rpc.common.conn;

import cn.nihility.rpc.client.RpcClientHandler;
import cn.nihility.rpc.client.RpcClientInitializer;
import cn.nihility.rpc.client.RpcProtocol;
import cn.nihility.rpc.client.RpcServiceInfo;
import cn.nihility.rpc.client.route.RpcLoadBalance;
import cn.nihility.rpc.client.route.RpcLoadBalanceRoundRobin;
import cn.nihility.rpc.common.util.ThreadPoolUtil;
import cn.nihility.rpc.exception.RpcRouteException;
import cn.nihility.rpc.service.IArithmetic;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(100),
        new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "ConnectionManager [" + count.getAndIncrement() + "]");
            }

        });

    private final Map<RpcProtocol, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
    private CopyOnWriteArraySet<RpcProtocol> rpcProtocolSet = new CopyOnWriteArraySet<>();
    private ReentrantLock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();
    private long waitTimeout = 5000;
    private volatile boolean isRunning = true;
    private RpcLoadBalance loadBalance = new RpcLoadBalanceRoundRobin();

    private ConnectionManager() {
    }

    private static class SingletonHolder {
        private static final ConnectionManager instance = new ConnectionManager();
    }

    public static ConnectionManager getInstance() {
        return SingletonHolder.instance;
    }

    public void connectServerNode(final RpcProtocol rpcProtocol) {
        rpcProtocolSet.add(rpcProtocol);

        threadPoolExecutor.submit(() -> {
            final InetSocketAddress remotePeer = new InetSocketAddress(rpcProtocol.getHost(), rpcProtocol.getPort());
            final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcClientInitializer());

            final ChannelFuture channelFuture = bootstrap.connect(remotePeer);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("Success connect to server [{}]", remotePeer);
                        final RpcClientHandler handler = future.channel().pipeline().get(RpcClientHandler.class);
                        handler.setRpcProtocol(rpcProtocol);
                        List<RpcServiceInfo> serviceInfoList = new ArrayList<>();
                        serviceInfoList.add(new RpcServiceInfo(IArithmetic.class.getName(), "v2.2.0"));
                        rpcProtocol.setServiceInfoList(serviceInfoList);
                        connectedServerNodes.put(rpcProtocol, handler);
                        signalAvailableHandler();
                    } else {
                        log.info("Can not connect to server [{}]", remotePeer);
                    }
                }
            });

            try {
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("Channel close exception", e);
            } finally {
                log.info("Channel [{}] shutdownGracefully", remotePeer);
                eventLoopGroup.shutdownGracefully();
            }

        });
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            log.warn("Waiting for available service");
            return connected.await(this.waitTimeout, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    private void waitingUntilHandlerActive() throws InterruptedException {
        log.warn("Waiting for available service");
        connected.await();
    }

    public RpcClientHandler chooseHandler(RpcProtocol rpcProtocol) throws Exception {
        int size = connectedServerNodes.values().size();
        while (isRunning && size <= 0) {
            try {
                waitingForHandler();
                size = connectedServerNodes.values().size();
            } catch (InterruptedException e) {
                log.error("Waiting for available service is interrupted!", e);
            }
        }
        RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
        if (handler != null) {
            return handler;
        } else {
            throw new Exception("Can not get available connection");
        }
    }

    public RpcClientHandler chooseHandler(String serviceKey) throws RpcRouteException {
        int size = connectedServerNodes.values().size();
        int loopCount = 0;
        while (isRunning && size <= 0) {
            try {
                if (++loopCount > 3) {
                    break;
                }
                waitingForHandler();
                size = connectedServerNodes.values().size();
            } catch (InterruptedException e) {
                log.error("Waiting for available service is interrupted!", e);
            }
        }
        RpcProtocol rpcProtocol = loadBalance.route(serviceKey, connectedServerNodes);
        RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
        if (handler == null) {
            throw new RpcRouteException("Can not get available connection");
        }
        return handler;
    }

    private void removeAndCloseHandler(RpcProtocol rpcProtocol) {
        RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
        if (handler != null) {
            handler.close();
        }
        connectedServerNodes.remove(rpcProtocol);
        rpcProtocolSet.remove(rpcProtocol);
    }

    public void removeHandler(RpcProtocol rpcProtocol) {
        rpcProtocolSet.remove(rpcProtocol);
        connectedServerNodes.remove(rpcProtocol);
        log.info("Remove one connection, host: [{}], port: [{}]", rpcProtocol.getHost(), rpcProtocol.getPort());
        if (isRunning) {
            connectServerNode(rpcProtocol);
        }
    }

    public void stop() {
        isRunning = false;
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        ThreadPoolUtil.shutdown();
    }

}
