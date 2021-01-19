package cn.nihility.http;

import cn.nihility.http.entity.HttpClientUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HttpClientUtil {

    public static void main1(String[] args) throws IOException {

        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        HttpPost post = new HttpPost("http://localhost:8080/start/cookie");

        CloseableHttpResponse response = httpClient.execute(post);
        HttpEntity entity = response.getEntity();
        System.out.println(EntityUtils.toString(entity));

        cookieStore.getCookies().forEach(ck -> System.out.println(ck.getName() + ":" + ck.getValue()));

        HttpGet httpGet = new HttpGet("http://localhost:8080/start/show");
        CloseableHttpResponse resp = httpClient.execute(httpGet);
        System.out.println(EntityUtils.toString(resp.getEntity()));

        httpClient.close();
        response.close();
        resp.close();

    }

    public static void main(String[] args) {
        doResponseHandler();
    }


    public static void clientConnectionRelease() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet("http://httpbin.org/get");

            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());

                // Get hold of the response entity
                HttpEntity entity = response.getEntity();

                // If the response does not enclose an entity, there is no need
                // to bother about connection release
                if (entity != null) {
                    InputStream inStream = entity.getContent();
                    try {
                        inStream.read();
                        // do something useful with the response
                    } catch (IOException ex) {
                        // In case of an IOException the connection will be released
                        // back to the connection manager automatically
                        throw ex;
                    } finally {
                        // Closing the input stream will trigger connection release
                        inStream.close();
                    }
                }
            } finally {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void doResponseHandler() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet("http://httpbin.org/");

            System.out.println("Executing request " + httpget.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 普通参数 + 实体参数
     *
     * @PostMapping("/post_way_three") public String postWayThree(@RequestBody HttpClientUser user, String name, Integer age) {}
     */
    public static void doPostWayThree() {
        // 创建客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        List<NameValuePair> params = new ArrayList<>();
        // 将参数放入键值对类 NameValuePair 中,再放入集合中
        try {
            params.add(new BasicNameValuePair("name", URLEncoder.encode("&", "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params.add(new BasicNameValuePair("age", "19"));
        // 设置 uri 信息,并将参数集合放入 uri;
        // 注: 这里也支持一个键值对一个键值对地往里面放 setParameter(String key, String value)
        URI uri = null;
        try {
            uri = new URIBuilder().setScheme("HTTP").setHost("127.0.0.1").setPort(8080)
                .setPath("/httpclient/post_way_three").setParameters(params).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // 创建请求
        HttpPost request = new HttpPost(uri);

        // 创建对象
        HttpClientUser user = new HttpClientUser("three请求post", 18);
        ObjectMapper objectMapper = new ObjectMapper();
        String bodyData = "";
        try {
            bodyData = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 转为 JSON 字符串
        StringEntity bodyEntity = new StringEntity(bodyData, StandardCharsets.UTF_8);

        // post 请求是将参数放在请求体里面传过去的;这里将 entity 放入 post 请求体中
        request.setEntity(bodyEntity);
        request.setHeader("Content-Type", "application/json");

        // 响应
        CloseableHttpResponse response = null;
        try {
            // 发送请求
            response = httpClient.execute(request);
            // 从响应中获取数据
            HttpEntity responseEntity = response.getEntity();
            System.out.println("status : " + response.getStatusLine());
            if (null != responseEntity) {
                System.out.println("length: " + responseEntity.getContentLength());
                System.out.println("content: " + EntityUtils.toString(responseEntity, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResource(httpClient, response);
        }

    }

    /**
     * 添加 body 参数
     *
     * @PostMapping("/post_way_two") public String postWayTwo(@RequestBody HttpClientUser user) {}
     */
    public static void doPostWayTwo() {
        // 创建客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        // 创建请求
        HttpPost request = new HttpPost("http://127.0.0.1:8080/httpclient/post_way_two");

        // 创建对象
        HttpClientUser user = new HttpClientUser("HttpClient请求post", 20);
        ObjectMapper objectMapper = new ObjectMapper();
        String bodyData = "";
        try {
            bodyData = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        StringEntity bodyEntity = new StringEntity(bodyData, StandardCharsets.UTF_8);

        // post 请求是将参数放在请求体里面传过去的;这里将 entity 放入 post 请求体中
        request.setEntity(bodyEntity);
        request.setHeader("Content-Type", "application/json");

        // 响应
        CloseableHttpResponse response = null;
        try {
            // 发送请求
            response = httpClient.execute(request);
            // 从响应中获取数据
            HttpEntity responseEntity = response.getEntity();
            System.out.println("status : " + response.getStatusLine());
            if (null != responseEntity) {
                System.out.println("length: " + responseEntity.getContentLength());
                System.out.println("content: " + EntityUtils.toString(responseEntity));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResource(httpClient, response);
        }

    }

    /**
     * 普通 URL 参数
     *
     * @PostMapping("/post_way_one") public String postWayOne(String name, Integer age) {}
     */
    public static void doPostNormalParam() {
        // 创建客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        // 字符数据最好 encoding 以下，这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)
        StringBuilder params = new StringBuilder();
        try {
            params.append("name=").append(URLEncoder.encode("&", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // params.append("name=&"); name 无法解析为 &
        params.append("&age=24");

        HttpPost request = new HttpPost("http://127.0.0.1:8080/httpclient/post_way_one?" + params.toString());

        // 设置 ContentType (注:如果只是传普通参数的话, ContentType 不一定非要用 application/json)
        request.setHeader("Content-Type", "application/json");

        // 响应
        CloseableHttpResponse response = null;
        try {
            // 发送请求
            response = httpClient.execute(request);
            // 从响应中获取数据
            HttpEntity responseEntity = response.getEntity();
            System.out.println("status : " + response.getStatusLine());
            if (null != responseEntity) {
                System.out.println("length: " + responseEntity.getContentLength());
                System.out.println("content: " + EntityUtils.toString(responseEntity));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResource(httpClient, response);
        }
    }

    /**
     * @PostMapping("/post_one") public String postOne() {}
     */
    public static void doPostOne() {
        // 创建客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost("http://127.0.0.1:8080/httpclient/post_one");
        // 响应
        CloseableHttpResponse response = null;
        try {
            // 发送请求
            response = httpClient.execute(request);
            // 从响应中获取数据
            HttpEntity responseEntity = response.getEntity();
            System.out.println("status : " + response.getStatusLine());
            if (null != responseEntity) {
                System.out.println("length: " + responseEntity.getContentLength());
                System.out.println("content: " + EntityUtils.toString(responseEntity));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResource(httpClient, response);
        }
    }

    /**
     * 接收接口：
     *
     * @RequestMapping("/url") public String requestOne() { return "ONE"; }
     */
    public static void doGetOne() {
        // 创建客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建请求
        HttpGet request = new HttpGet("http://127.0.0.1:8080/httpclient/one");
        // 响应
        CloseableHttpResponse response = null;
        try {
            // 发送 Get 请求
            response = httpClient.execute(request);
            // 从响应中获取数据
            HttpEntity responseEntity = response.getEntity();
            System.out.println("status : " + response.getStatusLine());
            if (null != responseEntity) {
                System.out.println("length: " + responseEntity.getContentLength());
                System.out.println("content: " + EntityUtils.toString(responseEntity));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResource(httpClient, response);
        }
    }

    /**
     * @RequestMapping("/wayone") public String requestOne(String name, Integer age) {}
     */
    public static void doGetWayOne() {
        // 获取客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        // 字符数据最好 encoding 以下，这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)
        StringBuilder params = new StringBuilder();
        try {
            params.append("name=").append(URLEncoder.encode("&", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // params.append("name=&"); name 无法解析为 &
        params.append("&age=24");

        // 创建请求
        HttpGet request = new HttpGet("http://127.0.0.1:8080/httpclient/wayone?" + params.toString());
        // 响应模型
        CloseableHttpResponse response = null;
        // 请求配置信息
        RequestConfig requestConfig = RequestConfig.custom()
            // 设置连接超时时间(单位毫秒)
            .setConnectTimeout(5000)
            // 设置请求超时时间(单位毫秒)
            .setConnectionRequestTimeout(5000)
            // socket读写超时时间(单位毫秒)
            .setSocketTimeout(5000)
            // 设置是否允许重定向(默认为true)
            .setRedirectsEnabled(true).build();

        // 应用请求配置
        request.setConfig(requestConfig);

        try {
            // 执行 Get 请求
            response = httpClient.execute(request);
            // 从响应中获取数据
            HttpEntity responseEntity = response.getEntity();
            System.out.println("status : " + response.getStatusLine());
            if (null != responseEntity) {
                System.out.println("length: " + responseEntity.getContentLength());
                System.out.println("content: " + EntityUtils.toString(responseEntity));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResource(httpClient, response);
        }


    }

    public static void closeResource(CloseableHttpClient httpClient, CloseableHttpResponse response) {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 根据是否是https请求，获取HttpClient客户端
     * <p>
     * TODO 本人这里没有进行完美封装。对于 校不校验校验证书的选择，本人这里是写死
     * 在代码里面的，你们在使用时，可以灵活二次封装。
     * <p>
     * 提示: 此工具类的封装、相关客户端、服务端证书的生成，可参考我的这篇博客:
     * <linked>https://blog.csdn.net/justry_deng/article/details/91569132</linked>
     *
     * @param isHttps 是否是HTTPS请求
     * @return HttpClient实例
     * @date 2019/9/18 17:57
     */
    private CloseableHttpClient getHttpClient(boolean isHttps) {
        CloseableHttpClient httpClient;
        if (isHttps) {
            SSLConnectionSocketFactory sslSocketFactory;
            try {
                /// 如果不作证书校验的话
                sslSocketFactory = getSocketFactory(false, null, null);

                /// 如果需要证书检验的话
                // 证书
                //InputStream ca = this.getClass().getClassLoader().getResourceAsStream("client/ds.crt");
                // 证书的别名，即:key。 注:cAalias只需要保证唯一即可，不过推荐使用生成keystore时使用的别名。
                // String cAalias = System.currentTimeMillis() + "" + new SecureRandom().nextInt(1000);
                //sslSocketFactory = getSocketFactory(true, ca, cAalias);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            httpClient = HttpClientBuilder.create().setSSLSocketFactory(sslSocketFactory).build();
            return httpClient;
        }
        httpClient = HttpClientBuilder.create().build();
        return httpClient;
    }

    /**
     * HTTPS辅助方法, 为HTTPS请求 创建SSLSocketFactory实例、TrustManager实例
     *
     * @param needVerifyCa  是否需要检验CA证书(即:是否需要检验服务器的身份)
     * @param caInputStream CA证书。(若不需要检验证书，那么此处传null即可)
     * @param cAalias       别名。(若不需要检验证书，那么此处传null即可)
     *                      注意:别名应该是唯一的， 别名不要和其他的别名一样，否者会覆盖之前的相同别名的证书信息。别名即key-value中的key。
     * @return SSLConnectionSocketFactory实例
     * @throws NoSuchAlgorithmException 异常信息
     * @throws CertificateException     异常信息
     * @throws KeyStoreException        异常信息
     * @throws IOException              异常信息
     * @throws KeyManagementException   异常信息
     * @date 2019/6/11 19:52
     */
    private static SSLConnectionSocketFactory getSocketFactory(boolean needVerifyCa, InputStream caInputStream, String cAalias)
        throws CertificateException, NoSuchAlgorithmException, KeyStoreException,
        IOException, KeyManagementException {
        X509TrustManager x509TrustManager;
        // https请求，需要校验证书
        if (needVerifyCa) {
            KeyStore keyStore = getKeyStore(caInputStream, cAalias);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            x509TrustManager = (X509TrustManager) trustManagers[0];
            // 这里传TLS或SSL其实都可以的
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
            return new SSLConnectionSocketFactory(sslContext);
        }
        // https请求，不作证书校验
        x509TrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
                // 不验证
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
        return new SSLConnectionSocketFactory(sslContext);
    }

    /**
     * 获取(密钥及证书)仓库
     * 注:该仓库用于存放 密钥以及证书
     *
     * @param caInputStream CA证书(此证书应由要访问的服务端提供)
     * @param cAalias       别名
     *                      注意:别名应该是唯一的， 别名不要和其他的别名一样，否者会覆盖之前的相同别名的证书信息。别名即key-value中的key。
     * @return 密钥、证书 仓库
     * @throws KeyStoreException        异常信息
     * @throws CertificateException     异常信息
     * @throws IOException              异常信息
     * @throws NoSuchAlgorithmException 异常信息
     * @date 2019/6/11 18:48
     */
    private static KeyStore getKeyStore(InputStream caInputStream, String cAalias)
        throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        // 证书工厂
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        // 秘钥仓库
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        keyStore.setCertificateEntry(cAalias, certificateFactory.generateCertificate(caInputStream));
        return keyStore;
    }

}
