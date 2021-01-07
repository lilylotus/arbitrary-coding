package cn.nihility.registrar;

import cn.nihility.entity.DogWalk;
import cn.nihility.entity.PersonWalk;
import cn.nihility.entity.Walk;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@Import(MyImportBeanDefinitionRegistrar.class)
@MapperScan(value = {"cn.nihility.registrar.mapper"})
//@ComponentScan(basePackages = {"cn.nihility.aware"})
public class RegistrarConfiguration {

    /**
     * 方式一： 直接以 Bean 的形式注册
     */
    /*@Bean
    public UserMapper userMapper() {
        return MapperProxy.getProxyUserMapper();
    }*/
    @Bean
    public Walk personWalkxxxx() {
        return new PersonWalk();
    }

    @Bean
    public Walk dogWalkxxxx() {
        return new DogWalk();
    }

    @Bean("getWalk")
    public Walk getPersonWalk() {
        return new PersonWalk();
    }

}
