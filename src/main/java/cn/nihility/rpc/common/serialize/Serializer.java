package cn.nihility.rpc.common.serialize;

public interface Serializer {

    <T> byte[] serialize(T obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);

}
