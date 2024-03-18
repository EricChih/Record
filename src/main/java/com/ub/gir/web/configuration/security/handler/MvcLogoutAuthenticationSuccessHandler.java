/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.handler;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
public class MvcLogoutAuthenticationSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    /**
     * Constructor
     */
    public MvcLogoutAuthenticationSuccessHandler() {
        super();
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler#onLogoutSuccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.Authentication)
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        super.onLogoutSuccess(request, response, authentication);
    }

}
