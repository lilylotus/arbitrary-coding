package cn.nihility.starter2;

import cn.nihility.registrar2.RegistrarConfig;
import cn.nihility.registrar2.mapper.SelectMapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class RegistrarApplication {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(RegistrarConfig.class);
        ctx.refresh();

        SelectMapper selectMapper = (SelectMapper) ctx.getBean("mapperInterface#index1");
        System.out.println(selectMapper.selectById(100));

        ctx.registerShutdownHook();
    }

}
