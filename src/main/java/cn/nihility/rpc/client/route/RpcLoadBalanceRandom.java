package cn.nihility.rpc.client.route;

import cn.nihility.rpc.client.RpcClientHandler;
import cn.nihility.rpc.client.RpcProtocol;
import cn.nihility.rpc.exception.RpcRouteException;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class RpcLoadBalanceRandom implements RpcLoadBalance {

    private Random random = new Random();

    public RpcProtocol doRoute(List<RpcProtocol> addressList) {
        int size = addressList.size();
        // Random
        return addressList.get(random.nextInt(size));
    }

    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws RpcRouteException {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if (addressList != null && !addressList.isEmpty()) {
            return doRoute(addressList);
        } else {
            throw new RpcRouteException("Can not find connection for service: " + serviceKey);
        }
    }

}
