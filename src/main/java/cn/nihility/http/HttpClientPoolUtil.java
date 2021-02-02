package cn.nihility.http;

import cn.nihility.http.entity.HttpClientUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * HttpClient 连接池工具
 *
 * HttpClient 及其连接池配置
 * 整个线程池中最大连接数 MAX_CONNECTION_TOTAL = 800
 * 路由到某台主机最大并发数，是 MAX_CONNECTION_TOTAL（整个线程池中最大连接数）的一个细分 ROUTE_MAX_COUNT = 500
 * 重试次数，防止失败情况 RETRY_COUNT = 3
 * 客户端和服务器建立连接的超时时间 CONNECTION_TIME_OUT = 5000
 * 客户端从服务器读取数据的超时时间 READ_TIME_OUT = 7000
 * 从连接池中获取连接的超时时间 CONNECTION_REQUEST_TIME_OUT = 5000
 * 连接空闲超时，清楚闲置的连接 CONNECTION_IDLE_TIME_OUT = 5000
 * 连接保持存活时间 DEFAULT_KEEP_ALIVE_TIME_MILLIS = 20 * 1000
 *
 * MaxTotal 和 DefaultMaxPerRoute 的区别
 * MaxTotal 是整个池子的大小；
 * DefaultMaxPerRoute 是根据连接到的主机对 MaxTotal 的一个细分；
 * 比如：MaxTotal=400，DefaultMaxPerRoute=200，而我只连接到 http://xxx.com 时，到这个主机的并发最多只有 200，而不是 400
 * 而我连接到 http://xxx1.com 和 http://xxx2.com 时，到每个主机的并发最多只有 200
 * 即加起来是 400（但不能超过 400）。所以起作用的设置是 DefaultMaxPerRoute。
 *
 */
public class HttpClientPoolUtil {

    private final static Logger log = LoggerFactory.getLogger(HttpClientPoolUtil.class);

    /** 线程安全，所有的线程都可以使用它一起发送 http 请求 */
    private static CloseableHttpClient httpClient;
    /** CookieStore 对象 */
    private static CookieStore cookieStore = null;
    /** Basic Auth 管理对象 **/
    private static BasicCredentialsProvider basicCredentialsProvider = null;

    /**
     * http 请求配置
     */
    private static RequestConfig requestConfig;

    private final static int MAX_TOTAL_COUNT = 600;
    private final static int MAX_PER_ROUT_COUNT = 60;

    static {

        /*SSLContext sslContext = null;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            sslContext = builder.build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            log.error("SSL 配置出错", e);
        }
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
        // 配置同时支持 HTTP 和 HTTPS
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory).build();
        // 默认实现 : PoolingHttpClientConnectionManager.getDefaultRegistry
        */
        // 注册访问协议相关的 Socket 工厂
        /*Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();*/

        // 1. 创建连接池管理器, 默认同时支持 HTTP/HTTPS
        /*
        * ClientConnectionPoolManager 会维护每个路由维护和最大连接数限制。
        * 默认情况下，此实现将为每个给定路由创建不超过 2 个并发连接，并且总共不超过 20 个连接。
        * 可以自由来调整连接限制。
        *
        * 另外构造函数中可以设置持久链接的存活时间 TTL（timeToLive），
        * 其定义了持久连接的最大使用时间，超过其 TTL 值的链接不会再被复用。
        *
        * 1.1 设置 TTL 为 60s（tomcat 服务器默认支持保持 60s 的连接，超过 60s，会关闭客户端的连接）
        * 1.2 设置连接器最多同时支持 1000 个链接
        * 1.3 设置每个路由最多支持 50 个链接。注意这里路由是指 IP+PORT 或者指域名。
        * (如果使用域名来访问则每个域名有自己的链接池，如果使用 IP+PORT 访问，则每个 IP+PORT 有自己的链接池)
        *
        * 默认注入了 PoolingHttpClientConnectionManager.getDefaultRegistry() -> http/https 支持
        * */
        //PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        final PoolingHttpClientConnectionManager connectionManager =
            new PoolingHttpClientConnectionManager(60000, TimeUnit.MILLISECONDS);
        connectionManager.setMaxTotal(MAX_TOTAL_COUNT);
        connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUT_COUNT);
        connectionManager.setValidateAfterInactivity(5000);
        final SocketConfig socketConfig = SocketConfig.custom()
            .setSoTimeout(30)
            .setTcpNoDelay(true)
            .build();
        connectionManager.setDefaultSocketConfig(socketConfig);

        /*
        * Http 请求配置
        * setConnectTimeout(1000) : 设置客户端发起TCP连接请求的超时时间
        * setConnectionRequestTimeout(3000) : 设置客户端从连接池获取链接的超时时间
        * setSocketTimeout(3000)  : 设置客户端等待服务端返回数据的超时时间
        * */
        requestConfig = RequestConfig.custom()
            .setConnectTimeout(3000)
            .setConnectionRequestTimeout(3000)
            .setSocketTimeout(5000)
            .build();

        // 设置重试次数 .setRetryHandler(requestRetryHandler)
        // DefaultHttpRequestRetryHandler requestRetryHandler = new DefaultHttpRequestRetryHandler(2, false);

        // 设置 Cookie
        cookieStore = new BasicCookieStore();
        // 设置 Basic Auth 对象
        basicCredentialsProvider = new BasicCredentialsProvider();
        // 创建监听器，在 JVM 停止或重启时，关闭连接池释放掉连接
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("执行关闭 HttpClient");
                httpClient.close();
                log.info("已经关闭 HttpClient");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }));

        // 2. 创建 HttpClient
        httpClient = HttpClients.custom()
            .setDefaultCookieStore(cookieStore)
            .setDefaultCredentialsProvider(basicCredentialsProvider)
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .disableAutomaticRetries()
            .evictExpiredConnections()
            .evictIdleConnections(60000, TimeUnit.MILLISECONDS)
            .build();
        /*
        * .disableAutomaticRetries() 禁用自动重试连接
        * */



        /*
        * HttpClient 会在使用某个连接前，监测这个连接是否已经过时，如果服务器端关闭了连接，那么会重现建立一个连接。
        * 但是这种过时检查并不是 100% 有效。所以建立创建一个监控进程来专门回收由于长时间不活动而被判定为失效的连接
        * */
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.info("===== Close Idle Connections ===");
                connectionManager.closeExpiredConnections();
                connectionManager.closeIdleConnections(5, TimeUnit.SECONDS);
            }
        }, 0, 5 * 1000);
    }

    public static void main(String[] args) {
        String httpGetUrl = "http://127.0.0.1:8080/httpclient/one";
        String httpGetUrl1 = "https://www.baidu.com/";
        String httpPostUrl = "http://127.0.0.1:8080/httpclient/post_way_three";
        String uploadPostUrl = "http://127.0.0.1:8080/httpclient/upload";
        String httpGetDownloadUrl = "http://10.0.41.80:8080/httpclient/download/redis-5.0.7.tar.gz";

        HttpClientUser var = new HttpClientUser("HttpPool用户名", 20);
        Map<String, String> urlParam = new HashMap<>(8);
        urlParam.put("name", "&&名称");
        urlParam.put("age", "30");

        /*int loop = 1 ;
        for (int i = 0; i < loop; i++) {
            System.out.println("http get [" + httpGet(httpGetUrl) + "]");
            System.out.println("http get [" + httpGet(httpGetUrl1) + "]");
            System.out.println("http post [" + httpPost(httpPostUrl, urlParam, var, null) + "]");

            System.out.println(httpPostUploadFile(uploadPostUrl, "C:\\Users\\intel\\Desktop\\kiam-uim-3.1.1-SNAPSHOT.jar"));

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        System.out.println(httpGetDownloadFile(httpGetDownloadUrl));
    }

    public static String httpGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse httpResponse = null;
        String result = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            int code = httpResponse.getStatusLine().getStatusCode();
            // 自动关闭流
            result = EntityUtils.toString(httpResponse.getEntity());
            if (code != HttpStatus.SC_OK) {
                log.error("请求 [{}] 返回错误码： [{}], [{}]", url, code, result);
            }
        } catch (IOException e) {
            log.error("http 请求异常 [{}]", url, e);
        } finally {
            closeHttpResponse(httpResponse);
        }
        return result;
    }

    public static void closeHttpResponse(CloseableHttpResponse httpResponse) {
        if (null != httpResponse) {
            try {
                httpResponse.close();
            } catch (IOException e) {
                log.error("关闭 CloseableHttpResponse 异常", e);
            }
        }
    }

    public static String httpPost(String uri, Map<String, String> uriParams,
                                  Object bodyParam, Map<String, String> headers) {
        // 字符数据最好 encoding，这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不 encoding 的话,传不过去)
        if (null != uriParams && !uriParams.isEmpty()) {
            final StringBuilder urlParamsBuilder = new StringBuilder(32);
            uriParams.forEach((k, v) -> {
                try {
                    urlParamsBuilder.append("&").append(k).append("=").append(URLEncoder.encode(v, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    log.error("URLEncoder 编码 UTF-8 不支持");
                }
            });
            final String uriParamsString = urlParamsBuilder.toString().substring(1);
            uri = uri + "?" + uriParamsString;
            log.info("请求 url 参数 [{}]", uriParamsString);
        }

        HttpPost httpPost = new HttpPost(uri);
        /*httpPost.setConfig(requestConfig);*/

        ObjectMapper mapper = new ObjectMapper();
        String paramsString;
        try {
            paramsString = mapper.writeValueAsString(bodyParam);
        } catch (JsonProcessingException e) {
            log.error("解析 JSON 对象 [{}] 出错", bodyParam, e);
            return null;
        }

        StringEntity paramEntity = new StringEntity(paramsString, StandardCharsets.UTF_8);
        paramEntity.setContentEncoding("UTF-8");
        paramEntity.setContentType("application/json");

        httpPost.setEntity(paramEntity);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        if (null != headers && !headers.isEmpty()) {
            headers.forEach((k, v) -> httpPost.setHeader(new BasicHeader(k, v)));
        }

        String result = null;
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int code = httpResponse.getStatusLine().getStatusCode();
            // 从响应中获取数据，内部会自动对 entity 是否存在做检查
            result = EntityUtils.toString(httpResponse.getEntity());
            if (code != HttpStatus.SC_OK) {
                log.error("请求[{}]，返回错误码:[{}]，请求参数:[{}]，[{}]", uri, code, bodyParam, result);
            }
        } catch (IOException e) {
            log.error("收集服务配置 http 请求异常", e);
        } finally {
           closeHttpResponse(httpResponse);
        }
        return result;
    }

    public static String httpPostUploadFile(String uri, String filePath) {
        log.info("请求[{}]上传文件路径[{}]", uri, filePath);
        int fileIndex = filePath.lastIndexOf("/");
        if (-1 == fileIndex) {
            fileIndex = filePath.lastIndexOf("\\");
        }

        String fileName = filePath.substring(fileIndex);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addBinaryBody("file", new File(filePath), ContentType.APPLICATION_OCTET_STREAM, fileName);
        HttpEntity httpRequestEntity = multipartEntityBuilder.build();

        final HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(httpRequestEntity);

        return execute(httpPost);
    }

    public static String httpGetDownloadFile(String url) {

        final HttpGet httpGet = new HttpGet(url);

        String fileName = url.substring(url.lastIndexOf("/"));
        log.info("下载文件 [{}]", fileName);

        CloseableHttpResponse httpResponse = null;
        String result = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            int code = httpResponse.getStatusLine().getStatusCode();
            // 从响应中获取数据文件数据流
            final HttpEntity responseEntity = httpResponse.getEntity();
            if (null != responseEntity) {
                if (responseEntity.isStreaming()) {
                    final Path dirPath = Paths.get("dir");
                    final File dirFile = dirPath.toFile();
                    log.info("保存文件路径 [{}]", dirFile.getAbsolutePath());
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    final File writeToFile = new File(dirFile, fileName);
                    if (!writeToFile.exists()) {
                        writeToFile.createNewFile();
                    }
                    try (final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(writeToFile));
                         final InputStream inputStream = responseEntity.getContent()) {
                        int len;
                        int size = 0;
                        byte[] buffer = new byte[4096];
                        while ((len = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, len);
                            size += len;
                        }
                        outputStream.flush();
                        log.info("下载文件大小 [{}]", size);
                    }

                    result = "文件下载完成";
                } else {
                    result = EntityUtils.toString(httpResponse.getEntity());
                }
            } else {
                result = "没有响应内容";
            }
            if (code != HttpStatus.SC_OK) {
                log.error("请求[{}]，返回状态码:[{}]，返回数据：[{}]", url, code, result);
            }
        } catch (IOException e) {
            log.error("收集服务配置 http 请求异常", e);
        } finally {
            closeHttpResponse(httpResponse);
        }
        return result;

    }

    public static String execute(final HttpUriRequest httpRequest) {
        CloseableHttpResponse httpResponse = null;
        String result = null;
        try {
            httpResponse = httpClient.execute(httpRequest);
            int code = httpResponse.getStatusLine().getStatusCode();
            // 从响应中获取数据
            result = EntityUtils.toString(httpResponse.getEntity());
            if (code != HttpStatus.SC_OK) {
                log.error("请求[{}]，返回状态码:[{}]，返回数据：[{}]", httpRequest.getURI().getPath(), code, result);
            }
        } catch (IOException e) {
            log.error("收集服务配置 http 请求异常", e);
        } finally {
            closeHttpResponse(httpResponse);
        }
        return result;
    }

}
