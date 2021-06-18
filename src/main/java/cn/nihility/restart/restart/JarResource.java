package cn.nihility.restart.restart;

import cn.nihility.restart.restart.util.ResourceUtils;
import cn.nihility.restart.restart.util.RestartUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JarResource {

    private static final Logger log = LoggerFactory.getLogger(JarResource.class);
    private final URL jarUrl;
    private Set<String> classNameSet;

    public JarResource(URL url) {
        this.jarUrl = url;

        if (null != jarUrl) {
            List<String> classNameList = RestartUtil.scanJarClassName(jarUrl, log);
            int size = (int) (classNameList.size() / 0.75 + 1);
            classNameSet = new HashSet<>(size);
            classNameSet.addAll(classNameList);
        }
    }

    public String getResourcePath(String resource) {
        return RestartUtil.getResourcePath(resource, jarUrl);
    }

    public InputStream getInputStream() throws IOException {
        URLConnection con = this.jarUrl.openConnection();
        ResourceUtils.useCachesIfNecessary(con);
        try {
            return con.getInputStream();
        } catch (IOException ex) {
            // Close the HTTP connection (if applicable).
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }

    public InputStream getInputStream(String resource) throws IOException {
        URL url = RestartUtil.getResourceUrl(resource, jarUrl);
        return null == url ? null : RestartUtil.getInputStream(url);
    }

    public URL getResourceUrl(String resource) {
        return RestartUtil.getResourceUrl(resource, jarUrl);
    }

    public byte[] loadClassData(String name) {
        if (containClass(name)) {
            return RestartUtil.loadClassData(name, jarUrl);
        } else {
            return null;
        }
    }

    public boolean containClass(String name) {
        return classNameSet != null && classNameSet.contains(name);
    }

    public String getResourceContent(String resource) {
        return RestartUtil.readContent(RestartUtil.getResourceUrl(resource, jarUrl));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JarResource that = (JarResource) o;
        return Objects.equals(jarUrl, that.jarUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jarUrl);
    }

}
