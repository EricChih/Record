/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.handler;


import java.util.ArrayList;
import java.util.List;

import com.ub.gir.web.configuration.security.SecurityAttributes;
import com.ub.gir.web.configuration.security.UrlPathResourceConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.logout.*;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
public class LogoutAuthenticationStatusHandlerConfig {

    private static final String DEFAULT_TARGET_URL = UrlPathResourceConfig.LOGOUT.getPagePath();

    /**
     * @return SimpleUrlLogoutSuccessHandler
     */
    @Bean
    public SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandler() {
        MvcLogoutAuthenticationSuccessHandler mvcLogoutAuthenticationSuccessHandler = new MvcLogoutAuthenticationSuccessHandler();
        mvcLogoutAuthenticationSuccessHandler.setDefaultTargetUrl(DEFAULT_TARGET_URL);
        return mvcLogoutAuthenticationSuccessHandler;
    }

    @Bean
    public SimpleUrlLogoutSuccessHandler simpleUrlLogoutSuccessHandlerForPwdChange() {
        MvcLogoutAuthenticationSuccessHandler mvcLogoutAuthenticationSuccessHandler = new MvcLogoutAuthenticationSuccessHandler();
        mvcLogoutAuthenticationSuccessHandler.setDefaultTargetUrl("/login?msg=pwdchange");
        return mvcLogoutAuthenticationSuccessHandler;
    }

    /**
     * @return HttpStatusReturningLogoutSuccessHandler
     */
    @Bean
    public HttpStatusReturningLogoutSuccessHandler httpStatusReturningLogoutSuccessHandler() {
        HttpStatusReturningLogoutSuccessHandler httpStatusReturningLogoutSuccessHandler = new HttpStatusReturningLogoutSuccessHandler();
        return httpStatusReturningLogoutSuccessHandler;
    }

    /**
     * @return SecurityContextLogoutHandler
     */
    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        securityContextLogoutHandler.setInvalidateHttpSession(true);
        securityContextLogoutHandler.setClearAuthentication(true);
        return securityContextLogoutHandler;

    }

    /**
     * @return CookieClearingLogoutHandler
     */
    @Bean
    public CookieClearingLogoutHandler cookieClearingLogoutHandler() {
        final String[] cookiesArg = {SecurityAttributes.CSRF_COOKIE_NAME, SecurityAttributes.SESSION_KEY, SecurityAttributes.REMEMBER_ME_KEY};
        CookieClearingLogoutHandler cookieClearingLogoutHandler = new CookieClearingLogoutHandler(cookiesArg);
        return cookieClearingLogoutHandler;
    }

    /**
     * @return LogoutHandler []
     */
    @Bean
    public LogoutHandler[] logoutHandlers() {

        List<LogoutHandler> logoutHandlers = new ArrayList<>();

        logoutHandlers.add(securityContextLogoutHandler());
        logoutHandlers.add(cookieClearingLogoutHandler());
//		logoutHandlers.add ( rememberMeServices.tokenBasedRememberMeServices () ) ;

        LogoutHandler[] logoutHandlerArray = logoutHandlers.toArray(new LogoutHandler[0]);

        return logoutHandlerArray;

    }

}
