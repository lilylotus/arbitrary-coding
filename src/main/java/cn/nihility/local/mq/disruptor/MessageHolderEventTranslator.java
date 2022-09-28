package cn.nihility.local.mq.disruptor;

import cn.nihility.local.mq.dto.LocalMessageHolder;
import com.lmax.disruptor.EventTranslatorOneArg;

/**
 * @author yuanzx
 * @date 2022/09/23 11:44
 */
public class MessageHolderEventTranslator implements EventTranslatorOneArg<LocalMessageHolder, LocalMessageHolder> {

    @Override
    public void translateTo(LocalMessageHolder event, long sequence, LocalMessageHolder message) {
        event.setSequence(sequence);
        event.setMessage(message.getMessage());
        event.setExchange(message.getExchange());
        event.setRoutingKey(message.getRoutingKey());
        event.setHeaders(message.getHeaders());
        event.setExtensions(message.getExtensions());
    }

}
