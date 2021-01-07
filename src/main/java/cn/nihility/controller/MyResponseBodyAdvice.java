package cn.nihility.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MyResponseBodyAdvice implements ResponseBodyAdvice<Object>, Ordered {

    private static final Logger log = LoggerFactory.getLogger(MyResponseBodyAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        log.info("MyResponseBodyAdvice -> supports MethodParameter [{}] converterType [{}]", returnType.getMember(), converterType.getName());
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        log.info("MyResponseBodyAdvice -> beforeBodyWrite body [{}] MethodParameter [{}] MediaType [{}]", body, returnType.getMember(), selectedContentType.getType());

        if ("text".equals(selectedContentType.getType())) {
            return "{" +
                    "\"apiVersion\"" + ":" + "\"1.0.1\"," +
                    "\"data\"" + ":" + "\"" + body + "\"," +
                    "\"message\"" + ":" + "\"MyResponseBodyAdvice 统一请求\"" +
                    "}";
        } else if ("application".equals(selectedContentType.getType()) && "json".equals(selectedContentType.getSubtype())) {
            Map<String, Object> unifyMap = new HashMap<>(8);
            unifyMap.put("apiVersion", "1.0.1");
            unifyMap.put("data", body);
            unifyMap.put("message", "MyResponseBodyAdvice 统一请求");
            return unifyMap;
        } else {
            return body;
        }

    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
