package cn.nihility.util.http;

public class RestResult<T> {

    private String code;
    private String traceId;
    private long timestamp;
    private String error;

    private transient T result;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "RestResult{" +
            "code='" + code + '\'' +
            ", traceId='" + traceId + '\'' +
            ", timestamp=" + timestamp +
            ", error='" + error + '\'' +
            ", result=" + result +
            '}';
    }
}
