package cn.nihility.nio.single.server;

import java.io.IOException;

public class ReactorStarter {

    public static void main(String[] args) throws IOException {
        new Thread(new ReactorServer(2333)).start();
    }

}
