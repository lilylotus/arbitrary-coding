package cn.nihility.local.schedule.util;

import cn.nihility.local.schedule.exception.ProxyInvokeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代理调用工具类
 *
 * @author yuanzx
 * @date 2022/09/26 16:12
 */
public class ProxyInvokeUtils {

    private static ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    private static ObjectFactory objectFactory = new DefaultObjectFactory();
    private static ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    private static final int ONE_METHOD_COUNT = 1;
    private static final int ZERO_ARG_PARAM_COUNT = 0;
    private static final String ARG_SPLIT_TAG = ":";
    private static final String NULL_STRING = "null";

    private ProxyInvokeUtils() {
    }

    /**
     * 获取调用的方法，仅会过滤 public 的方法，要是 parameterTypes 参数为空，当有方法重载时，默认获取第一个方法。
     *
     * @param clazz          反射调用的业务类
     * @param method         反射调用业务类中的方法名称
     * @param parameterTypes 方法参数类型列表
     * @return 反射调用的方法
     */
    public static Method getInvokeMethod(Class<?> clazz, String method, Class<?>[] parameterTypes) {
        Method result = null;

        List<Method> methodList = Arrays.stream(clazz.getMethods())
                .filter(m -> m.getName().equals(method)).collect(Collectors.toList());

        if (methodList.isEmpty()) {
            return null;
        } else if (methodList.size() == ONE_METHOD_COUNT) {
            result = methodList.get(0);
        } else {
            // 当参数为空（判定为为传）时，默认取第一个
            if (parameterTypes == null || parameterTypes.length == ZERO_ARG_PARAM_COUNT) {
                result = methodList.get(0);
            } else {
                try {
                    // 获取最符合方法参数的这个调用方法
                    result = clazz.getMethod(method, parameterTypes);
                } catch (NoSuchMethodException e) {
                    // NO-OP
                }
            }
        }

        return result;
    }

    /**
     * 获取在 spring ioc 容器中 bean 的对象
     *
     * @param beanClass spring ioc 容器中 bean 的类类型
     * @param beanName  在 ioc 容器中的名称
     * @return ioc 容器中加载的 bean 对象实例
     */
    public static Object getIocBeanInstance(ApplicationContext ctx, Class<?> beanClass, String beanName) {
        Object instance = null;

        try {
            instance = ctx.getBean(beanClass);
        } catch (BeansException ex) {
            // NO-OP
            try {
                instance = ctx.getBean(beanName, beanClass);
            } catch (BeansException ex2) {
                // NO-OP
                try {
                    instance = ctx.getBean(beanName);
                } catch (BeansException ex3) {
                    // NO-OP
                }
            }
        }

        return instance;

    }

    public static int getArrayLength(final Object array) {
        if (array == null) {
            return 0;
        }
        return Array.getLength(array);
    }

    /**
     * 调用方法传递的参数长度和方法定义的参数长度不匹配
     *
     * @param parameterCount 调用方法定义的参数长度
     * @param args           传入的参数长度
     * @return true - 传入参数长度和方法定义参数长度不一致
     */
    public static boolean invokeMethodArgLengthMismatch(int parameterCount, String[] args) {
        return parameterCount != getArrayLength(args);
    }

    /**
     * 将默认的空字符串和 null 字符串转为 null 对象
     *
     * @param value 字符串值
     * @return 解析后的字符值
     */
    private static String parseNullString(String value) {
        if (StringUtils.isBlank(value) || NULL_STRING.equals(value)) {
            return null;
        }
        return value;
    }

    public static MetaObject buildMetaObject(Object context) {
        return MetaObject.forObject(context, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    /**
     * 构建方法调用所需的参数列表
     *
     * @param handleArgs 方法的参数列表声明 ["message", "headers.action", null]
     * @param context    代理调用的上下文.
     * @return 方法调用所需参数
     */
    public static Object[] buildInvokeArgs(String[] handleArgs, Object context) {
        if (null == handleArgs || handleArgs.length == ZERO_ARG_PARAM_COUNT) {
            return new Object[0];
        }

        int index = 0;
        Object[] args = new Object[handleArgs.length];
        MetaObject mo = MetaObject.forObject(context, objectFactory, objectWrapperFactory, reflectorFactory);
        for (String arg : handleArgs) {
            if (StringUtils.isBlank(arg)) {
                args[index++] = null;
            } else {
                Object value;
                // 若是包含了 : 参数引用分隔 [上下文key:默认值]
                if (arg.contains(ARG_SPLIT_TAG)) {
                    String[] splitArgs = arg.split(ARG_SPLIT_TAG, -1);
                    String key = splitArgs[0];
                    value = mo.getValue(key);
                    if (value == null) {
                        value = parseNullString(splitArgs[1]);
                    }
                } else {
                    value = mo.getValue(arg);
                }
                args[index++] = value;
            }
        }

        return args;
    }

    /**
     * 代理调用执行方法
     *
     * @param instance 执行业务逻辑方法的实例
     * @param method   业务逻辑方法
     * @param args     方法所需参数
     * @return 方法执行后的返回值
     */
    public static Object invoke(Object instance, Method method, Object[] args) {
        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ProxyInvokeException("调用方法 [" + instance.getClass().getName()
                    + "#" + method.getName() + "] 异常", e);
        } catch (Exception ex) {
            throw new ProxyInvokeException("调用方法 [" + instance.getClass().getName()
                    + "#" + method.getName() + "] 业务异常", ex);
        }
    }

}
