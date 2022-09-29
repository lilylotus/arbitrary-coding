package cn.nihility.local.mq.test;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author intel
 * @date 2022/09/29 13:05
 */
//@Component
//@Aspect
@Slf4j
public class MqAspectComponent {

    @Pointcut("execution(* cn.nihility.local.mq.test.MqTestSenderService..*(..))")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
      log.info("around [{}]", joinPoint.getSignature().getName());
      return joinPoint.proceed();
    }

}


