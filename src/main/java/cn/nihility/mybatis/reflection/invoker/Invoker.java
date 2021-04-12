package cn.nihility.mybatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;

/**
 * 调用接口
 */
public interface Invoker {

    Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;

    Class<?> getType();

}
