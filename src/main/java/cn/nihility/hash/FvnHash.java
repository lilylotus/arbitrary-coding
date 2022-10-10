package cn.nihility.hash;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FvnHash {

    /**
     * 真实服务器列表, 由于增加与删除的频率比遍历高, 用链表存储比较划算
     */
    private static List<String> realNodes = new LinkedList<>();

    /**
     * 虚拟节点列表
     */
    private static TreeMap<Integer, String> virtualNodes = new TreeMap<>();

    /**
     * 改进的 32 位 FNV 算法
     * 能快速 hash 大量数据并保持较小的冲突率
     * 高度分散使它适用于 hash 一些非常相近的字符串，比如 URL，hostname，文件名，text，IP地址等
     */
    public static int fvnHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;

        for (int i = 0; i < str.length(); i++) {
            // 混淆明文
            hash = (hash ^ str.charAt(i)) * p;
        }

        // 牵一发而动全身
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        return (hash < 0 ? Math.abs(hash) : hash);
    }

    public static void init(List<String> servers) {
        realNodes.clear();
        virtualNodes.clear();
        for (String s : servers) {
            // 把服务器加入真实服务器列表中
            realNodes.add(s);
            String[] split = s.split("#");
            // 服务器名称, 省略端口号
            String name = split[0];
            // 根据服务器性能给每台真实服务器分配虚拟节点, 并把虚拟节点放到虚拟节点列表中.
            int virtualNodeNum = Integer.parseInt(split[1]) * 10;
            for (int i = 1; i <= virtualNodeNum; i++) {
                virtualNodes.put(fvnHash(name + "@" + i), name + "@" + i);
            }
        }
    }

    private static String getRealServer(String client) {
        // 计算客户端请求的哈希值
        int hash = fvnHash(client);
        // 得到大于该哈希值的所有map集合
        SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
        // 找到比该值大的第一个虚拟节点, 如果没有比它大的虚拟节点, 根据哈希环, 则返回第一个节点.
        Integer targetKey = subMap.isEmpty() ? virtualNodes.firstKey() : subMap.firstKey();
        // 通过该虚拟节点获得真实节点的名称
        String virtualNodeName = virtualNodes.get(targetKey);
        return virtualNodeName.split("@")[0];
    }

    public static String consistencyHash(String key, List<String> servicesId) {
        // 将服务节点加入 hash 环
        TreeMap<Integer, String> serverHashMap = new TreeMap<>();
        for (String server : servicesId) {
            serverHashMap.put(fvnHash(server), server);
        }

        // key 对应的 hash 环上服务节点
        Integer keyHash = fvnHash(key);

        SortedMap<Integer, String> subHashMap = serverHashMap.tailMap(keyHash);
        Integer keyIndex;

        if (subHashMap.isEmpty()) {
            keyIndex = serverHashMap.firstKey();
        } else {
            keyIndex = subHashMap.firstKey();
        }

        return serverHashMap.get(keyIndex);

    }

    public static String consistencyHash(String key, SortedMap<Integer, String> serverHashMap) {
        // key 对应的 hash 环上服务节点
        Integer keyHash = fvnHash(key);

        SortedMap<Integer, String> subHashMap = serverHashMap.tailMap(keyHash);
        Integer keyIndex;

        if (subHashMap.isEmpty()) {
            keyIndex = serverHashMap.firstKey();
        } else {
            keyIndex = subHashMap.firstKey();
        }

        return serverHashMap.get(keyIndex);
    }

    public static void main(String[] args) {
        List<String> services = new ArrayList<>();
        services.add("serverA#35");
        services.add("serverB#35");
        services.add("serverC#20");
        services.add("serverD#10");

        List<String> services2 = new ArrayList<>();
        services2.add("serverA#100");
        services2.add("serverB#100");
        services2.add("serverD#40");

        init(services);

        Random random = new Random(System.currentTimeMillis());
        Map<String, AtomicInteger> mapCompute = new HashMap<>(8);
        Map<String, AtomicInteger> mapCompute2 = new HashMap<>(8);

        final int count = 10000;
        for (int i = 0; i < count; i++) {
            //模拟产生一个请求
            String client = getN() + "." + getN() + "." + getN() + "." + getN() + ":" + (1000 + (random.nextInt(9000)));
            //计算请求的哈希值
            int hash = fvnHash(client);
            //判断请求将由哪台服务器处理
            String realServer = getRealServer(client);
            //System.out.printf("%20s : %12d 的请求将由 %s 处理%n", client, hash, realServer);
            AtomicInteger serverCount = mapCompute.computeIfAbsent(realServer, k -> new AtomicInteger(0));
            serverCount.getAndIncrement();
        }
        mapCompute.forEach((k, v) -> System.out.println(k + " = " + v.get()));
        System.out.println("==================");

        init(services2);
        for (int i = 0; i < count; i++) {
            //模拟产生一个请求
            String client = getN() + "." + getN() + "." + getN() + "." + getN() + ":" + (1000 + (random.nextInt(9000)));
            //计算请求的哈希值
            int hash = fvnHash(client);
            //判断请求将由哪台服务器处理
            String realServer = getRealServer(client);
            //System.out.printf("%20s : %12d 的请求将由 %s 处理%n", client, hash, realServer);
            AtomicInteger serverCount = mapCompute2.computeIfAbsent(realServer, k -> new AtomicInteger(0));
            serverCount.getAndIncrement();
        }

        mapCompute2.forEach((k, v) -> System.out.println(k + " = " + v.get()));
    }

    public static int getN() {
        return (int) (Math.random() * 128);
    }


    public static void main2(String[] args) {
        List<String> servicesId = new ArrayList<>();
        servicesId.add("serverA#100");
        servicesId.add("serverB#100");
        servicesId.add("serverC#80");
        servicesId.add("serverD#40");

        // 将服务节点加入 hash 环
        TreeMap<Integer, String> serverHashMap = new TreeMap<>();
        for (String server : servicesId) {
            serverHashMap.put(fvnHash(server), server);
        }
        for (String server : servicesId) {
            serverHashMap.put(fvnHash(server + "#" + UUID.randomUUID().toString().replace("-", "")), server);
        }

        Map<String, AtomicInteger> mapCompute = new HashMap<>(8);

        int count = 10000;

        for (int i = 0; i < count; i++) {
            String key = consistencyHash(UUID.randomUUID().toString().replace("-", ""), serverHashMap);
            AtomicInteger value = mapCompute.computeIfAbsent(key, k -> new AtomicInteger(0));
            value.getAndIncrement();
        }
        mapCompute.forEach((k, v) -> System.out.println(k + " = " + v.get()));
        /*System.out.println("====================");

        serverHashMap.remove(fvnHash("node-C"));
        serverHashMap.remove(fvnHash("node-C" + "#1"));
        serverHashMap.remove(fvnHash("node-C" + "#2"));
        serverHashMap.remove(fvnHash("node-C" + "#3"));
        serverHashMap.remove(fvnHash("node-C" + "#4"));

        for (int i = 0; i < count; i++) {
            String key = consistencyHash(UUID.randomUUID().toString().replace("-", ""), serverHashMap);
            AtomicInteger value = mapCompute.computeIfAbsent(key, k -> new AtomicInteger(0));
            value.getAndIncrement();
        }
        mapCompute.forEach((k, v) -> System.out.println(k + " = " + v.get()));*/
    }

}
