/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.filter.login;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
public class LoginAuthenticationFilterConfig {

    private static final String LOGIN_PROCESSING_URL = "/perform_login";

    /**
     * @return UsernamePasswordAuthenticationFilter
     */
    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter(@Qualifier("savedRequestAwareAuthenticationSuccessHandler") SavedRequestAwareAuthenticationSuccessHandler savedRequestAwareAuthenticationSuccessHandler,
                                                                                     @Qualifier("exceptionMappingAuthenticationFailureHandler") ExceptionMappingAuthenticationFailureHandler exceptionMappingAuthenticationFailureHandler,
                                                                                     @Qualifier("compositeSessionAuthenticationStrategy") CompositeSessionAuthenticationStrategy compositeSessionAuthenticationStrategy,
                                                                                     @Qualifier("providerManager") ProviderManager providerManager) {

        MvcLoginAuthenticationFilter mvcLoginAuthenticationFilter = new MvcLoginAuthenticationFilter(new AntPathRequestMatcher(LOGIN_PROCESSING_URL, HttpMethod.POST.name()));

        mvcLoginAuthenticationFilter.setPostOnly(true);

        mvcLoginAuthenticationFilter.setAuthenticationSuccessHandler(savedRequestAwareAuthenticationSuccessHandler);
        mvcLoginAuthenticationFilter.setAuthenticationFailureHandler(exceptionMappingAuthenticationFailureHandler);

        mvcLoginAuthenticationFilter.setSessionAuthenticationStrategy(compositeSessionAuthenticationStrategy);
        mvcLoginAuthenticationFilter.setAuthenticationManager(providerManager);

        return mvcLoginAuthenticationFilter;

    }

}
