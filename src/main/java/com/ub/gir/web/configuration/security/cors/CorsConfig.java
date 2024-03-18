/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.cors;


import java.util.Arrays;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


/**
 * <p>跨來源資源共用</p>
 * <p>Cross-Origin Resource Sharing Of Config</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/8/29
 */
@Configuration
@Slf4j
public class CorsConfig {

    @Value("${hualiteq.security.cors.endpoint-paths:/**}")
    private String endpointPaths;

    @Value("${hualiteq.security.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${hualiteq.security.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${hualiteq.security.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${hualiteq.security.cors.allowed-credentials:false}")
    private boolean allowedCredentials;

    @Value("${hualiteq.security.cors.max-age:3600L}")
    private long maxAge;

    public CorsConfig() {
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        //CorsRequestConfiguration corsRequestConfiguration = new CorsRequestConfiguration();
        CorsConfiguration corsRequestConfiguration = new CorsConfiguration();

        //corsRequestConfiguration.setAllowedOrigins(Collections.singletonList(allowedOrigins));
        corsRequestConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:8082"));
        //corsRequestConfiguration.setAllowedMethods(allowedMethods);
        corsRequestConfiguration.setAllowedMethods(Arrays.asList("GET","POST","DELETE","PUT","PATCH","OPTIONS"));

        //corsRequestConfiguration.setAllowedHeaders(allowedHeaders);
        corsRequestConfiguration.setAllowCredentials(allowedCredentials);
        corsRequestConfiguration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //source.registerCorsConfiguration("/**", corsRequestConfiguration.applyPermitDefaultValues());
        //source.registerCorsConfiguration(endpointPaths, corsRequestConfiguration);
        source.registerCorsConfiguration("/**", corsRequestConfiguration);

        return source;

    }

}
