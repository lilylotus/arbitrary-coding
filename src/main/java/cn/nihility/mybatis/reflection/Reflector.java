package cn.nihility.mybatis.reflection;

import cn.nihility.mybatis.exception.ReflectionException;
import cn.nihility.mybatis.reflection.invoker.*;
import cn.nihility.mybatis.reflection.property.PropertyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.*;

public class Reflector {

    private final static Logger log = LoggerFactory.getLogger(Reflector.class);

    /**
     * 反射对象类型
     */
    private final Class<?> type;
    private final String[] readablePropertyNames;
    private final String[] writablePropertyNames;
    private final Map<String, Invoker> setMethods = new HashMap<>();
    // property : property get method
    private final Map<String, Invoker> getMethods = new HashMap<>();
    private final Map<String, Class<?>> setTypes = new HashMap<>();
    // property get return type
    private final Map<String, Class<?>> getTypes = new HashMap<>();
    private Constructor<?> defaultConstructor;

    private final Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

    public Reflector(Class<?> clazz) {
        this.type = clazz;
        // 获取默认构造函数， clazz()
        addDefaultConstructor(clazz);
        // 添加属性 get 方法
        addGetMethods(clazz);
        // 添加属性 set 方法
        addSetMethods(clazz);
        // 添加针对属性的 set/get
        addFields(clazz);
        readablePropertyNames = getMethods.keySet().toArray(new String[0]);
        writablePropertyNames = setMethods.keySet().toArray(new String[0]);
        for (String name : readablePropertyNames) {
            caseInsensitivePropertyMap.put(name.toUpperCase(Locale.ENGLISH), name);
        }
        for (String name : writablePropertyNames) {
            caseInsensitivePropertyMap.put(name.toUpperCase(Locale.ENGLISH), name);
        }
    }

    private void addDefaultConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Arrays.stream(constructors)
                .filter(constructor -> constructor.getParameterTypes().length == 0)
                .findAny()
                .ifPresent(constructor -> this.defaultConstructor = constructor);
    }

    private void addGetMethods(Class<?> clazz) {
        // 类属性名称:对应 get 方法集合
        final Map<String, List<Method>> conflictingGetters = new HashMap<>();
        // 获取类及其父类（超类）中的所有方法
        Method[] methods = getClassMethods(clazz);
        // 获取类属性的 get 方法
        Arrays.stream(methods)
                .filter(method -> method.getParameterTypes().length == 0 && PropertyName.isGetter(method.getName()))
                .forEach(method -> addMethodConflict(conflictingGetters,
                        PropertyName.methodToProperty(method.getName()), method));

        // 解决冲突
        resolveGetterConflicts(conflictingGetters);
    }

    private void addSetMethods(Class<?> clazz) {
        final Map<String, List<Method>> conflictingSetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        Arrays.stream(methods)
                .filter(m -> m.getParameterTypes().length == 1 && PropertyName.isSetter(m.getName()))
                .forEach(m -> addMethodConflict(conflictingSetters,
                        PropertyName.methodToProperty(m.getName()), m));
        resolveSetterConflicts(conflictingSetters);
    }

    private void addFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String property = field.getName();

            if (!setMethods.containsKey(property)) {
                int modifiers = field.getModifiers();
                if (!(Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers))) {
                    addSetField(field);
                }
            }

            if (!getMethods.containsKey(property)) {
                addGetField(field);
            }
        }

        // 解析其父类属性
        if (clazz.getSuperclass() != null) {
            addFields(clazz.getSuperclass());
        }
    }

    private void addGetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            if (log.isTraceEnabled()) {
                log.trace("parse [{}] field [{}] get", type.getName(), field.getName());
            }
            getMethods.put(field.getName(), new GetFieldInvoker(field));
            Type fieldType = TypeParameterResolver.resolveFieldType(field, this.type);
            getTypes.put(field.getName(), typeToClass(fieldType));
        }
    }

    private void addSetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            if (log.isTraceEnabled()) {
                log.trace("parse [{}] field [{}] set", type.getName(), field.getName());
            }
            setMethods.put(field.getName(), new SetFieldInvoker(field));
            Type fieldType = TypeParameterResolver.resolveFieldType(field, this.type);
            setTypes.put(field.getName(), typeToClass(fieldType));
        }
    }

    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
        for (Map.Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
            Method winner = null;
            String propertyName = entry.getKey();
            // 是否模糊/有歧义
            boolean isAmbiguous = false;
            for (Method candidate : entry.getValue()) {
                // 第一次
                if (winner == null) {
                    winner = candidate;
                    continue;
                }

                Class<?> winnerReturnType = winner.getReturnType();
                Class<?> candidateReturnType = candidate.getReturnType();
                if (candidateReturnType.equals(winnerReturnType)) {
                    if (!boolean.class.equals(candidateReturnType)) {
                        isAmbiguous = true;
                        break;
                    } else if (candidate.getName().startsWith("is")) {
                        winner = candidate;
                    }
                } else if (candidateReturnType.isAssignableFrom(winnerReturnType)) {
                    // getter 返回类型是子类型
                } else if (winnerReturnType.isAssignableFrom(candidateReturnType)) {
                    winner = candidate;
                } else {
                    isAmbiguous = true;
                    break;
                }
            }
            addGetMethod(propertyName, winner, isAmbiguous);
        }
    }

    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
        for (Map.Entry<String, List<Method>> entry : conflictingSetters.entrySet()) {
            List<Method> setters = entry.getValue();
            String propertyName = entry.getKey();

            Class<?> getterType = getTypes.get(propertyName);
            boolean isGetterAmbiguous = getMethods.get(propertyName) instanceof AmbiguousMethodInvoker;
            boolean isSetterAmbiguous = false;

            Method match = null;
            for (Method setter : setters) {
                if (!isGetterAmbiguous && setter.getParameterTypes()[0].equals(getterType)) {
                    // 最佳匹配
                    match = setter;
                    break;
                }
                if (!isSetterAmbiguous) {
                    match = pickBetterMatch(match, setter, propertyName);
                    isSetterAmbiguous = (match == null);
                }
            }
            if (null != match) {
                addSetMethod(match, propertyName);
            }
        }
    }

    private Method pickBetterMatch(Method setter1, Method setter2, String property) {
        if (null == setter1) {
            return setter2;
        }
        Class<?> parameterType1 = setter1.getParameterTypes()[0];
        Class<?> parameterType2 = setter2.getParameterTypes()[0];

        if (parameterType1.isAssignableFrom(parameterType2)) {
            return setter2;
        } else if (parameterType2.isAssignableFrom(parameterType1)) {
            return setter1;
        }
        MethodInvoker invoker = new AmbiguousMethodInvoker(setter1,
                MessageFormat.format(
                        "Ambiguous setters defined for property ''{0}'' in class ''{1}'' with types ''{2}'' and ''{3}''.",
                        property, setter2.getDeclaringClass().getName(), parameterType1.getName(), parameterType2.getName()));
        setMethods.put(property, invoker);
        Type[] paramTypes = TypeParameterResolver.resolveParamTypes(setter1, type);
        setTypes.put(property, typeToClass(paramTypes[0]));
        return null;
    }

    private void addGetMethod(String name, Method method, boolean isAmbiguous) {
        if (log.isTraceEnabled()) {
            log.trace("parse [{}] add property [{}] get method [{}] isAmbiguous [{}]",
                    type.getName(), name, method.getName(), isAmbiguous);
        }
        MethodInvoker invoker = isAmbiguous
                ? new AmbiguousMethodInvoker(method, MessageFormat.format(
                "Illegal overloaded getter method with ambiguous type for property ''{0}'' in class ''{1}''. " +
                        "This breaks the JavaBeans specification and can cause unpredictable results.",
                name, method.getDeclaringClass().getName()))
                : new MethodInvoker(method);
        getMethods.put(name, invoker);
        Type returnType = TypeParameterResolver.resolveReturnType(method, type);
        getTypes.put(name, typeToClass(returnType));
    }

    private void addSetMethod(Method method, String name) {
        if (log.isTraceEnabled()) {
            log.trace("parse [{}] add property [{}] set method [{}]",
                    type.getName(), name, method.getName());
        }
        MethodInvoker invoker = new MethodInvoker(method);
        setMethods.put(name, invoker);
        Type[] paramTypes = TypeParameterResolver.resolveParamTypes(method, type);
        setTypes.put(name, typeToClass(paramTypes[0]));
    }

    private Class<?> typeToClass(Type src) {
        Class<?> result = null;
        if (src instanceof Class) {
            result = (Class<?>) src;
        } else if (src instanceof ParameterizedType) {
            result = (Class<?>) ((ParameterizedType) src).getRawType();
        } else if (src instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) src).getGenericComponentType();
            if (componentType instanceof Class) {
                result = Array.newInstance((Class<?>) componentType, 0).getClass();
            } else {
                Class<?> componentClass = typeToClass(componentType);
                result = Array.newInstance(componentClass, 0).getClass();
            }
        }
        if (result == null) {
            result = Object.class;
        }
        return result;
    }

    private void addMethodConflict(Map<String, List<Method>> conflictingMethod, String name, Method method) {
        if (isValidPropertyName(name)) {
            List<Method> methodList = conflictingMethod.computeIfAbsent(name, v -> new ArrayList<>());
            methodList.add(method);
        }
    }

    private boolean isValidPropertyName(String name) {
        return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
    }

    /**
     * 获取类及其父类（超类）中的所有方法
     */
    private Method[] getClassMethods(Class<?> clazz) {
        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

            // 需要检查接口的方法，也许是抽象类
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                addUniqueMethods(uniqueMethods, anInterface.getDeclaredMethods());
            }

            // 处理父类
            currentClass = currentClass.getSuperclass();
        }

        Collection<Method> methods = uniqueMethods.values();
        return methods.toArray(new Method[0]);
    }

    private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            if (!currentMethod.isBridge()) {
                // returnType#methodName:param1TypeName,param2TypeName
                String signature = getSignature(currentMethod);
                // 检查改方法是否存在
                // 如果存在，则它的拓展类一定有一个覆写的方法
                if (!uniqueMethods.containsKey(signature)) {
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }

    private String getSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        if (null != returnType) {
            sb.append(returnType.getName()).append('#');
        }
        sb.append(method.getName());
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(i == 0 ? ':' : ',').append(parameterTypes[i].getName());
        }
        return sb.toString();
    }

    public String printFieldMethod() {
        StringJoiner get = new StringJoiner(",", "[", "]");
        StringJoiner set = new StringJoiner(",", "[", "]");
        getMethods.forEach((k, v) -> get.add(k + ":" + v.getType().getName()));
        setMethods.forEach((k, v) -> set.add(k + ":" + v.getType().getName()));
        return "get -> " + get.toString() + ". set -> " + set.toString();
    }

    /**
     * Gets the type for a property getter.
     *
     * @param propertyName - the name of the property
     * @return The Class of the property getter
     */
    public Class<?> getGetterType(String propertyName) {
        Class<?> clazz = getTypes.get(propertyName);
        if (clazz == null) {
            throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    public String findPropertyName(String name) {
        return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Gets an array of the readable properties for an object.
     *
     * @return The array
     */
    public String[] getGettablePropertyNames() {
        return readablePropertyNames;
    }

    /**
     * Gets an array of the writable properties for an object.
     *
     * @return The array
     */
    public String[] getSettablePropertyNames() {
        return writablePropertyNames;
    }

    /**
     * Gets the type for a property setter.
     *
     * @param propertyName - the name of the property
     * @return The Class of the property setter
     */
    public Class<?> getSetterType(String propertyName) {
        Class<?> clazz = setTypes.get(propertyName);
        if (clazz == null) {
            throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    public boolean hasDefaultConstructor() {
        return defaultConstructor != null;
    }

    public Invoker getSetInvoker(String propertyName) {
        Invoker method = setMethods.get(propertyName);
        if (method == null) {
            throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    public Invoker getGetInvoker(String propertyName) {
        Invoker method = getMethods.get(propertyName);
        if (method == null) {
            throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    /**
     * Gets the name of the class the instance provides information for.
     *
     * @return The class name
     */
    public Class<?> getType() {
        return type;
    }

    public Constructor<?> getDefaultConstructor() {
        if (defaultConstructor != null) {
            return defaultConstructor;
        } else {
            throw new ReflectionException("There is no default constructor for " + type);
        }
    }

    /**
     * Check to see if a class has a writable property by name.
     *
     * @param propertyName - the name of the property to check
     * @return True if the object has a writable property by the name
     */
    public boolean hasSetter(String propertyName) {
        return setMethods.containsKey(propertyName);
    }

    /**
     * Check to see if a class has a readable property by name.
     *
     * @param propertyName - the name of the property to check
     * @return True if the object has a readable property by the name
     */
    public boolean hasGetter(String propertyName) {
        return getMethods.containsKey(propertyName);
    }

    /**
     * Checks whether can control member accessible.
     *
     * @return If can control member accessible, it return {@literal true}
     * @since 3.5.0
     */
    public static boolean canControlMemberAccessible() {
        try {
            SecurityManager securityManager = System.getSecurityManager();
            if (null != securityManager) {
                securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
            }
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }

}
