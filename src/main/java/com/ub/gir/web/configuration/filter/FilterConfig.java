/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.filter;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.web.filter.CommonsRequestLoggingFilter;


/**
 * <p>定義 Filter 設定列表</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
@Slf4j
public class FilterConfig {

    public static final String IGNORED_CONTENT_TYPE_KEY = "ignoredContentType";

    /**
     * Constructor
     */
    public FilterConfig() {
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    /**
     * must be set logging.level.root=debug
     */
    @Bean
    public CommonsRequestLoggingFilter loggingFilter() {

        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeHeaders(true);
        filter.setIncludeClientInfo(true);
        filter.setIncludePayload(true);
        filter.setIncludeQueryString(true);
        filter.setAfterMessagePrefix("#### CommonsRequestLoggingFilter Request : ");

        return filter;

    }

    /**
     * @return FilterRegistrationBean < ResponseHeaderFilter >
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public FilterRegistrationBean<ResponseHeaderFilter> responseHeaderRegistration() {

        FilterRegistrationBean<ResponseHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ResponseHeaderFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("responseHeaderFilter");
        registration.addUrlPatterns("/*");
        registration.addInitParameter("paramName", "paramValue");
        registration.setEnabled(true);

        return registration;

    }

    /**
     * @return FilterRegistrationBean < SessionCookieFilter >
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public FilterRegistrationBean<SessionCookieFilter> sessionCookieRegistration() {

        FilterRegistrationBean<SessionCookieFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SessionCookieFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.setName("sessionCookieFilter");
        registration.addUrlPatterns("/*");
        registration.addInitParameter("paramName", "paramValue");
        registration.setEnabled(false);

        return registration;

    }

    /**
     * @return FilterRegistrationBean < CachedHttpServletRequestWrapperFilter >
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    public FilterRegistrationBean<CachedHttpServletRequestWrapperFilter> cachedHttpServletRequestWrapperRegistration() {

        FilterRegistrationBean<CachedHttpServletRequestWrapperFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CachedHttpServletRequestWrapperFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        registration.setName("cachedHttpServletRequestWrapperFilter");
        registration.addUrlPatterns("/*");

        List<String> mediaTypes = new ArrayList<>(6);
        mediaTypes.add(MediaType.MULTIPART_FORM_DATA_VALUE);
        mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        mediaTypes.add(MediaType.TEXT_HTML_VALUE);
        mediaTypes.add(new MediaType("text","html", StandardCharsets.UTF_8).toString());
        mediaTypes.add(MediaType.TEXT_PLAIN_VALUE);
        mediaTypes.add(new MediaType("text","plain", StandardCharsets.UTF_8).toString());

        String mediaTypeContent = String.join(",", mediaTypes);

        registration.addInitParameter(IGNORED_CONTENT_TYPE_KEY, mediaTypeContent);
        registration.setEnabled(true);

        return registration;

    }

}