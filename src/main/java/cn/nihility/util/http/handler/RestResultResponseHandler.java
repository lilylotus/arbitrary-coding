package cn.nihility.util.http.handler;

import cn.nihility.util.http.JacksonUtils;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;

public class RestResultResponseHandler<T> extends AbstractResponseHandler<T> {

    @Override
    @SuppressWarnings("unchecked")
    public HttpRestResult<T> convertResult(CloseableHttpResponse response, Type responseType) throws Exception {
        Map<String, String> headers = convertHeader(response.getAllHeaders());
        InputStream body = entityContent(response);
        T extractBody = JacksonUtils.toObj(body, responseType);
        HttpRestResult<T> httpRestResult = convert((RestResult<T>) extractBody);
        httpRestResult.setHeader(headers);
        return httpRestResult;
    }

    private static <T> HttpRestResult<T> convert(RestResult<T> restResult) {
        HttpRestResult<T> httpRestResult = new HttpRestResult<>();
        httpRestResult.setCode(restResult.getCode());
        httpRestResult.setData(restResult.getData());
        httpRestResult.setMessage(restResult.getMessage());
        return httpRestResult;
    }

}
