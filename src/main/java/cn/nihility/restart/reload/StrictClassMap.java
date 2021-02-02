package cn.nihility.restart.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class StrictClassMap<V> extends HashMap<String, V> {

    private static final Logger log = LoggerFactory.getLogger(StrictClassMap.class);

    private static final long serialVersionUID = -4950446264854982944L;
    private final String name;

    public StrictClassMap(String name, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.name = name;
    }

    public StrictClassMap(String name, int initialCapacity) {
        super(initialCapacity);
        this.name = name;
    }

    public StrictClassMap(String name) {
        super();
        this.name = name;
    }

    public StrictClassMap(String name, Map<String, ? extends V> m) {
        super(m);
        this.name = name;
    }

    @Override
    public V put(String key, V value) {
        if (containsKey(key)) {
            /*throw new IllegalArgumentException(name + " already contains value for " + key
                    + (conflictMessageProducer == null ? "" : conflictMessageProducer.apply(super.get(key), value)));*/
            log.warn("重复类 [{}]", key);
            return get(key);
        }
        return super.put(key, value);
    }

    @Override
    public V get(Object key) {
        return super.get(key);
    }

}

