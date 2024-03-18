/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.cache.caffeine;


import com.ub.gir.web.configuration.cache.CacheLocal;


/**
 * <p></p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/5/11
 */
public class ContextStatusCacheSingleton {

    private final CacheLocal<String, Integer> cacheLocal = new ContextStatusCache();

    private ContextStatusCacheSingleton() {
    }

    public static ContextStatusCacheSingleton getInstance() {
        return ContextStatusCacheSingleton.ContextStatusCacheHolder.INSTANCE;
    }

    public CacheLocal<String, Integer> getCacheLocal() {
        return getInstance().cacheLocal;
    }

    private static class ContextStatusCacheHolder {
        private static final ContextStatusCacheSingleton INSTANCE = new ContextStatusCacheSingleton();
    }

}
