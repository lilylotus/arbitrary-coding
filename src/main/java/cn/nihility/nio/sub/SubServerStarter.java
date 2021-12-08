package cn.nihility.nio.sub;

import java.io.IOException;

public class SubServerStarter {

    public static void main(String[] args) throws IOException {
        final MasterReactorServer server = new MasterReactorServer(2333);
        server.addSubSelector(new SubReactorSelector());
        new Thread(server).start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
    }

}
