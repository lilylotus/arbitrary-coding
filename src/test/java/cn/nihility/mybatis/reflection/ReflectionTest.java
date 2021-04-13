package cn.nihility.mybatis.reflection;

import cn.nihility.mybatis.pojo.OtherReflectorObject;
import cn.nihility.mybatis.pojo.ReflectorObject;
import cn.nihility.mybatis.reflection.factory.DefaultObjectFactory;
import cn.nihility.mybatis.reflection.factory.DefaultReflectorFactory;
import cn.nihility.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author orchid
 * @date 2021-04-12 22:04:58
 */
public class ReflectionTest {

    @Test
    public void testMateObject() {
        ReflectorObject object = new ReflectorObject();
        OtherReflectorObject other = new OtherReflectorObject();
        object.setOtherReflectorObject(other);
        other.add(object);
        other.add(object);
        other.add(object);

        MetaObject metaObject = new MetaObject(object, new DefaultObjectFactory(),
            new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());

        String pro = "stringObject";
        String proList = "stringList[0]";

        String otherProName = "otherReflectorObject.name";
        String otherProAge = "otherReflectorObject.age";
        String otherProStringList = "otherReflectorObject.stringList[0]";
        String otherProStringList2 = "otherReflectorObject.stringList";
        String otherProStringList3 = "otherReflectorObject.reflectorObjectList[0].stringObject";
        String otherProStringMap = "otherReflectorObject.stringMap[key]";

        Assertions.assertTrue(metaObject.hasSetter(pro));
        Assertions.assertTrue(metaObject.hasSetter(proList));
        Assertions.assertTrue(metaObject.hasSetter(otherProName));
        Assertions.assertTrue(metaObject.hasSetter(otherProAge));
        Assertions.assertTrue(metaObject.hasSetter(otherProStringList));
        Assertions.assertTrue(metaObject.hasSetter(otherProStringMap));

        String proValue = "proValue";
        metaObject.setValue(pro, proValue);
        metaObject.setValue(otherProAge, 20);
        Assertions.assertTrue(metaObject.hasSetter(otherProStringList));

        String mapValue = "mapValue";
        metaObject.setValue(otherProStringMap, mapValue);

        metaObject.setValue(otherProStringList2, new ArrayList<>());
        //metaObject.setValue(otherProStringList, "string0");
        Assertions.assertEquals(proValue, metaObject.getValue(otherProStringList3));

        Assertions.assertEquals(mapValue, metaObject.getValue(otherProStringMap));

    }

    @Test
    public void testMetaObjectAddList() {
        ReflectorObject object = new ReflectorObject();
        String osPro = "otherReflectorObject.stringList";
        String osPro1 = "otherReflectorObject.stringList[0]";
        MetaObject metaObject = new MetaObject(object, new DefaultObjectFactory(),
            new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());

        Assertions.assertTrue(metaObject.hasSetter(osPro));

        String stringValue = "firstString";
        List<String> data = new ArrayList<>();
        data.add(stringValue);
        metaObject.setValue(osPro, data);

        @SuppressWarnings("unchecked")
        List<String> dataList = (List<String>) metaObject.getValue(osPro);
        Assertions.assertEquals(stringValue, dataList.get(0));

        Object stringValue1 = metaObject.getValue(osPro1);
        Assertions.assertEquals(stringValue, stringValue1);

    }

}
