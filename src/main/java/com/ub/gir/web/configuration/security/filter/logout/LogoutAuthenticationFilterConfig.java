/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.filter.logout;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
public class LogoutAuthenticationFilterConfig {

    private static final String LOGOUT_PROCESSING_URL = "/perform_logout";
    private static final String LOGOUT_PROCESSING_URL_FOR_PWD_CHANGE = "/perform_logout_pwdchange";


    /**
     * @return LogoutFilter
     */
    @Bean
    public LogoutFilter logoutFilter(@Qualifier("simpleUrlLogoutSuccessHandler") SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler,
                                     @Qualifier("logoutHandlers") LogoutHandler[] logoutHandlers) {
        LogoutFilter logoutFilter = new LogoutFilter(simpleUrlLogoutSuccessHandler, logoutHandlers);
        logoutFilter.setLogoutRequestMatcher(new AntPathRequestMatcher(LOGOUT_PROCESSING_URL));
        return logoutFilter;
    }

    @Bean
    public LogoutFilter logoutFilterForPwdChange(@Qualifier("simpleUrlLogoutSuccessHandlerForPwdChange") SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler,
                                     @Qualifier("logoutHandlers") LogoutHandler[] logoutHandlers) {
        LogoutFilter logoutFilter = new LogoutFilter(simpleUrlLogoutSuccessHandler, logoutHandlers);
        logoutFilter.setLogoutRequestMatcher(new AntPathRequestMatcher(LOGOUT_PROCESSING_URL_FOR_PWD_CHANGE));
        return logoutFilter;
    }

}
