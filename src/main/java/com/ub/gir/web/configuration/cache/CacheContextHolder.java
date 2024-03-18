/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.cache;


/**
 * <p></p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/4/20
 */
public class CacheContextHolder {

    //private static final ThreadLocal<CacheLocal<?, ?>> localCacheThreadLocal = new InheritableThreadLocal<>();
    private static final ThreadLocal<CacheLocal<?, ?>> localCacheThreadLocal = new ThreadLocal<>();

    private CacheContextHolder() {
        super();
    }

    public static void setContent(CacheLocal<?, ?> contextStatusCache) {
        localCacheThreadLocal.set(contextStatusCache);
    }

    public static CacheLocal<?, ?> getContent() {
        return localCacheThreadLocal.get();
    }

    public static void clearContent() {

        if (localCacheThreadLocal.get()!=null) {
            localCacheThreadLocal.remove();
        }

    }

}
