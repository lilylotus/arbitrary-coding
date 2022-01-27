package cn.nihility.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author orchid
 * @date 2021-11-04 23:02:28
 */
class YamlUtilTest {

    String yamlDump(final Yaml yaml, final Object obj) {
        StringWriter sw = new StringWriter();
        yaml.dump(obj, sw);
        return sw.toString();
    }

    @Test
    void testLoadYamlAsMap() throws FileNotFoundException {
        Map<String, Object> map = YamlUtil.yamlHandler(new FileInputStream("/space/coding/arbitrary-coding/src/test/resources/content.yml"));
        Assertions.assertEquals("success", map.get("a.b.c"));

        System.out.println(yamlDump(YamlUtil.createYaml(), map));
        System.out.println(yamlDump(YamlUtil.createDumperYaml(), map));
        System.out.println(YamlUtil.writeFlattenedMapToContent(map));
    }

}
