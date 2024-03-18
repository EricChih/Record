/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.cache;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
public class RequestCacheConfig {

    /**
     * @return HttpSessionRequestCache
     */
    @Bean
    public HttpSessionRequestCache httpSessionRequestCache() {
        HttpSessionRequestCache httpSessionRequestCache = new HttpSessionRequestCache();
        httpSessionRequestCache.setCreateSessionAllowed(true);
        return httpSessionRequestCache;
    }

}
