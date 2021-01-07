package cn.nihility.aop;

import java.lang.reflect.Proxy;

public class JDKProxy {

    public static void main(String[] args) {

        final Object proxyInstance = new ProxyInterfaceImpl();

        ProxyInterface instance = (ProxyInterface) Proxy.newProxyInstance(JDKProxy.class.getClassLoader(), new Class[]{ProxyInterface.class},
                (proxy, method, argv) -> {
                    System.out.println("Proxy Method [" + method.getName() + "]");
                    System.out.println("Declaring Class [" + method.getDeclaringClass().getName() + "]");
                    if (method.getDeclaringClass() == Object.class) {
                        return method.invoke(proxyInstance, argv);
                    } else {

                        return method.invoke(proxyInstance, "Proxy Invoke");
                    }
                });


        instance.say("null");

        System.out.println(instance);

    }

    interface ProxyInterface {
        void say(String msg);
    }

    static class ProxyInterfaceImpl implements ProxyInterface {
        @Override
        public void say(String msg) {
            System.out.println("ProxyInterfaceImpl Say(). msg [" + msg + "]");
        }
    }

}
