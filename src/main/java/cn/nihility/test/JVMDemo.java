package cn.nihility.test;

public class JVMDemo {

    private static final int _1K = 1024;
    private static final int _1M = _1K * _1K;
    private static final int _1G = _1M * _1M;


    public static void main(String[] args) throws InterruptedException {
        for (; ; ) {
            byte[] bytes = new byte[_1M];
            byte[] kb = new byte[_1K];
            Thread.sleep(300L);
        }

    }

}
