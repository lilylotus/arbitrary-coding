package cn.nihility.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class MyScope implements Scope {

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        System.out.println("获取 bean [" + name + "]");
        return objectFactory.getObject();
    }

    @Override
    public Object remove(String name) {
        System.out.println("remove");
        return null;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        System.out.println("registerDestructionCallback");
    }

    @Override
    public Object resolveContextualObject(String key) {
        System.out.println("resolveContextualObject");
        return null;
    }

    @Override
    public String getConversationId() {
        System.out.println("getConversationId");
        return null;
    }
}
