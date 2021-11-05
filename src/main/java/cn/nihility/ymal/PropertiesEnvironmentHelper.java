package cn.nihility.ymal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

public class PropertiesEnvironmentHelper {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesEnvironmentHelper.class);

    private final PropertySources propertySources;

    public PropertiesEnvironmentHelper(PropertySources propertySources) {
        this.propertySources = propertySources;
    }

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, Object> systemEnv() {
        return (Map) System.getenv();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, Object> systemProperties() {
        return (Map) System.getProperties();
    }


    public <T> T getProperty(final String key, Class<T> targetValueType, boolean resolveNestedPlaceholders) {

        if (null != propertySources) {
            for (PropertySource source : propertySources) {
                Object value = source.getProperty(key);
                if (null != value) {
                    if (resolveNestedPlaceholders && value instanceof String) {
                        value = resolveNestedPlaceholders((String) value);
                    }
                    logger.info("property [{}]:[{}] at [{}]", key, value, source.getName());
                    return convertValueIfNecessary(value, targetValueType);
                }
            }
        }

        logger.warn("Could not find key '{}' in any property source", key);

        return null;
    }

    private Object resolveNestedPlaceholders(String text) {
        final PropertyPlaceholderHelper strictHelper = createPlaceholderHelper(false);
        return doResolvePlaceholders(text, strictHelper);
    }

    private Object doResolvePlaceholders(String text, PropertyPlaceholderHelper helper) {
        return helper.replacePlaceholders(text, this::getPropertyAsRawString);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValueIfNecessary(Object value, Class<T> targetType) {
        if (targetType == null) {
            return (T) value;
        }
        if (ClassUtils.isAssignableValue(targetType, value)) {
            return (T) value;
        }
        ConversionService conversionService = DefaultConversionService.getSharedInstance();
        return conversionService.convert(value, targetType);
    }

    private PropertyPlaceholderHelper createPlaceholderHelper(boolean ignoreUnresolvablePlaceholders) {
        return new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX,
                PLACEHOLDER_SUFFIX, VALUE_SEPARATOR, ignoreUnresolvablePlaceholders);
    }

    private String getPropertyAsRawString(String key) {
        return getProperty(key, String.class, false);
    }


    public static void main(String[] args) {
        final Map<String, Object> systemEnv = systemEnv();
        final Map<String, Object> systemProperties = systemProperties();
        final Map<String, Object> s1 = new HashMap<>();
        final Map<String, Object> s2 = new HashMap<>();
        final Map<String, Object> s3 = new HashMap<>();

        final PropertySources sources = new PropertySources();
        sources.addLast(new PropertySource("systemEnv", systemEnv));
        sources.addLast(new PropertySource("systemProperties", systemProperties));
        sources.addLast(new PropertySource("s1", s1));
        sources.addLast(new PropertySource("s2", s2));
        sources.addLast(new PropertySource("s3", s3));

        s1.put("s1.a.b.c", "${s3.${s2.e.f}.x.y.z}");
        s2.put("s2.e.f", "ok");
        s3.put("s3.ok.x.y.z", "final success");
        s3.put("s3.ok.x.y", "${xx.yy:final success xx.yy}");

        final PropertiesEnvironmentHelper helper = new PropertiesEnvironmentHelper(sources);
        System.out.println(helper.getProperty("s1.a.b.c", String.class, true));
        System.out.println(helper.getProperty("s3.ok.x.y", String.class, true));

    }

}
