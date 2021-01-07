package cn.nihility.starter;


import cn.nihility.aware.ContextAwareConfig;
import cn.nihility.aware.MyApplicationContextAware;
import cn.nihility.entity.DogWalk;
import cn.nihility.entity.PersonWalk;
import cn.nihility.entity.Walk;
import cn.nihility.registrar.RegistrarConfiguration;
import cn.nihility.registrar.User;
import cn.nihility.registrar.mapper.UserMapper02;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationContextAwareStarter {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(ContextAwareConfig.class, RegistrarConfiguration.class);
        ctx.refresh();

        UserMapper02 userMapper02 = MyApplicationContextAware.getBean(UserMapper02.class);
        User user1 = userMapper02.selectUserById(1);
        System.out.println(user1);

        DogWalk dogWalk = MyApplicationContextAware.getBean(DogWalk.class);
        System.out.println(dogWalk);

        Walk walk = (Walk) MyApplicationContextAware.getBean("personWalkxxxx");
        walk.walk();

        walk = (Walk) MyApplicationContextAware.getBean("dogWalkxxxx");
        walk.walk();


        Walk walk1 = (Walk) MyApplicationContextAware.getBean("getWalk");
        walk1.walk();

        PersonWalk personWalkxxxx = MyApplicationContextAware.getBean("personWalkxxxx", PersonWalk.class);
        personWalkxxxx.walk();

        System.out.println("========== close");
        ctx.registerShutdownHook();
    }

}
