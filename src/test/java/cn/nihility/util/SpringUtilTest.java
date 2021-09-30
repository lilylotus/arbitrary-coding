package cn.nihility.util;

import cn.nihility.util.spring.PropertyPlaceholderHelper;
import org.junit.jupiter.api.Test;
import org.springframework.util.SocketUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.SocketUtils.PORT_RANGE_MAX;
import static org.springframework.util.SocketUtils.PORT_RANGE_MIN;

class SpringUtilTest {

    @Test
    void testPropertyPlaceholderHelper() {
        final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");

        String text = "foo=${b${inner}}";
        Properties props = new Properties();
        props.setProperty("bar", "bar");
        props.setProperty("inner", "ar");

        assertThat(helper.replacePlaceholders(text, props)).isEqualTo("foo=bar");

        text = "${top}";
        props = new Properties();
        props.setProperty("top", "${child}+${child}");
        props.setProperty("child", "${${differentiator}.grandchild}");
        props.setProperty("differentiator", "first");
        props.setProperty("first.grandchild", "actualValue");

        assertThat(helper.replacePlaceholders(text, props)).isEqualTo("actualValue+actualValue");
    }

    @Test
    void testSocketUtils() {
        int port = SocketUtils.findAvailableTcpPort();
        assertPortInRange(port, PORT_RANGE_MIN, PORT_RANGE_MAX);
        System.out.println(port);
    }

    private void assertPortInRange(int port, int minPort, int maxPort) {
        assertThat(port >= minPort).as("port [" + port + "] >= " + minPort).isTrue();
        assertThat(port <= maxPort).as("port [" + port + "] <= " + maxPort).isTrue();
    }

}
