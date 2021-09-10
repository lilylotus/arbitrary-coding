package cn.nihility.rpc.common.codec;

import cn.nihility.rpc.common.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(RpcDecoder.class);
    private final Class<?> genericClazz;
    private final Serializer serializer;

    public RpcDecoder(Class<?> genericClazz, Serializer serializer) {
        this.genericClazz = genericClazz;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }

        in.markReaderIndex();
        final int dataLength = in.readInt();

        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        final byte[] data = new byte[dataLength];
        in.readBytes(data);

        try {
            Object obj = serializer.deserialize(data, genericClazz);
            out.add(obj);
        } catch (Exception ex) {
            log.error("Decode byte data to object [{}] error", genericClazz.getName(), ex);
        }

    }

}
