package cn.nihility.local.mq.config;

import cn.nihility.local.mq.aop.ProxyMessageSendMethodInterceptor;
import cn.nihility.local.mq.service.IProxyMessageSendService;
import cn.nihility.local.mq.service.impl.DisruptorMessageSendServiceImpl;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.core.ClassLoaderAwareGeneratorStrategy;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * mq 剥离代理消息发送的后置代理处理器
 *
 * @author yuanzx
 * @date 2022/09/27 11:54
 */
public class LocalMessagePostProcessor implements BeanPostProcessor, Ordered {

    private final Map<Object, Boolean> proxyBeans = new ConcurrentHashMap<>(32);

    private MessageConfigurationProperties properties;

    private List<IProxyMessageSendService> messageSendServiceList;

    public LocalMessagePostProcessor(MessageConfigurationProperties properties,
                                     List<IProxyMessageSendService> messageSendServiceList) {
        this.properties = properties;
        this.messageSendServiceList = messageSendServiceList;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return wrapIfNecessary(bean, beanName);
    }

    /**
     * 若是配置了该 bean 的代理配置，则创建代理
     */
    private Object wrapIfNecessary(Object bean, String beanName) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (!Boolean.TRUE.equals(proxyBeans.get(targetClass))) {
            return createProxy(targetClass, beanName, bean);
        }
        return bean;
    }

    /**
     * 创建代理
     *
     * @param targetClass  目标 class
     * @param beanName     bean 名称
     * @param beanInstance spring 容器中 bean 实例
     * @return 代理后的实例对象
     */
    private Object createProxy(Class<?> targetClass, String beanName, Object beanInstance) {

        List<MessageSendProperties> senderConfigList = filterSenderProxyConfig(targetClass, beanName);
        // 定义的发送代理配置不存在，直接返回原始对象
        if (senderConfigList.isEmpty()) {
            return beanInstance;
        }

        IProxyMessageSendService messageSendService = messageSendServiceList.stream()
                .filter(m -> m.support(properties.getType())).findFirst().orElse(new DisruptorMessageSendServiceImpl());
        // 代理消息发送的拦截器
        ProxyMessageSendMethodInterceptor proxySendInterceptor =
                new ProxyMessageSendMethodInterceptor(senderConfigList, properties.getReceivers(), beanInstance, messageSendService);

        Class<?>[] interfaces = targetClass.getInterfaces();
        Class<?>[] types = new Class<?>[]{ProxyMessageSendMethodInterceptor.class};
        Callback[] callback = new Callback[]{proxySendInterceptor};
        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

        // 创建 cglib 代理增强类
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setInterfaces(interfaces);
        enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
        enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(classLoader));

        enhancer.setCallbacks(callback);
        enhancer.setCallbackTypes(types);

        enhancer.setInterceptDuringConstruction(false);

        Object proxy = enhancer.create();

        proxyBeans.put(targetClass, Boolean.TRUE);

        return proxy;

    }

    /**
     * 过滤出该类所有的配置项
     */
    private List<MessageSendProperties> filterSenderProxyConfig(Class<?> targetClass, String beanName) {
        List<MessageSendProperties> sendConfigList = properties.getSenders();
        if (null == sendConfigList) {
            return Collections.emptyList();
        }
        return sendConfigList.stream().filter(p -> support(p, targetClass, beanName)).collect(Collectors.toList());
    }

    private boolean support(MessageSendProperties properties, Class<?> targetClass, String beanName) {
        if (targetClass.equals(properties.getSendClass())) {
            return true;
        }
        return StringUtils.hasLength(beanName) && beanName.equals(properties.getSendBeanName());
    }

}
