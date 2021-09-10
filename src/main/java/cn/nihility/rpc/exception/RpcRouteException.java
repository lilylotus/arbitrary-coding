package cn.nihility.rpc.exception;

public final class RpcRouteException extends RuntimeException {

    public RpcRouteException() {
        super();
    }

    public RpcRouteException(String message) {
        super(message);
    }

    public RpcRouteException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcRouteException(Throwable cause) {
        super(cause);
    }

    protected RpcRouteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
