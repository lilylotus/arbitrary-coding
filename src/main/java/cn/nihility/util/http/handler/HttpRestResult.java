package cn.nihility.util.http.handler;

import org.apache.http.Header;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRestResult<T> extends RestResult<T> {

    private static final long serialVersionUID = -849620503125661517L;

    private Map<String, String> header = new LinkedHashMap<>();

    public HttpRestResult() {
    }

    public HttpRestResult(Map<String, String> header, int code, T data, String message) {
        super(code, message, data);
        this.header = header;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public String getHeaderValue(String key) {
        return header.get(key);
    }

}
