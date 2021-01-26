package cn.nihility.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取资源的工具类
 */
public class ResourceUtil {

    private static final Logger log = LoggerFactory.getLogger(ResourceUtil.class);

    private static final Pattern WORD_SEPARATOR = Pattern.compile("\\W+");
    private static final String NESTED_ARCHIVE_SEPARATOR = "!" + File.separator;
    private static final String USER_DIR = "user.dir";


    /* ==================== 处理 key 资源 ==================== */

    public static String toCamelCase(CharSequence string) {
        if (string == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Matcher matcher = WORD_SEPARATOR.matcher(string);
        int pos = 0;
        while (matcher.find()) {
            builder.append(capitalize(string.subSequence(pos, matcher.end()).toString()));
            pos = matcher.end();
        }
        builder.append(capitalize(string.subSequence(pos, string.length()).toString()));
        return builder.toString();
    }

    private static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String userDir() {
        return System.getProperty(USER_DIR);
    }


    /* ==================== 加载资源 ==================== */

    private boolean isNestedArchivePath(File file) {
        return file.getPath().contains(NESTED_ARCHIVE_SEPARATOR);
    }

    private boolean isAbsolutePath(String root) {
        // Windows contains ":" others start with "/"
        return root.contains(":") || root.startsWith("/");
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

    private InputStream getResource(String config) throws Exception {
        if (config.startsWith("classpath:")) {
            return getClasspathResource(config.substring("classpath:".length()));
        }
        config = handleUrl(config);
        return getFileResource(config);
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

    private InputStream getClasspathResource(String config) {
        while (config.startsWith("/")) {
            config = config.substring(1);
        }
        config = "/" + config;
        debug("Trying classpath: " + config);
        return getClass().getResourceAsStream(config);
    }

    private InputStream getFileResource(String config) throws Exception {
        File file = new File(config);
        debug("Trying file: " + config);
        if (file.canRead()) {
            return new FileInputStream(file);
        }
        return null;
    }

    private void debug(String msg) {
        log.debug(msg);
    }

}
