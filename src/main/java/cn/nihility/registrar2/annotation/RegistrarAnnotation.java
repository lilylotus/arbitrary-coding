package cn.nihility.registrar2.annotation;

import cn.nihility.registrar2.ImportBeanDefinitionRegistrarStarter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ImportBeanDefinitionRegistrarStarter.class})
@Inherited
@Documented
public @interface RegistrarAnnotation {
    /* 扫描的包 */
    String[] value() default {};
}
