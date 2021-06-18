package cn.nihility.restart.restart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class RestartClassLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(RestartClassLoader.class);

    private final List<JarResource> jarResourceList = new ArrayList<>(30);

    public RestartClassLoader(URL[] urls, ClassLoader parent, Set<URL> jarUrlSet) {
        super(urls, parent);
        jarUrlSet.forEach(this::addJarResource);
    }

    public void addJarResource(URL jarUrl) {
        if (jarUrl != null && (jarUrl.getProtocol().equals("file")
                || jarUrl.getProtocol().equals("jar") || jarUrl.getProtocol().equals("zip"))) {
            jarResourceList.add(new JarResource(jarUrl));
        }
    }

    private List<URL> getResourceList(final String resource) {
        final List<URL> resources = new ArrayList<>();
        jarResourceList.stream().map(r -> r.getResourceUrl(resource))
                .filter(Objects::nonNull).forEach(resources::add);
        return resources;
    }

    public static void main(String[] args) throws Exception {
        ScanLoadLibJar scan = new ScanLoadLibJar("D:\\load_path.txt");
        scan.scan();
        Set<URL> jarUrlList = scan.getLibJarUrlSet();
        URL[] urls = jarUrlList.toArray(new URL[0]);

        String resource = "META-INF/spring.factories";
        RestartClassLoader loader = new RestartClassLoader(urls, RestartClassLoader.class.getClassLoader(), jarUrlList);
        final List<URL> resUrlList = loader.getResourceList(resource);

        System.out.println(resUrlList.size());

        //final UrlListEnumeration<URL> em = new UrlListEnumeration<>(resUrlList);

        final Enumeration<URL> em = loader.getResources(resource);

        //final CompoundEnumeration<URL> enumeration = new CompoundEnumeration<>(null, em);

        while (em.hasMoreElements()) {
            System.out.println(em.nextElement());
        }

        final Class<?> aClass = Class.forName("cn.nihility.test.Hei", true, loader);
        System.out.println(aClass);

        final Method main = aClass.getDeclaredMethod("main", String[].class);
        main.invoke(null, new Object[]{new String[0]});
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> urlList = getResourceList(name);
        if (urlList.isEmpty()) {
            return super.getResources(name);
        } else {
            return new UrlListEnumeration<>(urlList);
        }
    }

    @Override
    public URL getResource(String name) {
        List<URL> urlList = getResourceList(name);
        if (urlList.isEmpty()) {
            return super.getResource(name);
        } else {
            return urlList.get(0);
        }
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
        byte[] clazzByteData = null;
        if (null == jar) {
            jar = containClass(name);
        }
        if (jar != null) {
            clazzByteData = jar.loadClassData(name);
        }
        return clazzByteData == null ? super.findClass(name) : defineClass(name, clazzByteData, 0, clazzByteData.length);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return this.findClass(name, null);
    }

    @Override
    protected void finalize() throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Finalized classloader [{}]", toString());
        }
        super.finalize();
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

    /**
     * Compound {@link Enumeration} that adds an additional item to the front.
     */
    private static class CompoundEnumeration<E> implements Enumeration<E> {

        private E firstElement;

        private final Enumeration<E> enumeration;

        CompoundEnumeration(E firstElement, Enumeration<E> enumeration) {
            this.firstElement = firstElement;
            this.enumeration = enumeration;
        }

        @Override
        public boolean hasMoreElements() {
            return (this.firstElement != null || this.enumeration.hasMoreElements());
        }

        @Override
        public E nextElement() {
            if (this.firstElement == null) {
                return this.enumeration.nextElement();
            }
            E element = this.firstElement;
            this.firstElement = null;
            return element;
        }

    }

    private static class UrlListEnumeration<E> implements Enumeration<E> {

        private final Iterator<E> iterator;

        private UrlListEnumeration(List<E> urlList) {
            iterator = urlList.iterator();
        }

        @Override
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        @Override
        public E nextElement() {
            return iterator.next();
        }
    }

}
