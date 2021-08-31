package cn.nihility.http;


import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefaultHttpClientUtilTest {

    @Test
    void testNormalHttpRequest() throws IOException {
        final HttpClientConnectionManager connectionManager = DefaultHttpClientUtil.createHttpClientConnectionManager();
        final CloseableHttpClient httpClient = DefaultHttpClientUtil.createHttpClient(connectionManager);

        HttpPost httpRequest = new HttpPost("http://10.0.41.80:50012/manage-central/umm/v1/admin/login");
        httpRequest.addHeader("Content-Type", "application/json");

        StringEntity stringEntity = new StringEntity("{\"username\":\"系统管理员\",\"password\":\"Aa12345678\"}",
            StandardCharsets.UTF_8);
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");

        httpRequest.setEntity(stringEntity);

        final BasicCookieStore cookieStore = new BasicCookieStore();
        HttpClientContext httpContext = HttpClientContext.create();
        httpContext.setRequestConfig(DefaultHttpClientUtil.createRequestConfig());
        httpContext.setCookieStore(cookieStore);

        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpRequest, httpContext);
        } catch (IOException ex) {
            httpRequest.abort();
            ex.printStackTrace();
        }

        assertNotNull(httpResponse);
        final StatusLine statusLine = httpResponse.getStatusLine();
        assertNotNull(statusLine);
        assertEquals(201, statusLine.getStatusCode());
        System.out.println("响应状态为:" + statusLine);
        System.out.println("响应消息为:" + EntityUtils.toString(httpResponse.getEntity()));

        for (Cookie cookie : cookieStore.getCookies()) {
            System.out.println(cookie.getPath() + ":" + cookie.getName() + ":" + cookie.getValue());
        }

        httpResponse.close();
        httpClient.close();
        DefaultHttpClientUtil.shutdown(connectionManager);

    }

    @Test
    void testNormalHttpRequestGet() throws IOException {
        final HttpClientConnectionManager connectionManager = DefaultHttpClientUtil.createHttpClientConnectionManager();
        final CloseableHttpClient httpClient = DefaultHttpClientUtil.createHttpClient(connectionManager);

        HttpGet httpRequest = new HttpGet("https://www.baidu.com");

        final BasicCookieStore cookieStore = new BasicCookieStore();
        HttpClientContext httpContext = HttpClientContext.create();
        httpContext.setRequestConfig(DefaultHttpClientUtil.createRequestConfig());
        httpContext.setCookieStore(cookieStore);

        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpRequest, httpContext);
        } catch (IOException ex) {
            httpRequest.abort();
            ex.printStackTrace();
        }

        assertNotNull(httpResponse);
        final StatusLine statusLine = httpResponse.getStatusLine();
        assertNotNull(statusLine);
        assertEquals(200, statusLine.getStatusCode());
        System.out.println("响应状态为:" + statusLine);
        System.out.println("响应消息为:" + EntityUtils.toString(httpResponse.getEntity()));

        for (Cookie cookie : cookieStore.getCookies()) {
            System.out.println(cookie.getPath() + ":" + cookie.getName() + ":" + cookie.getValue());
        }

        httpResponse.close();
        httpClient.close();
        DefaultHttpClientUtil.shutdown(connectionManager);

    }

    @Test
    void testNormalPost() {
        final HttpClientConnectionManager connectionManager = DefaultHttpClientUtil.createHttpClientConnectionManager();

        final HttpPost post = new HttpPost("http://10.0.41.80:50012/manage-central/umm/v1/admin/login");
        post.addHeader("Content-Type", "application/json");

        StringEntity stringEntity = new StringEntity("{\"username\":\"系统管理员\",\"password\":\"Aa12345678\"}",
            StandardCharsets.UTF_8);
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");

        post.setEntity(stringEntity);
        post.setConfig(DefaultHttpClientUtil.createRequestConfig());

        try (final CloseableHttpClient httpClient = DefaultHttpClientUtil.createHttpClient(connectionManager)) {
            try (CloseableHttpResponse httpResponse = httpClient.execute(post)) {
                final StatusLine statusLine = httpResponse.getStatusLine();
                assertNotNull(statusLine);
                assertEquals(201, statusLine.getStatusCode());
                System.out.println("响应状态为:" + statusLine);
                System.out.println("响应消息为:" + EntityUtils.toString(httpResponse.getEntity()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        DefaultHttpClientUtil.shutdown(connectionManager);
    }

    @Test
    void testPostFormData() {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpClient = ApacheHttpClientConfiguration.createHttpClient(false, cookieStore)) {

            HttpPost httpPost = new HttpPost("http://10.0.1.170:23806/xxl-job-admin/login");
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            // 将参数放入键值对类 NameValuePair 中,再放入集合中
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("userName", "admin"));
            params.add(new BasicNameValuePair("password", "123456"));
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);
            httpPost.setEntity(formEntity);

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                System.out.println("响应状态为:" + httpResponse.getStatusLine());
                System.out.println("响应消息为:" + EntityUtils.toString(httpResponse.getEntity()));
                System.out.println(httpResponse.getFirstHeader("Set-Cookie").getValue());
            }

            cookieStore.getCookies().forEach(cookie -> {
                System.out.println(cookie.getName() + " : " + cookie.getValue());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
