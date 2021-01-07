package cn.nihility.registrar2;

import cn.nihility.registrar2.annotation.RegistrarAnnotation;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;
import java.util.*;

public class ImportBeanDefinitionRegistrarStarter implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private ResourceLoader resourceLoader;
    private int index = 0;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        System.out.println("RegisterBeanDefinitions starting.");
        Map<String, Object> attributesMap =
                importingClassMetadata.getAnnotationAttributes(RegistrarAnnotation.class.getName(), true);
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(attributesMap);

        List<String> scanList = Optional.ofNullable(attributes)
                .map(at -> at.getStringArray("value"))
                .map(Arrays::asList)
                .orElse(new ArrayList<>(0));
        System.out.println("Scan " + scanList);

        final List<String> classNameList = new ArrayList<>(10);
        scanList.forEach(path -> findResources(path, classNameList));
        classNameList.forEach(clazz -> registryBean(registry, clazz));

        System.out.println("RegisterBeanDefinitions over.");

        /*BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MyFactoryBeanAdapt.class);
        GenericBeanDefinition beanDefinition = (GenericBeanDefinition) builder.getBeanDefinition();
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(UserMapper.class);
        //走public构造器(且要求参数最多的且参数是在spring容器中)
        beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        registry.registerBeanDefinition("userMapper", beanDefinition);*/
    }

    private void registryBean(BeanDefinitionRegistry registry, String clazzFullName) {
        System.out.println("registry class [" + clazzFullName + "]");

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FactoryBeanStarter.class);
        //builder.addPropertyValue("mapperInterface", clazzFullName);

        GenericBeanDefinition beanDefinition = (GenericBeanDefinition) builder.getBeanDefinition();
        /* 要有对应的 getter/setter 方法 */
        //beanDefinition.getPropertyValues().add("mapperInterface", clazzFullName);
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(clazzFullName);
        beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        /*GenericBeanDefinition beanDefinition = (GenericBeanDefinition) builder.getBeanDefinition();
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(mapper);
        // 走 public 构造器(且要求参数最多的且参数是在 spring 容器中)
        beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);*/

        String registryBeanName = "mapperInterface#index" + (++index);
        registry.registerBeanDefinition(registryBeanName, beanDefinition);
        System.out.println("Registry bean name [" + registryBeanName + "]");
    }

    private void findResources(String path, List<String> classNameList) {
        System.out.println("Scan resource path [" + path + "]");
        String location = "classpath*:" + path.replace(".", "/") + "/**/*.class";
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
        CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory(resourceLoader);
        try {
            Resource[] resources = resolver.getResources(location);
            for (Resource resource : resources) {
                MetadataReader metadata = factory.getMetadataReader(resource);
                String className = metadata.getClassMetadata().getClassName();
                System.out.println("Scan class name [" + className + "]");
                classNameList.add(className);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Scan resource path over");
    }
}
