package cn.nihility.util.http;

import cn.nihility.util.http.handler.HttpRestResult;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yzx
 */
class HttpClientUtilsTest {

    @Test
    void execute() {
        HttpPost post = new HttpPost("http://127.0.0.1:54100/test/v5/post/ok");
        RequestConfig config = HttpClientUtils.requestConfigOption(3, TimeUnit.SECONDS, 3, TimeUnit.SECONDS, true);
        post.setConfig(config);
        Response<String> response = HttpClientUtils.execute(post);
        System.out.println("status = " + response.getStatus());
        System.out.println("reason = " + response.getReason());
        Map<String, Collection<String>> headers = response.getHeaders();

        headers.forEach((k, v) -> {
            System.out.print(k + " = [");
            v.forEach(vv -> System.out.print(vv + " "));
            System.out.println(" ]");
        });

        String body = response.getBody();
        System.out.println(body);

    }

    @Test
    void execute2() {
        HttpPost post = new HttpPost("http://127.0.0.1:54100/test/v5/post/ok");
        RequestConfig config = HttpClientUtils.requestConfigOption(3, TimeUnit.SECONDS, 3, TimeUnit.SECONDS, true);
        post.setConfig(config);


        HttpRestResult<RestResult> result = HttpClientUtils.execute(post, RestResult.class);
        System.out.println("status = " + result.getCode());

        RestResult<String> data = result.getData();

        System.out.println(data);

    }

    @Test
    void t() {
        HttpPost post = new HttpPost("http://127.0.0.1:54100/test/v5/post/ok");
        RequestConfig config = HttpClientUtils.requestConfigOption(3, TimeUnit.SECONDS, 3, TimeUnit.SECONDS, true);
        post.setConfig(config);

        TypeReference<RestResult<String>> ref = new TypeReference<RestResult<String>>() {
        };

        HttpRestResult<RestResult<String>> result = HttpClientUtils.execute(post, ref);
        System.out.println("status = " + result.getCode());

        RestResult<String> data = result.getData();

        System.out.println(data);
    }

}
