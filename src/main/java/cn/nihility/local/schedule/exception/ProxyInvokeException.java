package cn.nihility.local.schedule.exception;

/**
 * @author yuanzx
 * @date 2022/09/26 17:00
 */
public class ProxyInvokeException extends RuntimeException {

    private static final long serialVersionUID = -7034800090745766939L;

    public ProxyInvokeException(String message) {
        super(message);
    }

    public ProxyInvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyInvokeException(Throwable cause) {
        super(cause);
    }

    public ProxyInvokeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
