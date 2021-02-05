package cn.nihility.restart.restart;

import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class RestartUtil {

    private static final String BOOT_INF_CLASS_PREFIX = "BOOT-INF/classes/";

    public static List<String> scanJarClassName(final URL jarUrl, final Logger log) {
        final List<String> classNameList = new ArrayList<>(60);
        File jarFile;
        try {
            jarFile = new File(jarUrl.toURI());
        } catch (URISyntaxException e) {
            return classNameList;
        }
        if (!jarFile.exists()) {
            log.warn("jar [{}] 不存在", jarUrl);
            return classNameList;
        }

        JarFile jar = null;
        try {
            jar = new JarFile(jarFile);
        } catch (IOException e) {
            log.error("解析 jar 出错，[{}]", jarUrl);
        }

        if (null != jar) {
            Enumeration<JarEntry> en = jar.entries();
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                String name = je.getName();
                //System.out.println("jar file : [" + name + "]");
                if (name.endsWith(".class")) {
                    // BOOT-INF/classes/cn/nihility/boot/controller/FindClassController.class
                    String clazz = name.replace(BOOT_INF_CLASS_PREFIX, "").replaceAll("/", ".").replace(".class", "");
                    classNameList.add(clazz);
                }
            }
        }

        return classNameList;
    }

    public static byte[] loadClassData(String className, URL jarUrl) {
        File jarFile;
        try {
            jarFile = new File(jarUrl.toURI());
        } catch (URISyntaxException e) {
            return null;
        }
        if (!jarFile.exists()) {
            return null;
        }

        JarFile jar;
        try {
            jar = new JarFile(jarFile);
        } catch (IOException e) {
            return null;
        }

        Enumeration<JarEntry> jarEntry = jar.entries();
        while (jarEntry.hasMoreElements()) {
            JarEntry je = jarEntry.nextElement();
            String jeName = je.getName();
            if (jeName.endsWith(".class")) {
                String clazz = jeName.replace(BOOT_INF_CLASS_PREFIX, "").replaceAll("/", ".").replace(".class", "");
                if (clazz.equals(className)) {
                    try (InputStream inputStream = jar.getInputStream(je)) {
                        ByteArrayOutputStream bao = new ByteArrayOutputStream(128);
                        int len;
                        byte[] buffer = new byte[1024];
                        while ((len = inputStream.read(buffer)) != -1) {
                            bao.write(buffer, 0, len);
                        }
                        byte[] clazzByteData = bao.toByteArray();
                        bao.close();
                        return clazzByteData;
                    } catch (IOException e) {
                        // nothing
                    }
                }
            }
        }

        return null;
    }

    public static String getResourcePath(String resource, URL jarUrl) {
        File jarFile;
        try {
            jarFile = new File(jarUrl.toURI());
        } catch (URISyntaxException e) {
            return null;
        }
        if (!jarFile.exists()) {
            return null;
        }

        JarFile jar;
        try {
            jar = new JarFile(jarFile);
        } catch (IOException e) {
            return null;
        }

        Enumeration<JarEntry> jarEntry = jar.entries();
        while (jarEntry.hasMoreElements()) {
            JarEntry je = jarEntry.nextElement();
            String jeName = je.getName();
            if (jeName.endsWith(".class")) {
                String clazz = jeName.replace(BOOT_INF_CLASS_PREFIX, "").replaceAll("/", ".").replace(".class", "");
                if (clazz.equals(resource)) {
                    return jeName;
                }
            } else if (jeName.equals(resource)) {
                return jeName;
            }
        }

        return null;
    }

}
