package cn.nihility.restart.restart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

public class RestartClassLoader extends URLClassLoader {

    private final static Logger log = LoggerFactory.getLogger(RestartClassLoader.class);

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

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name);
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

}
