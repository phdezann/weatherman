package org.phdezann.home.console.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class TomorrowIOCache {

    public enum CacheKey {
        TOMORROW_IO_REALTIME,
        TOMORROW_IO_FORECAST
    }

    private final Cache<String, String> cache;

    public TomorrowIOCache() {
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
    }

    public String getOrAdd(CacheKey key, Callable<String> creator) {
        try {
            return cache.get(key.name(), creator);
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

}
