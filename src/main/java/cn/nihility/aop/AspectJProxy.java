package cn.nihility.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class AspectJProxy {

    public static void main(String[] args) {

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(CglibProxy.class);
        enhancer.setCallback(new MethodInterceptorImpl());
        CglibProxy instance = (CglibProxy) enhancer.create();

        System.out.println("----------------");
        System.out.println(instance);
        instance.say("say");


    }

    static class MethodInterceptorImpl implements MethodInterceptor {
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            System.out.println("Method Enhance. method [" + method.getName() + "]");
//            Object ret = proxy.invoke(obj, args);
            Object ret = proxy.invokeSuper(obj, args);
            System.out.println("Proxy ret [" + ret + "]");
            return ret;
        }
    }


    static class CglibProxy {
        public void say(String msg) {
            System.out.println("CglibProxy Say(). msg [" + msg + "]");
        }
    }

}
