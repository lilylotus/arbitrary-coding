package cn.nihility.mybatis.reflection.factory;

import cn.nihility.mybatis.reflection.Reflector;

public interface ReflectorFactory {

    boolean isClassCacheEnabled();

    void setClassCacheEnabled(boolean classCacheEnabled);

    Reflector findForClass(Class<?> type);

}
