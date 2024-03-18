/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security;


import javax.annotation.Resource;

import com.ub.gir.web.plugins.checkmarx.ToolPlugins;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.XContentTypeOptionsHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Slf4j
public class SecurityConfig {

    @Value("${hualiteq.security.enabled:false}")
    private boolean enabled;

    @Value("${management.endpoints.web.base-path:/actuator}")
    private String actuatorPath;

    @Resource
    private LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint;

    @Resource
    private AccessDeniedHandlerImpl accessDeniedHandlerImpl;

    @Resource
    private UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter;

    @Resource
    private LogoutFilter logoutFilter;

    @Resource
    private LogoutFilter logoutFilterForPwdChange;

    @Resource
    private SessionManagementFilter sessionManagementFilter;

    @Resource
    private ConcurrentSessionFilter concurrentSessionFilter;

    @Resource
    private CorsConfigurationSource corsConfigurationSource;

    @Resource
    private RequestMatcher requestMatcher;

    @Resource
    private HttpSessionRequestCache httpSessionRequestCache;

    public SecurityConfig() {
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        handleResourcesChain(http);

        handleHeaderChain(http);

        if (enabled) {
            return enterHandleSecurityProcess(http);
        } else {
            return passHandleSecurityProcess(http);
        }

    }

    private HttpSecurity handleResourcesChain(HttpSecurity http) throws Exception {

        return http.authorizeHttpRequests(authz -> authz
                .antMatchers("/resources/**", "/static/**", "/webjars/**").permitAll()
                .antMatchers("/assets/**").permitAll()
                .antMatchers("/swagger-ui.html, /swagger-ui/**", "/api-docs/**").permitAll()
                .antMatchers(actuatorPath + "/**").permitAll()
        ).authorizeHttpRequests(authz -> authz
                        .mvcMatchers(UrlPathResourceConfig.LOGIN.getPagePath()).permitAll()
//                .mvcMatchers("/perform_login").permitAll()
        ).authorizeHttpRequests(authz -> authz.anyRequest().authenticated());

    }

    private HttpSecurity handleHeaderChain(HttpSecurity http) throws Exception {

//        http.headers(headers -> headers.withObjectPostProcessor(
//                new ObjectPostProcessor<HeaderWriterFilter>() {
//                    public HeaderWriterFilter postProcess(HeaderWriterFilter filter) {
//                        filter.setShouldWriteHeadersEagerly(true);
//                        return filter;
//                    }
//                })
//        );

        // Http Header Policy Protection
        return http.headers(headers -> headers
                // HTTP Strict Transport Security (HSTS) For https type
                .addHeaderWriter(new StaticHeadersWriter("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"))
                //.addHeaderWriter(new HstsHeaderWriter())
                // X-Content-Type-Options
                .addHeaderWriter(new XContentTypeOptionsHeaderWriter())
                // X-Frame-Options
                .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.DENY))
                //.addHeaderWriter(new StaticHeadersWriter("X-Frame-Options", "DENY"))
                // X-XSS-Protection
                .addHeaderWriter(new XXssProtectionHeaderWriter())
                        .contentSecurityPolicy("script-src 'self' http://localhost:8082; object-src 'self' http://localhost:8082; report-uri /csp-report-endpoint/")
        );

    }

    private SecurityFilterChain passHandleSecurityProcess(HttpSecurity http) throws Exception {

        //http.csrf().disable();

        if (ToolPlugins.checkAuthorization("authorization")) {
            http.authorizeRequests().anyRequest().permitAll();
        }

        //http.httpBasic(withDefaults());
        http.httpBasic().disable();

        return http.build();

    }

    private SecurityFilterChain enterHandleSecurityProcess(HttpSecurity http) throws Exception {

        http.cors().configurationSource(corsConfigurationSource);

        http.csrf().requireCsrfProtectionMatcher(requestMatcher);

        http.requestCache().requestCache(httpSessionRequestCache);

        // The web app entry point
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(loginUrlAuthenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandlerImpl)
        );

        // Black Box SameSite Defense
        //http.addFilterBefore(sessionCookieFilter, ChannelProcessingFilter.class);
        //http.addFilterAfter(new SessionCookieFilter(), BasicAuthenticationFilter.class);

        //http.addFilterBefore(new PasswordVerificationFilter(), UsernamePasswordAuthenticationFilter.class);

        // Header
        //http.addFilterBefore(headerWriterFilter, BasicAuthenticationFilter.class);

        // Login
        http.addFilter(usernamePasswordAuthenticationFilter);

        // The UI does a POST on /logout on logout
        http.addFilter(logoutFilter);
        http.addFilter(logoutFilterForPwdChange);

        // Session Management
        http.addFilter(sessionManagementFilter);
        http.addFilter(concurrentSessionFilter);

        return http.build();

    }

}