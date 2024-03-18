/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.cache.caffeine;


import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.*;
import com.ub.gir.web.configuration.cache.CacheLocal;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * <p>Caffeine Cache Initialization definition</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/5/10
 */
@Slf4j
public class ContextStatusCache implements CacheLocal<String, Integer> {

    // The initial capacity of the cache container.
    public static final int DEFAULT_INITIAL_CAPACITY = 50;

    // The maximum capacity of the cache. After exceeding the specified value, the cache item will be removed according to the LRU algorithm which is rarely used recently.
    public static final int DEFAULT_MAXIMUM_SIZE = 100;

    private final LoadingCache<String, Integer> loadingCache;

    /**
     * Constructor
     */
    public ContextStatusCache() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAXIMUM_SIZE, false);
    }

    /**
     * Constructor
     *
     * @param capacity that initialize capacity of the cache container.
     */
    public ContextStatusCache(int capacity) {
        this(capacity, DEFAULT_MAXIMUM_SIZE, false);
    }

    /**
     * Constructor
     *
     * @param capacity    that initialize capacity of the cache container.
     * @param maximumSize that was capacity of the cache. After exceeding the specified value, the cache item will be removed according to the LRU algorithm which is rarely used recently.
     * @param enabled     that turn on the culling strategy of references to prevent memory leaks.
     */
    public ContextStatusCache(int capacity, int maximumSize, boolean enabled) {

        final Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .initialCapacity(capacity)
                .maximumSize(maximumSize);

        if (enabled) {
            caffeine.weakKeys();
        }

        this.loadingCache = caffeine
                .removalListener(new ContextStatusCache.LocalRemovalListener())
                .build(new ContextStatusCache.LocalCacheLoader());

    }

    /**
     * Constructor
     *
     * @param capacity    that initialize capacity of the cache container.
     * @param maximumSize that was capacity of the cache. After exceeding the specified value, the cache item will be removed according to the LRU algorithm which is rarely used recently.
     * @param expire      that was time expires in "N seconds" after caching.
     * @param timeUnit    with <code>expire<code/> parameter is the expired time unit.
     * @param enabled     that turn on the culling strategy of references to prevent memory leaks.
     */
    public ContextStatusCache(int capacity, int maximumSize, long expire, TimeUnit timeUnit, boolean enabled) {

        final Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .initialCapacity(capacity)
                .maximumSize(maximumSize)
                .expireAfterAccess(expire, timeUnit);

        if (enabled) {
            caffeine.weakKeys();
        }

        this.loadingCache = caffeine
                .removalListener(new ContextStatusCache.LocalRemovalListener())
                .build(new ContextStatusCache.LocalCacheLoader());

    }

    @Override
    public Integer get(String key) {
        return this.loadingCache.get(key);
    }

    @Override
    public Integer get(String key, final Function<? super String, ? extends Integer> valueLoader) {
        return this.loadingCache.get(key, valueLoader);
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
        return this.loadingCache.asMap().size();
    }

    public LoadingCache<String, Integer> getLocalCache() {
        return this.loadingCache;
    }

    private Function<String, Integer> setValue(String key) {
        return t -> 0;
    }

    private static class LocalRemovalListener implements RemovalListener<String, Integer> {

        LocalRemovalListener() {
            super();
        }

        @Override
        public void onRemoval(@Nullable String key, @Nullable Integer value, @NonNull RemovalCause cause) {
            log.info("#### The value \"{}\" of key \"{}\" was removed for the reason : {} ", key, value, cause);
        }

    }

    private static class LocalCacheLoader implements CacheLoader<String, Integer> {

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
