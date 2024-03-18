/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.handler;


import com.ub.gir.web.configuration.security.UrlPathResourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Slf4j
public class MvcLoginAuthenticationFailureHandler extends ExceptionMappingAuthenticationFailureHandler {

    /**
     * Constructor
     */
    public MvcLoginAuthenticationFailureHandler() {
        super();
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler#onAuthenticationFailure(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.AuthenticationException)
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("login failed exception : {}", exception.toString());

        if (exception instanceof UsernameNotFoundException || exception instanceof BadCredentialsException ) {
            String defaultFailureUrl = UrlPathResourceConfig.FAILED.getPagePath();
            if (exception.getMessage().matches("\\d+"))
                defaultFailureUrl += exception.getMessage();

            super.setDefaultFailureUrl(defaultFailureUrl);
        } else if (exception instanceof LockedException) {
            super.setDefaultFailureUrl(UrlPathResourceConfig.LOCKED.getPagePath() + exception.getMessage());
        } else if(exception instanceof DisabledException){
            super.setDefaultFailureUrl(UrlPathResourceConfig.DISABLED.getPagePath());
        }

        super.onAuthenticationFailure(request, response, exception);

    }

}
