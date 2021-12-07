package cn.nihility.netty4;


import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyServerHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    private final String url;

    public NettyServerHandler(String url) {
        this.url = url;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }


}
