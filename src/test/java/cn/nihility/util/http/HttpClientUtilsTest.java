package cn.nihility.util.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author yzx
 */
class HttpClientUtilsTest {

    @Test
    void execute() {
        HttpPost post = new HttpPost("http://127.0.0.1:54100/test/v5/post/ok");
        RequestConfig config = HttpClientUtils.requestConfigOption(3, TimeUnit.SECONDS, 3, TimeUnit.SECONDS, true);
        post.setConfig(config);
        HttpClientUtils.execute(post);
    }

}
