/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.interceptor;


import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ub.gir.web.configuration.security.SecurityHelper;
import com.ub.gir.web.configuration.security.UrlPathResourceConfig;
import com.ub.gir.web.entity.db1.master.CfgPersonDB1Master;
import com.ub.gir.web.repository.db1.master.CfgPersonDB1MasterRepository;
import com.ub.gir.web.service.PwdService;

import javassist.NotFoundException;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


/**
 * <p>攔捷密碼變更</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
@Component
public class PwdVerificationDateInterceptor extends HandlerInterceptorAdapter {

    @Resource
    private CfgPersonDB1MasterRepository CfgPersonRepository;

    @Resource
    private PwdService pwdService;

    private static final UrlPathResourceConfig PWD_DEFAULT = UrlPathResourceConfig.PWD_DEFAULT;

    private static final UrlPathResourceConfig PWD_MISMATCH = UrlPathResourceConfig.PWD_MISMATCH;

    private static final UrlPathResourceConfig PWD_EXPIRED = UrlPathResourceConfig.PWD_EXPIRED;

    private static final UrlPathResourceConfig PWD_EXPIRING = UrlPathResourceConfig.PWD_EXPIRING;

    private static final UrlPathResourceConfig PWD_AS_DEFAULT_ERROR = UrlPathResourceConfig.PWD_AS_DEFAULT_ERROR;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        response.addHeader("Strict-Transport-Security", "max-age=315360000; includeSubDomains; preload");

        UserDetails userDetails = (UserDetails) SecurityHelper.getPrincipal();
        String username = userDetails.getUsername();
        char[] password = Objects.requireNonNull(SecurityHelper.getCurrentCredentials()).toCharArray();

        if (pwdService.isCurrentPasswordAsDefault(username, new String(password))) {
            response.sendRedirect(PWD_AS_DEFAULT_ERROR.getPagePath());
            return false;
        }

        if (pwdService.isValidatePwdRuleFailed(new String(password))) {
            response.sendRedirect(PWD_MISMATCH.getPagePath());
            return false;
        }

        Arrays.fill(password, '0');

        final CfgPersonDB1Master cfgPerson = CfgPersonRepository.findByName(username).orElseThrow(() -> new NotFoundException("查無使用者帳號"));

        if (pwdService.isFirstLogin(cfgPerson)) {
            response.sendRedirect(PWD_DEFAULT.getPagePath());
            return false;
        }

        if (pwdService.isRequiredChangePwd(cfgPerson)) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("<script>confirm('" + PWD_EXPIRED.getPageInformation() + "'); </script>");
            response.sendRedirect(PWD_EXPIRED.getPagePath());
            return false;
        }

        Map<Boolean, String> passwordExpiresMap = pwdService.checkReminderBeforePasswordExpires(cfgPerson);

        if (passwordExpiresMap.keySet().iterator().next()) {
            String expirationDays = passwordExpiresMap.values().iterator().next();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("<script>confirm('" + PWD_EXPIRING.getPageInformation().replace("{0}", expirationDays) + "'); </script>");
        }

        return true;

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        response.addHeader("Strict-Transport-Security", "max-age=315360000; includeSubDomains; preload");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    }

}
