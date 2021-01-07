package cn.nihility.spring.start;

import cn.nihility.spring.config.StartConfig;
import cn.nihility.spring.entity.DepartmentEntity;
import cn.nihility.spring.entity.PersonEntity;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringStarter {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(StartConfig.class);
        ctx.refresh();

        PersonEntity bean = (PersonEntity) ctx.getBean("major");
        System.out.println(bean);

        DepartmentEntity bean1 = ctx.getBean(DepartmentEntity.class);
        System.out.println(bean1);

        ctx.registerShutdownHook();
    }

}
