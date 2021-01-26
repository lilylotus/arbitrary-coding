package cn.nihility.pattern.singleton;

/**
 * 单例模式，懒汉方式，内部实体类方式，懒加载
 * 线程安全
 */
public class InnerStaticSingleton {

    private InnerStaticSingleton() {}

    /**
     * 利用 java jvm 类加载机制，只有直接引用的实例才会被加载到内存
     */
    static class SingletonHelper {
        private static final InnerStaticSingleton instance = new InnerStaticSingleton();
    }

    public static InnerStaticSingleton getInstance() {
        return SingletonHelper.instance;
    }

}
