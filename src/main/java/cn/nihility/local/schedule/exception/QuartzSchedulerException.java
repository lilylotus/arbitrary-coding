package cn.nihility.local.schedule.exception;

/**
 * quartz 自定义异常
 *
 * @author yuanzx
 */
public class QuartzSchedulerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public QuartzSchedulerException(String message) {
        super(message);
    }

    public QuartzSchedulerException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuartzSchedulerException(Throwable cause) {
        super(cause);
    }

    public QuartzSchedulerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
