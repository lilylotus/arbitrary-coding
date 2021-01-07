package cn.nihility.starter;


import cn.nihility.aspect.Arithmetic;
import cn.nihility.aspect.AspectConfig;
import cn.nihility.selector.ImportSelectorConfig;
import cn.nihility.selector.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationAspectStarter {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(AspectConfig.class);
        ctx.refresh();

        Arithmetic bean = ctx.getBean(Arithmetic.class);
        System.out.println(bean.add(1, 2));

        System.out.println("========== close");
        ctx.registerShutdownHook();
    }

}
