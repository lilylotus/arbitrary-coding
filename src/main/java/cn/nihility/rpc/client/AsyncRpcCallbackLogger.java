package cn.nihility.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncRpcCallbackLogger implements AsyncRpcCallback {

    private static final Logger log = LoggerFactory.getLogger(AsyncRpcCallbackLogger.class);

    @Override
    public void success(Object result) {
        log.info("success receive result [{}]", result);
    }

    @Override
    public void fail(Exception e) {
        log.error("fail rpc invoke", e);
    }
    
}
