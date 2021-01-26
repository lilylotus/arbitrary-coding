package cn.nihility.pattern.singleton;

/**
 * 单例模式，饿汉实现
 * Singleton classes are created for resources such as File System, Database connections, etc.
 * We should avoid the instantiation until unless client calls the getInstance method.
 * Also, this method doesn’t provide any options for exception handling.
 */
public class EagerInitializedSingleton {

    private static final EagerInitializedSingleton instance = new EagerInitializedSingleton();

    private EagerInitializedSingleton() {}

    public static EagerInitializedSingleton getInstance() {
        return instance;
    }

}
