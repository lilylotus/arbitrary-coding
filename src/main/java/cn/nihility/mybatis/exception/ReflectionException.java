package cn.nihility.mybatis.exception;

public class ReflectionException extends RuntimeException {

    private static final long serialVersionUID = -4883311050761807635L;

    public ReflectionException() {
        super();
    }

    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionException(Throwable cause) {
        super(cause);
    }

}
