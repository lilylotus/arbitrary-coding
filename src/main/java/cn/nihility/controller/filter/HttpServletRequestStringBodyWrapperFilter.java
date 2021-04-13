package cn.nihility.controller.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class HttpServletRequestStringBodyWrapperFilter implements Filter {

    private final static Logger log = LoggerFactory.getLogger(HttpServletRequestStringBodyWrapperFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("HttpServletRequestStringBodyWrapper Filter init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isTextRequestBody(request) && request instanceof HttpServletRequest) {
            HttpServletRequestStringBodyWrapper wrapper = new HttpServletRequestStringBodyWrapper((HttpServletRequest) request);
            chain.doFilter(wrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isTextRequestBody(ServletRequest request) {
        String contentType = null;
        if (request instanceof HttpServletRequest) {
            contentType = ((HttpServletRequest) request).getHeader("Content-Type");
        }
        if (null != contentType) {
            String[] ct = contentType.split(";");
            return !("multipart/form-data".equals(ct[0]));
        }
        return false;
    }

    @Override
    public void destroy() {
        log.info("HttpServletRequestStringBodyWrapper Filter destroy");
    }
}
