package cn.nihility.controller.result;

/**
 * 统一返回信息格式
 *
 * @param <T>
 */
public class ResultResponse<T> {
    /**
     * 业务响应码
     */
    private Integer code;
    /**
     * 信息描述
     */
    private String message;
    /**
     * 返回参数
     */
    private T data;

    private ResultResponse(ResultStatus resultStatus, T data) {
        this.code = resultStatus.getCode();
        this.message = resultStatus.getMessage();
        this.data = data;
    }

    /**
     * 业务成功返回业务代码和描述信息
     */
    public static ResultResponse<Void> success() {
        return new ResultResponse<Void>(ResultStatus.SUCCESS, null);
    }

    /**
     * 业务成功返回业务代码,描述和返回的参数
     */
    public static <T> ResultResponse<T> success(T data) {
        return new ResultResponse<T>(ResultStatus.SUCCESS, data);
    }

    /**
     * 业务成功返回业务代码,描述和返回的参数
     */
    public static <T> ResultResponse<T> success(ResultStatus resultStatus, T data) {
        if (resultStatus == null) {
            return success(data);
        }
        return new ResultResponse<T>(resultStatus, data);
    }

    /**
     * 业务异常返回业务代码和描述信息
     */
    public static <T> ResultResponse<T> failure() {
        return new ResultResponse<T>(ResultStatus.INTERNAL_SERVER_ERROR, null);
    }

    /**
     * 业务异常返回业务代码,描述和返回的参数
     */
    public static <T> ResultResponse<T> failure(ResultStatus resultStatus) {
        return failure(resultStatus, null);
    }

    /**
     * 业务异常返回业务代码,描述和返回的参数
     */
    public static <T> ResultResponse<T> failure(T data) {
        return failure(ResultStatus.INTERNAL_SERVER_ERROR, data);
    }


    /**
     * 业务异常返回业务代码,描述和返回的参数
     */
    public static <T> ResultResponse<T> failure(ResultStatus resultStatus, T data) {
        if (resultStatus == null) {
            return new ResultResponse<T>(ResultStatus.INTERNAL_SERVER_ERROR, null);
        }
        return new ResultResponse<T>(resultStatus, data);
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

    @Override
    public String toString() {
        return "ResultResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
