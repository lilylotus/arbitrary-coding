package cn.nihility.controller.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestBodyWrapperFilterConfig {

    @Bean
    public FilterRegistrationBean<HttpServletRequestStringBodyWrapperFilter> httpServletRequestStringBodyWrapperFilterFilterRegistrationBean() {
        final FilterRegistrationBean<HttpServletRequestStringBodyWrapperFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HttpServletRequestStringBodyWrapperFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("HttpServletRequestStringBodyWrapperFilter");
        registrationBean.setOrder(1);
        return registrationBean;
    }

}
