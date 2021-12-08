package cn.nihility.nio.multi;

import java.io.IOException;

public class MultiRectorServerStarter {

    public static void main(String[] args) throws IOException {
        MultiReactorServer server = new MultiReactorServer(2333);
        new Thread(server).start();
    }

}
