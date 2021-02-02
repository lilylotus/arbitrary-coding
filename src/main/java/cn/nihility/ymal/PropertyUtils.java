package cn.nihility.ymal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

final public class PropertyUtils {

    /**
     * Prefix for system property placeholders: "${".
     */
    public static final String PLACEHOLDER_PREFIX = "${";

    /**
     * Suffix for system property placeholders: "}".
     */
    public static final String PLACEHOLDER_SUFFIX = "}";

    /**
     * Value separator for system property placeholders: ":".
     */
    public static final String VALUE_SEPARATOR = ":";

    private static final String SIMPLE_PREFIX = PLACEHOLDER_PREFIX.substring(1);

    private static final Logger log = LoggerFactory.getLogger(PropertyUtils.class);

    public static Properties readProperties(File proFile) {
        try (final FileInputStream inputStream = new FileInputStream(proFile)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            log.error("加载 properties 文件 [{}] 失败", proFile.getAbsolutePath(), e);
        }
        return null;
    }

    /**
     * Resolve ${...} placeholders in the given text, replacing them with corresponding
     * system property values.
     * @param properties a properties instance to use in addition to System
     * @param text the String to resolve
     * @return the resolved String
     * @throws IllegalArgumentException if there is an unresolvable placeholder
     * @see #PLACEHOLDER_PREFIX
     * @see #PLACEHOLDER_SUFFIX
     */
    public static String resolvePlaceholders(Map<String, Object> properties, String text) {
        if (text == null) {
            return text;
        }
        return parseStringValue(properties, text, text, new HashSet<>());
    }

    private static String parseStringValue(Map<String, Object> properties, String value, String current, Set<String> visitedPlaceholders) {

        StringBuilder buf = new StringBuilder(current);

        int startIndex = current.indexOf(PLACEHOLDER_PREFIX);
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(buf, startIndex);
            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                String originalPlaceholder = placeholder;
                if (!visitedPlaceholders.add(originalPlaceholder)) {
                    throw new IllegalArgumentException("Circular placeholder reference '"
                            + originalPlaceholder + "' in property definitions");
                }
                // Recursive invocation, parsing placeholders contained in the
                // placeholder
                // key.
                placeholder = parseStringValue(properties, value, placeholder, visitedPlaceholders);
                // Now obtain the value for the fully resolved key...
                String propVal = resolvePlaceholder(properties, value, placeholder);
                if (propVal == null) {
                    int separatorIndex = placeholder.indexOf(VALUE_SEPARATOR);
                    if (separatorIndex != -1) {
                        String actualPlaceholder = placeholder.substring(0, separatorIndex);
                        String defaultValue = placeholder.substring(separatorIndex + VALUE_SEPARATOR.length());
                        propVal = resolvePlaceholder(properties, value, actualPlaceholder);
                        if (propVal == null) {
                            propVal = defaultValue;
                        }
                    }
                }
                if (propVal != null) {
                    // Recursive invocation, parsing placeholders contained in the
                    // previously resolved placeholder value.
                    propVal = parseStringValue(properties, value, propVal, visitedPlaceholders);
                    buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal);
                    startIndex = buf.indexOf(PLACEHOLDER_PREFIX, startIndex + propVal.length());
                }
                else {
                    // Proceed with unprocessed value.
                    startIndex = buf.indexOf(PLACEHOLDER_PREFIX, endIndex + PLACEHOLDER_SUFFIX.length());
                }
                visitedPlaceholders.remove(originalPlaceholder);
            }
            else {
                startIndex = -1;
            }
        }

        return buf.toString();
    }

    private static String resolvePlaceholder(Map<String, Object> properties, String text, String placeholderName) {
        return getProperty(properties, placeholderName, null, text);
    }

    /**
     * Search the System properties and environment variables for a value with the
     * provided key. Environment variables in {@code UPPER_CASE} style are allowed where
     * System properties would normally be {@code lower.case}.
     * @param key the key to resolve
     * @param defaultValue the default value
     * @param text optional extra context for an error message if the key resolution fails
     * (e.g. if System properties are not accessible)
     * @return a static property value or null of not found
     */
    public static String getProperty(Map<String, Object> properties, String key, String defaultValue, String text) {
        Object propVal = properties.get(key);
        if (propVal == null) {
            // Try with underscores.
            String name = key.replace('.', '_');
            propVal = properties.get(name);
        }
        if (propVal == null) {
            // Try uppercase with underscores as well.
            String name = key.toUpperCase(Locale.ENGLISH).replace('.', '_');
            propVal = properties.get(name);
        }
        if (propVal != null) {
            return String.valueOf(propVal);
        }

        //System.out.println("[" + key + "] 不存，使用默认值 [" + defaultValue + "]");
        return defaultValue;
    }

    private static int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + PLACEHOLDER_PREFIX.length();
        int withinNestedPlaceholder = 0;
        while (index < buf.length()) {
            if (substringMatch(buf, index, PLACEHOLDER_SUFFIX)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--;
                    index = index + PLACEHOLDER_SUFFIX.length();
                }
                else {
                    return index;
                }
            }
            else if (substringMatch(buf, index, SIMPLE_PREFIX)) {
                withinNestedPlaceholder++;
                index = index + SIMPLE_PREFIX.length();
            }
            else {
                index++;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        String holder = "xxx ${a.b.${e.f.g}.d} end";
        final int startIndex = holder.indexOf(PLACEHOLDER_PREFIX);
        final int endIndex = findPlaceholderEndIndex(holder, startIndex);

        String placeholder = holder.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);

        System.out.println(placeholder);
    }

    private static boolean substringMatch(CharSequence str, int index,
                                          CharSequence substring) {
        for (int j = 0; j < substring.length(); j++) {
            int i = index + j;
            if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
                return false;
            }
        }
        return true;
    }

}
