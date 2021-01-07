package cn.nihility.starter2;

import cn.nihility.registrar2.FactoryBeanStarter;
import cn.nihility.registrar2.entity.EntityA;
import cn.nihility.registrar2.entity.EntityAB;
import cn.nihility.registrar2.entity.EntityB;
import cn.nihility.registrar2.mapper.SelectMapper;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BeanDefinitionApplication {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

        BeanDefinitionBuilder builderA = BeanDefinitionBuilder.genericBeanDefinition(EntityA.class);
        GenericBeanDefinition beanDefinitionA = (GenericBeanDefinition) builderA.getBeanDefinition();
        ctx.registerBeanDefinition("entityA", beanDefinitionA);


        BeanDefinitionBuilder builderB = BeanDefinitionBuilder.genericBeanDefinition(EntityB.class);
        GenericBeanDefinition beanDefinitionB = (GenericBeanDefinition) builderB.getBeanDefinition();
        ctx.registerBeanDefinition("entityB", beanDefinitionB);

        BeanDefinitionBuilder builderAB = BeanDefinitionBuilder.genericBeanDefinition(EntityAB.class);

        builderAB.addConstructorArgReference("entityA");
        builderAB.addConstructorArgReference("entityB");

        builderAB.addPropertyValue("mapperInterface", "cn.nihility.registrar2.mapper.SelectMapper");
        /*builderAB.addPropertyReference("entityA", "entityA");
        builderAB.addPropertyReference("entityB", "entityB");*/
        /*builderAB.addConstructorArgValue("BuilderAB");
        builderAB.addConstructorArgValue("entityA");
        builderAB.addConstructorArgValue(EntityB.class.getName());*/

        //builderAB.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        /*builderAB.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);*/

        /*builderAB.addConstructorArgValue(EntityA.class.getName());
        builderAB.addConstructorArgValue(EntityB.class.getName());*/

        GenericBeanDefinition beanDefinitionAB = (GenericBeanDefinition) builderAB.getBeanDefinition();
         /*MutablePropertyValues propertyValues = beanDefinitionAB.getPropertyValues();
       propertyValues.add("entityA", "entityA");
        propertyValues.add("name", "new Name");
        propertyValues.add("entityB", EntityB.class.getName());*/
        /*propertyValues.add("entityA", "entityA");
        propertyValues.add("entityB", "entityB");*/

        ctx.registerBeanDefinition("entityAB", beanDefinitionAB);


        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FactoryBeanStarter.class);
        builder.addConstructorArgValue(SelectMapper.class.getName());
        //builder.addPropertyValue("mapperInterface", SelectMapper.class);
        ctx.registerBeanDefinition("factoryBeanStarter", builder.getBeanDefinition());

        ctx.refresh();

        EntityA beanA = (EntityA) ctx.getBean("entityA");
        System.out.println(beanA);

        EntityB beanB = (EntityB) ctx.getBean("entityB");
        System.out.println(beanB);

        EntityAB beanAB = (EntityAB) ctx.getBean("entityAB");
        System.out.println(beanAB);

        System.out.println("-------");
        System.out.println(beanA == beanAB.getEntityA());
        System.out.println(beanB == beanAB.getEntityB());

        System.out.println("----------------");
        SelectMapper selectMapper = ctx.getBean(SelectMapper.class);
        System.out.println(selectMapper.selectById(100));

        FactoryBeanStarter factoryBeanStarter = (FactoryBeanStarter) ctx.getBean("&factoryBeanStarter");
        System.out.println(factoryBeanStarter);


        ctx.registerShutdownHook();
    }

}
