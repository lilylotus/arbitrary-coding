package cn.nihility.mybatis;

import cn.nihility.mybatis.reflection.ParamNameUtil;

import java.lang.reflect.Method;

public class MTest {

    public static void main(String[] args) throws NoSuchMethodException {
        Method method = MTest.class.getDeclaredMethod("param", int.class, int.class, String.class);

        System.out.println(ParamNameUtil.getParamNames(method));

    }

    public void param(int a, int b, String c) {

    }

}
