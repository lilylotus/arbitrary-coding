package cn.nihility.restart.restart;

import cn.nihility.boot.restart.restart.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

public class UrlResource {

    private final URL url;

    public UrlResource(URL url) {
        this.url = url;
    }

    public InputStream getInputStream() throws IOException {
        URLConnection con = this.url.openConnection();
        ResourceUtils.useCachesIfNecessary(con);
        try {
            return con.getInputStream();
        }
        catch (IOException ex) {
            // Close the HTTP connection (if applicable).
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }

    public void fillProperties(Properties props) {
        try (InputStream is = getInputStream()) {
            String filename = url.getFile();
            if (filename != null && filename.endsWith(".xml")) {
                props.loadFromXML(is);
            } else {
                props.load(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readResourceString() {
        StringBuilder ret = new StringBuilder();
        try (InputStream is = getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                ret.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

}
