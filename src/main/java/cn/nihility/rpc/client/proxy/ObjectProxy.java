package cn.nihility.rpc.client.proxy;

import cn.nihility.rpc.client.RpcClientHandler;
import cn.nihility.rpc.client.RpcFuture;
import cn.nihility.rpc.common.codec.RpcRequest;
import cn.nihility.rpc.client.conn.ConnectionManager;
import cn.nihility.rpc.common.util.RpcServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

public class ObjectProxy<T, P> implements InvocationHandler, RpcService<T, P, SerializableFunction<T>> {

    private static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);

    private final Class<T> clazz;
    private final String version;

    public ObjectProxy(Class<T> clazz, String version) {
        this.clazz = clazz;
        this.version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(clazz.getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setVersion(version);
        // Debug
        if (logger.isDebugEnabled()) {
            logger.debug(method.getDeclaringClass().getName());
            logger.debug(method.getName());
            for (int i = 0; i < method.getParameterTypes().length; ++i) {
                logger.debug(method.getParameterTypes()[i].getName());
            }
            for (Object arg : args) {
                logger.debug(Objects.toString(arg));
            }
        }

        String serviceKey = RpcServiceUtil.makeServiceKey(method.getDeclaringClass().getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcFuture rpcFuture = handler.sendRequest(request);
        final Object result = rpcFuture.get();
        logger.info("Proxy result [{}]", result);
        return result;
    }

    @Override
    public RpcFuture call(String funcName, Object... args) throws Exception {
        String serviceKey = RpcServiceUtil.makeServiceKey(this.clazz.getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcRequest request = createRequest(this.clazz.getName(), funcName, args);
        return handler.sendRequest(request);
    }

    @Override
    public RpcFuture call(SerializableFunction<T> tSerializableFunction, Object... args) throws Exception {
        String serviceKey = RpcServiceUtil.makeServiceKey(this.clazz.getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcRequest request = createRequest(this.clazz.getName(), tSerializableFunction.getName(), args);
        return handler.sendRequest(request);
    }

    private RpcRequest createRequest(String className, String methodName, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setVersion(version);
        Class[] parameterTypes = new Class[args.length];
        // Get the right class type
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(parameterTypes);

        // Debug
        if (logger.isDebugEnabled()) {
            logger.debug(className);
            logger.debug(methodName);
            for (Class<?> parameterType : parameterTypes) {
                logger.debug(parameterType.getName());
            }
            for (Object arg : args) {
                logger.debug(Objects.toString(arg));
            }
        }

        return request;
    }

    private Class<?> getClassType(Object obj) {
        Class<?> classType = obj.getClass();
//        String typeName = classType.getName();
//        switch (typeName) {
//            case "java.lang.Integer":
//                return Integer.TYPE;
//            case "java.lang.Long":
//                return Long.TYPE;
//            case "java.lang.Float":
//                return Float.TYPE;
//            case "java.lang.Double":
//                return Double.TYPE;
//            case "java.lang.Character":
//                return Character.TYPE;
//            case "java.lang.Boolean":
//                return Boolean.TYPE;
//            case "java.lang.Short":
//                return Short.TYPE;
//            case "java.lang.Byte":
//                return Byte.TYPE;
//        }
        return classType;
    }

}
