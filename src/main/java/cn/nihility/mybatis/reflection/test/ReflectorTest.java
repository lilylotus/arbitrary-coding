package cn.nihility.mybatis.reflection.test;

import cn.nihility.mybatis.reflection.MetaClass;
import cn.nihility.mybatis.reflection.MetaObject;
import cn.nihility.mybatis.reflection.Reflector;
import cn.nihility.mybatis.reflection.factory.DefaultObjectFactory;
import cn.nihility.mybatis.reflection.factory.DefaultReflectorFactory;
import cn.nihility.mybatis.reflection.invoker.Invoker;
import cn.nihility.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;

import java.lang.reflect.InvocationTargetException;

public class ReflectorTest {

    public static void main(String[] args) {

        ReflectorObject object = new ReflectorObject();
        MetaObject metaObject = new MetaObject(object, new DefaultObjectFactory(),
                new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());

        String pro = "stringObject";
        String listPro = "innerObject.stringList[0]";

        String innerPro = "innerObject." + pro;
        System.out.println(metaObject.hasGetter(pro));
        System.out.println(metaObject.hasSetter(innerPro));

        metaObject.setValue(pro, "nihao");
        System.out.println(metaObject.getValue(pro));

        metaObject.setValue(innerPro, "innerProValue");
        System.out.println(metaObject.getValue(innerPro));

        metaObject.setValue(listPro, "listFirst");
        System.out.println(metaObject.getValue(listPro));
    }

    public static void main2(String[] args) throws InvocationTargetException, IllegalAccessException {
        Reflector reflector = new Reflector(ReflectorObject.class);
        Reflector reflector2 = new Reflector(ReflectorObjectExtend.class);

        System.out.println(reflector.printFieldMethod());
        System.out.println(reflector2.printFieldMethod());

        MetaClass metaClass = new MetaClass(ReflectorObject.class, new DefaultReflectorFactory());

        String pro = "stringObject";
        String stringObject = metaClass.findProperty(pro);
        System.out.println(stringObject);

        ReflectorObject reflectorObject = new ReflectorObject();
        Invoker setInvoker = metaClass.getSetInvoker(pro);
        setInvoker.invoke(reflectorObject, new Object[] {"setValue"});

        Invoker proInvoker = metaClass.getGetInvoker(pro);
        System.out.println(proInvoker.invoke(reflectorObject, new Object[]{}));

        Class<?> clazz = metaClass.getGetterType("stringList");
        System.out.println(clazz);

    }

}
