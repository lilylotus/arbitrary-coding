package cn.nihility.util.http.handler;

import cn.nihility.util.http.IoUtils;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.lang.reflect.Type;
import java.util.Map;

public class StringResponseHandler extends AbstractResponseHandler<String> {

    @Override
    public HttpRestResult<String> convertResult(CloseableHttpResponse response, Type responseType) throws Exception {
        Map<String, String> headers = convertHeader(response.getAllHeaders());
        java.lang.String extractBody = IoUtils.toString(entityContent(response), getCharset());
        return new HttpRestResult<>(headers, statusCode(response), extractBody, null);
    }

}
