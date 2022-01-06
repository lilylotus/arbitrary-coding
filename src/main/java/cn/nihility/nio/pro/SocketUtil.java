package cn.nihility.nio.pro;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class SocketUtil {

    private SocketUtil() {
    }

    /**
     * 将 byte[] 转换为 int 类型
     */
    public static int byte2int(byte[] res) {
        // 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000
        return (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位或
            | ((res[2] << 24) >>> 8) | (res[3] << 24);
    }

    /**
     * 将 int 类型装换为 byte[] 数组
     */
    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }

    public static void sendData(SocketChannel channel, ByteBuffer byteBuffer, String data) throws IOException {
        byte[] sendBytes = data.getBytes(StandardCharsets.UTF_8);
        byteBuffer.clear();
        byteBuffer.put(int2byte(sendBytes.length));
        byteBuffer.put(sendBytes);
        byteBuffer.flip();
        channel.write(byteBuffer);
    }

    public static void sendData(HighSocketServer.SocketContainer socketContainer, String data) throws IOException {
        sendData(socketContainer.getSocketChannel(), socketContainer.getByteBuffer(), data);
    }

}
