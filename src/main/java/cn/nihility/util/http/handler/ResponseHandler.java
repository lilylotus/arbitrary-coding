package cn.nihility.util.http.handler;

import org.apache.http.client.methods.CloseableHttpResponse;

import java.lang.reflect.Type;

public interface ResponseHandler<T> {

    /**
     * set response type.
     *
     * @param responseType responseType
     */
    void setResponseType(Type responseType);

    /**
     * handle response convert to HttpRestResult.
     *
     * @param response http response
     * @return HttpRestResult {@link HttpRestResult}
     * @throws Exception ex
     */
    HttpRestResult<T> handle(CloseableHttpResponse response) throws Exception;

}
