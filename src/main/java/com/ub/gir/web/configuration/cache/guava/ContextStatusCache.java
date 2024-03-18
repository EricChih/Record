/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.cache.guava;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.google.common.cache.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import com.ub.gir.web.configuration.cache.CacheLocal;

import lombok.extern.slf4j.Slf4j;


/**
 * <p>Guava Cache Initialization definition</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/4/19
 */
@Slf4j
public class ContextStatusCache implements CacheLocal<String, Integer> {

    // The number of threads that can write to the cache concurrently.
    public static final int DEFAULT_CONCURRENCY_LEVEL = 10;

    // The initial capacity of the cache container.
    public static final int DEFAULT_INITIAL_CAPACITY = 50;

    // The maximum capacity of the cache. After exceeding the specified value, the cache item will be removed according to the LRU algorithm which is rarely used recently.
    public static final int DEFAULT_MAXIMUM_SIZE = 100;

    private final LoadingCache<String, Integer> loadingCache;

    public ContextStatusCache() {
        this(Runtime.getRuntime().availableProcessors(), DEFAULT_INITIAL_CAPACITY, DEFAULT_MAXIMUM_SIZE, false);
    }

    /**
     * Constructor
     *
     * @param concurrencyLevel that threads can write to the cache concurrently.
     * @param capacity         that initialize capacity of the cache container.
     */
    public ContextStatusCache(int concurrencyLevel, int capacity) {
        this(concurrencyLevel, capacity, DEFAULT_MAXIMUM_SIZE, false);
    }

    /**
     * Constructor
     *
     * @param concurrencyLevel that threads can write to the cache concurrently.
     * @param capacity         that initialize capacity of the cache container.
     * @param maximumSize      that was capacity of the cache. After exceeding the specified value, the cache item will be removed according to the LRU algorithm which is rarely used recently.
     * @param enabled          that turn on the culling strategy of references to prevent memory leaks.
     */
    public ContextStatusCache(int concurrencyLevel, int capacity, int maximumSize, boolean enabled) {

        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .concurrencyLevel(concurrencyLevel)
                .initialCapacity(capacity)
                .maximumSize(maximumSize);

        if (enabled) {
            cacheBuilder.weakKeys();
        }

        this.loadingCache = cacheBuilder
                .removalListener(new ContextStatusCache.LocalRemovalListener())
                .build(new ContextStatusCache.LocalCacheLoader());

    }

    /**
     * Constructor
     *
     * @param concurrencyLevel that threads can write to the cache concurrently.
     * @param capacity         that initialize capacity of the cache container.
     * @param maximumSize      that was capacity of the cache. After exceeding the specified value, the cache item will be removed according to the LRU algorithm which is rarely used recently.
     * @param expire           that was time expires in "N seconds" after caching.
     * @param timeUnit         with <code>expire<code/> parameter is the expired time unit.
     * @param enabled          that turn on the culling strategy of references to prevent memory leaks.
     */
    public ContextStatusCache(int concurrencyLevel, int capacity, int maximumSize, long expire, TimeUnit timeUnit, boolean enabled) {

        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .concurrencyLevel(concurrencyLevel)
                .initialCapacity(capacity)
                .maximumSize(maximumSize)
                .expireAfterAccess(expire, timeUnit);

        if (enabled) {
            cacheBuilder.weakKeys();
        }

        this.loadingCache = cacheBuilder
                .removalListener(new ContextStatusCache.LocalRemovalListener())
                .build(new ContextStatusCache.LocalCacheLoader());

    }

    public Integer getUnchecked(String key) {
        return this.loadingCache.getUnchecked(key);
    }

    @Override
    public Integer get(String key) {

        try {
            return this.loadingCache.get(key);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Integer get(String key, final Function<? super String, ? extends Integer> valueLoader){

        try {
            return this.loadingCache.get(key, (Callable<? extends Integer>) valueLoader);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Integer getIfPresent(String key) {
        return this.loadingCache.getIfPresent(key);
    }

    @Override
    public void put(String key, Integer value) {
        this.loadingCache.put(key, value);
    }

    @Override
    public void refresh(String key) {
        this.loadingCache.refresh(key);
    }

    @Override
    public void invalidate(String key) {
        this.loadingCache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        this.loadingCache.invalidateAll();
    }

    @Override
    public void clean() {
        this.loadingCache.cleanUp();
    }

    @Override
    public long size() {
        return this.loadingCache.size();
    }

    public long size2() {
        return this.loadingCache.asMap().size();
    }

    public LoadingCache<String, Integer> getLocalCache() {
        return this.loadingCache;
    }

    private static class LocalRemovalListener implements RemovalListener<String, Integer> {

        LocalRemovalListener() {
            super();
        }

        @Override
        public void onRemoval(RemovalNotification<String, Integer> removalNotification) {
            log.info("#### The value \"{}\" of key \"{}\" was removed for the reason : {} ", removalNotification.getValue(), removalNotification.getKey(), removalNotification.getCause());
        }

    }

    private static class LocalCacheLoader extends CacheLoader<String, Integer> {

        LocalCacheLoader() {
            super();
        }

        @Override
        public @NonNull Integer load(@NonNull String key) throws Exception {
            TimeUnit.MILLISECONDS.sleep(100);
            log.info("#### The key \"{}\" does not exist ! ", key);
            return 0;
        }

    }

}
