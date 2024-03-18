/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.filter.session;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.ub.gir.web.configuration.security.UrlPathResourceConfig;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
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
public class SessionAuthenticationStrategyConfig {

    private static final String INVALID_SESSION_URL = UrlPathResourceConfig.SESSION_INVALID.getPagePath();

    private static final String EXPIRED_SESSION_URL = UrlPathResourceConfig.SESSION_EXPIRED.getPagePath();

    @Value("${hualiteq.session.maximum-sessions}")
    private int maximumSessions;

    @Value("${hualiteq.session.exception-if-maximum-exceeded}")
    private boolean exceptionIfMaximumExceeded;

    @Value("${hualiteq.session.migrate-session-attributes}")
    private boolean migrateSessionAttributes;

    @Resource
    private CsrfAuthenticationStrategy csrfAuthenticationStrategy;

    /**
     * @return SessionRegistry
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * For SessionManagementFilter
     *
     * @return SimpleRedirectInvalidSessionStrategy
     */
    @Bean
    public SimpleRedirectInvalidSessionStrategy simpleRedirectInvalidSessionStrategy() {
        SimpleRedirectInvalidSessionStrategy simpleRedirectInvalidSessionStrategy = new SimpleRedirectInvalidSessionStrategy(INVALID_SESSION_URL);

        // Determines whether a new session should be created before redirecting
        simpleRedirectInvalidSessionStrategy.setCreateNewSession(true);
        return simpleRedirectInvalidSessionStrategy;
    }

    /**
     * For ConcurrentSessionFilter
     *
     * @return SimpleRedirectSessionInformationExpiredStrategy
     */
    @Bean
    public SimpleRedirectSessionInformationExpiredStrategy simpleRedirectSessionInformationExpiredStrategy() {
        SimpleRedirectSessionInformationExpiredStrategy simpleRedirectSessionInformationExpiredStrategy = new SimpleRedirectSessionInformationExpiredStrategy(EXPIRED_SESSION_URL);
        return simpleRedirectSessionInformationExpiredStrategy;
    }

    /**
     * @return ConcurrentSessionControlAuthenticationStrategy
     */
    @Bean
    public ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy() {

        ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());

        // Only one user is allowed to log in. If the same account is logged in twice, then the first account will be kicked offline and jump to the login page.
        concurrentSessionControlAuthenticationStrategy.setMaximumSessions(maximumSessions);

        // true: do not allow new sessions, keep old sessions. false: destroy the old session, the new session takes effect.
        concurrentSessionControlAuthenticationStrategy.setExceptionIfMaximumExceeded(exceptionIfMaximumExceeded);

        return concurrentSessionControlAuthenticationStrategy;

    }

    /**
     * @return SessionFixationProtectionStrategy
     */
    @Bean
    public SessionFixationProtectionStrategy sessionFixationProtectionStrategy() {

        //ChangeSessionIdAuthenticationStrategy changeSessionIdAuthenticationStrategy = new ChangeSessionIdAuthenticationStrategy();
        SessionFixationProtectionStrategy sessionFixationProtectionStrategy = new SessionFixationProtectionStrategy();

        // SessionFixationConfigurer : newSession, migrateSession, changeSessionId, none
        sessionFixationProtectionStrategy.setMigrateSessionAttributes(migrateSessionAttributes);
//		sessionFixationProtectionStrategy.setAlwaysCreateSession ( true ) ;

        return sessionFixationProtectionStrategy;

    }

    /**
     * @return RegisterSessionAuthenticationStrategy
     */
    @Bean
    public RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy() {
        RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy = new RegisterSessionAuthenticationStrategy(sessionRegistry());
        return registerSessionAuthenticationStrategy;
    }

    /**
     * @return CompositeSessionAuthenticationStrategy
     */
    @Bean
    public CompositeSessionAuthenticationStrategy compositeSessionAuthenticationStrategy() {

        List<SessionAuthenticationStrategy> delegateStrategies = new ArrayList<>();

        delegateStrategies.add(csrfAuthenticationStrategy);
        delegateStrategies.add(concurrentSessionControlAuthenticationStrategy());
        delegateStrategies.add(sessionFixationProtectionStrategy());
        delegateStrategies.add(registerSessionAuthenticationStrategy());

        CompositeSessionAuthenticationStrategy compositeSessionAuthenticationStrategy = new CompositeSessionAuthenticationStrategy(delegateStrategies);

        return compositeSessionAuthenticationStrategy;

    }

}
