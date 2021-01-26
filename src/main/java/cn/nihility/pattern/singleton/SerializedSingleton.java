package cn.nihility.pattern.singleton;

import java.io.Serializable;

/**
 * 分布式系统
 */
public class SerializedSingleton implements Serializable {
    private static final long serialVersionUID = -8959442833159638793L;

    private SerializedSingleton(){}

    private static class SingletonHelper{
        private static final SerializedSingleton instance = new SerializedSingleton();
    }

    public static SerializedSingleton getInstance(){
        return SingletonHelper.instance;
    }

    /**
     * 防止序列化后破环单例
     */
    protected Object readResolve() {
        return getInstance();
    }

}
