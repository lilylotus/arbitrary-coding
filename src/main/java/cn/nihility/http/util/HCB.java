package cn.nihility.http.util;

import cn.nihility.http.constant.SSLProtocolVersion;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * HTTP Client 构建工具类
 */
public class HCB extends HttpClientBuilder {

    /* 记录是否设置了连接池 */
    private volatile boolean isSetPool = false;
    /* ssl 协议版本 */
    private SSLProtocolVersion sslpv = SSLProtocolVersion.SSLv3;

    /* 用于配置 ssl */
    /*private SSLs ssls = SSLs.getInstance();*/

    private HCB(){}
    public static HCB custom(){
        return new HCB();
    }

}
