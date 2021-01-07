package cn.nihility.registrar2;

import cn.nihility.registrar2.entity.SelectEntity;
import cn.nihility.registrar2.annotation.Select;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;
import java.util.StringJoiner;

public class FactoryBeanStarter implements FactoryBean<Object>, BeanClassLoaderAware {

    private Class<?> mapperInterface;
    private ClassLoader classLoader;

    public FactoryBeanStarter() {
        System.out.println("FactoryBeanStarter Constructor");
    }

    public FactoryBeanStarter(Class<?> mapperInterface) {
        this.mapperInterface = mapperInterface;
        System.out.println("FactoryBeanStarter Constructor [" + mapperInterface.getName() + "]");
    }

    @Override
    public Object getObject() {
        return Proxy.newProxyInstance(classLoader,
                new Class<?>[] {mapperInterface},
                (proxy, method, args) -> {
                    System.out.println("proxy object [" + proxy.getClass().getName() + "]");
                    System.out.println("proxy method [" + method.getName() + "]");
                    System.out.println("proxy args [" + splitArgs(args) + "]");

                    Select annotation = method.getAnnotation(Select.class);
                    if (null != annotation) {
                        String sql = annotation.sql();
                        System.out.println("Exec sql [" + sql + "]");
                        return new SelectEntity(sql);
                    }
                    return new SelectEntity("Factory Bean Proxy Default Entity");
                });
    }

    private String splitArgs(Object[] args) {
        if (args == null) {
            return "null";
        } else {
            StringJoiner joiner = new StringJoiner(",");
            for (Object arg : args) {
                joiner.add(String.valueOf(arg));
            }
            return joiner.toString();
        }
    }



    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
