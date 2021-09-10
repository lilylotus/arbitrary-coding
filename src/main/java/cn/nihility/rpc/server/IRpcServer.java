package cn.nihility.rpc.server;

public interface IRpcServer {

    /**
     * 启动服务
     */
    void start();

    /**
     * 关闭服务
     */
    void shutdown();

}
