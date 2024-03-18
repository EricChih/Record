/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.filter;


import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ub.gir.web.configuration.request.CachedHttpServletRequestWrapper;
import com.ub.gir.web.util.DataUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import static com.ub.gir.web.configuration.filter.FilterConfig.IGNORED_CONTENT_TYPE_KEY;


/**
 * <p>將傳入的 Request 包裝成 Wrap</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/09/22
 */
@Slf4j
public class CachedHttpServletRequestWrapperFilter extends OncePerRequestFilter {

    private Set<String> contentTypePatterns;

    /**
     * Constructor
     */
    public CachedHttpServletRequestWrapperFilter() {
        super();
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.springframework.web.filter.OncePerRequestFilter#initFilterBean() throws ServletException
     */
    @Override
    protected void initFilterBean() throws ServletException {

        FilterConfig filterConfig = super.getFilterConfig();

        Objects.requireNonNull(filterConfig);

        final String ignoredContentType = filterConfig.getInitParameter(IGNORED_CONTENT_TYPE_KEY);

        if (StringUtils.hasText(ignoredContentType)) {
            contentTypePatterns = Arrays.stream(ignoredContentType.split(","))
                    .collect(Collectors.toSet());
        }

    }

    /* (non-Javadoc)
     * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain) throws ServletException, IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {

            final String contentType = request.getContentType();

            String logDetails = DataUtil.preventLogForging(contentType);
            log.info("Content-Type : {}", logDetails);

            final String cleanContentType = parseContentType(contentType);

            // for form-data and urlencoded data
            if ((!StringUtils.hasText(contentType)) || matchContentType(cleanContentType)) {
                filterChain.doFilter(request, response);
            } else {

                CachedHttpServletRequestWrapper requestWrapper = new CachedHttpServletRequestWrapper(request);

                // for json
                filterChain.doFilter(requestWrapper, responseWrapper);

            }

        } finally {
            responseWrapper.copyBodyToResponse();
        }

    }

    private String parseContentType(String contentType) throws ServletException {

        if (StringUtils.hasText(contentType)) {

//            if (contentType.contains(";")) {
//                return contentType.substring(0, contentType.indexOf(";"));
//            }

            return String.valueOf(MediaType.parseMediaType(contentType));

        }

        //throw new ServletException("Content-Type cannot be empty !");

        return contentType;

    }

    private boolean matchContentType(String contentType) {
        return contentTypePatterns.stream()
                .anyMatch(e -> e.equalsIgnoreCase(contentType));
    }

}
