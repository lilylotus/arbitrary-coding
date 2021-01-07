package cn.nihility.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aspect
@Component
public class LocalAspect {

    @Pointcut("execution(* cn.nihility.aspect.*.*(..))")
    public void addAroundOperation() {
    }

    @Around("addAroundOperation()")
    public Object addAroundPointCut(ProceedingJoinPoint joinPoint) {
        System.out.println("addAroundPointCut begin");
        Object result = null;
        try {
            dealJoinPoint(joinPoint);
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        System.out.println("around result " + result);
        System.out.println("addAroundPointCut end");

        return result;
    }

    @AfterReturning(value = "addAroundOperation()", returning = "retVal")
    public void afterReturn(JoinPoint joinPoint, Object retVal) {
        System.out.println("afterReturn " + retVal);
    }

    private void dealJoinPoint(JoinPoint joinPoint) {
        System.out.println("method arguments");
        Object[] args = joinPoint.getArgs();
        if (args != null) {
            String argString = Stream.of(args).map(Object::toString).collect(Collectors.joining(" "));
            System.out.println(argString);
        }

        System.out.println("proxy object");
        Object thisObject = joinPoint.getThis();
        System.out.println(thisObject.getClass().getSimpleName());

        System.out.println("target object");
        Object targetObj = joinPoint.getTarget();
        System.out.println(targetObj.getClass().getSimpleName());

        System.out.println("description of the method that is being advised");
        Signature signature = joinPoint.getSignature();
        System.out.println("DeclaringType " + signature.getDeclaringType());
        System.out.println("DeclaringTypeName " + signature.getDeclaringTypeName());
        System.out.println("Name " + signature.getName());
        System.out.println("Modifiers " + signature.getModifiers() + ":" + Modifier.toString(signature.getModifiers()));
        System.out.println("Class " + signature.getClass());

        System.out.println("toString");
        System.out.println(joinPoint);


    }

}
