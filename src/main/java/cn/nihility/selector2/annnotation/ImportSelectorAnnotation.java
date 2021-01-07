package cn.nihility.selector2.annnotation;

import cn.nihility.selector2.ImportSelectorStarter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ImportSelectorStarter.class})
@Inherited
@Documented
public @interface ImportSelectorAnnotation {
    /* 扫描的包 */
    String[] value() default {};
}
