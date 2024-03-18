/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.listener;



import com.ub.gir.web.configuration.cache.CacheLocal;
import com.ub.gir.web.configuration.cache.caffeine.ContextStatusCacheSingleton;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;


/**
 * <p>監聽 Security 登入成功事件</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/4/13
 */
@Component
@Slf4j
public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final CacheLocal<String, Integer> cacheLocal ;

    public AuthenticationSuccessEventListener () {
        super() ;
        log.info("**** Start-Up : {}", this.getClass().getName());
        cacheLocal= ContextStatusCacheSingleton.getInstance().getCacheLocal();
    }

    @Override
    public void onApplicationEvent(@NonNull AuthenticationSuccessEvent event) {

        log.info("Number of registered accounts : {}", cacheLocal.size());

        if(cacheLocal.size()!=0){
            cacheLocal.invalidateAll();
        }
    }

}
