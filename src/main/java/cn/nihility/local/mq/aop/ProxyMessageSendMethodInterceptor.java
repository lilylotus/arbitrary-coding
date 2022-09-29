package cn.nihility.local.mq.aop;

import cn.nihility.local.mq.config.MessageConfigurationProperties;
import cn.nihility.local.mq.config.MessageReceiveProperties;
import cn.nihility.local.mq.config.MessageSendProperties;
import cn.nihility.local.mq.dto.LocalMessageHolder;
import cn.nihility.local.mq.service.IProxyMessageSendService;
import cn.nihility.local.mq.service.impl.DisruptorMessageSendServiceImpl;
import cn.nihility.local.mq.util.MessageUtils;
import cn.nihility.local.schedule.util.ProxyInvokeUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 针对一个代理消息队列发送实现
 *
 * @author yuanzx
 * @date 2022/09/27 13:37
 */
public class ProxyMessageSendMethodInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ProxyMessageSendMethodInterceptor.class);

    private final AtomicBoolean refresh = new AtomicBoolean(true);

    /**
     * 代理发送的配置
     */
    private List<MessageSendProperties> senderConfigList;
    /**
     * 消息接收的配置
     */
    private List<MessageReceiveProperties> receiverConfigList;
    /**
     * 原始的 bean 实例
     */
    private Object originBeanInstance;
    /**
     * 代理消息发送的对象实体
     */
    private Object proxyBeanInstance;
    /**
     * 代理的方法集合
     */
    private Set<String> methodNameSet;
    /**
     * 消息发送的实现
     */
    private IProxyMessageSendService messageSendService;

    /**
     * Springs 上下文
     */
    private ApplicationContext applicationContext;
    /**
     * 代理的 class
     */
    private Class<?> proxyClass;
    /**
     * 代理对象在 spring 容器中的名称
     */
    private String beanName;
    /**
     * 消息发送的业务类
     */
    private List<IProxyMessageSendService> messageSendServiceList;

    public ProxyMessageSendMethodInterceptor(ApplicationContext applicationContext, Class<?> targetClass, String beanName,
                                             List<IProxyMessageSendService> messageSendServiceList,
                                             Object originBeanInstance) {
        this.applicationContext = applicationContext;
        this.proxyClass = targetClass;
        this.beanName = beanName;
        this.messageSendServiceList = messageSendServiceList;
        this.originBeanInstance = originBeanInstance;
    }

    /**
     * 容器更新，刷新内部变量属性
     */
    private synchronized void innerRefreshContext() {
        if (refresh.get()) {

            this.proxyBeanInstance = ProxyInvokeUtils.getIocBeanInstance(applicationContext, proxyClass, beanName);
            MessageConfigurationProperties configurationProperties =
                    (MessageConfigurationProperties) ProxyInvokeUtils.getIocBeanInstance(applicationContext,
                            MessageConfigurationProperties.class, Introspector.decapitalize(MessageConfigurationProperties.class.getSimpleName()));
            this.senderConfigList = MessageUtils.filterSenderProxyConfig(configurationProperties, proxyClass, beanName);
            this.receiverConfigList = configurationProperties.getReceivers();
            this.messageSendService = messageSendServiceList.stream()
                    .filter(m -> m.support(configurationProperties.getType()))
                    .findFirst().orElse(new DisruptorMessageSendServiceImpl());
            this.methodNameSet = collectMethodNameSet(senderConfigList);

            log.info("本地消息队列类型 [{}]", configurationProperties.getType());

            refresh.compareAndSet(true, false);
        }
    }

    /**
     * 设置属性实例上下文标记
     */
    public void refreshContext() {
        refresh.compareAndSet(false, true);
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
        if (ReflectionUtils.isObjectMethod(method)) {
            return method.invoke(proxyBeanInstance, args);
        }

        // 首先判断是否需要刷新
        innerRefreshContext();

        MessageSendProperties senderConfig = matchProxySendConfig(method);
        if (null == senderConfig) {
            return method.invoke(originBeanInstance, args);
        }
        MessageReceiveProperties receiverConfig = matchReceiveConfig(senderConfig.getRoutingKey());
        if (null == receiverConfig) {
            log.warn("本地消息发送路由 [{}] 没有定义接收配置，无法代理发送", senderConfig.getRoutingKey());
            return method.invoke(originBeanInstance, args);
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
