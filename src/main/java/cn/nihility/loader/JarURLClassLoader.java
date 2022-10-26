package cn.nihility.loader;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarURLClassLoader {

    private final URL jar;
    private final URLClassLoader classLoader;

    public JarURLClassLoader(URL jar) {
        this.jar = jar;
        classLoader = new URLClassLoader(new URL[]{jar});
    }

    public Set<Class<?>> loadClass(Class<?> superClass, String basePackage) {
        try (JarFile jarFile = ((JarURLConnection) jar.openConnection()).getJarFile()) {
            return loadClassFromJar(superClass, basePackage, jarFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptySet();
    }

    public Class<?> loadClass(String fullClassPath) {
        try (JarFile jarFile = ((JarURLConnection) jar.openConnection()).getJarFile()) {
            return loadClassFromJar(fullClassPath, jarFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Class<?> loadClassFromJar(String fullClassPath, JarFile jar) {
        String classFullName = fullClassPath.replace(".", "/");
        Enumeration<JarEntry> entries = jar.entries();
        Class<?> clazz = null;
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String entryName = jarEntry.getName();
            if (entryName.charAt(0) == '/') {
                entryName = entryName.substring(1);
            }
            if (jarEntry.isDirectory() || !entryName.endsWith(".class") || !entryName.startsWith(classFullName)) {
                continue;
            }
            String className = entryName.substring(0, entryName.length() - 6);
            if (className.equals(classFullName)) {
                clazz = innerLoadClass(className.replace("/", "."));
                break;
            }
        }
        return clazz;
    }

    private Set<Class<?>> loadClassFromJar(Class<?> superClass, String basePackage, JarFile jar) {
        Set<Class<?>> classes = new HashSet<>();
        String pkgPath = basePackage.replace(".", "/");
        Enumeration<JarEntry> entries = jar.entries();
        Class<?> clazz;
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String entryName = jarEntry.getName();
            if (entryName.charAt(0) == '/') {
                entryName = entryName.substring(1);
            }
            if (jarEntry.isDirectory() || !entryName.startsWith(pkgPath) || !entryName.endsWith(".class")) {
                continue;
            }
            String className = entryName.substring(0, entryName.length() - 6);
            clazz = innerLoadClass(className.replace("/", "."));
            if (clazz != null && !clazz.isInterface() && superClass.isAssignableFrom(clazz)) {
                classes.add(clazz);
            }
        }
        return classes;
    }

    private Class<?> innerLoadClass(String name) {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
