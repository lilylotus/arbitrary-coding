package cn.nihility.serialize;

import cn.nihility.exception.IllegalParseException;
import cn.nihility.util.JacksonUtil;

public class JacksonSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) throws IllegalParseException {
        return JacksonUtil.serialize(obj);
    }

    @Override
    public <T> T deserialize(byte[] src, Class<T> type) throws IllegalParseException {
        return JacksonUtil.deserialize(src, type);
    }

}
