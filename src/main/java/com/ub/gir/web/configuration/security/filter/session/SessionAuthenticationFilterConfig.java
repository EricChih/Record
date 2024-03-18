/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.filter.session;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.session.SimpleRedirectInvalidSessionStrategy;
import org.springframework.security.web.session.SimpleRedirectSessionInformationExpiredStrategy;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
public class SessionAuthenticationFilterConfig {

    /**
     * @return HttpSessionSecurityContextRepository
     */
    @Bean
    public HttpSessionSecurityContextRepository httpSessionSecurityContextRepository() {
        HttpSessionSecurityContextRepository httpSessionSecurityContextRepository = new HttpSessionSecurityContextRepository();

        // SessionManagementConfigurer : SessionCreationPolicy = ALWAYS, NEVER, IF_REQUIRED, STATELESS
        httpSessionSecurityContextRepository.setAllowSessionCreation(true);

        return httpSessionSecurityContextRepository;

    }

    /**
     * @return SessionManagementFilter
     */
    @Bean
    public SessionManagementFilter sessionManagementFilter(@Qualifier("compositeSessionAuthenticationStrategy") CompositeSessionAuthenticationStrategy compositeSessionAuthenticationStrategy,
                                                           @Qualifier("simpleRedirectInvalidSessionStrategy") SimpleRedirectInvalidSessionStrategy simpleRedirectInvalidSessionStrategy) {
        SessionManagementFilter sessionManagementFilter = new SessionManagementFilter(httpSessionSecurityContextRepository(), compositeSessionAuthenticationStrategy);
        sessionManagementFilter.setInvalidSessionStrategy(simpleRedirectInvalidSessionStrategy);
//	    sessionManagementFilter.setAuthenticationFailureHandler ( failureHandler ) ;
        return sessionManagementFilter;
    }

    /**
     * For ConcurrentSessionControlAuthenticationStrategy.setExceptionIfMaximumExceeded ( false )
     *
     * @return ConcurrentSessionFilter
     */
    @Bean
    public ConcurrentSessionFilter concurrentSessionFilter(@Qualifier("sessionRegistry") SessionRegistry sessionRegistry,
                                                           @Qualifier("simpleRedirectSessionInformationExpiredStrategy") SimpleRedirectSessionInformationExpiredStrategy simpleRedirectSessionInformationExpiredStrategy,
                                                           @Qualifier("logoutHandlers") LogoutHandler[] logoutHandlers) {
        ConcurrentSessionFilter concurrentSessionFilter = new ConcurrentSessionFilter(sessionRegistry, simpleRedirectSessionInformationExpiredStrategy);
        concurrentSessionFilter.setLogoutHandlers(logoutHandlers);
        return concurrentSessionFilter;
    }

}
