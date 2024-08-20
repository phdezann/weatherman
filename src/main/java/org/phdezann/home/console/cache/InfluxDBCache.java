package org.phdezann.home.console.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class InfluxDBCache {

    public enum CacheKey {
        REALTIME,
        HISTORICAL,
    }

    private final Cache<String, String> cache;

    public InfluxDBCache() {
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();
    }

    public String getOrAdd(CacheKey key, Callable<String> creator) {
        try {
            return cache.get(key.name(), creator);
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

}
