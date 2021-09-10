package cn.nihility.rpc.client.route;


import cn.nihility.rpc.client.RpcClientHandler;
import cn.nihility.rpc.client.RpcProtocol;
import cn.nihility.rpc.exception.RpcRouteException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RpcLoadBalanceLFU implements RpcLoadBalance {

    private ConcurrentMap<String, HashMap<RpcProtocol, Integer>> jobLfuMap = new ConcurrentHashMap<>();
    private long cacheValidTime = 0;

    public RpcProtocol doRoute(String serviceKey, List<RpcProtocol> addressList) {
        // cache clear
        if (System.currentTimeMillis() > cacheValidTime) {
            jobLfuMap.clear();
            cacheValidTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }

        // lfu item init
        HashMap<RpcProtocol, Integer> lfuItemMap = jobLfuMap.get(serviceKey);
        if (lfuItemMap == null) {
            lfuItemMap = new HashMap<>();
            jobLfuMap.putIfAbsent(serviceKey, lfuItemMap);   // 避免重复覆盖
        }

        // put new
        for (RpcProtocol address : addressList) {
            if (!lfuItemMap.containsKey(address) || lfuItemMap.get(address) > 1000000) {
                lfuItemMap.put(address, 0);
            }
        }

        // remove old
        List<RpcProtocol> delKeys = new ArrayList<>();
        for (RpcProtocol existKey : lfuItemMap.keySet()) {
            if (!addressList.contains(existKey)) {
                delKeys.add(existKey);
            }
        }
        for (RpcProtocol delKey : delKeys) {
            lfuItemMap.remove(delKey);
        }

        // load least used count address
        List<Map.Entry<RpcProtocol, Integer>> lfuItemList = new ArrayList<>(lfuItemMap.entrySet());
        lfuItemList.sort(Map.Entry.comparingByValue());

        Map.Entry<RpcProtocol, Integer> addressItem = lfuItemList.get(0);
        RpcProtocol minAddress = addressItem.getKey();
        addressItem.setValue(addressItem.getValue() + 1);

        return minAddress;
    }

    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws RpcRouteException {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if (addressList != null && !addressList.isEmpty()) {
            return doRoute(serviceKey, addressList);
        } else {
            throw new RpcRouteException("Can not find connection for service: " + serviceKey);
        }
    }

}
