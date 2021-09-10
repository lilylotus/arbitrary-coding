package cn.nihility.rpc.client;

import cn.nihility.rpc.client.proxy.ObjectProxy;
import cn.nihility.rpc.common.Beat;
import cn.nihility.rpc.common.conn.ConnectionManager;
import cn.nihility.rpc.common.util.ThreadPoolUtil;
import cn.nihility.rpc.service.Arithmetic;
import cn.nihility.rpc.service.IArithmetic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

public class RpcClient {

    private static final Logger log = LoggerFactory.getLogger(RpcClient.class);

    private RpcClient() {
    }

    public static void submit(Runnable runnable) {
        ThreadPoolUtil.clientSubmit(runnable);
    }

    public static void main(String[] args) throws InterruptedException {
        final RpcProtocol protocol = new RpcProtocol("10.0.41.80", 50090);
        final ConnectionManager client = ConnectionManager.getInstance();
        client.connectServerNode(protocol);

        final IArithmetic arithmetic = (IArithmetic) Proxy.newProxyInstance(RpcClient.class.getClassLoader(),
            Arithmetic.class.getInterfaces(),
            new ObjectProxy<>(Arithmetic.class, "v2.2.0"));

        System.out.println(arithmetic.add(20, 30));
        System.out.println(arithmetic.add(50, 30));
        System.out.println(arithmetic.add(2340, 30));

        Thread.sleep(Beat.BEAT_TIMEOUT * 1000L + 2000L);
        System.out.println("Sleep beat time");
        System.out.println(arithmetic.add(666, 30));

        System.out.println(arithmetic.division(30, 0));

        client.stop();

       /* try {
            final RpcClientHandler handler = client.chooseHandler(protocol);
            RpcRequest request = new RpcRequest();
            request.setVersion("v1.1.0");
            request.setRequestId(UUID.randomUUID().toString());
            request.setClassName(Arithmetic.class.getName());
            request.setMethodName("add");
            request.setParameters(new Object[]{20, 30});
            request.setParameterTypes(new Class[]{Integer.class, Integer.class});
            RpcFuture rpcFuture = handler.sendRequest(request);
            final Object o = rpcFuture.get();

            System.out.println(o);

            rpcFuture = handler.sendRequest(request);
            System.out.println(rpcFuture.get());

            rpcFuture = handler.sendRequest(request);
            System.out.println(rpcFuture.get(3, TimeUnit.SECONDS));

            Thread.sleep(Beat.BEAT_TIMEOUT * 1000L + 2000L);
            System.out.println("Sleep beat time");
            rpcFuture = handler.sendRequest(request);
            System.out.println(rpcFuture.get());

        } catch (Exception e) {
            e.printStackTrace();
        }*/ /*finally {
            client.stop();
        }*/

    }

}
