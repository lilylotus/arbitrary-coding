package cn.nihility.limiting;

import java.util.Date;
import java.util.LinkedList;

/**
 * 漏斗算法
 * 请求来了之后会首先进到漏斗里，然后漏斗以恒定的速率将请求流出进行处理，从而起到平滑流量的作用
 * 当请求的流量过大时，漏斗达到最大容量时会溢出，此时请求被丢弃
 */
public class LeakyBucketLimiter {

    private final int capacity;//漏斗容量
    private final int rate;//漏斗速率
    private volatile int left;//剩余容量
    private LinkedList<Request> requestList;

    public LeakyBucketLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        this.left = capacity;
        requestList = new LinkedList<>();

        //开启一个定时线程，以固定的速率将漏斗中的请求流出，进行处理
        new Thread(() -> {
            while (true) {
                if (!requestList.isEmpty()) {
                    Request request = requestList.removeFirst();
                    handleRequest(request);
                }
                try {
                    Thread.sleep(1000 / rate); //睡眠
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleRequest(Request request) {
        request.setHandleTime(new Date());
        System.out.println(request.getCode() + "号请求被处理，请求发起时间："
                + request.getLaunchTime() + ",请求处理时间：" + request.getHandleTime() + ",处理耗时："
                + (request.getHandleTime().getTime() - request.getLaunchTime().getTime()) + "ms");
    }

    public synchronized boolean tryAcquire(Request request) {
        if (left <= 0) {
            return false;
        } else {
            left--;
            requestList.addLast(request);
            return true;
        }
    }

    public static void main(String[] args) {
        LeakyBucketLimiter leakyBucketLimiter = new LeakyBucketLimiter(5, 2);
        for (int i = 1; i <= 10; i++) {
            Request request = new Request(i, new Date());
            if (leakyBucketLimiter.tryAcquire(request)) {
                System.out.println(i + "号请求被接受");
            } else {
                System.out.println(i + "号请求被拒绝");
            }
        }
    }

    static class Request {
        private int code;
        private Date launchTime;
        private Date handleTime;

        public Request(int code, Date launchTime) {
            this.launchTime = launchTime;
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public void setHandleTime(Date handleTime) {
            this.handleTime = handleTime;
        }

        public Date getLaunchTime() {
            return launchTime;
        }

        public Date getHandleTime() {
            return handleTime;
        }
    }

}
