/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.cors;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.cors.CorsConfiguration;


/**
 * <p>Cross-Origin Resource Sharing (CORS) 跨來源資源共用</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/9/11
 */
@Slf4j
public class CorsRequestConfiguration extends CorsConfiguration {

    /**
     * Constructor
     */
    public CorsRequestConfiguration() {
        super();
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    /**
     * Constructor
     *
     * @param other
     */
    public CorsRequestConfiguration(CorsConfiguration other) {
        super(other);
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    /**
     * @param allowedOrigin
     */
    public void setAllowedOrigins(String allowedOrigin) {
        final List<String> list = checkContent(allowedOrigin);
        super.setAllowedOrigins(list);
    }

    /**
     * @param allowedMethod
     */
    public void setAllowedMethods(String allowedMethod) {
        final List<String> list = checkContent(allowedMethod);
        super.setAllowedMethods(list);
    }

    /**
     * @param allowedHeader
     */
    public void setAllowedHeaders(String allowedHeader) {
        final List<String> list = checkContent(allowedHeader);
        super.setAllowedHeaders(list);
    }

    /**
     * @param exposedHeader
     */
    public void setExposedHeaders(String exposedHeader) {
        final List<String> list = checkContent(exposedHeader);
        super.setExposedHeaders(list);
    }

    /**
     * @param content
     * @return
     */
    private List<String> checkContent(String content) {

        log.info("CORS support content : {}", content);

        if (content!=null) {
            return Arrays.stream(content.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        return null;

    }

}
