/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.filter.login;


import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ub.gir.web.configuration.security.SecurityAttributes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;


/**
 * <p>Web登入方式的帳號與密碼處理</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Slf4j
public class MvcLoginAuthenticationFilter extends BaseLoginAuthenticationFilter {

    public static final String SPRING_SECURITY_FORM_CSRF_KEY = SecurityAttributes.CSRF_PARAMETER_KEY;

    private String csrfParameter = "csrf";

    /**
     * Constructor
     */
    public MvcLoginAuthenticationFilter() {
        super();
    }

    public MvcLoginAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter#attemptAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        response.addHeader("X-Frame-Options", "DENY");
        return super.attemptAuthentication(request, response);
    }

    @Nullable
    protected String obtainCsrf(HttpServletRequest request) {
        return request.getParameter(this.csrfParameter);
    }

    public final String getCsrfParameter() {
        return this.csrfParameter;
    }

}
