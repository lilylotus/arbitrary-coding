package cn.nihility.http.constant;

import org.apache.http.Consts;

/**
 * HTTP Header
 */
public class Header {

    public static final String APP_FORM_URLENCODED="application/x-www-form-urlencoded";
    public static final String TEXT_PLAIN="text/plain";
    public static final String TEXT_HTML="text/html";
    public static final String TEXT_XML="text/xml";
    public static final String TEXT_JSON="text/json";
    public static final String CONTENT_CHARSET_ISO_8859_1 = Consts.ISO_8859_1.name();
    public static final String CONTENT_CHARSET_UTF8 = Consts.UTF_8.name();
    public static final String DEF_PROTOCOL_CHARSET = Consts.ASCII.name();
    public static final String CONN_CLOSE = "close";
    public static final String KEEP_ALIVE = "keep-alive";
    public static final String EXPECT_CONTINUE = "100-continue";

}
