package org.phdezann.home.console.cache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;

public class TicCache {

    public enum CacheKey {
        PAPP
    }

    private final com.google.common.cache.Cache<String, String> cache;

    public TicCache() {
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();
    }

    public void put(CacheKey key, String value) {
        cache.put(key.name(), value);
    }

    public Optional<String> get(CacheKey key) {
        return Optional.ofNullable(cache.getIfPresent(key.name()));
    }

}
