package cn.nihility.controller.body;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class HttpServletRequestReplacedFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServletRequest wrapperServletRequest = null;
        if (request instanceof HttpServletRequest) {
            wrapperServletRequest = new MyRequestWrapper((HttpServletRequest) request);
        }
        // 通过 filter 把新的封装包含流的 request 传递
        if (null == wrapperServletRequest) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(wrapperServletRequest, response);
        }
    }
}
