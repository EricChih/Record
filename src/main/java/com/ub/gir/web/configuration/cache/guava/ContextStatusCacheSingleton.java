/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.cache.guava;


import com.ub.gir.web.configuration.cache.CacheLocal;


/**
 * <p></p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/5/9
 */
public class ContextStatusCacheSingleton {

    private final CacheLocal<String, Integer> cacheLocal = new ContextStatusCache();

    private ContextStatusCacheSingleton() {
    }

    public static ContextStatusCacheSingleton getInstance() {
        return ContextStatusCacheHolder.INSTANCE;
    }

    public CacheLocal<String, Integer> getCacheLocal() {
        return getInstance().cacheLocal;
    }

    private static class ContextStatusCacheHolder {
        private static final ContextStatusCacheSingleton INSTANCE = new ContextStatusCacheSingleton();
    }

}
