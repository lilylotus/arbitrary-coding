package cn.nihility.ymal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Yaml 处理工具接口
 */
public class YamlUtil {

    private static final Logger log = LoggerFactory.getLogger(YamlUtil.class);
    private static final DumperOptions OPTIONS = new DumperOptions();

    static {
        OPTIONS.setIndent(2);
        OPTIONS.setPrettyFlow(true);
        OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        OPTIONS.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    }

    public static void main(String[] args) {
        String loadRootPath = "F:\\workCoding\\iam-common\\backend\\build\\libs\\new\\application.yml";
        final Map<String, Object> map = yamlHandler(new File(loadRootPath));
        System.out.println(map);
    }

    /**
     * LinkedHashMap 有序的数据
     */
    public static LinkedHashMap<String, Object> loadAsLinkedHashMap(File ymlFile) {
        return yamlHandler(ymlFile);
    }

    /**
     * 单个yaml文件处理
     */
    public static LinkedHashMap<String, Object> yamlHandler(File yamlFile) {
        //返回的结果
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        try (UnicodeReader reader = new UnicodeReader(new FileInputStream(yamlFile))) {
            //单文件处理
            Yaml yaml = new Yaml();
            Object object = yaml.load(reader);
            //这里只是简单处理，需要多个方式可以自己添加
            if (object instanceof Map) {
                Map map = (Map) object;
                buildFlattenedMap(result, map, null);
            }
        } catch (IOException e) {
            log.error("读取 yaml [{}] 异常", yamlFile.getAbsolutePath(), e);
        }
        return result;
    }

    public static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, final String path) {
        //循环读取原数据
        source.forEach((key, value) -> {
            //如果存在路径进行拼接
            if (StringUtils.hasText(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }
            //数据类型匹配
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                //如果是map,就继续读取
                Map<String, Object> map = (Map) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                Collection<Object> collection = (Collection) value;
                if (collection.isEmpty()) {
                    result.put(key, "");
                } else {
                    int count = 0;
                    for (Object object : collection) {
                        // 使用 key[0]: value 方式，数组
                        buildFlattenedMap(result, Collections.singletonMap("[" + count++ + "]", object), key);
                    }
                    // 使用 - value 方式 数组方式
                    // result.put(key, value);
                }
            } else {
                result.put(key, value != null ? value : "");
            }
        });
    }

    /**
     * 把对象转为 YAML 格式，并返回该 YAML
     * @param obj 要转为 YAML 格式的对象
     */
    public static void serializeObjectToYaml(Object obj, File toFile) {
        try (final BufferedWriter writer =
                     new BufferedWriter(new OutputStreamWriter(
                             new FileOutputStream(toFile), StandardCharsets.UTF_8))) {
            Yaml yaml = new Yaml(OPTIONS);
            yaml.dump(obj, writer);
        } catch (FileNotFoundException e) {
            log.error("写入文件 [{}] 为找到", toFile.getAbsolutePath(), e);
        } catch (IOException e) {
            log.error("写 yml 文件 [{}] 出错", toFile.getAbsolutePath(), e);
        }
    }

    /**
     * 配置 yaml 中 Key 数据， key 不存在则添加，存在则更新
     * @param property person.name
     * @param yamlData 从 yaml 文件中解析后的数据
     */
    public static void putValue(String property, LinkedHashMap yamlData, Object value) {
        if (null == property || "".equals(property.trim()) || yamlData == null) {
            return;
        }
        PropertyTokenizer tokenizer = new PropertyTokenizer(property);
        String curKey = tokenizer.getIndexedName();
        if (tokenizer.hasNext()) {
            if (yamlData.containsKey(curKey)) {
                putValue(tokenizer.getChildren(), (LinkedHashMap) yamlData.get(curKey), value);
            } else {
                LinkedHashMap<Object, Object> item = new LinkedHashMap<>();
                yamlData.put(curKey, item);
                putValue(tokenizer.getChildren(), item, value);
            }
        } else {
            yamlData.put(curKey, value);
        }
    }

    public static void writeFlattenedMap(LinkedHashMap<String, Object> flattenedMap, File writeToFile) {
        LinkedHashMap<String, Object> writeMap = new LinkedHashMap<>(flattenedMap.size());
        flattenedMap.forEach((k, v) -> putValue(k, writeMap, v));
        serializeObjectToYaml(writeMap, writeToFile);
    }

    public static void mergeReadYamlData(LinkedHashMap<String, Object> to, final LinkedHashMap<String, Object> result) {
        to.forEach(result::put);
    }

    public static void mergeMapData(LinkedHashMap<String, Object> to, final LinkedHashMap<String, Object> result) {
        to.forEach((k, v) -> {
            if (result.containsKey(k)) {
                if (!Objects.equals(v, result.get(k))) {
                    System.out.println("已配置值 [" + k + ":" + v + "]");
                    result.put(k, v);
                }
            }
        });
    }

}
