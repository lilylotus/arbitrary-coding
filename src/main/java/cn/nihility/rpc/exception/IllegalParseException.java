package cn.nihility.rpc.exception;

public class IllegalParseException extends RuntimeException {
    private static final long serialVersionUID = 9179051204862661976L;

    public IllegalParseException() {
        super();
    }

    public IllegalParseException(String s) {
        super(s);
    }

    public IllegalParseException(Throwable cause) {
        super(cause);
    }

    public IllegalParseException(String s, Throwable cause) {
        super(s, cause);
    }

}
