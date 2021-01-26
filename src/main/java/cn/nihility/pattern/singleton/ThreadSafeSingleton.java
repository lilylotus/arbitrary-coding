package cn.nihility.pattern.singleton;

/**
 * 单例设计模式，懒汉方式，线程安全
 */
public class ThreadSafeSingleton {

    private static ThreadSafeSingleton instance;

    private ThreadSafeSingleton() {}

    /**
     * 采用 synchronized 关键字，实现类锁，性能不高
     */
    public static synchronized ThreadSafeSingleton getInstance() {
        if (null == instance) {
            instance = new ThreadSafeSingleton();
        }
        return instance;
    }

    /**
     * 采用 double check locking 方式，既保证了线程安全又保证了性能
     */
    public static ThreadSafeSingleton getInstanceUsingDoubleLocking() {
        if (null == instance) {
            synchronized (ThreadSafeSingleton.class) {
                if (null == instance) {
                    instance = new ThreadSafeSingleton();
                }
            }
        }
        return instance;
    }

}
