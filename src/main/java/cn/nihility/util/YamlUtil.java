package cn.nihility.util;

import cn.nihility.ymal.PropertyTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Yaml 处理工具接口
 */
public class YamlUtil {

    private static final Logger log = LoggerFactory.getLogger(YamlUtil.class);
    private static final ClassLoaderWrapper CLASS_LOADER_WRAPPER = new ClassLoaderWrapper();
    private static final DumperOptions OPTIONS = new DumperOptions();

    private YamlUtil() {
    }

    static {
        OPTIONS.setIndent(2);
        OPTIONS.setPrettyFlow(true);
        OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        OPTIONS.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    }

    public static <T> T loadAs(String yamlPath, Class<T> clazz) {
        InputStream inputStream = CLASS_LOADER_WRAPPER.getResourceAsStream(yamlPath);
        if (inputStream == null) {
            log.error("配置文件 [{}] 未找到", yamlPath);
            return null;
        }

        // yaml 是线程不安全的，要每次初始化
        Yaml yaml = new Yaml(OPTIONS);
        T result = yaml.loadAs(inputStream, clazz);
        try {
            inputStream.close();
        } catch (IOException e) {
            log.error("close [{}] failure", yamlPath, e);
        }

        return result;
    }

    public static <T> T parseContentAs(String yamlDataContent, Class<T> clazz) {
        if (null == yamlDataContent) {
            return null;
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(yamlDataContent.getBytes(StandardCharsets.UTF_8));
        // yaml 是线程不安全的，要每次初始化
        Yaml yaml = new Yaml(OPTIONS);
        T result = yaml.loadAs(inputStream, clazz);
        try {
            inputStream.close();
        } catch (IOException e) {
            // nothing
        }
        return result;
    }

    /**
     * LinkedHashMap 有序的数据
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadAsLinkedHashMap(String yamlPath) {
        return loadAs(yamlPath, LinkedHashMap.class);
    }

    /**
     * LinkedHashMap 有序的数据
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadContentAsLinkedHashMap(String yamlDataContent) {
        return parseContentAs(yamlDataContent, LinkedHashMap.class);
    }

    /**
     * 获取 yaml 中指定 key 的数据,目前仅处理 Map 类型数据
     *
     * @param property person.name
     * @param yamlData 从 yaml 文件中解析后的数据，处理 LinkedHashMap 类型的数据
     * @return 指定属性的值， null 不存在
     */
    public static Object getValue(String property, Map<String, Object> yamlData) {
        if (null == property || "".equals(property.trim()) || yamlData == null) {
            return null;
        }
        cn.nihility.ymal.PropertyTokenizer tokenizer = new cn.nihility.ymal.PropertyTokenizer(property);
        if (tokenizer.hasNext()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> curData = (Map<String, Object>) yamlData.get(tokenizer.getIndexedName());
            return getValue(tokenizer.getChildren(), curData);
        } else {
            return yamlData.get(tokenizer.getIndexedName());
        }
    }

    /**
     * 配置 yaml 中指定 key 的数据， 仅更新现有的值，不会新增
     *
     * @param property person.name
     * @param yamlData 从 yaml 文件中解析后的数据，处理 LinkedHashMap 的数据
     * @return 指定属性的值， null 不存在
     */
    public static boolean setValue(String property, Map<String, Object> yamlData, Object value) {
        if (null == property || "".equals(property.trim()) || yamlData == null) {
            return false;
        }
        cn.nihility.ymal.PropertyTokenizer tokenizer = new cn.nihility.ymal.PropertyTokenizer(property);
        String curKey = tokenizer.getIndexedName();
        if (tokenizer.hasNext()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> curValue = (Map<String, Object>) yamlData.get(curKey);
            return setValue(tokenizer.getChildren(), curValue, value);
        } else {
            if (yamlData.containsKey(curKey)) {
                yamlData.put(curKey, value);
                return true;
            } else {
                log.warn("property: {}, curKey[{}] in yamlData is not exist", property, curKey);
                return false;
            }
        }
    }

    /**
     * 配置 yaml 中 Key 数据， key 不存在则添加，存在则更新
     *
     * @param property person.name
     * @param yamlData 从 yaml 文件中解析后的数据
     * @return 指定属性的值， null 不存在
     */
    public static void putValue(String property, Map<String, Object> yamlData, Object value) {
        if (null == property || "".equals(property.trim()) || yamlData == null) {
            return;
        }
        cn.nihility.ymal.PropertyTokenizer tokenizer = new cn.nihility.ymal.PropertyTokenizer(property);
        String curKey = tokenizer.getIndexedName();
        if (tokenizer.hasNext()) {
            if (yamlData.containsKey(curKey)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> curData = (Map<String, Object>) yamlData.get(curKey);
                putValue(tokenizer.getChildren(), curData, value);
            } else {
                Map<String, Object> item = new LinkedHashMap<>();
                yamlData.put(curKey, item);
                putValue(tokenizer.getChildren(), item, value);
            }
        } else {
            yamlData.put(curKey, value);
        }
    }

    /**
     * 把对象转为 YAML 格式，并返回该 YAML
     *
     * @param obj 要转为 YAML 格式的对象
     */
    public static String serializeObjectToYaml(Object obj) {
        if (null == obj) {
            return null;
        }

        StringWriter sw = new StringWriter();
        Yaml yaml = new Yaml(OPTIONS);
        yaml.dump(obj, sw);

        return sw.toString();
    }

    /* ================ 以 a.b.c: value 的 map 格式来处理 yaml 文件数据 ================ */

    /**
     * yaml 文件加载为 LinkedHashMap 数据格式
     *
     * @param yamlFilePath yaml 文件路径
     * @return x.xx.xx: value 格式的 yaml map 数据
     */
    public static Map<String, Object> yamlFileHandler(String yamlFilePath) {
        InputStream inputStream = CLASS_LOADER_WRAPPER.getResourceAsStream(yamlFilePath, YamlUtil.class.getClassLoader());
        return yamlHandler(inputStream);
    }

    public static void main(String[] args) {
        Map<String, Object> map = yamlFileHandler("config/urm/urm.yml");
        System.out.println(map);
        writeFlattenedMapToFile(map, new File("D:/flattenedMap.yaml"));
    }

    /**
     * yaml 内容解析为 LinkedHashMap 数据格式
     *
     * @param yamlContent yaml 文件内容
     * @return x.xx.xx: value 格式的 yaml map 数据
     */
    public static Map<String, Object> yamlContentHandler(String yamlContent) {
        if (StringUtil.isEmpty(yamlContent)) {
            return new LinkedHashMap<>();
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
        return yamlHandler(inputStream);
    }

    public static Map<String, Object> yamlHandler(InputStream inputStream) {
        //返回的结果
        Map<String, Object> result = new LinkedHashMap<>();
        if (null == inputStream) {
            log.warn("yaml 数据加载流为空");
            return result;
        }
        try (UnicodeReader reader = new UnicodeReader(inputStream)) {
            //单文件处理
            Yaml yaml = new Yaml();
            @SuppressWarnings("unchecked")
            Map<String, Object> data = yaml.loadAs(reader, LinkedHashMap.class);
            // 这里只是简单处理，需要多个方式可以自己添加
            buildFlattenedMap(result, data, null);
        } catch (IOException e) {
            log.error("加载 yaml 数据格式为 Map 格式出错", e);
        }
        return result;
    }

    /**
     * 把 Yaml 原始加载的 yaml 文件格式改为 xx.xxx.xx: value 格式的 map
     *
     * @param result 转换后扁平化的 map
     * @param source Yaml 类加载 yml 文件后的原始数据
     * @param path   key 值
     */
    public static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, final String path) {
        //循环读取原数据
        source.forEach((key, value) -> {
            //如果存在路径进行拼接
            if (StringUtil.hasText(path)) {
                key = path + '.' + key;
                /*if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }*/
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
                result.put(key, value);
            } else {
                result.put(key, value == null ? "" : value);
            }
        });
    }

    /**
     * 写入扁平格式化后的 yaml map 数据到指定文件
     *
     * @param flattenedMap 格式化后的 yaml 文件数据
     * @param writeToFile  写入的文件
     */
    public static void writeFlattenedMapToFile(Map<String, Object> flattenedMap, File writeToFile) {
        final Map<String, Object> writeMap = new LinkedHashMap<>(flattenedMap.size());
        flattenedMap.forEach((k, v) -> buildFlattenedYamlData(k, writeMap, v));
        serializeObjectToYamlFile(writeMap, writeToFile);
    }

    /**
     * 包扁平格式化后的 yaml map 数据变为 yaml 格式的 String
     *
     * @param flattenedMap 格式化后的 yaml 文件数据
     */
    public static String writeFlattenedMapToContent(Map<String, Object> flattenedMap) {
        final LinkedHashMap<String, Object> writeMap = new LinkedHashMap<>(flattenedMap.size());
        flattenedMap.forEach((k, v) -> buildFlattenedYamlData(k, writeMap, v));
        return serializeObjectToYamlContent(writeMap);
    }

    /**
     * 把扁平化处理的 yaml map (xxx.xxx.xx: value) 数据格式变为
     * xxx:
     * xxx:
     * xx: value
     * 的数据格式
     *
     * @param property person.name
     * @param yamlData 从 yaml 文件中解析后的数据
     */
    public static void buildFlattenedYamlData(String property, Map<String, Object> yamlData, Object value) {
        if (null == property || "".equals(property.trim()) || yamlData == null) {
            return;
        }
        cn.nihility.ymal.PropertyTokenizer tokenizer = new PropertyTokenizer(property);
        String curKey = tokenizer.getIndexedName();
        if (tokenizer.hasNext()) {
            if (yamlData.containsKey(curKey)) {
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, Object> curValue = (LinkedHashMap<String, Object>) yamlData.get(curKey);
                buildFlattenedYamlData(tokenizer.getChildren(), curValue, value);
            } else {
                Map<String, Object> item = new LinkedHashMap<>(8);
                yamlData.put(curKey, item);
                buildFlattenedYamlData(tokenizer.getChildren(), item, value);
            }
        } else {
            yamlData.put(curKey, value);
        }
    }

    /**
     * 把对象转为 YAML 格式，并写入到指定文件
     *
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
     *
     * @param obj 要转换为 yaml 格式的对象
     */
    public static String serializeObjectToYamlContent(Object obj) {
        StringWriter sw = new StringWriter(1024);
        Yaml yaml = new Yaml(OPTIONS);
        yaml.dump(obj, sw);
        return sw.toString();
    }

}
