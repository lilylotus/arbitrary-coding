package cn.nihility.local.mq.disruptor;

import cn.nihility.local.mq.config.MessageReceiveProperties;
import cn.nihility.local.mq.dto.LocalMessageHolder;
import cn.nihility.local.mq.exception.DisruptorInvokeException;
import cn.nihility.local.schedule.exception.ProxyInvokeException;
import cn.nihility.local.schedule.util.ProxyInvokeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * @author yuanzx
 * @date 2022/09/23 13:07
 */
@Service
@Slf4j
public class DisruptorHandlerProxyInvoke implements ApplicationContextAware {

    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    /**
     * 代理调用正真的消费实例对象方法消费 mq 消息
     *
     * @param receiveConfig 当前 mq 的配置
     * @param message       当前 mq 的消费信息数据
     * @return 调用后返回的结果
     */
    public static Object invokeProxyMethod(MessageReceiveProperties receiveConfig, LocalMessageHolder message) {
        final Class<?> recClass = receiveConfig.getRecClass();
        final String beanName = receiveConfig.getRecBeanName();
        final String methodName = receiveConfig.getRecMethodName();

        Object instance = ProxyInvokeUtils.getIocBeanInstance(ctx, recClass, beanName);
        if (instance == null) {
            log.error("Disruptor 消息队列调用真实消费对象实例 [{}:{}]", beanName, recClass);
            throw new DisruptorInvokeException("调用真实消费对象实例不存在 [" + recClass + "]");
        }

        Method method = ProxyInvokeUtils.getInvokeMethod(recClass, methodName, receiveConfig.getRecParameterTypes());
        if (null == method) {
            log.error("调用真实消费对象方法 [{}] 不存在", methodName);
            throw new DisruptorInvokeException("调用真实消费对象 [" +
                    recClass.getName() + "] 方法不存在 [" + methodName + "]");
        }

        String[] recArgs = receiveConfig.getRecArgs();
        if (ProxyInvokeUtils.invokeMethodArgLengthMismatch(method.getParameterCount(), recArgs)) {
            throw new ProxyInvokeException("调用方法 [" + recClass.getName()
                    + "#" + methodName + "] 所需参数和传入的参数长度不一致");
        }

        try {
            Object[] args = ProxyInvokeUtils.buildInvokeArgs(recArgs, message);
            return method.invoke(instance, args);
        } catch (Exception e) {
            log.error("[{}} 调用真实的消费方法 [{}#{}] 异常", receiveConfig.getRoutingKey(),
                    recClass.getName(), methodName);
            throw new DisruptorInvokeException("调用真实的消费方法[" +
                    recClass.getName() + "#" + methodName + "] 异常", e);
        }

    }

}
