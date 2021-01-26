package cn.nihility.pattern.singleton;

/**
 * 单例模式，延迟初始化（懒汉）方式
 * 懒汉简单实现方式，非线程安全
 */
public class LazyInitializedSingleton {

    private static LazyInitializedSingleton instance;

    private LazyInitializedSingleton() {}

    public static LazyInitializedSingleton getInstance() {
        if (null == instance) {
            instance = new LazyInitializedSingleton();
        }
        return instance;
    }

}
