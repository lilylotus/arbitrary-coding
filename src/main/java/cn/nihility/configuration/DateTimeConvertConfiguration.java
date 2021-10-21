package cn.nihility.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 1. 使用自定义的 Converter Bean
 * 2. 在实体类中注解转换规则
 *      @DateTimeFormat(pattern = "yyyy-MM-dd")
 *      private Date startDate;
 * 3. 全局 application.yml 处理
 * spring:
 * 	jackson:
 * 		date-format: yyyy-MM-dd HH:mm:ss
 * 		time-zone: GMT+8
 *
 */
@Configuration
public class DateTimeConvertConfiguration {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 以下两个 Bean 会注入到 spring mvc 的参数解析器
     * 当传入的字符串要转为 LocalDateTime 类时，spring 会调用该 Converter 对这个入参进行转换
     * 这里的 Converter 不能写为 lambda 的形式，会报启动异常
     * <p>
     * {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#requestMappingHandlerAdapter()}
     * adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer())
     * {@link WebMvcConfigurationSupport#getConfigurableWebBindingInitializer()}
     * WEB MVC 启动时会注册 {@link RequestMappingHandlerAdapter} 会初始化 {@link WebBindingInitializer}
     * <p>
     * {@link org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#addFormatters(org.springframework.format.FormatterRegistry)}
     * 时会检查 Converter 的泛型类型， Lambda 表达式的接口是 Converter，并不能得到具体的类型
     */
    @Configuration
    protected static class LocalDateConverterConfiguration {

        @Bean
        public Converter<String, LocalDate> stringLocalDateConverter() {
            return new Converter<String, LocalDate>() {
                @Override
                public LocalDate convert(String source) {
                    return StringUtils.hasText(source) ?
                            LocalDate.parse(source, DATE_FORMATTER) : null;
                }
            };
        }

        @Bean
        public Converter<String, LocalDate> stringLocalDateTimeConverter() {
            return new Converter<String, LocalDate>() {
                @Override
                public LocalDate convert(String source) {
                    return StringUtils.hasText(source) ?
                            LocalDate.parse(source, DATE_TIME_FORMATTER) : null;
                }
            };
        }

    }

}
