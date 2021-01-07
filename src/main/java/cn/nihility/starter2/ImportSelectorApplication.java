package cn.nihility.starter2;

import cn.nihility.selector2.ImportSelectorStarterConfig;
import cn.nihility.selector2.entity.SelectorEntity;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ImportSelectorApplication {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(ImportSelectorStarterConfig.class);
        ctx.refresh();

        // SelectorEntity.class ， SelectorEntity.class.getName() 都可以
//        SelectorEntity bean = ctx.getBean(SelectorEntity.class);
        Object bean = ctx.getBean(SelectorEntity.class.getName());
        System.out.println(bean);

        ctx.registerShutdownHook();

    }

}
