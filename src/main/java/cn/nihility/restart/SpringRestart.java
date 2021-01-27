package cn.nihility.restart;

import cn.nihility.restart.archive.Archive;
import cn.nihility.restart.archive.ExplodedArchive;
import cn.nihility.restart.archive.JarFileArchive;
import cn.nihility.restart.lunch.LaunchedURLClassLoader;
import cn.nihility.restart.lunch.MainMethodRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SpringRestart {

    private static final Logger log = LoggerFactory.getLogger(SpringRestart.class);

    private static final String NESTED_ARCHIVE_SEPARATOR = "!" + File.separator;

    private Archive parent;

    public SpringRestart() {
        try {
            this.parent = createArchive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected final Archive createArchive() throws Exception {
        ProtectionDomain protectionDomain = getClass().getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
        String path = (location != null) ? location.getSchemeSpecificPart() : null;
        if (path == null) {
            throw new IllegalStateException("Unable to determine code source archive");
        }
        File root = new File(path);
        if (!root.exists()) {
            throw new IllegalStateException(
                    "Unable to determine code source archive from " + root);
        }
        return (root.isDirectory() ? new ExplodedArchive(root)
                : new JarFileArchive(root));
    }

    /**
     * 加载重启时需要加载 jar 包顺序路径配置文件
     *
     * @return jar 加载路径
     */
    private List<String> loadStartPath() {
        final String loadPath = "D:\\load_path.txt";
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(loadPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取重启加载路径文件 [{}] 出错", loadPath, e);
        }
        return lines == null ? Collections.emptyList() : lines;
    }

    /**
     * 获取项目启动目录
     */
    private String getHomeDirectory() {
        return getProperty("user.dir", null, "用户启动目录");
    }

    public static String getProperty(String key, String defaultValue, String text) {
        try {
            String propVal = System.getProperty(key);
            if (propVal == null) {
                // Fall back to searching the system environment.
                propVal = System.getenv(key);
            }
            if (propVal == null) {
                // Try with underscores.
                String name = key.replace('.', '_');
                propVal = System.getenv(name);
            }
            if (propVal == null) {
                // Try uppercase with underscores as well.
                String name = key.toUpperCase(Locale.ENGLISH).replace('.', '_');
                propVal = System.getenv(name);
            }
            if (propVal != null) {
                return propVal;
            }
        } catch (Throwable ex) {
            log.error("Could not resolve key [{}] in [{}] as system property or in environment: ", key, text, ex);
        }
        return defaultValue;
    }

    private List<Archive> getLoadPathArchives() throws Exception {
        List<Archive> lib = new ArrayList<>();
        for (String path : loadStartPath()) {
            for (Archive archive : getClassPathArchives(path)) {
                if (archive instanceof ExplodedArchive) {
                    List<Archive> nested = new ArrayList<>(
                            archive.getNestedArchives(new ArchiveEntryFilter()));
                    nested.add(0, archive);
                    lib.addAll(nested);
                }
                else {
                    lib.add(archive);
                }
            }
        }
        return lib;
    }

    private List<Archive> getClassPathArchives(String path) throws Exception {
        String root = cleanupPath(handleUrl(path));
        List<Archive> lib = new ArrayList<>();
        File file = new File(root);
        if (!"/".equals(root)) {
            if (!isAbsolutePath(root)) {
                file = new File(getHomeDirectory(), root);
            }
            if (file.isDirectory()) {
                log.debug("Adding classpath entries from [{}]", file);
                Archive archive = new ExplodedArchive(file, false);
                lib.add(archive);
            }
        }
        Archive archive = getArchive(file);
        if (archive != null) {
            log.debug("Adding classpath entries from archive [{}]", archive.getUrl() + root);
            lib.add(archive);
        }
        List<Archive> nestedArchives = getNestedArchives(root);
        if (nestedArchives != null) {
            log.debug("Adding classpath entries from nested [{}]", root);
            lib.addAll(nestedArchives);
        }
        return lib;
    }

    private List<Archive> getNestedArchives(String path) throws Exception {
        Archive parent = this.parent;
        String root = path;
        if (!root.equals("/") && root.startsWith("/")
                || parent.getUrl().equals(getHomeDirectory())) {
            // If home dir is same as parent archive, no need to add it twice.
            return null;
        }
        int index = root.indexOf('!');
        if (index != -1) {
            File file = new File(getHomeDirectory(), root.substring(0, index));
            if (root.startsWith("jar:file:")) {
                file = new File(root.substring("jar:file:".length(), index));
            }
            parent = new JarFileArchive(file);
            root = root.substring(index + 1);
            while (root.startsWith("/")) {
                root = root.substring(1);
            }
        }
        if (root.endsWith(".jar")) {
            File file = new File(getHomeDirectory(), root);
            if (file.exists()) {
                parent = new JarFileArchive(file);
                root = "";
            }
        }
        if (root.equals("/") || root.equals("./") || root.equals(".")) {
            // The prefix for nested jars is actually empty if it's at the root
            root = "";
        }
        Archive.EntryFilter filter = new PrefixMatchingArchiveFilter(root);
        List<Archive> archives = new ArrayList<>(parent.getNestedArchives(filter));
        if (("".equals(root) || ".".equals(root)) && !path.endsWith(".jar")
                && parent != this.parent) {
            // You can't find the root with an entry filter so it has to be added
            // explicitly. But don't add the root of the parent archive.
            archives.add(parent);
        }
        return archives;
    }

    private Archive getArchive(File file) throws IOException {
        if (isNestedArchivePath(file)) {
            return null;
        }
        String name = file.getName().toLowerCase(Locale.ENGLISH);
        if (name.endsWith(".jar") || name.endsWith(".zip")) {
            return new JarFileArchive(file);
        }
        return null;
    }

    private boolean isNestedArchivePath(File file) {
        return file.getPath().contains(NESTED_ARCHIVE_SEPARATOR);
    }

    private boolean isAbsolutePath(String root) {
        // Windows contains ":" others start with "/"
        return root.contains(":") || root.startsWith("/");
    }

    private String handleUrl(String path) throws UnsupportedEncodingException {
        if (path.startsWith("jar:file:") || path.startsWith("file:")) {
            path = URLDecoder.decode(path, "UTF-8");
            if (path.startsWith("file:")) {
                path = path.substring("file:".length());
                if (path.startsWith("//")) {
                    path = path.substring(2);
                }
            }
        }
        return path;
    }

    private String cleanupPath(String path) {
        path = path.trim();
        // No need for current dir path
        if (path.startsWith("./")) {
            path = path.substring(2);
        }
        String lowerCasePath = path.toLowerCase(Locale.ENGLISH);
        if (lowerCasePath.endsWith(".jar") || lowerCasePath.endsWith(".zip")) {
            return path;
        }
        if (path.endsWith("/*")) {
            path = path.substring(0, path.length() - 1);
        }
        else {
            // It's a directory
            if (!path.endsWith("/") && !path.equals(".")) {
                path = path + "/";
            }
        }
        return path;
    }

    private void restart() throws Exception {
        ClassLoader classLoader = createClassLoader();
    }

    /**
     * Create a classloader for the specified archives.
     *
     * @return the classloader
     * @throws Exception if the classloader cannot be created
     */
    protected ClassLoader createClassLoader() throws Exception {
        /*List<URL> urls = new ArrayList<>(archives.size());
        for (Archive archive : archives) {
            urls.add(archive.getUrl());
        }
        return createClassLoader(urls.toArray(new URL[0]));*/

        return null;
    }

    /**
     * Create a classloader for the specified URLs.
     *
     * @param urls the URLs
     * @return the classloader
     * @throws Exception if the classloader cannot be created
     */
    protected ClassLoader createClassLoader(URL[] urls) throws Exception {
        return new LaunchedURLClassLoader(urls, getClass().getClassLoader());
    }

    private void restart(String[] args, String mainClass, ClassLoader classLoader)
            throws Exception {
        Thread.currentThread().setContextClassLoader(classLoader);
        createMainMethodRunner(mainClass, args, classLoader).run();
    }

    private MainMethodRunner createMainMethodRunner(String mainClass, String[] args, ClassLoader classLoader) {
        return new MainMethodRunner(mainClass, args);
    }

    /**
     * Convenience class for finding nested archives that have a prefix in their file path
     * (e.g. "lib/").
     */
    private static final class PrefixMatchingArchiveFilter implements Archive.EntryFilter {

        private final String prefix;

        private final ArchiveEntryFilter filter = new ArchiveEntryFilter();

        private PrefixMatchingArchiveFilter(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean matches(Archive.Entry entry) {
            if (entry.isDirectory()) {
                return entry.getName().equals(this.prefix);
            }
            return entry.getName().startsWith(this.prefix) && this.filter.matches(entry);
        }

    }

    /**
     * Convenience class for finding nested archives (archive entries that can be
     * classpath entries).
     */
    private static final class ArchiveEntryFilter implements Archive.EntryFilter {

        private static final String DOT_JAR = ".jar";

        private static final String DOT_ZIP = ".zip";

        @Override
        public boolean matches(Archive.Entry entry) {
            return entry.getName().endsWith(DOT_JAR) || entry.getName().endsWith(DOT_ZIP);
        }

    }

}
