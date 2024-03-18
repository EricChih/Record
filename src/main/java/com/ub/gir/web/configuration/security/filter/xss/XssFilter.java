package com.ub.gir.web.configuration.security.filter.xss;


import com.ub.gir.web.configuration.security.UrlPathResourceConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String contentType = request.getContentType();
        if(StringUtils.contains(contentType, MediaType.APPLICATION_JSON_VALUE) && StringUtils.contains(path,
                UrlPathResourceConfig.LOGIN.getPagePath())){
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(new XssRequestWrapper(request), response);
    }


}
