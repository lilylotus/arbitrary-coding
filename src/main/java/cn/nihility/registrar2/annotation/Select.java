package cn.nihility.registrar2.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface Select {
    /* 指定 SQL 语句 */
    String sql() default "";

}
