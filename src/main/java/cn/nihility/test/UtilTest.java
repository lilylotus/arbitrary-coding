package cn.nihility.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class UtilTest {

    public static void main(String[] args) throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        System.out.println(localHost.getHostAddress());
    }

}
