package cn.nihility.restart.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ReloadClassLoader extends ClassLoader {

    private static final Logger log = LoggerFactory.getLogger(ReloadClassLoader.class);

    private static final String BOOT_INF_CLASS_PREFIX = "BOOT-INF/classes/";

    private final List<String> loadLibJarList;
    private final String mainClassLoaderJarPath;

    private StrictClassMap<String> classMapJarLocation = new StrictClassMap<>("ReloadClassLoader:class", 512);
    private Map<String, Class<?>> loadedClassMap = new HashMap<>(512);

    public ReloadClassLoader(List<String> loadLibJarList, String mainClassLoaderJarPath, ClassLoader parent) {
        super(parent);
        this.loadLibJarList = loadLibJarList;
        this.mainClassLoaderJarPath = mainClassLoaderJarPath;

        parseLoadLibJarList();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (classMapJarLocation.containsKey(name)) {
            return this.findClass(name);
        }
        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        log.debug("加载类 [{}]", name);
        if (classMapJarLocation.containsKey(name)) {
            byte[] clazzByteData;
            if (loadedClassMap.containsKey(name)) {
                return loadedClassMap.get(name);
            } else {
                clazzByteData = loadClassData(name, classMapJarLocation.get(name));
            }

            if (null == clazzByteData) {
                log.error("类 [{}] 未加载", name);
                return super.findClass(name);
            } else {
                Class<?> loadClazz = defineClass(name, clazzByteData, 0, clazzByteData.length);
                loadedClassMap.put(name, loadClazz);
                return loadClazz;
            }
        } else {
            super.findClass(name);
        }
        return super.findClass(name);
    }

    private byte[] loadClassData(String className, String jarPath) {
        File jarFile = new File(jarPath);
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

    /**
     * 解析指定加载 jar 的列表
     */
    private void parseLoadLibJarList() {
        if (null != loadLibJarList && loadLibJarList.size() > 0) {
            loadLibJarList.forEach(this::parseJarFile);
        }
    }

    private void parseJarFile(String jarFileLocation) {
        File jarFile = new File(jarFileLocation);
        if (!jarFile.exists()) {
            log.warn("jar [{}] 不存在", jarFileLocation);
            return;
        }

        JarFile jar = null;
        try {
            jar = new JarFile(jarFile);
        } catch (IOException e) {
            log.error("解析 jar 出错，[{}]", jarFileLocation);
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
                    classMapJarLocation.put(clazz, jarFileLocation);
                }
            }
        }
    }

}
