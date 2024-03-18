/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.filter.entrypoint;


import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * <p>匿名用戶訪問無權限資源時的異常</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
public class MvcAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    /**
     * Constructor
     */
    public MvcAuthenticationEntryPoint(final String loginFormUrl) {
        super(loginFormUrl);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint#commence(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.AuthenticationException)
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        if (isAjaxRequest(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "無權限訪問此內容");
        } else {
            redirectStrategy.sendRedirect(request, response, getLoginFormUrl());
        }

    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        final String ajaxFlag = request.getHeader("X-Requested-With");
        return StringUtils.hasText(ajaxFlag) && "XMLHttpRequest".equalsIgnoreCase(ajaxFlag);
    }

}
