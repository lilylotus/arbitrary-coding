package cn.nihility.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class InstanceProxy {

    private final static Logger LOGGER = LoggerFactory.getLogger(InstanceProxy.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put("name", "你好");
        properties.put("age", "20");
        properties.put("active", "true");

        final ProClazz proClazz = new ProClazz();
        setTargetFromProperties(proClazz, properties);
        System.out.println(proClazz);
    }

    public static void setTargetFromProperties(final Object target, final Properties properties) {
        if (target == null || properties == null) {
            return;
        }

        List<Method> methods = Arrays.asList(target.getClass().getMethods());
        properties.forEach((key, value) -> setProperty(target, key.toString(), value, methods));
    }

    private static void setProperty(final Object target, final String propName, final Object propValue, final List<Method> methods) {
        // use the english locale to avoid the infamous turkish locale bug
        String methodName = "set" + propName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propName.substring(1);
        Method writeMethod = methods.stream().filter(m -> m.getName().equals(methodName) && m.getParameterCount() == 1).findFirst().orElse(null);

        if (writeMethod == null) {
            String methodName2 = "set" + propName.toUpperCase(Locale.ENGLISH);
            writeMethod = methods.stream().filter(m -> m.getName().equals(methodName2) && m.getParameterCount() == 1).findFirst().orElse(null);
        }

        if (writeMethod == null) {
            LOGGER.error("Property {} does not exist on target {}", propName, target.getClass());
            throw new RuntimeException(String.format("Property %s does not exist on target %s", propName, target.getClass()));
        }

        try {
            Class<?> paramClass = writeMethod.getParameterTypes()[0];
            if (paramClass == int.class || paramClass == Integer.class) {
                writeMethod.invoke(target, Integer.parseInt(propValue.toString()));
            } else if (paramClass == long.class || paramClass == Long.class) {
                writeMethod.invoke(target, Long.parseLong(propValue.toString()));
            } else if (paramClass == boolean.class || paramClass == Boolean.class) {
                writeMethod.invoke(target, Boolean.parseBoolean(propValue.toString()));
            } else if (paramClass == String.class) {
                writeMethod.invoke(target, propValue.toString());
            } else {
                writeMethod.invoke(target, propValue);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to set property {} on target {}", propName, target.getClass(), e);
            throw new RuntimeException(e);
        }
    }

    static class ProClazz {
        private String name;
        private Integer age;
        private Boolean active;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        @Override
        public String toString() {
            return "ProClazz{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", active=" + active +
                    '}';
        }
    }

}
