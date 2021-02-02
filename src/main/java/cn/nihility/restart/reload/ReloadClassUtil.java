package cn.nihility.restart.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;

public class ReloadClassUtil {

    private static final Logger log = LoggerFactory.getLogger(ReloadClassUtil.class);

    public static URL fileToURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            log.error("处理 jar 文件路径 [{}] 转 URL 出错", file.getPath(), e);
        }
        return null;
    }

    public static boolean isAbsolutePath(String root) {
        // Windows contains ":" others start with "/"
        return root.contains(":") || root.startsWith("/");
    }

    public static String handleUrl(String path) {
        if (path.startsWith("jar:file:") || path.startsWith("file:")) {
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // nothing
            }
            if (path.startsWith("file:")) {
                path = path.substring("file:".length());
                if (path.startsWith("//")) {
                    path = path.substring(2);
                }
            }
        }
        return path;
    }

    public static String cleanupPath(String path) {
        path = path.trim();
        // No need for current dir path
        if (path.startsWith("./")) {
            path = path.substring(2);
        }
        String lowerCasePath = path.toLowerCase(Locale.ENGLISH);
        if (lowerCasePath.endsWith(".jar")) {
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

}
