package cn.nihility.restart.restart;

import cn.nihility.restart.reload.PropertyUtil;
import cn.nihility.restart.reload.ReloadClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ScanLoadLibJar {

    private final static Logger log = LoggerFactory.getLogger(ScanLoadLibJar.class);

    private final Set<URL> libJarUrlSet = new LinkedHashSet<>(64);
    private final Set<String> libJarLocationSet = new LinkedHashSet<>(64);

    private final String loadLibConfigFile;

    public ScanLoadLibJar(String loadLibConfigFile) {
        this.loadLibConfigFile = loadLibConfigFile;
    }

    public void scan() {
        libJarUrlSet.addAll(loadLibPathJarFiles());
        libJarLocationSet.addAll(loadLibPathJarLocation());
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
                lib.addAll(loadPathJar(path));
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
                lib.addAll(loadPathJarLocation(path));
            });
        }
        return lib;
    }

    /**
     * 获取指定路径下所有 jar 路径
     * @param path 指定 jar 查的路径
     */
    private List<String> loadPathJarLocation(String path) {
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
     * 加载指定路径中的所有 jar 包
     * @param path jar 包所在的路径，目录或单个 jar 文件路径
     * @return jar 包路径的 url
     */
    private List<URL> loadPathJar(String path) {
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
     * 获取项目启动目录
     */
    private String getHomeDirectory() {
        return PropertyUtil.getProperty("user.dir", null, "用户启动目录");
    }


    public Set<URL> getLibJarUrlSet() {
        return libJarUrlSet;
    }

    public Set<String> getLibJarLocationSet() {
        return libJarLocationSet;
    }
}
