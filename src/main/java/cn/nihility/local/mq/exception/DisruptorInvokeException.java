package cn.nihility.local.mq.exception;

/**
 * Disruptor 消息队列调用异常
 *
 * @author yuanzx
 * @date 2022/09/23 13:45
 */
public class DisruptorInvokeException extends RuntimeException {

    private static final long serialVersionUID = 3676443787468169176L;

    public DisruptorInvokeException() {
    }

    public DisruptorInvokeException(String message) {
        super(message);
    }

    public DisruptorInvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DisruptorInvokeException(Throwable cause) {
        super(cause);
    }

    public DisruptorInvokeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
