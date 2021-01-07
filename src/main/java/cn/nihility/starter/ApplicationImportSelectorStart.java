package cn.nihility.starter;


import cn.nihility.selector.ImportSelectorConfig;
import cn.nihility.selector.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationImportSelectorStart {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(ImportSelectorConfig.class);
        ctx.refresh();

        UserService bean = ctx.getBean(UserService.class);
        System.out.println(bean);

        System.out.println(UserService.class.getName());
        System.out.println(UserService.class.getSimpleName());

        System.out.println("========== close");
        ctx.registerShutdownHook();
    }

}
