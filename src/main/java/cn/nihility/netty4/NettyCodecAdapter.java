package cn.nihility.netty4;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

public final class NettyCodecAdapter {

    private final Codec codec;

    private final InternalEncoder encoder = new InternalEncoder();
    private final InternalDecoder decoder = new InternalDecoder();

    public NettyCodecAdapter(Codec codec) {
        this.codec = codec;
    }

    public InternalEncoder getEncoder() {
        return encoder;
    }

    public InternalDecoder getDecoder() {
        return decoder;
    }

    private class InternalEncoder extends MessageToByteEncoder<Object> {

        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
            byte[] content = codec.encode(msg);
            out.writeInt(content.length);
            out.writeBytes(content);
        }

    }

    private class InternalDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {
            if (input.readableBytes() < 4) {
                return;
            }

            input.markReaderIndex();
            int len = input.readInt();

            if (input.readableBytes() < len) {
                input.resetReaderIndex();
                return;
            }

            byte[] buffer = new byte[len];
            input.readBytes(buffer);

            Object obj = codec.decode(buffer, Object.class);
            out.add(obj);

        }
    }

}
