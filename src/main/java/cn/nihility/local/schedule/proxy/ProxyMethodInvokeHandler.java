package cn.nihility.local.schedule.proxy;

import cn.nihility.local.schedule.exception.ProxyInvokeException;
import cn.nihility.local.schedule.properties.QuartzScheduleJob;
import cn.nihility.local.schedule.util.ProxyInvokeUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author yuanzx
 * @date 2022/09/26 16:08
 */
public class ProxyMethodInvokeHandler implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ProxyMethodInvokeHandler.applicationContext = applicationContext;
    }

    /**
     * 代理方式调度到具体的业务逻辑
     *
     * @param job 定时任务配置信息
     * @return 业务操作返回的调度结果
     */
    public static Object proxyInvoke(QuartzScheduleJob job) {
        if (null == applicationContext) {
            throw new ProxyInvokeException("请先实例化 [" + ProxyMethodInvokeHandler.class.getName() + "] ApplicationContext 不可为空");
        }

        Class<?> serviceClass = job.getServiceClass();
        String beanName = job.getBeanName();
        String methodName = job.getMethodName();
        Class<?>[] parameterTypes = job.getParameterTypes();
        String[] param = job.getParam();

        Object beanInstance = ProxyInvokeUtils.getIocBeanInstance(applicationContext, serviceClass, beanName);
        if (null == beanInstance) {
            throw new ProxyInvokeException("spring 容器中 [" + Objects.toString(serviceClass, beanName) + "] 实例为空");
        }
        Method invokeMethod = ProxyInvokeUtils.getInvokeMethod(beanInstance.getClass(), methodName, parameterTypes);
        if (null == invokeMethod) {
            throw new ProxyInvokeException("调用 [" + beanInstance.getClass().getName() + "#" + methodName + "] 方法不存在");
        }
        if (invokeMethod.getParameterTypes().length != param.length) {
            throw new ProxyInvokeException("调用方法 [" + beanInstance.getClass().getName()
                    + "#" + methodName + "] 所需参数和传入的参数长度不一致");
        }

        return ProxyInvokeUtils.invoke(beanInstance, invokeMethod, param);

    }

}
