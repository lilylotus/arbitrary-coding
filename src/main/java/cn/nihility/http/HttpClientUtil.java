package cn.nihility.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClientUtil {

    public static void main(String[] args) throws IOException {

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

}
