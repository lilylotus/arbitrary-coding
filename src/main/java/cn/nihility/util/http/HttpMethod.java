package cn.nihility.util.http;

import org.apache.http.client.methods.*;

public class HttpMethod {

    public static final String GET = "GET";

    public static final String HEAD = "HEAD";

    public static final String POST = "POST";

    public static final String PUT = "PUT";

    public static final String PATCH = "PATCH";

    public static final String DELETE = "DELETE";

    public static final String OPTIONS = "OPTIONS";

    public static final String TRACE = "TRACE";

    public static HttpRequestBase createRequest(String method, String url) {
        if (GET.equals(method)) {
            return new HttpGet(url);
        }
        if (POST.equals(method)) {
            return new HttpPost(url);
        }
        if (PUT.equals(method)) {
            return new HttpPut(url);
        }
        if (DELETE.equals(method)) {
            return new HttpDelete(url);
        }
        if (HEAD.equals(method)) {
            return new HttpHead(url);
        }
        if (TRACE.equals(method)) {
            return new HttpTrace(url);
        }
        if (PATCH.equals(method)) {
            return new HttpPatch(url);
        }
        if (OPTIONS.equals(method)) {
            return new HttpTrace(url);
        }
        throw new IllegalArgumentException("不支持 HTTP 请求方法 [" + method + "]");
    }

}
