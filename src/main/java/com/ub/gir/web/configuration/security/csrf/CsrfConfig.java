/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.csrf;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;


/**
 * <p>跨站請求偽造</p>
 * <p>Cross-Site Request Forgery Of Config</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
@Slf4j
public class CsrfConfig {

    @Value("${hualiteq.security.csrf.enabled:false}")
    private boolean csrfEnabled;

    @Value("${hualiteq.security.csrf.support.allowed-urls:#{null}}")
    private String targetUrls;

    @Value("${hualiteq.security.csrf.support.allowed-methods}")
    private String targetMethods;

    public CsrfConfig(){
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    /**
     * @return RequestMatcher
     */
    @Bean
    public RequestMatcher requestMatcher() {

        CsrfRequestMatcher csrfRequestMatcher = new CsrfRequestMatcher();
        csrfRequestMatcher.setSupportEnabled(csrfEnabled);
        csrfRequestMatcher.setAllowedUrls(targetUrls);
        csrfRequestMatcher.setAllowedMethods(targetMethods);

        return csrfRequestMatcher;

    }

    /**
     * @return HttpSessionCsrfTokenRepository
     */
    @Bean
    public HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository() {
        HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
        return httpSessionCsrfTokenRepository;
    }

    /**
     * @return CookieCsrfTokenRepository
     */
    @Bean
    public CookieCsrfTokenRepository cookieCsrfTokenRepository() {
        CookieCsrfTokenRepository cookieCsrfTokenRepository = new CookieCsrfTokenRepository();
        cookieCsrfTokenRepository.setCookieHttpOnly(false);
        return cookieCsrfTokenRepository;
    }

    /**
     * @return CsrfAuthenticationStrategy
     */
    @Bean
    public CsrfAuthenticationStrategy csrfAuthenticationStrategy() {
        return new CsrfAuthenticationStrategy(httpSessionCsrfTokenRepository());
    }

}
