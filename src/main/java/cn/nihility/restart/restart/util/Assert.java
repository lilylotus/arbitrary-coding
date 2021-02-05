package cn.nihility.restart.restart.util;

public class Assert {

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasLength(String text, String message) {
        if (!(text != null && !text.isEmpty())) {
            throw new IllegalArgumentException(message);
        }
    }

}
