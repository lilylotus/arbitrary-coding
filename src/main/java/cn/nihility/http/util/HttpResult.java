package cn.nihility.http.util;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

import java.io.Serializable;

/**
 * HTTP 请求返回对象
 */
public class HttpResult implements Serializable {

    private static final long serialVersionUID = -5310613946789864797L;

    /**
     * 执行结果-body
     */
    private String body;

    /**
     * 状态码-statusCode
     */
    private int statusCode;

    /**
     * 状态行-StatusLine
     */
    private StatusLine statusLine;

    /**
     * 请求头信息
     */
    private Header[] requestHeaders;

    /**
     * 响应头信息
     */
    private Header[] responseHeaders;

    /**
     * 协议版本
     */
    private ProtocolVersion protocolVersion;

    /**
     * HttpResponse结果对象
     */
    private HttpResponse response;

    public HttpResult(HttpResponse resp) {
        this.statusLine = resp.getStatusLine();
        this.responseHeaders = resp.getAllHeaders();
        this.protocolVersion = resp.getProtocolVersion();
        this.statusCode = resp.getStatusLine().getStatusCode();
        this.response = resp;
    }

    /**
     * 从返回的头信息中查询指定头信息
     *
     * @param name	头信息名称
     */
    public Header getHeader(final String name) {
        Header[] headers = this.response.getHeaders(name);
        return headers != null && headers.length > 0 ? headers[0] : null;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(StatusLine statusLine) {
        this.statusLine = statusLine;
    }

    public Header[] getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Header[] requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public Header[] getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Header[] responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }
}
