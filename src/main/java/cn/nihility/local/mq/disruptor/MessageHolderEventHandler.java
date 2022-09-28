package cn.nihility.local.mq.disruptor;

import cn.nihility.local.mq.config.MessageReceiveProperties;
import cn.nihility.local.mq.dto.LocalMessageHolder;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yuanzx
 * @date 2022/09/23 11:41
 */
@Slf4j
public class MessageHolderEventHandler implements EventHandler<LocalMessageHolder> {

    private MessageReceiveProperties receiveConfig;

    public MessageHolderEventHandler(MessageReceiveProperties receiveConfig) {
        this.receiveConfig = receiveConfig;
    }

    @Override
    public void onEvent(LocalMessageHolder event, long sequence, boolean endOfBatch) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Disruptor 消息处理 [{}] sequence [{}]", event.getRoutingKey(), sequence);
        }
        try {
            DisruptorHandlerProxyInvoke.invokeProxyMethod(receiveConfig, event);
        } catch (Exception ex) {
            // NO-OP
            log.error("Disruptor 消息处理异常", ex);
        }
    }

}
