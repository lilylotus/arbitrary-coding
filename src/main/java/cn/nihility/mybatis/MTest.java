package cn.nihility.mybatis;

import cn.nihility.mybatis.reflection.ParamNameUtil;

import java.lang.reflect.Method;

public class MTest {

    public static void main(String[] args) {
        int index = 0;
        int v = index++; // 0
        int v1 = ++index; // 2

        System.out.println(v);
        System.out.println(v1);
    }

    public static void main2(String[] args) throws NoSuchMethodException {
        Method method = MTest.class.getDeclaredMethod("param", int.class, int.class, String.class);

        System.out.println(ParamNameUtil.getParamNames(method));

    }

    public void param(int a, int b, String c) {

    }

}
