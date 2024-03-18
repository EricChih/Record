/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.handler;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.util.StringUtils;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
public class MvcAccessDeniedHandler extends AccessDeniedHandlerImpl {

    /**
     * Constructor
     */
    public MvcAccessDeniedHandler() {
        super();
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.access.AccessDeniedHandlerImpl#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.access.AccessDeniedException)
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        if (isAjaxRequest(request)) {
            response.sendError(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase());
        } else {
            super.handle(request, response, accessDeniedException);
        }

    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        final String ajaxFlag = request.getHeader("X-Requested-With");
        return StringUtils.hasText(ajaxFlag) && "XMLHttpRequest".equalsIgnoreCase(ajaxFlag);
    }

}
