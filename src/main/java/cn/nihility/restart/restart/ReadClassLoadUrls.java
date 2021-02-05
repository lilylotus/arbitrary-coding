package cn.nihility.restart.restart;

import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public class ReadClassLoadUrls {

    public static List<URL> fromClassLoader(ClassLoader classLoader) {
        List<URL> urls = new ArrayList<>();
        for (URL url : urlsFromClassLoader(classLoader)) {
            urls.add(url);
            urls.addAll(getUrlsFromClassPathOfJarManifestIfPossible(url));
        }
        return fromUrls(urls);
    }

    public static List<URL> fromUrls(Collection<URL> urls) {
        return fromUrls(new ArrayList<>(urls).toArray(new URL[urls.size()]));
    }

    public static List<URL> fromUrls(URL... urls) {
        List<URL> reloadableUrls = new ArrayList<>(urls.length);
        reloadableUrls.addAll(Arrays.asList(urls));
        return Collections.unmodifiableList(reloadableUrls);
    }

    private static URL[] urlsFromClassLoader(ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader) {
            return ((URLClassLoader) classLoader).getURLs();
        }
        return Stream.of(ManagementFactory.getRuntimeMXBean()
                .getClassPath().split(File.pathSeparator))
                .map(ReadClassLoadUrls::toURL).toArray(URL[]::new);
    }

    private static URL toURL(String classPathEntry) {
        try {
            return new File(classPathEntry).toURI().toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(
                    "URL could not be created from '" + classPathEntry + "'", ex);
        }
    }

    private static List<URL> getUrlsFromClassPathOfJarManifestIfPossible(URL url) {
        JarFile jarFile = getJarFileIfPossible(url);
        if (jarFile == null) {
            return Collections.emptyList();
        }
        try {
            return getUrlsFromManifestClassPathAttribute(url, jarFile);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Failed to read Class-Path attribute from manifest of jar " + url,
                    ex);
        }
    }

    private static JarFile getJarFileIfPossible(URL url) {
        try {
            File file = new File(url.toURI());
            if (file.isFile()) {
                return new JarFile(file);
            }
        } catch (Exception ex) {
            // Assume it's not a jar and continue
        }
        return null;
    }

    private static List<URL> getUrlsFromManifestClassPathAttribute(URL jarUrl, JarFile jarFile) throws IOException {
        Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            return Collections.emptyList();
        }
        String classPath = manifest.getMainAttributes()
                .getValue(Attributes.Name.CLASS_PATH);
        if (!StringUtils.hasText(classPath)) {
            return Collections.emptyList();
        }
        String[] entries = StringUtils.delimitedListToStringArray(classPath, " ");
        List<URL> urls = new ArrayList<>(entries.length);
        List<URL> nonExistentEntries = new ArrayList<>();
        for (String entry : entries) {
            try {
                URL referenced = new URL(jarUrl, entry);
                if (new File(referenced.getFile()).exists()) {
                    urls.add(referenced);
                } else {
                    nonExistentEntries.add(referenced);
                }
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(
                        "Class-Path attribute contains malformed URL", ex);
            }
        }
        if (!nonExistentEntries.isEmpty()) {
            System.out.println("The Class-Path manifest attribute in " + jarFile.getName()
                    + " referenced one or more files that do not exist: "
                    + StringUtils.collectionToCommaDelimitedString(nonExistentEntries));
        }
        return urls;
    }

}
