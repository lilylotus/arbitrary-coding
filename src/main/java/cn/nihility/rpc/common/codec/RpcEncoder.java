package cn.nihility.rpc.common.codec;


import cn.nihility.rpc.common.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcEncoder extends MessageToByteEncoder<Object> {

    private static final Logger log = LoggerFactory.getLogger(RpcEncoder.class);

    private final Class<?> genericClazz;
    private final Serializer serializer;

    public RpcEncoder(Class<?> genericClazz, Serializer serializer) {
        this.genericClazz = genericClazz;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf out) throws Exception {
        if (genericClazz.isInstance(obj)) {
            try {
                byte[] data = serializer.serialize(obj);
                out.writeInt(data.length);
                out.writeBytes(data);
            } catch (Exception ex) {
                log.error("Encode target object [{}] error", obj, ex);
            }
        }
    }

}
