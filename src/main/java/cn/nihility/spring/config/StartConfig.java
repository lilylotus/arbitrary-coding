package cn.nihility.spring.config;

import cn.nihility.spring.entity.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class StartConfig {

    @Bean("major")
    public PersonEntity major1() {
        return new PersonEntity("Major", 40);
    }

    @Bean
    public IMember member1() {
        return new Member1();
    }

    @Bean
    public IMember member2() {
        return new Member2();
    }

    @Bean
    public DepartmentEntity departmentEntity(List<IMember> members) {
        return new DepartmentEntity(major1(), members);
    }


}
