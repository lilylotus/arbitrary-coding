package cn.nihility.util.http.handler;

import cn.nihility.util.http.JacksonUtils;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.InputStream;
import java.lang.reflect.Type;

public class BeanResponseHandler<T> extends AbstractResponseHandler<T> {

    @Override
    public HttpRestResult<T> convertResult(CloseableHttpResponse response, Type responseType) throws Exception {
        InputStream body = entityContent(response);
        T extractBody = JacksonUtils.toObj(body, responseType);
        return new HttpRestResult<T>(convertHeader(response.getAllHeaders()), statusCode(response), extractBody, null);
    }

}
