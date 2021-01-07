package cn.nihility.selector;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
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
import java.util.Map;

public class MyImportSelector implements ImportSelector, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(ImportScan.class.getName(), true);
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(attributes);
        assert annotationAttributes != null;
        List<String> basePackages = Arrays.asList(annotationAttributes.getStringArray("value"));
        System.out.println(basePackages);

        List<String> clazzNameList = new ArrayList<>();
        findResource(clazzNameList, basePackages);

        System.out.println("MyImportSelector -> selectImports clazzNameList " + clazzNameList);
        return clazzNameList.toArray(new String[0]);
//        return new String[] {OrderService.class.getName(), UserService.class.getName()};
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private void findResource(List<String> clazzNameList, List<String> basePackages) {
        basePackages.forEach(b -> findResource(clazzNameList, b));
    }

    private void findResource(List<String> clazzNameList, String basePackage) {
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
        // "classpath*:your/package/name/**/*.class"
        String location = "classpath*:" + basePackage.replace(".", "/") + "/*/*.class";
        System.out.println("Location : " + location);
        try {
            Resource[] resources = resolver.getResources(location);
            for (Resource r : resources) {
                MetadataReader reader = metaReader.getMetadataReader(r);
                System.out.println(reader.getClassMetadata().getClassName());

                if (!reader.getClassMetadata().isInterface()) {
                    clazzNameList.add(reader.getClassMetadata().getClassName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
