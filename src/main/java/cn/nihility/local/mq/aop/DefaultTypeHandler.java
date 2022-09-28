package cn.nihility.local.mq.aop;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的常见类默认值处理器
 *
 * @author yuanzx
 * @date 2022/09/27 16:13
 */
public class DefaultTypeHandler {

    private static final Map<Type, Object> DEFAULT_TYPE_VALUE = new ConcurrentHashMap<>();

    static {
        register(Boolean.class, Boolean.FALSE);
        register(boolean.class, Boolean.FALSE);

        register(Byte.class, (byte) 0);
        register(byte.class, (byte) 0);

        register(Short.class, (short) 0);
        register(short.class, (short) 0);

        register(Integer.class, 0);
        register(int.class, 0);

        register(Long.class, 0L);
        register(long.class, 0L);

        register(Float.class, 0F);
        register(float.class, 0F);

        register(Double.class, 0D);
        register(double.class, 0D);

        register(String.class, "");
        register(List.class, Collections.emptyList());
        register(Map.class, Collections.emptyMap());

        register(Object.class, new Object());

        register(BigInteger.class, BigInteger.valueOf(0L));
        register(BigDecimal.class, BigDecimal.valueOf(0L));

        register(Byte[].class, new Byte[0]);
        register(byte[].class, new Byte[0]);

        register(Date.class, new Date());
        register(Instant.class, Instant.now());
        register(LocalDateTime.class, LocalDateTime.now());
        register(LocalDate.class, LocalDate.now());
        register(LocalTime.class, LocalTime.now());

        register(Character.class, "");
        register(char.class, Character.MIN_VALUE);
    }

    private DefaultTypeHandler() {
    }

    private static void register(Class<?> type, Object defaultValue) {
        DEFAULT_TYPE_VALUE.put(type, defaultValue);
    }

    public static Object mapping(Class<?> type) {
        if (type.isInterface()) {
            return null;
        }
        return DEFAULT_TYPE_VALUE.get(type);
    }

}
