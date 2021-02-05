package cn.nihility.restart.restart.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class RestartUtil {

    private static final Logger log = LoggerFactory.getLogger(RestartUtil.class);
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

    public static URL getResourceUrl(String resource, URL jarUrl) {
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

        // jar:file:/D:/coding/idea/boot-learn/build/ad/ad.jar!/META-INF/spring.factories
        Enumeration<JarEntry> jarEntry = jar.entries();
        while (jarEntry.hasMoreElements()) {
            JarEntry je = jarEntry.nextElement();
            String jeName = je.getName();

            if (jeName.equals(resource)) {
                final String baseUrl = jarUrl.toString();
                final String finalUrl = baseUrl + "!/" + jeName;
                if (jarUrl.getProtocol().equals("file")) {
                    try {
                        return new URL("jar", null, -1, finalUrl);
                    } catch (MalformedURLException e) {
                        log.error("创建 jar URL 异常 [{}]", finalUrl, e);
                    }
                } else if (jarUrl.getProtocol().equals("jar")) {
                    try {
                        return new URL(finalUrl);
                    } catch (MalformedURLException e) {
                        log.error("创建 jar URL 异常 [{}]", finalUrl, e);
                    }
                }
                break;
            }
        }

        return null;
    }

    public static InputStream getInputStream(URL url) throws IOException {
        URLConnection con = url.openConnection();
        ResourceUtils.useCachesIfNecessary(con);
        try {
            return con.getInputStream();
        } catch (IOException ex) {
            // Close the HTTP connection (if applicable).
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }

    public static String readContent(URL url) {
        final StringBuilder builder = new StringBuilder();

        if (url == null) {
            return null;
        }

        InputStream is = null;
        try {
            is = getInputStream(url);
        } catch (IOException e) {
            log.error("获取 url [{}] 连接出错", url, e);
        }

        if (is == null) {
            return builder.toString();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (IOException e) {
            log.error("读入 url [{}] 内容出错", url, e);
        }

        return builder.toString();
    }

}
