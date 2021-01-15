package cn.nihility.thread;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.*;

public class ThreadPoolUtil {

    public static void main(String[] args) {
        /*
        * 3 种阻塞队列： 工作队列 workQueue [BlockingQueue<Runnable>]
        * 有界队列： ArrayBlockingQueue (基于数组的先进先出队列)
        * 可选的有界队列：LinkedBlockingQueue (基于链表的先进先出队列)
        * 无界队列：DelayQueue，PriorityBlockingQueue, SynchronousQueue (无缓冲的等待队列)
        *
        * 线程池创建工厂：class DefaultThreadFactory implements ThreadFactory
        *
        * 4 种拒绝策略： ThreadPoolExecutor.AbortPolicy
        * 拒绝策略： class AbortPolicy implements RejectedExecutionHandler
        * AbortPolicy：默认，队列满了丢任务抛出异常
        * DiscardPolicy：队列满了丢任务不异常
        * DiscardOldestPolicy：将最早进入队列的任务删，之后再尝试加入队列
        * CallerRunsPolicy：如果添加到线程池失败，那么主线程会自己去执行该任务
        *
        * */
        ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 10, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(8));

        execTask(executor);
    }

    public static void threadPool() {
        /*
        * 创建一个单线程的线程池
        * 线程池只有一个线程在工作，单线程串行执行所有任务；
        * 此线程池保证所有任务的执行顺序按照任务的提交顺序执行
        * corePoolSize、maximumPoolSize 都为 1 的线程池，无界队列，此线程池一般用于顺序执行任务
        * 缺点：队列大小为 Integer.MAX_VALUE ， 可能导致堆积大量等待执行的线程，导致 OOM
        * */
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        execTask(singleThreadExecutor);

        /*
        * 创建固定大小的线程池
        * 每次提交一个任务就创建一个线程，直到线程达到线程池的最大大小，一旦达到最大值就会保持不变
        * corePoolSize、maximumPoolSize一样，线程数不会超过 nThreads，也不会出现空闲线程回收
        * 任务执行顺序是无序的
        * 缺点：队列大小为 Integer.MAX_VALUE ， 可能导致堆积大量等待执行的线程，导致 OOM
        * */
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
        execTask(fixedThreadPool);


        /*
        * 创建一个可缓存的线程池
        * 如果线程池的大小超过了处理任务所需要的线程，会回收部分空闲的线程
        * 当任务数增加时，此线程池又可以智能的添加新线程来处理任务。
        * 此线程池不会对线程池大小做限制，线程池大小完全取决于系统能够创建的最大线程大小
        *
        * 任务执行顺序是无序的
        *
        * 无核心线程数，最大线程数为 int 的最大值
        * 使用 SynchronousQueue 队列，此队列不存放任务
        * 每次有任务过来如果没有空闲线程就会创建新的线程来执行此任务
        *
        * 缺点：允许的最大线程数为 Integer.MAX_VALUE，导致线程数量过多，导致 OOM
        * */
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        execTask(cachedThreadPool);

        /*
        * new DelayedWorkQueue() 采用的是延迟队列
        * 缺点：允许的最大线程数为 Integer.MAX_VALUE，导致线程数量过多，导致 OOM
        * */
        final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);
        /*
        * 会在固定的周期（period）速率调用任务
        * 若是任务执行时间大于 period，则任务执行完后立马执行下个任务
        * 若是任务执行时间小于 period，则下一个任务需要等待 period 到达后才能执行
        * initialDelay + 2 * period
        * */
        scheduledThreadPool.scheduleAtFixedRate(
                () -> System.out.println(timeFormatter.format(LocalTime.now()) + " scheduleAtFixedRate task execute"),
                1, 1, TimeUnit.SECONDS);
        /*
        * 每隔多少 period 时间执行任务，是在上一个任务执行完成后，等待 period 执行下一个任务
        * */
        scheduledThreadPool.scheduleWithFixedDelay(
                () -> System.out.println(timeFormatter.format(LocalTime.now()) + " scheduleWithFixedDelay task execute"),
                1, 1, TimeUnit.SECONDS);
    }

    static void execTask(ExecutorService executorService) {
        final Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 20; i++) {
            final String task = "worker[" + i + "]";
            try {
                executorService.execute(() -> {
                    try {
                        Thread.sleep(200L * random.nextInt(10));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + " : " + task);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        executorService.shutdown();
    }

}
