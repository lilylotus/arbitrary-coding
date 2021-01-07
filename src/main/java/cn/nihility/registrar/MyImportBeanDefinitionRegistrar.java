package cn.nihility.registrar;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware, ResourceLoaderAware {

    private ResourceLoader resourceLoader;
    private int index = 0;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        System.out.println("MyImportBeanDefinitionRegistrar -> registerBeanDefinitions");

        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName()));
        assert attributes != null;
        List<String> list = Arrays.asList(attributes.getStringArray("value"));
        System.out.println("mapper package list : " + list);

        try {
            List<String> mapperList = findResource(list.get(0));
            mapperList.forEach(mapper -> registryBean(registry, mapper));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MyFactoryBeanAdapt.class);
        GenericBeanDefinition beanDefinition = (GenericBeanDefinition) builder.getBeanDefinition();
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(UserMapper.class);
        //走public构造器(且要求参数最多的且参数是在spring容器中)
        beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        registry.registerBeanDefinition("userMapper", beanDefinition);*/
    }

    private void registryBean(BeanDefinitionRegistry registry, String mapper) {
        System.out.println("registry Bean " + mapper);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MyFactoryBeanAdapt.class);
        builder.addPropertyValue("mapperInterface", mapper);

        //AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

        GenericBeanDefinition beanDefinition = (GenericBeanDefinition) builder.getBeanDefinition();
        beanDefinition.getPropertyValues().add("mapperInterface", mapper);

        /*GenericBeanDefinition beanDefinition = (GenericBeanDefinition) builder.getBeanDefinition();
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(mapper);
        //走public构造器(且要求参数最多的且参数是在spring容器中)
        beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);*/

        registry.registerBeanDefinition("userMapper#index" + (++index), beanDefinition);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("beanFactory : " + beanFactory);
    }

    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("environment : " + Optional.ofNullable(environment.getProperty("app.name")).orElse("no app.name property"));
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private List<String> findResource(String basePackage) throws IOException {
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
        // "classpath*:your/package/name/**/*.class"
        String location = "classpath*:" + basePackage.replace(".", "/") + "/**/*.class";
        System.out.println("Location : " + location);
        Resource[] resources = resolver.getResources(location);

        List<String> mapperList = new ArrayList<>();
        for (Resource r : resources) {
            MetadataReader reader = metaReader.getMetadataReader(r);
            System.out.println(reader.getClassMetadata().getClassName());

            mapperList.add(reader.getClassMetadata().getClassName());
        }

        return mapperList;
    }
}
