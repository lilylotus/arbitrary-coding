package cn.nihility.restart.lunch;

import cn.nihility.restart.RestartApplicationContext;
import cn.nihility.restart.archive.Archive;
import cn.nihility.restart.archive.ExplodedArchive;
import cn.nihility.restart.archive.JarFileArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
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

public class StartLauncher extends Thread {

    private static final Logger log = LoggerFactory.getLogger(StartLauncher.class);

    private static final String NESTED_ARCHIVE_SEPARATOR = "!" + File.separator;

    private Archive parent;
    private String mainClassName = "cn.nihility.BootLearnApplication";

    public StartLauncher() {
        setDaemon(false);
        setName("restartedMain");

        try {
            parent = createArchive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        final StartLauncher launcher = new StartLauncher();
        /*final List<Archive> loadPathArchives = launcher.getLoadPathArchives();
        System.out.println(loadPathArchives);*/

        System.out.println(launcher.restart());
    }

    protected final Archive createArchive() throws Exception {
        final Class<?> mainClass = Class.forName(mainClassName);
        ProtectionDomain protectionDomain = mainClass.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
        String path = (location != null) ? location.getSchemeSpecificPart() : null;
        log.info("mainClass 包路径 [{}]", path);
        if (path == null) {
            throw new IllegalStateException("Unable to determine code source archive");
        }

        int index = path.indexOf("!");
        if (index != -1) {
            path = path.substring(0, index);
        }

        path = cleanupPath(handleUrl(path));

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
            log.debug("resolve key [{}] value [{}]", key, propVal);
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
                || parent.getUrl().equals(Paths.get(getHomeDirectory()).toUri().toURL())) {
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

    private List<Archive> getNestedArchives2(String path) throws Exception {
        Archive parent = null;
        String root = path;
        if (!root.equals("/") && root.startsWith("/")) {
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
        if (null != parent) {
            List<Archive> archives = new ArrayList<>(parent.getNestedArchives(filter));
            if (("".equals(root) || ".".equals(root)) && !path.endsWith(".jar")) {
                // You can't find the root with an entry filter so it has to be added
                // explicitly. But don't add the root of the parent archive.
                archives.add(parent);
            }
            return archives;
        } else {
            return Collections.emptyList();
        }
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

    /**
     * Create a classloader for the specified archives.
     *
     * @return the classloader
     * @throws Exception if the classloader cannot be created
     */
    public ClassLoader createClassLoader() throws Exception {
        final List<Archive> loadPathArchives = getLoadPathArchives();
        List<URL> urls = new ArrayList<>(loadPathArchives.size());
        for (Archive archive : loadPathArchives) {
            urls.add(archive.getUrl());
        }
        return createClassLoader(urls.toArray(new URL[0]));
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

    @Override
    public void run() {
        restart();
    }

    public String restart() {
        /*final Thread.UncaughtExceptionHandler exceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();*/
        //final String mainClass = deduceMainApplicationClassName();
        final String mainClass = "cn.nihility.BootLearnApplication";

        if (mainClass != null) {
            try {
                Throwable error = doRestart(mainClass);
                if (error == null) {
                    log.info("重启成功， mainClass [{}]", mainClass);
                    return "重启成功";
                }
                log.error("重启出现异常， mainClass [{}]", mainClass, error);
                return "重启异常 [" + error.getMessage() + "]";
            } catch (Exception e) {
                log.error("重启出现异常， mainClass [{}]", mainClass, e);
                return "重启异常 [" + e.getMessage() + "]";
            }
        } else {
            log.warn("启动 main 类未找到");
            return "启动类未找到，重启失败";
        }
    }

    private Throwable doRestart(String mainClassName) throws Exception {
        ClassLoader classLoader;
        try {
            classLoader = createClassLoader();
        } catch (Exception e) {
            log.error("创建指定 jar 加载的 ClassLoader 失败, mainClass [{}]", mainClassName, e);
            return e;
        }
        log.info("classLoader [{}]", classLoader);
        System.out.println("classLoader [" + classLoader + "]");

        RestartApplicationContext.closeApplicationContext();

        Thread.currentThread().setContextClassLoader(classLoader);

        Class<?> mainClass = getContextClassLoader().loadClass(mainClassName);
        Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
        mainMethod.invoke(null, new Object[] { new String[0] });

        /*RestartLauncher launcher = new RestartLauncher(classLoader, mainClassName, new String[0], exceptionHandler);
        launcher.start();
        launcher.join();
        return launcher.getError();*/

        return null;
    }

    private String deduceMainApplicationClassName() {
        StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if ("main".equals(stackTraceElement.getMethodName())) {
                return stackTraceElement.getClassName();
            }
        }
        return null;
    }

    private Class<?> deduceMainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    return Class.forName(stackTraceElement.getClassName());
                }
            }
        }
        catch (ClassNotFoundException ex) {
            // Swallow and continue
        }
        return null;
    }

    private void restart(String[] args, String mainClass, ClassLoader classLoader)
            throws Exception {
        Thread.currentThread().setContextClassLoader(classLoader);
        createMainMethodRunner(mainClass, args).run();
    }

    private MainMethodRunner createMainMethodRunner(String mainClass, String[] args) {
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
