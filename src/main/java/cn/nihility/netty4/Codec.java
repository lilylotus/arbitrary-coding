package cn.nihility.netty4;

public interface Codec {

    byte[] encode(Object message);

    <T> T decode(byte[] message, Class<T> clazz);

}
