package cn.nihility.nio.single.client;

public class ReactorClientStarter {

    public static void main(String[] args) {
        System.out.println("开始");
        new Thread(new ReactorClient("127.0.0.1", 2333)).start();
        new Thread(new ReactorClient("127.0.0.1", 2333)).start();
        System.out.println("结束");
    }

}
