package cn.nihility.ymal;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

public abstract class AbstractDataParser {

    protected static final String DOT = ".";

    protected static final String VALUE = "value";

    protected static final String EMPTY_STRING = "";

    private String extension;

    private AbstractDataParser nextParser;

    protected AbstractDataParser(String extension) {
        if (StringUtils.isEmpty(extension)) {
            throw new IllegalArgumentException("extension cannot be empty");
        }
        this.extension = extension.toLowerCase();
    }

    protected abstract Map<String, Object> doParse(String data) throws IOException;

    /**
     * Parsing data configuration content.
     *
     * @param data      config data
     * @param extension file extension. json or xml or yml or yaml or properties
     * @return result of Properties
     * @throws IOException thrown if there is a problem parsing config.
     */
    public final Map<String, Object> parseData(String data, String extension)
        throws IOException {
        if (extension == null || extension.length() < 1) {
            throw new IllegalStateException("The file extension cannot be empty");
        }
        if (this.isLegal(extension.toLowerCase())) {
            return this.doParse(data);
        }
        if (this.nextParser == null) {
            throw new IllegalStateException(getTips(extension));
        }
        return this.nextParser.parseData(data, extension);
    }

    protected AbstractDataParser setNextParser(AbstractDataParser nextParser) {
        this.nextParser = nextParser;
        return this;
    }

    public AbstractDataParser addNextParser(AbstractDataParser nextParser) {
        if (this.nextParser == null) {
            this.nextParser = nextParser;
        } else {
            this.nextParser.addNextParser(nextParser);
        }
        return this;
    }

    /**
     * Verify dataId extensions.
     *
     * @param extension file extension. json or xml or yml or yaml or properties
     * @return valid or not
     */
    public final boolean checkFileExtension(String extension) {
        if (this.isLegal(extension.toLowerCase())) {
            return true;
        }
        if (this.nextParser == null) {
            return false;
        }
        return this.nextParser.checkFileExtension(extension);
    }

    protected boolean isLegal(String extension) {
        return this.extension.equalsIgnoreCase(extension)
            || this.extension.contains(extension);
    }

    protected void flattenedMap(Map<String, Object> result, Map<String, Object> dataMap,
                                String parentKey) {
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String fullKey = StringUtils.isEmpty(parentKey) ? key :
                (key.startsWith("[") ? parentKey.concat(key) : parentKey.concat(DOT).concat(key));

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                flattenedMap(result, map, fullKey);
                continue;
            } else if (value instanceof Collection) {
                int count = 0;
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) value;
                for (Object object : collection) {
                    flattenedMap(result,
                        Collections.singletonMap("[" + (count++) + "]", object),
                        fullKey);
                }
                continue;
            }

            result.put(fullKey, value);
        }
    }

    /**
     * Reload the key ending in `value` if need.
     */
    protected Map<String, Object> reloadMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>(map);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.contains(DOT)) {
                int idx = key.lastIndexOf(DOT);
                String suffix = key.substring(idx + 1);
                if (VALUE.equalsIgnoreCase(suffix)) {
                    result.put(key.substring(0, idx), entry.getValue());
                }
            }
        }
        return result;
    }

    public static String getTips(String fileName) {
        return String.format(
            "[%s] must contains file extension with properties|yaml|yml|xml|json",
            fileName);
    }

}
