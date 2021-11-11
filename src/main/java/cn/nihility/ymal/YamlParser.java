package cn.nihility.ymal;

import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.core.io.ByteArrayResource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class YamlParser extends AbstractDataParser {

    protected YamlParser() {
        super(",yml,yaml,");
    }

    @Override
    protected Map<String, Object> doParse(String yamlContent) {
        YamlMapFactoryBean yamlFactory = new YamlMapFactoryBean();
        yamlFactory.setResources(new ByteArrayResource(yamlContent.getBytes()));
        //yamlFactory.setResources(new FileSystemResource("test.yml"));

        Map<String, Object> result = new LinkedHashMap<>();
        flattenedMap(result, Objects.requireNonNull(yamlFactory.getObject()), EMPTY_STRING);

        return result;
    }

}
