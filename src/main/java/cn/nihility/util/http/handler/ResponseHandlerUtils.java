package cn.nihility.util.http.handler;

import cn.nihility.util.http.JacksonUtils;
import com.fasterxml.jackson.databind.JavaType;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseHandlerUtils {

    private final static String STRING_TYPE = String.class.getName();
    private final static String REST_RESULT_TYPE = RestResult.class.getName();
    private final static String DEFAULT_BEAN_TYPE = "default_bean_type";
    private final static Map<Type, String> RESPONSE_TYPE_CLASS_NAME = new ConcurrentHashMap<>(8);

    private ResponseHandlerUtils() {
    }

    /**
     * Select a response handler by responseType.
     *
     * @param responseType responseType
     * @return ResponseHandler
     */
    public static ResponseHandler selectResponseHandler(Type responseType) {
        ResponseHandler responseHandler;
        String name;
        if (responseType == null) {
            name = STRING_TYPE;
        } else {
            name = RESPONSE_TYPE_CLASS_NAME.computeIfAbsent(responseType, k -> {
                JavaType javaType = JacksonUtils.constructJavaType(responseType);
                return javaType.getRawClass().getName();
            });
        }
        if (STRING_TYPE.equals(name)) {
            responseHandler = new StringResponseHandler();
        } else if (REST_RESULT_TYPE.equals(name)) {
            responseHandler = new RestResultResponseHandler();
        } else {
            responseHandler = new BeanResponseHandler();
        }
        responseHandler.setResponseType(responseType);
        return responseHandler;
    }

}
