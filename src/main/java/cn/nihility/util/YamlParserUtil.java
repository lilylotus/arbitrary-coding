package cn.nihility.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.*;
import java.util.*;

public class YamlParserUtil {

    private static final Logger logger = LoggerFactory.getLogger(YamlParserUtil.class);

    private static final DumperOptions OPTIONS = new DumperOptions();

    static {
        OPTIONS.setIndent(2);
        OPTIONS.setPrettyFlow(true);
        OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        OPTIONS.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
    }

    public static void main(String[] args) throws FileNotFoundException {
        FileInputStream is = new FileInputStream("test.yml");
        Map<String, Object> result = process(is);
        System.out.println(result);

        String yaml = flattenedMapToYaml(result);
        System.out.println(yaml);
    }

    public static Yaml createYaml() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        return new Yaml(options);
    }

    public static String objectToYaml(Object obj) {
        StringWriter sw = new StringWriter(1024);
        Yaml yaml = new Yaml(OPTIONS);
        yaml.dump(obj, sw);
        return sw.toString();
    }

    public static Map<String, Object> process(InputStream yamlInputStream) {
        Yaml yaml = createYaml();
        int count = 0;
        final Map<String, Object> result = new LinkedHashMap<>();

        try (Reader reader = new UnicodeReader(yamlInputStream)) {
            for (Object object : yaml.loadAll(reader)) {
                if (null != object) {
                    Map<String, Object> tempMap = parseFlattenedMap(asMap(object));
                    if (!tempMap.isEmpty()) {
                        count++;
                        result.putAll(tempMap);
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded [{}] document{} from YAML resource.", count, (count > 1 ? "s" : ""));
            }
        } catch (IOException ex) {
            logger.error("Process Yaml resource case exception", ex);
        }

        return result;
    }

    public static String flattenedMapToYaml(Map<String, Object> flattenedMap) {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>(flattenedMap.size());
        flattenedMap.forEach((k, v) -> buildFlattenedYamlData(k, v, result));
        return objectToYaml(result);
    }

    @SuppressWarnings("unchecked")
    private static void buildFlattenedYamlData(String property, Object value, Map<String, Object> source) {
        if (null == property || "".equals(property.trim()) || source == null) {
            return;
        }
        PropertyTokenizer tokenizer = new PropertyTokenizer(property);
        String curKey = tokenizer.getIndexedName();
        if (tokenizer.hasNext()) {
            if (source.containsKey(curKey)) {
                Object currentKeyValue = source.get(curKey);
                if (currentKeyValue instanceof Map) {
                    // 说明是正常的 yaml 格式的数据
                    @SuppressWarnings("unchecked")
                    LinkedHashMap<String, Object> curValue = (LinkedHashMap<String, Object>) currentKeyValue;
                    buildFlattenedYamlData(tokenizer.getChildren(), value, curValue);
                } else if (currentKeyValue instanceof String) {
                    // 目前知道的是 yaml 中套用了 properties 的写法
                    source.put(property, value);
                }
            } else {
                Map<String, Object> item = new LinkedHashMap<>(8);
                source.put(curKey, item);
                buildFlattenedYamlData(tokenizer.getChildren(), value, item);
            }
        } else {
            Object val = source.get(curKey);
            if (null == val) {
                source.put(curKey, value);
            } else {
                if (val instanceof Map) {
                    Map<String, Object> result = new LinkedHashMap<>();

                    LinkedHashMap<String, Object> curValue = (LinkedHashMap<String, Object>) val;
                    buildFlattenedMap(result, curValue, null);
                    source.put(curKey, value);
                    result.forEach((k, v) -> source.put(curKey + "." + k, v));
                } else {
                    source.put(curKey, value);
                }
            }

        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object object) {
        // YAML can have numbers as keys
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map)) {
            // A document can be a text literal
            result.put("document", object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                value = asMap(value);
            }
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                // It has to be a map key in this case
                result.put("[" + key.toString() + "]", value);
            }
        });
        return result;
    }

    public static Map<String, Object> parseFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    private static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        source.forEach((key, value) -> {
            if (StringUtils.hasText(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) value;
                if (collection.isEmpty()) {
                    result.put(key, "");
                } else {
                    int count = 0;
                    for (Object object : collection) {
                        buildFlattenedMap(result, Collections.singletonMap(
                                "[" + (count++) + "]", object), key);
                    }
                }
            } else {
                result.put(key, (value != null ? value : ""));
            }
        });
    }


    static class PropertyTokenizer implements Iterator<PropertyTokenizer> {
        private String name;
        private String indexedName;
        private String index;
        private String children;

        public PropertyTokenizer(String fullname) {
            // richType.richField 或者 List -> list[0] 或者 map -> map[key]
            // generateLeaderNodeRule[.*[部]$]: "部领导"
            try {
                int delim = fullname.indexOf('.');
                if (delim > -1) {
                    name = fullname.substring(0, delim);
                    children = fullname.substring(delim + 1);
                } else {
                    name = fullname;
                    children = null;
                }
                indexedName = name;
                delim = name.indexOf('[');
                // list[0].fieldProperty
                // name = list , indexedName = list[0]
                if (delim > -1) {
                    // list[0] : map[key]
                    index = name.substring(delim + 1, name.length() - 1);
                    name = name.substring(0, delim);
                }
            } catch (Exception ex) {
                logger.error("token [{}] parse case error", fullname, ex);
                name = fullname;
                indexedName = name;
                children = null;
            }
        }

        public String getName() {
            return name;
        }

        public String getIndex() {
            return index;
        }

        public String getIndexedName() {
            return indexedName;
        }

        public String getChildren() {
            return children;
        }

        @Override
        public boolean hasNext() {
            return children != null;
        }

        @Override
        public PropertyTokenizer next() {
            return new PropertyTokenizer(children);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
        }
    }

}
