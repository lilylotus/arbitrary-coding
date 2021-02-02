package cn.nihility.restart.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class PropertyUtil {

    private static final Logger log = LoggerFactory.getLogger(PropertyUtil.class);

    public static String getProperty(String key, String defaultValue, String text) {
        try {
            String propVal = System.getProperty(key);
            if (propVal == null) {
                // Fall back to searching the system environment.
                propVal = System.getenv(key);
            }
            if (propVal == null) {
                // Try with underscores.
                String name = key.replace('.', '_');
                propVal = System.getenv(name);
            }
            if (propVal == null) {
                // Try uppercase with underscores as well.
                String name = key.toUpperCase(Locale.ENGLISH).replace('.', '_');
                propVal = System.getenv(name);
            }
            log.debug("resolve key [{}] value [{}]", key, propVal);
            if (propVal != null) {
                return propVal;
            }
        } catch (Throwable ex) {
            log.error("Could not resolve key [{}] in [{}] as system property or in environment: ", key, text, ex);
        }
        return defaultValue;
    }

}
