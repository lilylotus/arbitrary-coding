package cn.nihility.controller.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.util.WebUtils;

import java.lang.annotation.Annotation;

@RestControllerAdvice
public class ResponseResultBodyAdvice implements ResponseBodyAdvice<Object>, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ResponseResultBodyAdvice.class);
    private static final Class<? extends Annotation> ANNOTATION_TYPE = ResponseResultBody.class;

    /**
     * 对 @ResponseResultBody 注解拦截
     *
     * @return true 采用了 @ResponseResultBody 注解
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        log.info("ResponseResultBodyAdvice supports [{}]", returnType.getContainingClass());
        return AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ANNOTATION_TYPE)
                || returnType.hasMethodAnnotation(ANNOTATION_TYPE);
    }

    /**
     * 使用了 @ResponseResultBody 注解就转换
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        log.info("ResponseResultBodyAdvice beforeBodyWrite [{}]", (body == null ? "空" : body.getClass().getName()));
        if (body instanceof ResultResponse) {
            return body;
        } else if ("text".equals(selectedContentType.getType())) {
            return "{" +
                    "\"apiVersion\"" + ":" + "\"1.0.0\"," +
                    "\"data\"" + ":" + "\"" + body + "\"," +
                    "\"message\"" + ":" + "\"ResponseResultBodyAdvice 统一请求\"" +
                    "}";
        }
        return ResultResponse.success(body);
    }


    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ResultResponse<?>> exceptionHandler(Exception ex, WebRequest request) {
        log.error("ExceptionHandler: {}", ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        if (ex instanceof ResultException) {
            return this.handleResultException((ResultException) ex, headers, request);
        }
        return this.handleException(ex, headers, request);
    }


    /**
     * 对ResultException类返回返回结果的处理
     */
    protected ResponseEntity<ResultResponse<?>> handleResultException(ResultException ex, HttpHeaders headers, WebRequest request) {
        return this.handleExceptionInternal(ex, ResultResponse.failure(ex.getResultStatus()),
                headers, ex.getResultStatus().getHttpStatus(), request);
    }

    /**
     * 异常类的统一处理
     */
    protected ResponseEntity<ResultResponse<?>> handleException(Exception ex, HttpHeaders headers, WebRequest request) {
        return this.handleExceptionInternal(ex, ResultResponse.failure(ex.getMessage()),
                headers, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleExceptionInternal(java.lang.Exception, java.lang.Object, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
     * <p>
     * A single place to customize the response body of all exception types.
     * <p>The default implementation sets the {@link WebUtils#ERROR_EXCEPTION_ATTRIBUTE}
     * request attribute and creates a {@link ResponseEntity} from the given
     * body, headers, and status.
     */
    protected ResponseEntity<ResultResponse<?>> handleExceptionInternal(
            Exception ex, ResultResponse<?> body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        return new ResponseEntity<>(body, headers, status);
    }

    /**
     * 优先级越高，在多个统一返回的时候内容在越里层
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 10;
    }
}
