package cn.nihility.serialize;

import cn.nihility.exception.IllegalParseException;

public interface Serializer {

    byte[] serialize(Object obj) throws IllegalParseException;

    <T> T deserialize(byte[] src, Class<T> type) throws IllegalParseException;

}
