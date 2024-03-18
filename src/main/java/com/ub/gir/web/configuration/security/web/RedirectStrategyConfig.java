/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.web;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.DefaultRedirectStrategy;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
public class RedirectStrategyConfig {

    /**
     * @return DefaultRedirectStrategy
     */
    @Bean
    public DefaultRedirectStrategy defaultRedirectStrategy() {
        DefaultRedirectStrategy defaultRedirectStrategy = new DefaultRedirectStrategy();
        defaultRedirectStrategy.setContextRelative(false);
        return defaultRedirectStrategy;
    }

}
