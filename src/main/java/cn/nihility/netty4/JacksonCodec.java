package cn.nihility.netty4;


import cn.nihility.util.JacksonUtil;

public class JacksonCodec implements Codec {

    @Override
    public byte[] encode(Object message) {
        return JacksonUtil.serialize(message);
    }

    @Override
    public <T> T decode(byte[] message, Class<T> clazz) {
        return JacksonUtil.deserialize(message, clazz);
    }

}
