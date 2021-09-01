package cn.nihility.aqs;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Mutex implements Lock, Serializable {

    private static final long serialVersionUID = 8113677336866605443L;

    // 内部类，自定义同步器
    private static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -873229122397514982L;

        // 是否处于占用状态
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        // 当状态为0的时候获取锁
        @Override
        public boolean tryAcquire(int acquires) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        // 释放锁，将状态设置为0
        @Override
        protected boolean tryRelease(int releases) {
            if (getState() == 0) throw new IllegalMonitorStateException();
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        // 返回一个Condition，每个condition都包含了一个condition队列
        Condition newCondition() {
            return new ConditionObject();
        }
    }

    // 仅需要将操作代理到Sync上即可
    private final Sync sync = new Sync();

    public void lock() {
        sync.acquire(1);
    }

    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    public void unlock() {
        sync.release(1);
    }

    public Condition newCondition() {
        return sync.newCondition();
    }

    public boolean isLocked() {
        return sync.isHeldExclusively();
    }

    public boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    public static void main(String[] args) throws InterruptedException {
        final Mutex mutex1 = new Mutex();
        ReentrantLock mutex = new ReentrantLock();
        ExecutorService service = Executors.newFixedThreadPool(10);

        final MutexCount mutexCount = new MutexCount(0, mutex);
        final CountDownLatch latch = new CountDownLatch(100);

        System.out.println(mutexCount.getCount());
        for (int i = 0; i < 100; i++) {
            service.execute(() -> {
                //mutexCount.plus();
                System.out.println("thread : " + Thread.currentThread().getName());

                mutex1.lock();
                mutexCount.setCount(mutexCount.getCount() + 1);
                mutex1.unlock();

                latch.countDown();

            });
        }

        latch.await();
        System.out.println(mutexCount.getCount());

        service.shutdown();
    }

    static class MutexCount {
        private volatile int count;
        private Lock lock;

        public MutexCount(int count, Lock lock) {
            this.count = count;
            this.lock = lock;
        }

        /*public synchronized void plus() {
            count += 1;
        }*/

        public void plus() {
            System.out.println("Thread [" + Thread.currentThread().getName() + "] plus");
            while (!lock.tryLock()) {
                System.out.println("Thread [" + Thread.currentThread().getName() + "] tryLock");
            }

            //lock.lock();
            try {
                Random random = new Random(System.currentTimeMillis());
                try {
                    Thread.sleep(100L * random.nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count += 1;
            } finally {
                lock.unlock();
            }
        }

        public int getCount() {
            Random random = new Random(System.currentTimeMillis());
            try {
                Thread.sleep(100L * random.nextInt(5));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "MutexCount{" +
                "count=" + count +
                '}';
        }
    }
}
