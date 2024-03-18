/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.cache;


import java.util.function.Function;


/**
 * <p></p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/4/19
 */
public interface CacheLocal<K, V> {

    V get(K key);

    V get(K key, Function<? super String, ? extends Integer> valueLoader);

    V getIfPresent(K key);

    void put(K key, V value);

    void refresh(K key);

    void invalidate(K key);

    void invalidateAll();

    void clean();

    long size();

}
