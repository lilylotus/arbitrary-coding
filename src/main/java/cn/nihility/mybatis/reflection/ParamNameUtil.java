package cn.nihility.mybatis.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParamNameUtil {

    public static List<String> getParamNames(Method method) {
        return getParameterNames(method);
    }

    public static List<String> getParamNames(Constructor<?> constructor) {
        return getParameterNames(constructor);
    }

    /**
     * 获取方法参数名称 -> arg0, arg1, arg2
     * @param executable 获取参数名称的方法
     * @return 方法入参名称
     */
    private static List<String> getParameterNames(Executable executable) {
        return Arrays.stream(executable.getParameters()).map(Parameter::getName).collect(Collectors.toList());
    }

    private ParamNameUtil() {
        super();
    }

}
