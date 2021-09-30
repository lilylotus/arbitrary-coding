package cn.nihility.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * <pre>
 *  获取ip工具类
 * </pre>
 */
public class IpUtil {

    private static final String IP_ADDRESS_LOCALHOST = "127.0.0.1";

    private static final String UNKNOWN = "unknown";

    /**
     * HttpServletRequest获取Ip地址
     *
     * @param request 请求信息
     * @return ip地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String flag = ",";
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.contains(flag)) {
                ip = ip.split(flag)[0];
            }
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 将ip地址转换成long类型的数字,仅适用于ipv4
     * 例如: 192.168.0.1 to 3232235521
     *
     * @param ipStr ip字符串
     * @return long类型数据
     */
    public static long ipStr2Long(String ipStr) {
        if (StringUtils.isBlank(ipStr)) {
            throw new IllegalArgumentException("ipStr must not be null or blank");
        }
        String[] ipSubArray = ipStr.split("\\.");
        return (Long.parseLong(ipSubArray[0]) << 24) + (Long.parseLong(ipSubArray[1]) << 16)
            + (Long.parseLong(ipSubArray[2]) << 8) + (Long.parseLong(ipSubArray[0]));
    }

    /**
     * 将由字符串ip转换的数字还原成对应ip字符串
     *
     * @param number 数字
     * @return ip
     */
    public static String long2IpStr(long number) {
        return (number >>> 24) + "." +
            ((number >>> 16) & 0xFF) + "." +
            (number >>> 8 & 0xFF) + "." +
            (number & 0xFF);
    }

    /**
     * <p>ip使用long表示.</p>
     * <p>
     * ip有4段, 每段最大值为255, 即 2^8 - 1, 刚好是一个字节能表示的最大值,
     * 所以4个字节的int刚好能用来表示一个ip地址.
     * </p>
     *
     * @param ip IPV4 的点分十进制 IP 地址
     */
    public static long ipToLong(String ip) {
        String[] ips = ip.split("\\.");
        long ipFour = 0;
        //因为每个位置最大255，刚好在2进制里表示8位
        for (String ip4 : ips) {
            int ip4a = Integer.parseInt(ip4);
            //这里应该用+也可以,但是位运算更快
            ipFour = (ipFour << 8) | ip4a;
        }

        return ipFour;
    }

    /**
     * 使用long表示的ip地址, 转换成字符串的ip
     *
     * @param ip 使用int表示的ip地址
     */
    public static String longToIp(long ip) {
        //思路很简单，每8位拿一次，就是对应位的IP
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i >= 0; i--) {
            int ipa = (int) ((ip >> (8 * i)) & (0xff));
            sb.append(".").append(ipa);
        }

        return sb.substring(1);
    }

}
