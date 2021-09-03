package cn.nihility.exception;

import java.io.IOException;

/**
 * ZIP 解压/压缩 异常
 */
public class ZipOperationException extends IOException {
    private static final long serialVersionUID = 6053347911910776459L;

    public ZipOperationException() {
    }

    public ZipOperationException(String message) {
        super(message);
    }

    public ZipOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZipOperationException(Throwable cause) {
        super(cause);
    }

}
