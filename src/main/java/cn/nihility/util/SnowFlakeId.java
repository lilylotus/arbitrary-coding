package cn.nihility.util;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * <p>
 * 1 位标识，由于 long 基本类型在 Java 中是带符号的，最高位是符号位，正数是 0，负数是 1，所以 id 一般是正数，最高位是 0
 * 41 位时间截(毫秒级)，注意，41 位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)，
 * 开始时间截一般是我们的id生成器开始使用的时间，由我们程序来指定的。
 * 41 位的时间截，可以使用 69 年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
 * 10 位的数据机器位，可以部署在 1024 个节点，包括5位 dataCenterId 和 5 位 workerId
 * 12 位序列，毫秒内的计数，12 位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生 4096 个 ID 序号
 * <p>
 * 加起来刚好 64 位，为一个 Long 型。(转换成字符串后长度最多 19)
 * SnowFlake 的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由 dataCenter 数据中心 ID 和 workerId 机器 ID 作区分)，并且效率较高，
 * 经测试，SnowFlake 每秒能够产生 26 万 ID 左右。
 * <p>
 * 优点：
 * 1.毫秒数在高位，自增序列在低位，整个ID都是趋势递增的。
 * 2.不依赖数据库等第三方系统，以服务的方式部署，稳定性更高，生成ID的性能也是非常高的。
 * 3.可以根据自身业务特性分配bit位，非常灵活。
 * <p>
 * 缺点：
 * 强依赖机器时钟，如果机器上时钟回拨，会导致发号重复或者服务会处于不可用状态。
 */
public class SnowFlakeId {

    /**
     * 起始的时间戳
     */
    private final static long START_STAMP = 1571533232000L;

    /**
     * 每一部分占用的位数
     */
    private final static long SEQUENCE_BIT = 12;   // 序列号占用的位数
    private final static long MACHINE_BIT = 5;     // 机器标识占用的位数
    private final static long DATA_CENTER_BIT = 5; // 数据中心占用的位数

    /**
     * 每一部分的最大值
     * 计算原理：
     * 1 向左移动 12 位数(结果是 13 位)： 1 << 12 = 1000000000000
     * 只需要 13 位数 -1 即 12 位的最大值，这里使用它的反码（1000000000000的反码 == 0111111111111）
     * ~ 表示该数的反码
     */
    private final static long MAX_DATA_CENTER_NUM = ~(-1L << DATA_CENTER_BIT);    // 31
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);        // 31
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);       // 4095

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATA_CENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATA_CENTER_LEFT + DATA_CENTER_BIT;

    private volatile long dataCenterId;   //数据中心
    private volatile long machineId;      //机器标识
    private volatile long sequence = 0L; //序列号
    private volatile long lastStamp = -1L;//上一次时间戳

    private static SnowFlakeId snowFlakeId;

    /**
     * 实例化
     *
     * @param dataCenterId 数据中心id
     * @param machineId    机器标识id
     */
    public SnowFlakeId(long dataCenterId, long machineId) {
        if (dataCenterId > MAX_DATA_CENTER_NUM || dataCenterId < 0) {
            throw new IllegalArgumentException("dataCenterId can't be greater than MAX_DATA_CENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    /**
     * 产生下一个 Long ID
     */
    public synchronized long nextId() {
        long currStamp = getNewStamp();
        if (currStamp < lastStamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id.");
        }

        if (currStamp == lastStamp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStamp = currStamp;
        // 时间戳部分 | 数据中心部分 | 机器标识部分 | 序列号部分
        return (currStamp - START_STAMP) << TIMESTAMP_LEFT
                | dataCenterId << DATA_CENTER_LEFT
                | machineId << MACHINE_LEFT
                | sequence;
    }

    /**
     * 下一个 Binary String ID
     */
    public String nextBinaryStringId() {
        return Long.toBinaryString(nextId());
    }

    /**
     * 下一个 Long String ID
     */
    public String nextLongStringId() {
        return Long.toString(nextId());
    }

    /**
     * 获取下一个最新的时间戳
     * 如果不是最新会一直循环到最新
     */
    private long getNextMill() {
        long mill = getNewStamp();
        while (mill <= lastStamp) {
            mill = getNewStamp();
        }
        return mill;
    }

    /**
     * 获得当前时间戳
     */
    private long getNewStamp() {
        return System.currentTimeMillis();
    }

    private static Long getWorkId() {
        try {
            String hostAddress = Inet4Address.getLocalHost().getHostAddress();
            System.out.println("host address [" + hostAddress + "]");
            int[] ints = StringUtils.toCodePoints(hostAddress);
            int sums = 0;
            for (int b : ints) {
                sums += b;
            }
            return (long) (sums % 32);
        } catch (UnknownHostException e) {
            // 如果获取失败，则使用随机数备用
            return RandomUtils.nextLong(0, 31);
        }
    }

    private static Long getDataCenterId() {
        final String hostName = SystemUtils.getHostName();
        System.out.println("host name [" + hostName + "]");
        int[] ints = StringUtils.toCodePoints(hostName);
        int sums = 0;
        for (int i : ints) {
            sums += i;
        }
        return (long) (sums % 32);
    }

    /**
     * 获取雪花算法的下一个 ID
     */
    public static long nextSnowId() {
        if (snowFlakeId == null) {
            synchronized (SnowFlakeId.class) {
                if (snowFlakeId == null) {
                    snowFlakeId = new SnowFlakeId(getDataCenterId(), getWorkId());
                }
            }
        }
        return snowFlakeId.nextId();
    }


    /* ======================= test ========================= */

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            /*System.out.println(snowFlake.nextLongStringId());*/
            SnowFlakeId.nextSnowId();
        }
        System.out.println((System.nanoTime() - startTime) / 1000000 + "ms");
    }

    public static void main3(String[] args) {
        SnowFlakeId snowFlake = new SnowFlakeId(20, 18);
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            /*System.out.println(snowFlake.nextLongStringId());*/
            snowFlake.nextId();
        }
        System.out.println((System.nanoTime() - startTime) / 1000000 + "ms");
    }

    public static void main1(String[] args) {
         /*
        0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
         */

        long start = System.currentTimeMillis();
        SnowFlakeId snowFlake = new SnowFlakeId(2, 3);
        Map<Long, Integer> longs = new HashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(500);
        for (int i = 0; i < 1000000; i++) {
            executorService.execute(() -> {
                Long tmp = snowFlake.nextId();

                if (!longs.containsKey(tmp)) {
                    longs.put(tmp, 0);
                    //System.out.println("生成新的：" + tmp + "二进制：" + Long.toBinaryString(tmp));
                } else {
                    int count = longs.get(tmp) + 1;
                    longs.put(tmp, count);
                    System.out.println("重复：" + tmp + " 次数：" + longs.get(tmp));
                }
            });
        }
        System.out.println("用时毫秒" + (System.currentTimeMillis() - start));
        executorService.shutdown();
    }

}
