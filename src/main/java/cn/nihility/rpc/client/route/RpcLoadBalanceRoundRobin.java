package cn.nihility.rpc.client.route;

import cn.nihility.rpc.client.RpcClientHandler;
import cn.nihility.rpc.client.RpcProtocol;
import cn.nihility.rpc.exception.RpcRouteException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author intel
 * @date 2021/09/10 10:28
 */
public class RpcLoadBalanceRoundRobin implements RpcLoadBalance {

    private AtomicInteger roundRobin = new AtomicInteger(0);

    public RpcProtocol doRoute(List<RpcProtocol> addressList) {
        int size = addressList.size();
        // Round robin
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return addressList.get(index);
    }

    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws RpcRouteException {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if (addressList != null && !addressList.isEmpty()) {
            return doRoute(addressList);
        } else {
            throw new RpcRouteException("Can not find connection for service : " + serviceKey);
        }
    }

}
