package org.phdezann.home.console.cache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;

public class TemperatureCache {

    public enum CacheKey {
        TEMPERATURE
    }

    private final com.google.common.cache.Cache<String, Double> cache;

    public TemperatureCache() {
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    }

    public void put(CacheKey key, double value) {
        cache.put(key.name(), value);
    }

    public Optional<Double> get(CacheKey key) {
        return Optional.ofNullable(cache.getIfPresent(key.name()));
    }

}
