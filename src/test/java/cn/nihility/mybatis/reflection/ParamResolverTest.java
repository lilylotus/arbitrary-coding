package cn.nihility.mybatis.reflection;

import cn.nihility.mybatis.annotation.Param;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class ParamResolverTest {

    /**
     * {0=10, 1=20, param1=10, param2=20}
     */
    public int add(int x, int y) {
        return x + y;
    }

    /**
     * {0=10, 1=20, param1=10, param2=20}
     */
    public int add2(int x, @Param("p2") int y) {
        return x + y;
    }

    /**
     * {p1=10, param1=10}
     */
    public int echo(@Param("p1") int x) {
        return x;
    }

    @Test
    public void testParamResolver() throws NoSuchMethodException {
        Method method = ParamResolverTest.class.getDeclaredMethod("add", int.class, int.class);
        Method method2 = ParamResolverTest.class.getDeclaredMethod("add2", int.class, int.class);
        Method method1 = ParamResolverTest.class.getDeclaredMethod("echo", int.class);
        ParamNameResolver resolver = new ParamNameResolver(method, false);
        ParamNameResolver resolver1 = new ParamNameResolver(method1, false);
        ParamNameResolver resolver2 = new ParamNameResolver(method2, false);

        Integer[] params = new Integer[]{10, 20};
        Integer[] params1 = new Integer[]{10};

        Object paramResolverValue = resolver.getNamedParams(params);
        System.out.println(paramResolverValue);

        Object paramResolverValue2 = resolver2.getNamedParams(params);
        System.out.println(paramResolverValue2);

        Object p1Value = resolver1.getNamedParams(params1);
        System.out.println(p1Value);

    }

}
