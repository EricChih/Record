/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.filter;


import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.net.HttpHeaders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * <p>初始 Response Header</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/9/21
 */
@Slf4j
public class ResponseHeaderFilter extends OncePerRequestFilter {

    public ResponseHeaderFilter() {
        super();
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain) throws ServletException, IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Black Box Cross-Frame Scripting Protection
        //response.setHeader("Content-Security-Policy", "default-src 'self'");
        //response.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, "script-src 'self'");

        // set nonce value for CSP
        SecureRandom secureRandom = new SecureRandom();
        byte[] random = new byte[16];
        secureRandom.nextBytes(random);
        String nonce = Base64.getEncoder().encodeToString(random);
        request.setAttribute("cspNonce", nonce);
        response.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, "script-src 'self' 'nonce-" + nonce + "' http://localhost:8082; object-src 'self' http://localhost:8082; report-uri /csp-report-endpoint/");


        // HTTP Strict Transport Security (HSTS) For https type
        response.setHeader(HttpHeaders.STRICT_TRANSPORT_SECURITY, "max-age=31536000 ; includeSubDomains; preload");

        // X-Content-Type-Options
        response.setHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS, "nosniff");

        // X-Frame-Options
        response.setHeader(HttpHeaders.X_FRAME_OPTIONS, "DENY");

        // X-XSS-Protection
        response.setHeader(HttpHeaders.X_XSS_PROTECTION, "1; mode=block");

        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader(HttpHeaders.EXPIRES, "0");

        filterChain.doFilter(request, response);

    }

}
