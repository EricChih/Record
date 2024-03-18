/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.handler;


import com.ub.gir.web.configuration.security.UrlPathResourceConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
public class LoginAuthenticationStatusHandlerConfig {

    private static final String ACCESS_DENIED_PAGE = UrlPathResourceConfig.ACCESS_DENIED.getPagePath();

    private static final String DEFAULT_TARGET_URL = UrlPathResourceConfig.DEFAULT.getPagePath();

    private static final String DEFAULT_FAILURE_URL = UrlPathResourceConfig.ERROR.getPagePath();

    /**
     * @return AccessDeniedHandlerImpl
     */
    @Bean
    public AccessDeniedHandlerImpl accessDeniedHandlerImpl() {
        MvcAccessDeniedHandler mvcAccessDeniedHandler = new MvcAccessDeniedHandler();
        mvcAccessDeniedHandler.setErrorPage(ACCESS_DENIED_PAGE);
        return mvcAccessDeniedHandler;
    }

    /**
     * @return SavedRequestAwareAuthenticationSuccessHandler
     */
    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler savedRequestAwareAuthenticationSuccessHandler() {
        MvcLoginAuthenticationSuccessHandler mvcLoginAuthenticationSuccessHandler = new MvcLoginAuthenticationSuccessHandler();
        mvcLoginAuthenticationSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
        mvcLoginAuthenticationSuccessHandler.setDefaultTargetUrl(DEFAULT_TARGET_URL);
        return mvcLoginAuthenticationSuccessHandler;
    }

    /**
     * @return ExceptionMappingAuthenticationFailureHandler
     */
    @Bean
    public ExceptionMappingAuthenticationFailureHandler exceptionMappingAuthenticationFailureHandler() {
        MvcLoginAuthenticationFailureHandler mvcLoginAuthenticationFailureHandler = new MvcLoginAuthenticationFailureHandler();
        mvcLoginAuthenticationFailureHandler.setDefaultFailureUrl(DEFAULT_FAILURE_URL);
        return mvcLoginAuthenticationFailureHandler;
    }

}
