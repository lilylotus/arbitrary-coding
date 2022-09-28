package cn.nihility.local.mq.disruptor;

import cn.nihility.local.mq.dto.LocalMessageHolder;
import com.lmax.disruptor.EventFactory;

/**
 * @author yuanzx
 * @date 2022/09/22 18:08
 */
public class MessageHolderEventFactory implements EventFactory<LocalMessageHolder> {

    @Override
    public LocalMessageHolder newInstance() {
        return new LocalMessageHolder();
    }

}
