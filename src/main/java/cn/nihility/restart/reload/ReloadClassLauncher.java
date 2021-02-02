package cn.nihility.restart.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReloadClassLauncher extends Thread {

    private final Logger log = LoggerFactory.getLogger(ReloadClassLauncher.class);

    private final List<URL> libJarUrlList = new ArrayList<>(60);
    private final List<String> libJarLocationList = new ArrayList<>(60);

    private final String loadLibConfigFile;
    private final String mainClassName;
    private String mainClassLoaderJar;
    private String mainClassLoaderJarPath;

    public ReloadClassLauncher(String loadLibConfigFile, String mainClassName) {
        this.loadLibConfigFile = loadLibConfigFile;
        this.mainClassName = mainClassName;
        setDaemon(false);
    }

    private void resolveRootPath() throws URISyntaxException, ClassNotFoundException {
        Class<?> mainClazz = Class.forName(mainClassName, true, getClass().getClassLoader());
        ProtectionDomain protectionDomain = mainClazz.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
        String mainJarPath = (location != null) ?
                ReloadClassUtil.cleanupPath(ReloadClassUtil.handleUrl(location.getSchemeSpecificPart())) : null;
        // /D:/coding/idea/boot-learn/build/libs/boot-learn-0.0.1-SNAPSHOT.jar!/BOOT-INF/classes!/
        if (null != mainJarPath) {
            mainJarPath = mainJarPath.substring(0, mainJarPath.indexOf("!"));
            mainClassLoaderJarPath = mainJarPath;
            mainJarPath = mainJarPath.substring(mainJarPath.lastIndexOf("/") + 1);
            this.mainClassLoaderJar = mainJarPath;
        }
        log.info("main class 加载路径 [{}]", mainClassLoaderJar);
    }

    public ClassLoader createClassLoader(ClassLoader parent) {
        return new ReloadClassLoader(libJarLocationList, mainClassLoaderJarPath, parent);
    }

    @Override
    public void run() {
        try {
            relaunch();
        } catch (URISyntaxException | ClassNotFoundException e) {
            log.error("重启失败 [{}]", mainClassName, e);
        }
    }

    public void relaunch() throws URISyntaxException, ClassNotFoundException {

        resolveRootPath();

        libJarUrlList.addAll(loadLibPathJarFiles());
        libJarLocationList.addAll(loadLibPathJarLocation());

        ClassLoader parent = getClass().getClassLoader();
        ClassLoader current = createClassLoader(parent);

        //RestartApplicationContext.closeApplicationContext();

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            //
        }

        Thread.currentThread().setContextClassLoader(current);

        ReloadClassLoaderThread launcher = new ReloadClassLoaderThread(mainClassName, new String[0], current);
        /*launcher.reload();*/
        launcher.start();
        try {
            launcher.join();
        } catch (InterruptedException e) {
            log.error("重启被打断 [{}]", mainClassName, e);
        }
        Throwable error = launcher.getError();

        if (null != error) {
            log.error("重启出错 [{}]", mainClassName, error);
        }
    }

    public static void main(String[] args) {
        ReloadClassLauncher launcher = new ReloadClassLauncher("D:\\load_path.txt",
                "cn.nihility.boot.BootLearnApplication");
        List<URL> urls = launcher.loadLibPathJarFiles();

        System.out.println(urls.size());
        System.out.println(urls);
    }

    /**
     * 加载 lib 配置文件中指定目录中所有的 jar 包
     * @return jar 的 URL 列表
     */
    public List<URL> loadLibPathJarFiles() {
        List<String> configLines = readReloadConfigPath(loadLibConfigFile);
        final List<URL> lib = new ArrayList<>(60);
        if (configLines.size() > 0) {
            configLines.forEach(path -> {
                try {
                    lib.addAll(loadPathJar(path));
                } catch (UnsupportedEncodingException e) {
                    // do nothing
                }
            });
        }
        return lib;
    }

    /**
     * 加载 lib 配置文件中指定目录中所有的 jar 包的路径
     * @return jar 的 绝对路径 列表
     */
    public List<String> loadLibPathJarLocation() {
        List<String> configLines = readReloadConfigPath(loadLibConfigFile);
        final List<String> lib = new ArrayList<>(60);
        if (configLines.size() > 0) {
            configLines.forEach(path -> {
                try {
                    lib.addAll(loadPathJarLocation(path));
                } catch (UnsupportedEncodingException e) {
                    // do nothing
                }
            });
        }
        return lib;
    }

    /**
     * 加载重启时需要加载 jar 包顺序路径配置文件
     *
     * @return jar 加载路径
     */
    private List<String> readReloadConfigPath(String loadConfigPath) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(loadConfigPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取重启加载路径文件 [{}] 出错", loadConfigPath, e);
        }
        return lines == null ? Collections.emptyList() : lines;
    }

    /**
     * 加载指定路径中的所有 jar 包
     * @param path jar 包所在的路径，目录或单个 jar 文件路径
     * @return jar 包路径的 url
     */
    private List<URL> loadPathJar(String path) throws UnsupportedEncodingException {
        String root = ReloadClassUtil.cleanupPath(ReloadClassUtil.handleUrl(path));
        log.debug("加载 [{}] 中的 jar", root);

        final List<URL> lib = new ArrayList<>();

        File file = new File(root);
        // 不是绝对路径开头
        if (!"/".equals(root) && !ReloadClassUtil.isAbsolutePath(root)) {
            file = new File(getHomeDirectory(), root);
        }

        if (file.isDirectory()) {
            log.debug("Adding classpath entries from [{}] directory", file);
            File[] jarFiles = file.listFiles(((dir, name) -> filterJarFile(name)));
            if (null != jarFiles && jarFiles.length > 0) {
                for (File jar : jarFiles) {
                    URL jarUrl = ReloadClassUtil.fileToURL(jar);
                    if (null != jarUrl) {
                        lib.add(jarUrl);
                    }
                }
            }
        } else if (file.isFile()) {
            log.debug("Adding classpath entries from [{}] file", file);
            URL jarUrl = ReloadClassUtil.fileToURL(file);
            if (null != jarUrl) {
                lib.add(jarUrl);
            }
        }

        return lib;
    }

    private boolean filterJarFile(String name) {
        //return (!name.equals(mainClassLoaderJar)) && (name.endsWith(".jar") || name.endsWith(".zip"));
        return (name.endsWith(".jar") || name.endsWith(".zip"));
    }

    /**
     * 获取指定路径下所有 jar 路径
     * @param path 指定 jar 查的路径
     */
    private List<String> loadPathJarLocation(String path) throws UnsupportedEncodingException {
        String root = ReloadClassUtil.cleanupPath(ReloadClassUtil.handleUrl(path));
        log.debug("加载 [{}] 中的 jar", root);

        final List<String> lib = new ArrayList<>(64);

        File file = new File(root);
        // 不是绝对路径开头
        if (!"/".equals(root) && !ReloadClassUtil.isAbsolutePath(root)) {
            file = new File(getHomeDirectory(), root);
        }

        if (file.isDirectory()) {
            log.debug("Adding classpath entries from [{}] directory", file);
            File[] jarFiles = file.listFiles(((dir, name) -> filterJarFile(name)));
            if (null != jarFiles && jarFiles.length > 0) {
                for (File jar : jarFiles) {
                    if (jar.isFile() && jar.canRead()) {
                        lib.add(jar.getAbsolutePath());
                    }
                }
            }
        } else if (file.isFile() && file.canRead()) {
            log.debug("Adding classpath entries from [{}] file", file);
            lib.add(file.getAbsolutePath());
        }

        return lib;
    }

    /**
     * 获取项目启动目录
     */
    private String getHomeDirectory() {
        return PropertyUtil.getProperty("user.dir", null, "用户启动目录");
    }

}
