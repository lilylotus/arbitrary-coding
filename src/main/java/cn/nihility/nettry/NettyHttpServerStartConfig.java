package cn.nihility.nettry;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyHttpServerStartConfig implements CommandLineRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NettyHttpServerStartConfig.class);

    @Override
    public void run(String... args) throws Exception {
        Thread nettyThread = new Thread(() -> {
            NettyHttpServer server = new NettyHttpServer(8089);
            log.info("Netty Http Server 启动");
            server.run();
        }, "Netty-Http-Server-Thread");
        nettyThread.start();
    }

}
