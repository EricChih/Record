/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.csrf;


import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;


/**
 * <p>Cross Site Request Forgery (CSRF) 跨站請求偽造</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/8/23
 */
@Slf4j
public class CsrfRequestMatcher implements RequestMatcher {

    private boolean supportEnabled;

    private String allowedMethods;

    private String allowedUrls;

    /**
     * Constructor
     */
    public CsrfRequestMatcher() {
        this(null,null);
    }

    public CsrfRequestMatcher(String allowedMethods) {
        this(null, allowedMethods);
    }

    public CsrfRequestMatcher(String allowedUrls, String allowedMethods) {
        log.info("**** Start-Up : {}", this.getClass().getName());
        this.allowedUrls = allowedUrls;
        this.allowedMethods = allowedMethods;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.util.matcher.RequestMatcher#matches(javax.servlet.http.HttpServletRequest)
     */
    /**
     * @param request
     * @return boolean true:enable CSRF check. false:disable CSRF check.
     */
    @Override
    public boolean matches(HttpServletRequest request) {

        log.info("CSRF support enabled : {}", supportEnabled);
        log.info("CSRF allowed urls : {}", allowedUrls);
        log.info("CSRF allowed methods : {}", allowedMethods);
        log.info("Get Http methods : {}", request.getMethod());

        // Disable CSRF only on some request matches
        if(!handleCsrfEnabled()){
            return false;
        }

        // Skip allowed methods
        // If the request match one allowed method the CSRF protection will be skipped
        if (handleHttpMethods(request)) {
            return false;
        }

        // Skip allowed urls
        // If the request match one url the CSRF protection will be disabled
        return !handleHttpUrls(request);

    }

    /**
     * @return boolean
     */
    private boolean handleCsrfEnabled() {
        return isSupportEnabled();
    }

    /**
     * @return boolean
     */
    private boolean handleHttpMethods(HttpServletRequest request) {

        if (Objects.isNull(allowedMethods)) {
            return false;
        }

        final Set<String> allowedMethodSets = Arrays.stream(allowedMethods.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        return allowedMethodSets.stream()
                .anyMatch(e -> e.equalsIgnoreCase(request.getMethod()));

    }

    /**
     * @return boolean
     */
    private boolean handleHttpUrls(HttpServletRequest request) {

        if (Objects.isNull(allowedUrls)) {
            return false;
        }

        final List<RequestMatcher> requestMatchers = Arrays.stream(allowedUrls.split(","))
                .map(String::trim)
                .map(AntPathRequestMatcher::new)
                .collect(Collectors.toList());

        AndRequestMatcher andRequestMatcher = new AndRequestMatcher(requestMatchers);

        return andRequestMatcher.matches(request);

    }

    public boolean isSupportEnabled() {
        return supportEnabled;
    }

    public void setSupportEnabled(boolean supportEnabled) {
        this.supportEnabled = supportEnabled;
    }

    public void setAllowedMethods(String allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public void setAllowedUrls(String allowedUrls) {
        this.allowedUrls = allowedUrls;
    }

}
