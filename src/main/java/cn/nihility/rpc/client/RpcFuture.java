package cn.nihility.rpc.client;


import cn.nihility.rpc.common.codec.RpcRequest;
import cn.nihility.rpc.common.codec.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

public class RpcFuture implements Future<Object> {

    private static final Logger log = LoggerFactory.getLogger(RpcFuture.class);

    private final Sync sync;
    private final RpcRequest request;
    private RpcResponse response;
    private final long startTime;
    private final List<AsyncRpcCallback> pendingCallbacks = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public RpcFuture(RpcRequest request) {
        this.request = request;
        this.sync = new Sync();
        this.startTime = System.currentTimeMillis();
    }

    public void done(RpcResponse response) {
        this.response = response;
        sync.release(1);
        invokeCallbacks();
        // Threshold
        long durationTime = System.currentTimeMillis() - startTime;
        if (durationTime > 5000) {
            log.warn("Service response time is too slow. Request id [{}].Response duration time [{}] ms",
                request.getRequestId(), durationTime);
        }
    }

    public RpcFuture addCallback(AsyncRpcCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRpcCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    private void runCallback(final AsyncRpcCallback callback) {
        final RpcResponse res = this.response;
        RpcClient.submit(new Runnable() {
            @Override
            public void run() {
                if (res.isSuccess()) {
                    callback.success(res.getResult());
                } else {
                    callback.fail(new RuntimeException("Response error",
                        new Throwable("[" + res.getRequestId() + "] " + res.getError())));
                }
            }
        });
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    private Object obtainResult() {
        return this.response == null ? null : response.getResult();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(1);
        return obtainResult();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean acquire = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if (acquire) {
            return obtainResult();
        } else {
            throw new TimeoutException("Timeout exception. Request id: " + this.request.getRequestId()
                + ". Request class name: " + this.request.getClassName()
                + ". Request method: " + this.request.getMethodName());
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 3700290785831159879L;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == 1;
        }

        @Override
        protected boolean tryRelease(int arg) {
            return getState() == 0 && compareAndSetState(0, 1);
        }

        protected boolean isDone() {
            return getState() == 1;
        }

    }

}
