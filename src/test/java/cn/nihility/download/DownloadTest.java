package cn.nihility.download;

import cn.nihility.dowload.Downloader;
import cn.nihility.dowload.FileDownloader;
import cn.nihility.dowload.MultiThreadFileDownloader;
import cn.nihility.dowload.SimpleDownloader;
import cn.nihility.dowload.support.MultiThreadDownloadProgressPrinter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.CompletableFuture;

public class DownloadTest {

    @Test
    public void downloadLiteFile() throws IOException {
        //通过内存下载
        //String fileURL = "https://cn.bing.com/th?id=OHR.SierraNevada_ZH-CN0564237735_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp";
        String fileURL = "https://mirrors.bfsu.edu.cn/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz";
        Downloader downloader = new SimpleDownloader();
        downloader.download(fileURL, "D:\\temporary");
    }

    @Test
    public void downloadLargeFile() throws IOException {
        //单线程下载
        //String fileURL = "https://cn.bing.com/th?id=OHR.SierraNevada_ZH-CN0564237735_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp";
        String fileURL = "https://mirrors.bfsu.edu.cn/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz";
        Downloader downloader = new FileDownloader();
        downloader.download(fileURL, "D:\\temporary\\download");
    }

    @Test
    public void downloadAsync() throws IOException {
        final int threadCoreSize = 2;
        String fileURL = "https://mirrors.bfsu.edu.cn/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz";
        Downloader downloader = new MultiThreadFileDownloader(threadCoreSize);
        downloader.download(fileURL, "D:\\temporary\\download");
    }

    @Test
    public void test() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.execute("", HttpMethod.GET, null, (response) -> {
            System.out.println(response.getRawStatusCode());
            return null;
        });
    }

}
