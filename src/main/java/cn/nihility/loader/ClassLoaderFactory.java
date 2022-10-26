/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.nihility.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Utility class for building class loaders for Catalina.  The factory
 * method requires the following parameters in order to build a new class
 * loader (with suitable defaults in all cases):</p>
 * <ul>
 * <li>A set of directories containing unpacked classes (and resources)
 *     that should be included in the class loader's
 *     repositories.</li>
 * <li>A set of directories containing classes and resources in JAR files.
 *     Each readable JAR file discovered in these directories will be
 *     added to the class loader's repositories.</li>
 * <li><code>ClassLoader</code> instance that should become the parent of
 *     the new class loader.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 */
public final class ClassLoaderFactory {


    private static final Logger log = LoggerFactory.getLogger(ClassLoaderFactory.class);

    private static final Pattern PATH_PATTERN = Pattern.compile("(\"[^\"]*\")|(([^,])*)");

    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a new class loader, based on the configuration
     * defaults and the specified directory paths:
     *
     * @param unpacked Array of pathnames to unpacked directories that should
     *                 be added to the repositories of the class loader, or <code>null</code>
     *                 for no unpacked directories to be considered
     * @param packed   Array of pathnames to directories containing JAR files
     *                 that should be added to the repositories of the class loader,
     *                 or <code>null</code> for no directories of JAR files to be considered
     * @param parent   Parent class loader for the new class loader, or
     *                 <code>null</code> for the system class loader.
     * @return the new class loader
     * @throws Exception if an error occurs constructing the class loader
     */
    public static ClassLoader createClassLoader(File unpacked[], File packed[], final ClassLoader parent) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("  Creating new class loader");
        }

        // Construct the "class path" for this class loader
        Set<URL> set = new LinkedHashSet<>();

        // Add unpacked directories
        if (unpacked != null) {
            for (File file : unpacked) {
                if (!file.canRead()) {
                    continue;
                }
                file = new File(file.getCanonicalPath() + File.separator);
                URL url = file.toURI().toURL();
                if (log.isDebugEnabled()) {
                    log.debug("  Including directory " + url);
                }
                set.add(url);
            }
        }

        // Add packed directory JAR files
        if (packed != null) {
            for (File directory : packed) {
                if (!directory.isDirectory() || !directory.canRead()) {
                    continue;
                }
                String filenames[] = directory.list();
                if (filenames == null) {
                    continue;
                }
                for (String s : filenames) {
                    String filename = s.toLowerCase(Locale.ENGLISH);
                    if (!filename.endsWith(".jar")) {
                        continue;
                    }
                    File file = new File(directory, s);
                    if (log.isDebugEnabled()) {
                        log.debug("  Including jar file " + file.getAbsolutePath());
                    }
                    URL url = file.toURI().toURL();
                    set.add(url);
                }
            }
        }

        // Construct the class loader itself
        final URL[] array = set.toArray(new URL[0]);
        return AccessController.doPrivileged(
            (PrivilegedAction<URLClassLoader>) () -> {
                if (parent == null) {
                    return new URLClassLoader(array);
                } else {
                    return new URLClassLoader(array, parent);
                }
            });
    }

    public static ClassLoader createClassLoader(final URL[] urls, ClassLoader parent) {
        return AccessController.doPrivileged(
            (PrivilegedAction<URLClassLoader>) () -> {
                if (parent == null) {
                    return new URLClassLoader(urls);
                } else {
                    return new URLClassLoader(urls, parent);
                }
            });
    }

    // Protected for unit testing
    private static String[] getPaths(String value) {
        boolean containQuotation = value.contains("\"");
        if (!containQuotation) {
            return value.split(",");
        }

        List<String> result = new ArrayList<>();
        Matcher matcher = PATH_PATTERN.matcher(value);
        while (matcher.find()) {
            String path = value.substring(matcher.start(), matcher.end());

            path = path.trim();
            if (path.length() == 0) {
                continue;
            }

            char first = path.charAt(0);
            char last = path.charAt(path.length() - 1);

            if (first == '"' && last == '"' && path.length() > 1) {
                path = path.substring(1, path.length() - 1);
                path = path.trim();
                if (path.length() == 0) {
                    continue;
                }
            } else if (path.contains("\"")) {
                // Unbalanced quotes
                // Too early to use standard i18n support. The class path hasn't
                // been configured.
                throw new IllegalArgumentException(
                    "The double quote [\"] character can only be used to quote paths. It must " +
                        "not appear in a path. This loader path is not valid: [" + value + "]");
            }  // Not quoted - NO-OP


            result.add(path);
        }

        return result.toArray(new String[0]);
    }

    /**
     * @param paths  "${catalina.base}/lib","${catalina.base}/lib/*.jar","${catalina.home}/lib","${catalina.home}/lib/*.jar"
     * @param parent parent class loader
     */
    public static ClassLoader createClassLoader(String paths, ClassLoader parent) throws Exception {
        if ((paths == null) || ("".equals(paths))) {
            return parent;
        }
        List<Repository> repositories = getPathRepositories(paths);

        return ClassLoaderFactory.createClassLoader(repositories, parent);
    }

    public static List<Repository> getPathRepositories(String paths) {
        List<Repository> repositories = new ArrayList<>();
        String[] repositoryPaths = getPaths(paths);

        for (String repository : repositoryPaths) {
            // Local repository
            if (repository.endsWith("*.jar")) {
                repository = repository.substring
                    (0, repository.length() - "*.jar".length());
                repositories.add(new Repository(repository, RepositoryType.GLOB));
            } else if (repository.endsWith(".jar")) {
                repositories.add(new Repository(repository, RepositoryType.JAR));
            } else {
                repositories.add(new Repository(repository, RepositoryType.DIR));
            }
        }
        return repositories;
    }


    /**
     * Create and return a new class loader, based on the configuration
     * defaults and the specified directory paths:
     *
     * @param repositories List of class directories, jar files, jar directories
     *                     or URLS that should be added to the repositories of
     *                     the class loader.
     * @param parent       Parent class loader for the new class loader, or
     *                     <code>null</code> for the system class loader.
     * @return the new class loader
     * @throws Exception if an error occurs constructing the class loader
     */
    public static ClassLoader createClassLoader(List<Repository> repositories, final ClassLoader parent) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Creating new class loader");
        }

        final URL[] array = getRepositoriesUrls(repositories);

        return AccessController.doPrivileged(
            (PrivilegedAction<URLClassLoader>) () -> {
                if (parent == null) {
                    return new URLClassLoader(array);
                } else {
                    return new URLClassLoader(array, parent);
                }
            });
    }

    public static URL[] getRepositoriesUrls(List<Repository> repositories) throws IOException {
        // Construct the "class path" for this class loader
        Set<URL> set = new LinkedHashSet<>();

        if (repositories != null) {
            for (Repository repository : repositories) {
                if (repository.getType() == RepositoryType.URL) {
                    URL url = buildClassLoaderUrl(repository.getLocation());
                    if (log.isDebugEnabled()) {
                        log.debug("  Including URL " + url);
                    }
                    set.add(url);
                } else if (repository.getType() == RepositoryType.DIR) {
                    File directory = new File(repository.getLocation());
                    directory = directory.getCanonicalFile();
                    if (!validateFile(directory, RepositoryType.DIR)) {
                        continue;
                    }
                    URL url = buildClassLoaderUrl(directory);
                    if (log.isDebugEnabled()) {
                        log.debug("  Including directory " + url);
                    }
                    set.add(url);
                } else if (repository.getType() == RepositoryType.JAR) {
                    File file = new File(repository.getLocation());
                    file = file.getCanonicalFile();
                    if (!validateFile(file, RepositoryType.JAR)) {
                        continue;
                    }
                    URL url = buildClassLoaderUrl(file);
                    if (log.isDebugEnabled()) {
                        log.debug("  Including jar file " + url);
                    }
                    set.add(url);
                } else if (repository.getType() == RepositoryType.GLOB) {
                    File directory = new File(repository.getLocation());
                    directory = directory.getCanonicalFile();
                    if (!validateFile(directory, RepositoryType.GLOB)) {
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("  Including directory glob "
                            + directory.getAbsolutePath());
                    }
                    String filenames[] = directory.list();
                    if (filenames == null) {
                        continue;
                    }
                    for (String s : filenames) {
                        String filename = s.toLowerCase(Locale.ENGLISH);
                        if (!filename.endsWith(".jar")) {
                            continue;
                        }
                        File file = new File(directory, s);
                        file = file.getCanonicalFile();
                        if (!validateFile(file, RepositoryType.JAR)) {
                            continue;
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("    Including glob jar file "
                                + file.getAbsolutePath());
                        }
                        URL url = buildClassLoaderUrl(file);
                        set.add(url);
                    }
                }
            }
        }

        // Construct the class loader itself
        final URL[] array = set.toArray(new URL[0]);
        if (log.isDebugEnabled()) {
            for (int i = 0; i < array.length; i++) {
                log.debug("  location " + i + " is " + array[i]);
            }
        }
        return array;
    }

    private static boolean validateFile(File file,
                                        RepositoryType type) throws IOException {
        if (RepositoryType.DIR == type || RepositoryType.GLOB == type) {
            if (!file.isDirectory() || !file.canRead()) {
                String msg = "Problem with directory [" + file +
                    "], exists: [" + file.exists() +
                    "], isDirectory: [" + file.isDirectory() +
                    "], canRead: [" + file.canRead() + "]";
                log.warn(msg);
                return false;
            }
        } else if (RepositoryType.JAR == type) {
            if (!file.canRead()) {
                log.warn("Problem with JAR file [" + file +
                    "], exists: [" + file.exists() +
                    "], canRead: [" + file.canRead() + "]");
                return false;
            }
        }
        return true;
    }


    /*
     * These two methods would ideally be in the utility class
     * org.apache.tomcat.util.buf.UriUtil but that class is not visible until
     * after the class loaders have been constructed.
     */
    private static URL buildClassLoaderUrl(String urlString) throws MalformedURLException {
        // URLs passed to class loaders may point to directories that contain
        // JARs. If these URLs are used to construct URLs for resources in a JAR
        // the URL will be used as is. It is therefore necessary to ensure that
        // the sequence "!/" is not present in a class loader URL.
        String result = urlString.replaceAll("!/", "%21/");
        return new URL(result);
    }

    public static URL convert2JarUrl(URL fileUrl) throws MalformedURLException {
        String filePath = fileUrl.toString();
        if (!filePath.endsWith("!/") && !filePath.endsWith("%21/")) {
            filePath = filePath + "!/";
        }
        return new URL("jar", null, -1, filePath);
    }


    private static URL buildClassLoaderUrl(File file) throws MalformedURLException {
        // Could be a directory or a file
        String fileUrlString = file.toURI().toString();
        fileUrlString = fileUrlString.replaceAll("!/", "%21/");
        return new URL(fileUrlString);
    }


    public enum RepositoryType {
        DIR,
        GLOB,
        JAR,
        URL
    }

    public static class Repository {
        private final String location;
        private final RepositoryType type;

        public Repository(String location, RepositoryType type) {
            this.location = location;
            this.type = type;
        }

        public String getLocation() {
            return location;
        }

        public RepositoryType getType() {
            return type;
        }
    }

}
