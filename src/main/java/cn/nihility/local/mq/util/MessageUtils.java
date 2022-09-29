package cn.nihility.local.mq.util;

import cn.nihility.local.mq.config.MessageConfigurationProperties;
import cn.nihility.local.mq.config.MessageSendProperties;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuanzx
 * @date 2022/09/29 10:29
 */
public class MessageUtils {

    private MessageUtils() {
    }

    /**
     * 过滤匹配改类的发送配置项
     */
    public static List<MessageSendProperties> filterSenderProxyConfig(MessageConfigurationProperties config,
                                                                      Class<?> targetClass, String beanName) {
        List<MessageSendProperties> sendConfigList = config.getSenders();
        if (null == sendConfigList) {
            return Collections.emptyList();
        }
        return sendConfigList.stream().filter(p -> support(p, targetClass, beanName)).collect(Collectors.toList());
    }

    public static boolean support(MessageSendProperties properties, Class<?> targetClass, String beanName) {
        if (targetClass.equals(properties.getSendClass())) {
            return true;
        }
        return StringUtils.hasLength(beanName) && beanName.equals(properties.getSendBeanName());
    }

}
