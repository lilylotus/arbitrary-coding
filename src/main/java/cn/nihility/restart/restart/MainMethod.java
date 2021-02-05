package cn.nihility.restart.restart;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MainMethod {

    private final Method method;

    MainMethod() {
        this(Thread.currentThread());
    }

    MainMethod(Thread thread) {
        if (null == thread) {
            throw new IllegalArgumentException("Thread must not be null");
        }
        this.method = getMainMethod(thread);
    }

    private Method getMainMethod(Thread thread) {
        for (StackTraceElement element : thread.getStackTrace()) {
            if ("main".equals(element.getMethodName())) {
                Method method = getMainMethod(element);
                if (method != null) {
                    return method;
                }
            }
        }
        throw new IllegalStateException("Unable to find main method");
    }

    private Method getMainMethod(StackTraceElement element) {
        try {
            Class<?> elementClass = Class.forName(element.getClassName());
            Method method = elementClass.getDeclaredMethod("main", String[].class);
            if (Modifier.isStatic(method.getModifiers())) {
                return method;
            }
        } catch (Exception ex) {
            // Ignore
        }
        return null;
    }

    /**
     * Returns the actual main method.
     *
     * @return the main method
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * Return the name of the declaring class.
     *
     * @return the declaring class name
     */
    public String getDeclaringClassName() {
        return this.method.getDeclaringClass().getName();
    }

}
