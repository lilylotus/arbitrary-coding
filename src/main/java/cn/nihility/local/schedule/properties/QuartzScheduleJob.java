package cn.nihility.local.schedule.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Quartz Job 定义
 *
 * @author yunzx
 * @date 2022/09/26 14:14
 */
@Getter
@Setter
@ToString
public class QuartzScheduleJob implements Serializable {

    /**
     * 定时任务调用的服务类
     */
    private Class<?> serviceClass;

    /**
     * 定时任务调度是执行的任务 spring ioc 容器的名称
     */
    private String beanName;

    /**
     * 调度任务的方法名称
     */
    private String methodName;

    /**
     * 调度任务方法的参数类型
     */
    private Class<?>[] parameterTypes;

    /**
     * 调度任务的参数
     */
    private String[] param;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * 是否停用
     */
    private Boolean pause;

}
