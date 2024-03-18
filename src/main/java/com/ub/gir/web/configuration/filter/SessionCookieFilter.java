/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.filter;


import java.io.IOException;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ub.gir.web.configuration.security.SecurityAttributes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.filter.GenericFilterBean;


/**
 * <p>初始 Response Cookie</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/8/30
 */
@Slf4j
public class SessionCookieFilter extends GenericFilterBean {

    public SessionCookieFilter() {
        super();
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain) throws IOException, ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        final Cookie[] cookies = request.getCookies();

        if (cookies!=null) {

            Cookie cookie = Arrays.stream(cookies)
                    .filter(x -> x.getName().equals(SecurityAttributes.SESSION_KEY))
                    .findFirst()
                    .orElse(null);

            if (cookie!=null) {

                String cookieName = cookie.getName();
                String cookieValue = cookie.getValue();

                // SameSite = Strict,Lax,None
                ResponseCookie responseCookie =
                        ResponseCookie.from(cookieName, cookieValue)
                                .httpOnly(true)
                                .secure(false)
                                .sameSite(org.springframework.boot.web.server.Cookie.SameSite.STRICT.attributeValue())
                                .build();

                //response.addCookie(cookie);

                // Set-Cookie
                //response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
                //response.setHeader(HttpHeaders.SET_COOKIE, "no-store, no-cache, must-revalidate");
                response.setHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

            }

        }

        filterChain.doFilter(request, response);

    }

}
