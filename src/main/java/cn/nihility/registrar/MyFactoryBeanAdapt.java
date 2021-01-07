package cn.nihility.registrar;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;
import java.util.Arrays;

public class MyFactoryBeanAdapt implements FactoryBean {

    private Class<?> mapperInterface;

    public MyFactoryBeanAdapt() {
        System.out.println("MyFactoryBeanAdapt()");
    }

    public MyFactoryBeanAdapt(Class<?> mapperInterface) {
        System.out.println("MyFactoryBeanAdapt(interface)");
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object getObject() {
        return Proxy.newProxyInstance(UserMapper.class.getClassLoader(),
                new Class<?>[]{mapperInterface},
                (proxy, method, arg) -> {
                    System.out.println("proxy object : " + proxy.getClass().getName());
                    System.out.println("proxy method : " + method.getName());
                    System.out.println("proxy method args : " + Arrays.asList(arg));

                    Select annotation = method.getAnnotation(Select.class);
                    String selectSql = annotation.value()[0];
                    System.out.println("Select Sql : " + selectSql);

                    Integer arg0 = (Integer) arg[0];
                    System.out.println("Select param : " + arg0);

                    if (1 == arg0) {
                        return new User("Proxy User", 1);
                    }

                    return null;
                });
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    public Class<?> getMapperInterface() {
        return mapperInterface;
    }

    public void setMapperInterface(Class<?> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }
}
