package cn.nihility.limiting;

/**
 * 计数器滑动窗口算法是计数器固定窗口算法的改进，解决了固定窗口切换时可能会产生两倍于阈值流量请求的缺点。
 */
public class CounterSlideWindowLimiter {

    private final int windowSize; //窗口大小，毫秒为单位
    private final int limit;//窗口内限流大小
    private final int splitNum;//切分小窗口的数目大小
    private int[] counters;//每个小窗口的计数数组
    private int index;//当前小窗口计数器的索引
    private long startTime;//窗口开始时间

    public CounterSlideWindowLimiter(int windowSize, int limit, int splitNum) {
        this.limit = limit;
        this.windowSize = windowSize;
        this.splitNum = splitNum;
        counters = new int[splitNum];
        index = 0;
        startTime = System.currentTimeMillis();
    }


    public synchronized boolean tryAcquire() {
        long curTime = System.currentTimeMillis();
        //计算滑动小窗口的数量
        long windowsNum = Math.max(curTime - windowSize - startTime, 0) / (windowSize / splitNum);
        //滑动窗口
        slideWindow(windowsNum);
        int count = 0;
        for (int i = 0; i < splitNum; i++) {
            count += counters[i];
        }
        if (count >= limit) {
            return false;
        } else {
            counters[index]++;
            return true;
        }

    }

    private synchronized void slideWindow(long windowsNum) {
        if (0 == windowsNum) {
            return;
        }
        long slideNum = Math.min(windowsNum, splitNum);
        int currentIndex = index;
        for (int i = 0; i < slideNum; i++) {
            currentIndex = (currentIndex + 1) % splitNum;
            counters[currentIndex] = 0;
        }
        this.index = currentIndex;
        //更新滑动窗口时间
        startTime = startTime + windowsNum * (windowSize / splitNum);
    }

    public static void main(String[] args) throws InterruptedException {
        //每秒20个请求
        int limit = 20;
        CounterSlideWindowLimiter limiter = new CounterSlideWindowLimiter(1000, limit, 10);
        int count = 0;

        Thread.sleep(3000);
        //计数器滑动窗口算法模拟100组间隔30ms的50次请求
        System.out.println("计数器滑动窗口算法测试开始");
        System.out.println("开始模拟100组间隔150ms的50次请求");
        int faliCount = 0;
        for (int j = 0; j < 100; j++) {
            count = 0;
            for (int i = 0; i < 50; i++) {
                if (limiter.tryAcquire()) {
                    count++;
                }
            }
            Thread.sleep(150);
            //模拟50次请求，看多少能通过
            for (int i = 0; i < 50; i++) {
                if (limiter.tryAcquire()) {
                    count++;
                }
            }
            if (count > limit) {
                System.out.println("时间窗口内放过的请求超过阈值，放过的请求数" + count + ",限流：" + limit);
                faliCount++;
            }
            Thread.sleep((int) (Math.random() * 100));
        }
        System.out.println("计数器滑动窗口算法测试结束，100组间隔150ms的50次请求模拟完成，限流失败组数：" + faliCount);
        System.out.println("===========================================================================================");


        //计数器固定窗口算法模拟100组间隔30ms的50次请求
        System.out.println("计数器固定窗口算法测试开始");
        //模拟100组间隔30ms的50次请求
        CounterLimiter counterLimiter = new CounterLimiter(1000, limit);
        System.out.println("开始模拟100组间隔150ms的50次请求");
        faliCount = 0;
        for (int j = 0; j < 100; j++) {
            count = 0;
            for (int i = 0; i < 50; i++) {
                if (counterLimiter.tryAcquire()) {
                    count++;
                }
            }
            Thread.sleep(150);
            //模拟50次请求，看多少能通过
            for (int i = 0; i < 50; i++) {
                if (counterLimiter.tryAcquire()) {
                    count++;
                }
            }
            if (count > limit) {
                System.out.println("时间窗口内放过的请求超过阈值，放过的请求数" + count + ",限流：" + limit);
                faliCount++;
            }
            Thread.sleep((int) (Math.random() * 100));
        }
        System.out.println("计数器滑动窗口算法测试结束，100组间隔150ms的50次请求模拟完成，限流失败组数：" + faliCount);
    }

}
