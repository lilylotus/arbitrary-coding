package cn.nihility.spring.start;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class BootStart {

    public static void main(String[] args) throws Exception {
        String libsPath = "D:\\coding\\idea\\boot-learn\\build\\libs\\lib";
        File libFiles = new File(libsPath);

        List<URL> urls = new ArrayList<>(50);
        //File[] files = libFiles.listFiles(((file, s) -> s.endsWith(".jar")));
        File[] files = libFiles.listFiles();

        File classFile = new File("D:\\coding\\idea\\boot-learn\\out\\production\\classes");

        assert files != null;
        for (File file : files) {
            urls.add(file.toURI().toURL());
        }
        urls.add(classFile.toURI().toURL());

        URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), BootStart.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
        Class<?> mainClass = Class.forName("cn.nihility.boot.BootLearnApplication", false, classLoader);
        Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
        mainMethod.setAccessible(true);
        mainMethod.invoke(null, new Object[] { args });
    }

}
