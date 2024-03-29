package cn.nihility.rpc.common.codec;

import java.io.Serializable;

public class RpcResponse implements Serializable {
    private static final long serialVersionUID = -1855852748385140949L;

    private String requestId;
    private String error;
    private Object result;

    public boolean isSuccess() {
        return error == null;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", error='" + error + '\'' +
                ", result=" + result +
                '}';
    }

}
