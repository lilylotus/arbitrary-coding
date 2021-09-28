package cn.nihility.scope;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MyScopeConfiguration {

    @Bean
    public CustomScopeConfigurer myCustomScopeConfigurer(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope("my", new MyScope());
        configurer.postProcessBeanFactory(configurableListableBeanFactory);
        return configurer;
    }

    @Bean
    @Scope("my")
    public RefreshBean myRefreshBean() {
        return new RefreshBean("myRefreshBean");
    }

    @Bean
    public RefreshBean singletonRefreshBean() {
        return new RefreshBean("singletonRefreshBean");
    }

}
