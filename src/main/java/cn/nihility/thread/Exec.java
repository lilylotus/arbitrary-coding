package cn.nihility.thread;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Exec {

    private static final ThreadLocal<DateTimeFormatter> DT =
            ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));

    public static void main(String[] args) {
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(3, 4, 100L, TimeUnit.MILLISECONDS,
                        new MyQueue1<>(5), new MyThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy());
        for (int i = 0; i < 8; i++) {
            try {
                final Thread thread = new Thread(() -> {
                    System.out.println("exec thread name : " + Thread.currentThread().getName());

                    while (true) {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            // TODO
                        }
                        // TODO
                    }
                }, "id:" + (i + 1));
                //System.out.println("thread id:" + (i+1) + ", hash [" + thread.hashCode() + "]");
                executor.execute(thread);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        executor.shutdown();


    }

    public static void main1(String[] args) throws InterruptedException {

//        final ExecutorService service = Executors.newFixedThreadPool(10);

        // new SynchronousQueue<>()
        //ArrayBlockingQueue
        // LinkedBlockingQueue
        // PriorityBlockingQueue
        // PriorityBlockingQueue

        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(1, 3, 100L, TimeUnit.MILLISECONDS,
                        new MyQueue<>(2), new MyThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy());

        final String format = LocalDateTime.now().format(DT.get());
        System.out.println(format);

        executor.execute(() -> { try { Thread.sleep(5000L); } catch (Exception ex) { ex.printStackTrace();} System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now().format(DT.get())); });
        executor.execute(() -> { try { Thread.sleep(5000L); } catch (Exception ex) { ex.printStackTrace();} System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now().format(DT.get())); });
        executor.execute(() -> { try { Thread.sleep(5000L); } catch (Exception ex) { ex.printStackTrace();} System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now().format(DT.get())); });
        executor.execute(() -> { try { Thread.sleep(500L); } catch (Exception ex) { ex.printStackTrace();} System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now().format(DT.get())); });
        executor.execute(() -> { try { Thread.sleep(500L); } catch (Exception ex) { ex.printStackTrace();} System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now().format(DT.get())); });

        System.out.println("active : " + executor.getActiveCount() + " complete " + executor.getCompletedTaskCount());
        Thread.sleep(1000L);

        executor.execute(() -> { System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now().format(DT.get())); try { Thread.sleep(500L); } catch (Exception ex) { ex.printStackTrace();} });
        System.out.println("active : " + executor.getActiveCount() + " complete " + executor.getCompletedTaskCount());

        /*for (int i = 0; i < 100; i++) {
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now().format(DT.get()));
                *//*try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*//*
            });
        }*/
        executor.shutdown();
    }

    public static class MyThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public MyThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            System.out.println("a new thread [" +  t.getName() + "], hashCode [" + r.hashCode() + "]");
            return t;
        }
    }

    static class MyQueue<T> extends ArrayBlockingQueue<T> {

        private static final long serialVersionUID = 5038415840368060202L;

        public MyQueue(int capacity) {
            super(capacity);
            System.out.println("create MyQueue");
        }

        @Override
        public boolean add(T t) {
            System.out.println("add MyQueue [" + t.toString()  + "]");
            return super.add(t);
        }

        @Override
        public boolean offer(T t) {
            System.out.println("offer MyQueue [" + t.toString()  + "]");
            return super.offer(t);
        }

        @Override
        public T peek() {
            final T peek = super.peek();
            System.out.println("peek MyQueue ");
            return peek;
        }

        @Override
        public T poll() {
            System.out.println("poll MyQueue ");
            return super.poll();
        }

        @Override
        public void put(T t) throws InterruptedException {
            System.out.println("put MyQueue [" + t.toString()  + "]");
            super.put(t);
        }

        @Override
        public boolean remove(Object o) {
            System.out.println("remove MyQueue [" + o.toString()  + "]");
            return super.remove(o);
        }
    }

    static class MyQueue1<T> extends ArrayBlockingQueue<T> {

        public MyQueue1(int capacity) {
            super(capacity);
            System.out.println("new MyQueue1 1: " + capacity);
        }

        public MyQueue1(int capacity, boolean fair) {
            super(capacity, fair);
            System.out.println("new MyQueue1 2: " + capacity);
        }

        public MyQueue1(int capacity, boolean fair, Collection<? extends T> c) {
            super(capacity, fair, c);
            System.out.println("new MyQueue1 3: " + capacity);
        }

        @Override
        public boolean add(T t) {
            System.out.println("add(T t) [" + t + "]");
            return super.add(t);
        }

        @Override
        public boolean offer(T t) {
            System.out.println("offer(T t) [" + t + "]");
            return super.offer(t);
        }

        @Override
        public void put(T t) throws InterruptedException {
            System.out.println("put(T t) [" + t + "]");
            super.put(t);
        }

        @Override
        public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
            System.out.println("offer(T t, long timeout, TimeUnit unit) [" + t + "]");
            return super.offer(t, timeout, unit);
        }

        @Override
        public T poll() {
            T t = super.poll();
            System.out.println("poll() [" + t + "]");
            return t;
        }

        @Override
        public T take() throws InterruptedException {
            T t = super.take();
            System.out.println("poll() [" + t + "]");
            return t;
        }

        @Override
        public T poll(long timeout, TimeUnit unit) throws InterruptedException {
            T t = super.poll(timeout, unit);
            System.out.println("poll(long timeout, TimeUnit unit) [" + t + "]");
            return t;
        }

        @Override
        public T peek() {
            T t = super.peek();
            System.out.println("peek() [" + t + "]");
            return t;
        }

        @Override
        public int size() {
            int t = super.size();
            System.out.println("size() [" + t + "]");
            return t;
        }

        @Override
        public int remainingCapacity() {
            return super.remainingCapacity();
        }

        @Override
        public boolean remove(Object o) {
            return super.remove(o);
        }

        @Override
        public boolean contains(Object o) {
            return super.contains(o);
        }

        @Override
        public Object[] toArray() {
            return super.toArray();
        }

        @Override
        public <T1> T1[] toArray(T1[] a) {
            return super.toArray(a);
        }

        @Override
        public String toString() {
            return super.toString();
        }

        @Override
        public void clear() {
            super.clear();
        }

        @Override
        public int drainTo(Collection<? super T> c) {
            return super.drainTo(c);
        }

        @Override
        public int drainTo(Collection<? super T> c, int maxElements) {
            return super.drainTo(c, maxElements);
        }

        @Override
        public Iterator<T> iterator() {
            return super.iterator();
        }

        @Override
        public Spliterator<T> spliterator() {
            return super.spliterator();
        }

        @Override
        public T remove() {
            return super.remove();
        }

        @Override
        public T element() {
            return super.element();
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            return super.addAll(c);
        }

        @Override
        public boolean isEmpty() {
            return super.isEmpty();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return super.containsAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return super.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return super.retainAll(c);
        }

        @Override
        public boolean removeIf(Predicate<? super T> filter) {
            return false;
        }

        @Override
        public Stream<T> stream() {
            return null;
        }

        @Override
        public Stream<T> parallelStream() {
            return null;
        }

        @Override
        public void forEach(Consumer<? super T> action) {

        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            System.out.println("clone");
            return super.clone();
        }

        @Override
        protected void finalize() throws Throwable {
            System.out.println("finalize");
            super.finalize();
        }


    }

}
