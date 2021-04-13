package cn.nihility.mybatis.reflection;

import cn.nihility.mybatis.annotation.Param;
import cn.nihility.mybatis.exception.BindingException;
import cn.nihility.mybatis.session.ResultHandler;
import cn.nihility.mybatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class ParamNameResolver {

    private static final String GENERIC_NAME_PREFIX = "param";

    private final SortedMap<Integer, String> names;
    private boolean hasParamAnnotation;

    public ParamNameResolver(Method method, boolean useActualParamName) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        final SortedMap<Integer, String> map = new TreeMap<>();

        int paramCount = parameterAnnotations.length;
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            if (isSpecialParameter(parameterTypes[paramIndex])) {
                // skip special param type
                continue;
            }
            String name = null;
            for (Annotation an : parameterAnnotations[paramIndex]) {
                if (an instanceof Param) {
                    hasParamAnnotation = true;
                    name = ((Param) an).value();
                    break;
                }
            }

            if (null == name) {
                if (useActualParamName) {
                    name = getActualParamName(method, paramIndex);
                }
                if (null == name) {
                    name = Integer.toString(map.size());
                }
            }
            map.put(paramIndex, name);
        }

        names = Collections.unmodifiableSortedMap(map);
    }

    /**
     * Returns parameter names referenced by SQL providers.
     */
    public String[] getNames() {
        return names.values().toArray(new String[0]);
    }

    /**
     * <p>
     * A single non-special parameter is returned without a name.
     * Multiple parameters are named using the naming rule.
     * In addition to the default names, this method also adds the generic names (param1, param2,
     * ...).
     * </p>
     *
     * {0=10, 1=20, param1=10, param2=20}
     * public int add(int x, int y) {}
     *
     * {0=10, p2=20, param1=10, param2=20}
     *
     * {p1=10, param1=10}
     * public int echo(@Param("p1") int x) {}
     */
    public Object getNamedParams(Object[] args) {
        /* names 实在初始化 ParamNameResolver 时已经解析完成, {0:arg0,1:arg1 ... }
         * Executable 对应一个 mapper 方法也是一个 SQL mapper.xml 语句段 statement
         * args -> 执行方法传递过来的实参
         * */
        final int paramCount = names.size();
        if (args == null || paramCount == 0) {
            return null;
            // 仅有一个参数，且没有 @Param 自定义参数名称注解
        } else if (!hasParamAnnotation && paramCount == 1) {
            return args[names.firstKey()];
        } else {
            final Map<String, Object> param = new ParamMap<>();
            int i = 0;
            for (Map.Entry<Integer, String> entry : names.entrySet()) {
                // {0:arg0} -> index:parameterName (JDK 默认的参数名称)
                param.put(entry.getValue(), args[entry.getKey()]);
                // add generic param names (param1, param2, ...)
                final String genericParamName = GENERIC_NAME_PREFIX + (i + 1);
                // ensure not to overwrite parameter named with @Param
                if (!names.containsValue(genericParamName)) {
                    param.put(genericParamName, args[entry.getKey()]);
                }
                i++;
            }
            return param;
        }
    }

    private String getActualParamName(Method method, int paramIndex) {
        return ParamNameUtil.getParamNames(method).get(paramIndex);
    }

    private static boolean isSpecialParameter(Class<?> clazz) {
        return RowBounds.class.isAssignableFrom(clazz) || ResultHandler.class.isAssignableFrom(clazz);
    }

    public static class ParamMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -2212268410512043556L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }

}
