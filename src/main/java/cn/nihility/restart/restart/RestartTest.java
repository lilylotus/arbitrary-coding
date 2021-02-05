package cn.nihility.restart.restart;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

public class RestartTest extends URLClassLoader {

    private final List<JarResource> jarResourceList = new ArrayList<>(30);

    public RestartTest(Set<URL> jarUrlList, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        jarUrlList.forEach(jar -> jarResourceList.add(new JarResource(jar)));
    }

    /*public RestartTest(Set<URL> jarUrlList, URL[] urls, ClassLoader parent) {
        jarUrlList.forEach(jar -> jarResourceList.add(new JarResource(jar)));
    }*/

    public static void main(String[] args) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
        URL url = new URL("file", null, -1, "/D:/coding/idea/boot-learn/build/ad/ad.jar");
       /* ScanLoadLibJar scan = new ScanLoadLibJar("D:/load_path.txt");
        scan.scan();
        Set<URL> jarUrlList = scan.getLibJarUrlSet();

        URL url = new URL("file", null, -1, "/D:/coding/idea/boot-learn/build/ad/ad.jar");
        System.out.println(url.toURI());

        System.out.println(url.getProtocol());*/

        String factories = "META-INF/spring.factories";

        /*final URL resources = RestartUtil.getResourceUrl(factories, url);
        System.out.println(resources);
        System.out.println(RestartUtil.readContent(resources));*/

        final JarResource resource = new JarResource(url);
        final URL resourceUrl = resource.getResourceUrl(factories);
        System.out.println(resourceUrl);

        System.out.println(resource.getResourceContent(factories));


    }

    public static void main2(String[] args) throws ClassNotFoundException {
        ScanLoadLibJar scan = new ScanLoadLibJar("D:/load_path.txt");
        scan.scan();
        Set<URL> jarUrlList = scan.getLibJarUrlSet();
        URL[] urls = jarUrlList.toArray(new URL[0]);

        RestartTest restart = new RestartTest(jarUrlList, urls, RestartTest.class.getClassLoader());

        Class<?> aClass = restart.findClass("cn.nihility.test.Hei");
        System.out.println(aClass);
        String name = aClass.getName();
        System.out.println(name);

        try {
            Enumeration<URL> resources = restart.getResources("META-INF/spring.factories");
            System.out.println(resources);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                System.out.println(url);

                /*UrlResource urlResource = new UrlResource(url);
                System.out.println(urlResource.readResourceString());

                break;*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(aClass.getResource("/"));

        //System.out.println(restart.getClazzLoadPath(aClass));


    }

    private String getClazzLoadPath(Class<?> clazz) {
        ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = null;
        try {
            location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
        } catch (URISyntaxException e) {
            //
        }
        return (location != null) ? location.getSchemeSpecificPart() : null;
    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        return super.findResources(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        JarResource jar;
        if ((jar = containClass(name)) == null) {
            return super.loadClass(name, resolve);
        }

        return this.findClass(name, jar);
    }

    private Class<?> findClass(String name, JarResource jar) throws ClassNotFoundException {
        if (null == jar) {
            jar = containClass(name);
        }
        byte[] clazzByteData = jar.loadClassData(name);
        return clazzByteData == null ? super.findClass(name) : defineClass(name, clazzByteData, 0, clazzByteData.length);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return this.findClass(name, null);
    }

    private JarResource containClass(final String name) {
        return jarResourceList.stream().filter(jar -> jar.containClass(name)).findAny().orElse(null);

        /*JarResource resource = null;
        for (JarResource jar : jarResourceList) {
            if (jar.containClass(name)) {
                resource = jar;
                break;
            }
        }
        return resource;*/
    }

}
