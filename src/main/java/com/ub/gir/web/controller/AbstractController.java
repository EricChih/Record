/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.controller;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * <p>視圖控制層的基底類別</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
public abstract class AbstractController {

    @Resource
    protected HttpServletRequest request;

    @Resource
    protected HttpServletResponse response;

    @Resource
    protected HttpSession session;

    protected String getCurrentPath() {
        return request.getServletPath();
    }

    protected String getCurrentURI() {
        return request.getRequestURI();
    }

    protected Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
