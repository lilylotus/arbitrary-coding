package cn.nihility.test;

import cn.nihility.entity.Walk;

import java.util.ServiceLoader;

/**
 * 需要在 META-INF/services 目录下建立以类名/接口名的文件
 * 如建立 cn.nihility.entity.Walk 接口全限定名的文件，会自动实例化写在该文件中实现类文件名接口的实现类
 */
public class ServiceLoaderDemo {

    public static void main(String[] args) {
        final ServiceLoader<Walk> load = ServiceLoader.load(Walk.class);
       load.forEach(w -> w.walk());
    }

}
