package cn.nihility.pattern.singleton;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * 利用反射机制破坏单例模式
 */
public class ReflectionSingletonTest {

    public static void main(String[] args) {
        EagerInitializedSingleton instanceOne = EagerInitializedSingleton.getInstance();
        EagerInitializedSingleton instanceTwo = null;

        final Constructor<?>[] constructors = EagerInitializedSingleton.class.getDeclaredConstructors();
        final Constructor<?> ct = Arrays.stream(constructors)
            .filter(constructor -> constructor.getParameterTypes().length == 0)
            .findAny().orElse(null);

        if (null != ct) {
            ct.setAccessible(true);
            try {
                instanceTwo = (EagerInitializedSingleton) ct.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        System.out.println(instanceOne.hashCode());
        if (null != instanceTwo) {
            System.out.println(instanceTwo.hashCode());
        }

    }

}
