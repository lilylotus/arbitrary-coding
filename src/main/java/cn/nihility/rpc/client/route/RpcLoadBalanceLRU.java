package cn.nihility.rpc.client.route;


import cn.nihility.rpc.client.RpcClientHandler;
import cn.nihility.rpc.client.RpcProtocol;
import cn.nihility.rpc.exception.RpcRouteException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RpcLoadBalanceLRU implements RpcLoadBalance {

    private ConcurrentMap<String, LinkedHashMap<RpcProtocol, RpcProtocol>> jobLRUMap = new ConcurrentHashMap<>();
    private long cacheValidTime = 0;

    public RpcProtocol doRoute(String serviceKey, List<RpcProtocol> addressList) {
        // cache clear
        if (System.currentTimeMillis() > cacheValidTime) {
            jobLRUMap.clear();
            cacheValidTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }

        // init lru
        LinkedHashMap<RpcProtocol, RpcProtocol> lruHashMap = jobLRUMap.get(serviceKey);
        if (lruHashMap == null) {
            /*
             * LinkedHashMap
             * a、accessOrder：ture=访问顺序排序（get/put时排序）/ACCESS-LAST；false=插入顺序排期/FIFO；
             * b、removeEldestEntry：新增元素时将会调用，返回true时会删除最老元素；
             *      可封装LinkedHashMap并重写该方法，比如定义最大容量，超出是返回true即可实现固定长度的LRU算法；
             */
            lruHashMap = new LinkedHashMap<RpcProtocol, RpcProtocol>(16, 0.75f, true) {
                private static final long serialVersionUID = -7972969841651566653L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<RpcProtocol, RpcProtocol> eldest) {
                    return super.size() > 1000;
                }
            };
            jobLRUMap.putIfAbsent(serviceKey, lruHashMap);
        }

        // put new
        for (RpcProtocol address : addressList) {
            lruHashMap.computeIfAbsent(address, key -> address);
        }
        // remove old
        List<RpcProtocol> delKeys = new ArrayList<>();
        for (RpcProtocol existKey : lruHashMap.keySet()) {
            if (!addressList.contains(existKey)) {
                delKeys.add(existKey);
            }
        }
        for (RpcProtocol delKey : delKeys) {
            lruHashMap.remove(delKey);
        }

        // load
        RpcProtocol eldestKey = lruHashMap.entrySet().iterator().next().getKey();
        return lruHashMap.get(eldestKey);
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
