package cn.nihility.controller;

public class ResultUtil {
    public static <T> BaseResponse<T> success(T object) {
        BaseResponse<T> result = new BaseResponse<>();
        result.setCode(0);
        result.setMessage("成功");
        result.setData(object);
        return result;
    }

    public static BaseResponse success() {
        return success(null);
    }

    public static <T> BaseResponse<T> error(Integer code, String msg) {
        BaseResponse<T> result = new BaseResponse<>();
        result.setCode(code);
        result.setMessage(msg);
        return result;
    }
}
