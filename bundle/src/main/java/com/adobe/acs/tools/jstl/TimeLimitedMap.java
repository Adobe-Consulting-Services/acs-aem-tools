package com.lq.aem.lqcom.utils;

import java.util.Collection;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * The time limited map tracks entries as well as entry ages. If an entry is
 * older than the max age (TTL) then the entry is discarded lazily (meaning it
 * is discarded the next time it is read.)
 */
public class TimeLimitedMap<K, V> implements Map<K, V> {

    private final ConcurrentHashMap<K, V> store;
    private final ConcurrentHashMap<K, Long> timestamps;
    private final long ttl;

    public TimeLimitedMap(TimeUnit ttlUnit, long ttlValue) {
        this.store = new ConcurrentHashMap<K, V>();
        this.timestamps = new ConcurrentHashMap<K, Long>();
        this.ttl = ttlUnit.toMillis(ttlValue);
    }

    @Override
    public V get(Object key) {
        V value = this.store.get(key);

        if (value != null && expired(key, value)) {
            store.remove((K) key);
            timestamps.remove((K) key);
            return null;
        } else {
            return value;
        }
    }

    public long getAge(Object key) {
        Long timestamp = timestamps.get((K) key);
        if (timestamp == null) return Long.MAX_VALUE;
        return (System.currentTimeMillis() - timestamp);
    }
    
    private boolean expired(Object key, V value) {
        long age = getAge(key);
        return (age < 0 || age >= ttl);
    }

    @Override
    public V put(K key, V value) {
        timestamps.put(key, System.currentTimeMillis());
        return store.put(key, value);
    }

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public boolean isEmpty() {
        return store.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return store.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return store.containsValue(value);
    }

    @Override
    public V remove(Object key) {
        timestamps.remove(key);
        return store.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        timestamps.clear();
        store.clear();
    }

    @Override
    public Set<K> keySet() {
        clearExpired();
        return unmodifiableSet(store.keySet());
    }

    @Override
    public Collection<V> values() {
        clearExpired();
        return unmodifiableCollection(store.values());
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        clearExpired();
        return unmodifiableSet(store.entrySet());
    }

    private void clearExpired() {
        for (K k : store.keySet()) {
            this.get(k);
        }
    }
}
