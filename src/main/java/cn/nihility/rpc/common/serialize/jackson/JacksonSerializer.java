package cn.nihility.rpc.common.serialize.jackson;


import cn.nihility.rpc.common.serialize.Serializer;
import cn.nihility.util.JacksonUtil;

public final class JacksonSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T obj) {
        return JacksonUtil.serialize(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return JacksonUtil.deserialize(bytes, clazz);
    }

}
