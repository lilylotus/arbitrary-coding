package cn.nihility.rpc.common;


import cn.nihility.rpc.common.codec.RpcRequest;

public final class Beat {

    public static final int BEAT_INTERVAL = 30;

    public static final int BEAT_TIMEOUT = 3 * BEAT_INTERVAL;

    public static final String BEAT_ID = "BEAT_PING_PONG";

    public static final RpcRequest BEAT_PING = new RpcRequest(BEAT_ID);

    private Beat() {
    }

}
