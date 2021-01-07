package cn.nihility.controller;

import java.io.Serializable;

public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = 7674155940241240581L;
    private boolean flag;
    private Integer code;
    private String message;
    private T data;

    public BaseResponse() {
    }

    public BaseResponse(boolean flag, Integer code, String message) {
        this.flag = flag;
        this.code = code;
        this.message = message;
    }

    public BaseResponse(boolean flag, StatusCode statusCode) {
        this.code = statusCode.getCode();
        this.flag = false;
        this.message = statusCode.getMsg();

    }

    public BaseResponse(boolean flag, StatusCode statusCode, T data) {
        this.code = statusCode.getCode();
        this.flag = false;
        this.message = statusCode.getMsg();
        this.data = data;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
