package cn.nihility.selector2;

import cn.nihility.selector2.annnotation.ImportSelectorAnnotation;
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

import java.io.IOException;
import java.util.*;

public class ImportSelectorStarter implements ImportSelector, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        System.out.println("SelectorImport Start Loading ...");

        Map<String, Object> attributesMap =
                importingClassMetadata.getAnnotationAttributes(ImportSelectorAnnotation.class.getName(), true);
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(attributesMap);

        final List<String> scanPath = Optional.ofNullable(attributes)
                .map(at -> at.getStringArray("value"))
                .map(Arrays::asList)
                .orElse(new ArrayList<>(0));

        final List<String> classNameList = new ArrayList<>(10);
        scanPath.forEach(path -> scanPathClass(path, classNameList));

        System.out.println("SelectorImport Start Loading End");
        return classNameList.toArray(new String[0]);
    }

    private void scanPathClass(String path, List<String> classNameList) {
        System.out.println("Scan class path [" + path + "]");
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        CachingMetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourceLoader);

        // "classpath*:your/package/name/**/*.class"
        String location = "classpath*:" + path.replace(".", "/") + "/**/*.class";
        System.out.println("location [" + location + "]");

        try {
            Resource[] resources = resolver.getResources(location);
            for (Resource resource : resources) {
                MetadataReader metadata = readerFactory.getMetadataReader(resource);
                String className = metadata.getClassMetadata().getClassName();
                System.out.println("Scan class name [" + className + "]");
                classNameList.add(className);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Scan class over");
    }
}
