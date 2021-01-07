package cn.nihility.loader;

import org.springframework.boot.loader.PropertiesLauncher;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;

public class LoaderTest {

    /**
     * Prefix for system property placeholders: "${".
     */
    public static final String PLACEHOLDER_PREFIX = "${";

    /**
     * Suffix for system property placeholders: "}".
     */
    public static final String PLACEHOLDER_SUFFIX = "}";

    private static final String SIMPLE_PREFIX = PLACEHOLDER_PREFIX.substring(1);


    public static void main(String[] args) {
        String current = "Hello ${user.dir} Dir";
        StringBuilder buf = new StringBuilder(current);

        int startIndex = current.indexOf(PLACEHOLDER_PREFIX);
        System.out.println(startIndex);
        while (startIndex != -1) {

            int endIndex = findPlaceholderEndIndex(buf, startIndex);
            System.out.println(endIndex);

            if (endIndex != -1) {
                String subString = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                System.out.println(subString);

                startIndex = -1;

            } else {
                startIndex = -1;
            }
        }
    }

    private static int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + PLACEHOLDER_PREFIX.length();
        int withinNestedPlaceholder = 0;

        while (index < buf.length()) {
            if (subStringMatch(buf, index, PLACEHOLDER_SUFFIX)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--;
                    index = index + PLACEHOLDER_SUFFIX.length();
                } else {
                    return index;
                }
            } else if (subStringMatch(buf, index, SIMPLE_PREFIX)) {
                withinNestedPlaceholder++;
                index = index + SIMPLE_PREFIX.length();
            } else {
                index++;
            }
        }
        return -1;
    }

    private static boolean subStringMatch(CharSequence buf, int index, String subString) {
        for (int j = 0; j < subString.length(); j++) {
            int startIndex = index + j;
            if (startIndex >= buf.length() || buf.charAt(startIndex) != subString.charAt(j)) {
                return false;
            }
        }
        return true;
    }


    public static void main1(String[] args) throws URISyntaxException, MalformedURLException {

        System.out.println(PropertiesLauncher.toCamelCase("camel-case"));

        ProtectionDomain protectionDomain = LoaderTest.class.getProtectionDomain();
        System.out.println(protectionDomain.getClassLoader());
        System.out.println(protectionDomain.getCodeSource().getLocation());
        System.out.println(protectionDomain.getCodeSource().getLocation().toURI());

        URI uri = protectionDomain.getCodeSource().getLocation().toURI();

        System.out.println(uri.getScheme());
        System.out.println(uri.getSchemeSpecificPart());
        System.out.println(uri.getPath());

        System.out.println(uri.toURL());

        System.out.println(System.getProperty("user.dir"));

    }

}
