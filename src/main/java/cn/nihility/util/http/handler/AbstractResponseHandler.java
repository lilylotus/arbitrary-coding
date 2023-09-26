package cn.nihility.util.http.handler;

import cn.nihility.util.http.IoUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractResponseHandler<T> implements ResponseHandler<T> {

    private Type responseType;

    private String charset;

    @Override
    public void setResponseType(Type responseType) {
        this.responseType = responseType;
    }

    @Override
    public HttpRestResult<T> handle(CloseableHttpResponse response) throws Exception {
        int statusCode = response.getStatusLine().getStatusCode();
        return (HttpStatus.SC_OK == statusCode) ? convertResult(response, this.responseType) : handleError(response);
    }

    private HttpRestResult<T> handleError(CloseableHttpResponse response) throws Exception {
        String message = IoUtils.toString(entityContent(response), charset);
        return new HttpRestResult<>(convertHeader(response.getAllHeaders()), statusCode(response), null, message);
    }

    public InputStream entityContent(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        if (entity.isStreaming()) {
            return entity.getContent();
        }
        return null;
    }

    public int statusCode(CloseableHttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    public Map<String, String> convertHeader(Header[] headers) {
        Map<String, String> h = new LinkedHashMap<>();
        if (null != headers) {
            for (Header header : headers) {
                h.put(header.getName(), header.getValue());
            }
        }
        return h;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return charset;
    }

    /**
     * Abstract convertResult method, Different types of converters for expansion.
     *
     * @param response     http client response
     * @param responseType responseType
     * @return HttpRestResult
     * @throws Exception ex
     */
    public abstract HttpRestResult<T> convertResult(CloseableHttpResponse response, Type responseType) throws Exception;

}
