package cn.nihility.scope;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MyScopeTest {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(MyScopeConfiguration.class);
        ctx.refresh();

        RefreshBean bean = ctx.getBean("myRefreshBean", RefreshBean.class);
        System.out.println(("获取 bean [" + bean + "]"));

        bean = ctx.getBean("myRefreshBean", RefreshBean.class);
        System.out.println(("获取 bean [" + bean + "]"));

        bean = ctx.getBean("myRefreshBean", RefreshBean.class);
        System.out.println(("获取 bean [" + bean + "]"));

        bean = ctx.getBean("singletonRefreshBean", RefreshBean.class);
        System.out.println(("获取 singleton bean [" + bean + "]"));

        bean = ctx.getBean("singletonRefreshBean", RefreshBean.class);
        System.out.println(("获取 singleton bean [" + bean + "]"));

        ctx.registerShutdownHook();
    }

}
