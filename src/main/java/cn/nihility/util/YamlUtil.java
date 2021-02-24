package cn.nihility.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Yaml 处理工具接口
 */
public class YamlUtil {

    private static final Logger log = LoggerFactory.getLogger(YamlUtil.class);
    private static final DumperOptions OPTIONS = new DumperOptions();
    private static final ClassLoaderWrapper CLASS_LOADER_WRAPPER = new ClassLoaderWrapper();

    static {
        OPTIONS.setIndent(2);
        OPTIONS.setPrettyFlow(true);
        OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        OPTIONS.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    }

    /* ================ 以 a.b.c: value 的 map 格式来处理 yaml 文件数据 ================ */

    /**
     * yaml 文件加载为 LinkedHashMap 数据格式
     * @param yamlFilePath yaml 文件路径
     * @return x.xx.xx: value 格式的 yaml map 数据
     */
    public static LinkedHashMap<String, Object> yamlFileHandler(String yamlFilePath) {
        InputStream inputStream = CLASS_LOADER_WRAPPER.getResourceAsStream(yamlFilePath, YamlUtil.class.getClassLoader());
        return yamlHandler(inputStream);
    }

    /**
     * yaml 内容解析为 LinkedHashMap 数据格式
     * @param yamlContent yaml 文件内容
     * @return x.xx.xx: value 格式的 yaml map 数据
     */
    public static LinkedHashMap<String, Object> yamlContentHandler(String yamlContent) {
        if (StringUtil.isEmpty(yamlContent)) {
            return new LinkedHashMap<>(4);
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
        return yamlHandler(inputStream);
    }

    public static LinkedHashMap<String, Object> yamlHandler(InputStream inputStream) {
        //返回的结果
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        if (null == inputStream) {
            log.warn("yaml 数据加载流为空");
            return result;
        }
        try (UnicodeReader reader = new UnicodeReader(inputStream)) {
            //单文件处理
            Yaml yaml = new Yaml();
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> data = yaml.loadAs(reader, LinkedHashMap.class);
            // 这里只是简单处理，需要多个方式可以自己添加
            buildFlattenedMap(result, data, null);
        } catch (IOException e) {
            log.error("加载 yaml 数据格式为 Map 格式出错", e);
        }
        return result;
    }

    /**
     * 把 Yaml 原始加载的 yaml 文件格式改为 xx.xxx.xx: value 格式的 map
     * @param result 转换后扁平化的 map
     * @param source Yaml 类加载 yml 文件后的原始数据
     * @param path key 值
     */
    public static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, final String path) {
        //循环读取原数据
        source.forEach((key, value) -> {
            //如果存在路径进行拼接
            if (StringUtil.hasText(path)) {
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
                // 如果是 map,就继续读取
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) value;
                if (collection.isEmpty()) {
                    result.put(key, "");
                } else {
                    /*int count = 0;
                    for (Object object : collection) {
                        buildFlattenedMap(result, Collections.singletonMap("[" + count++ + "]", object), key);
                    }*/
                    result.put(key, value);
                }
            } else {
                result.put(key, value != null ? value : "");
            }
        });
    }

    /**
     * 写入扁平格式化后的 yaml map 数据到指定文件
     * @param flattenedMap 格式化后的 yaml 文件数据
     * @param writeToFile 写入的文件
     */
    public static void writeFlattenedMapToFile(LinkedHashMap<String, Object> flattenedMap, File writeToFile) {
        final LinkedHashMap<String, Object> writeMap = new LinkedHashMap<>(flattenedMap.size());
        flattenedMap.forEach((k, v) -> buildFlattenedYamlData(k, writeMap, v));
        serializeObjectToYamlFile(writeMap, writeToFile);
    }

    /**
     * 包扁平格式化后的 yaml map 数据变为 yaml 格式的 String
     * @param flattenedMap 格式化后的 yaml 文件数据
     */
    public static String writeFlattenedMapToContent(LinkedHashMap<String, Object> flattenedMap) {
        final LinkedHashMap<String, Object> writeMap = new LinkedHashMap<>(flattenedMap.size());
        flattenedMap.forEach((k, v) -> buildFlattenedYamlData(k, writeMap, v));
        return serializeObjectToYamlContent(writeMap);
    }

    /**
     * 把扁平化处理的 yaml map (xxx.xxx.xx: value) 数据格式变为
     * xxx:
     *   xxx:
     *     xx: value
     * 的数据格式
     * @param property person.name
     * @param yamlData 从 yaml 文件中解析后的数据
     */
    public static void buildFlattenedYamlData(String property, LinkedHashMap<String, Object> yamlData, Object value) {
        if (null == property || "".equals(property.trim()) || yamlData == null) {
            return;
        }
        PropertyTokenizer tokenizer = new PropertyTokenizer(property);
        String curKey = tokenizer.getIndexedName();
        if (tokenizer.hasNext()) {
            if (yamlData.containsKey(curKey)) {
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, Object> curValue = (LinkedHashMap<String, Object>) yamlData.get(curKey);
                buildFlattenedYamlData(tokenizer.getChildren(), curValue, value);
            } else {
                LinkedHashMap<String, Object> item = new LinkedHashMap<>(8);
                yamlData.put(curKey, item);
                buildFlattenedYamlData(tokenizer.getChildren(), item, value);
            }
        } else {
            yamlData.put(curKey, value);
        }
    }

    /**
     * 把对象转为 YAML 格式，并写入到指定文件
     * @param obj 要转为 YAML 格式的对象
     */
    public static void serializeObjectToYamlFile(Object obj, File toFile) {
        try (final BufferedWriter writer =
                 new BufferedWriter(new OutputStreamWriter(
                     new FileOutputStream(toFile), StandardCharsets.UTF_8))) {
            Yaml yaml = new Yaml(OPTIONS);
            yaml.dump(obj, writer);
        } catch (FileNotFoundException e) {
            log.error("写入文件 [{}] 未找到", toFile.getAbsolutePath(), e);
        } catch (IOException e) {
            log.error("写 yml 文件 [{}] 出错", toFile.getAbsolutePath(), e);
        }
    }

    /**
     * 把对象转为 yaml 文件格式的 String 内容
     * @param obj 要转换为 yaml 格式的对象
     */
    public static String serializeObjectToYamlContent(Object obj) {
        StringWriter sw = new StringWriter(1024);
        Yaml yaml = new Yaml(OPTIONS);
        yaml.dump(obj, sw);
        return sw.toString();
    }

    public static void main(String[] args) throws IOException {
//        LinkedHashMap<String, Object> data = loadAsLinkedHashMap("F:\\workCoding\\kiam-umc\\backend\\src\\main\\resources\\config\\urm\\urm.yml");
        //LinkedHashMap<String, Object> data = yamlFileHandler("config\\urm\\urm.yml");

        List<String> lines = Files.readAllLines(Paths.get("F:\\workCoding\\kiam-umc\\backend\\src\\main\\resources\\config\\urm\\urm.yml"));
        StringJoiner yaml = new StringJoiner("\n");
        lines.forEach(yaml::add);
        String yamlContent = yaml.toString();

        LinkedHashMap<String, Object> data = yamlContentHandler(yamlContent);
        buildFlattenedYamlData("management.health.mail.enabled", data, true);
        buildFlattenedYamlData("management.health.mail.test", data, true);
        buildFlattenedYamlData("a.b.c.d", data, true);

        data.put("e.f.g.h", true);

        writeFlattenedMapToFile(data, new File("D:\\flatten2.yml"));
        String content = writeFlattenedMapToContent(data);
        System.out.println(content);

    }

}
