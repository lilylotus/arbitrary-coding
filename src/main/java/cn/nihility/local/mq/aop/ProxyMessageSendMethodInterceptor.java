package cn.nihility.local.mq.aop;

import cn.nihility.local.mq.config.MessageReceiveProperties;
import cn.nihility.local.mq.config.MessageSendProperties;
import cn.nihility.local.mq.dto.LocalMessageHolder;
import cn.nihility.local.mq.service.IProxyMessageSendService;
import cn.nihility.local.schedule.util.ProxyInvokeUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 针对一个代理消息队列发送实现
 *
 * @author yuanzx
 * @date 2022/09/27 13:37
 */
public class ProxyMessageSendMethodInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ProxyMessageSendMethodInterceptor.class);

    /**
     * 代理发送的配置
     */
    private List<MessageSendProperties> senderConfigList;
    /**
     * 消息接收的配置
     */
    private List<MessageReceiveProperties> receiverConfigList;
    /**
     * 代理消息发送的对象实体
     */
    private Object beanInstance;
    /**
     * 代理的方法集合
     */
    private Set<String> methodNameSet;
    /**
     * 消息发送的实现
     */
    private IProxyMessageSendService messageSendService;

    public ProxyMessageSendMethodInterceptor(List<MessageSendProperties> senderConfigList,
                                             List<MessageReceiveProperties> receiverConfigList,
                                             Object beanInstance, IProxyMessageSendService messageSendService) {
        this.senderConfigList = senderConfigList;
        this.receiverConfigList = receiverConfigList;
        this.beanInstance = beanInstance;
        this.messageSendService = messageSendService;
        this.methodNameSet = collectMethodNameSet(senderConfigList);
    }

    /**
     * 搜集所有消息代理发送的方法名称
     */
    private Set<String> collectMethodNameSet(List<MessageSendProperties> properties) {
        return properties.stream()
                .map(MessageSendProperties::getSendMethod)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * 获取匹配到当前调用方法的代理消息队列发送配置
     */
    private MessageSendProperties matchProxySendConfig(Method method) {
        final String methodName = method.getName();
        final int parameterCount = method.getParameterCount();
        if (methodNameSet.contains(methodName)) {
            return senderConfigList.stream().filter(p ->
                    p.getSendMethod().equals(methodName)
                            && parameterCount == ProxyInvokeUtils.getArrayLength(p.getSendParameterTypes())
            ).findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        if (Object.class.equals(method.getDeclaringClass())) {
            return methodProxy.invoke(beanInstance, args);
        }

        MessageSendProperties senderConfig = matchProxySendConfig(method);
        if (null == senderConfig) {
            return methodProxy.invoke(beanInstance, args);
        }
        MessageReceiveProperties receiverConfig = matchReceiveConfig(senderConfig.getRoutingKey());
        if (null == receiverConfig) {
            log.warn("本地消息发送路由 [{}] 没有定义接收配置，无法代理发送", senderConfig.getRoutingKey());
            return methodProxy.invoke(beanInstance, args);
        }

        // 代理消息发送
        final int argLength = args.length;
        String[] sendArgs = senderConfig.getSendArgs();

        // 封装消息实体
        LocalMessageHolder message = new LocalMessageHolder();
        MetaObject meta = ProxyInvokeUtils.buildMetaObject(message);
        for (int index = 0; index < argLength; index++) {
            meta.setValue(sendArgs[index], args[index]);
        }

        messageSendService.send(message, senderConfig, receiverConfig);

        Class<?> returnType = method.getReturnType();
        return DefaultTypeHandler.mapping(returnType);

    }

    private MessageReceiveProperties matchReceiveConfig(String routingKey) {
        if (null == receiverConfigList) {
            return null;
        }
        return receiverConfigList.stream().filter(c -> c.getRoutingKey().equals(routingKey)).findFirst().orElse(null);
    }

}
