/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.security.filter.login;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ub.gir.web.util.JsonUtil;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;


/**
 * <p>登入帳號與密碼的處理</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/4/11
 */
@Slf4j
public class BaseLoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private String targetContent;

    private boolean postOnly = true;

    public BaseLoginAuthenticationFilter() {
        super();
    }

    public BaseLoginAuthenticationFilter(String defaultFilterProcessesUrl) {
        this(new AntPathRequestMatcher(defaultFilterProcessesUrl));
    }

    public BaseLoginAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super.setRequiresAuthenticationRequestMatcher(requiresAuthenticationRequestMatcher);
    }

    protected BaseLoginAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager) {
        super.setFilterProcessesUrl(defaultFilterProcessesUrl);
        super.setAuthenticationManager(authenticationManager);
    }

    protected BaseLoginAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher, AuthenticationManager authenticationManager) {
        super.setRequiresAuthenticationRequestMatcher(requiresAuthenticationRequestMatcher);
        super.setAuthenticationManager(authenticationManager);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter#attemptAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        if (postOnly && !request.getMethod().equalsIgnoreCase(HttpMethod.POST.name())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        final String contentType = request.getContentType();

        if (StringUtils.hasText(contentType)) {

            if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(contentType)) {

                Map<String, String> contentWrapper = null;

                try {
                    contentWrapper = obtainContentWrapper(request);
                } catch (IOException e) {
                    log.error("Data stream error : {}", e.getMessage(), e);
                }

                //matchContentWrapper(contentWrapper, targetContent);

            }

        }

        response.addHeader("X-Frame-Options", "DENY");

        return super.attemptAuthentication(request, response);

    }

    @Nullable
    protected Map<String, String> obtainContentWrapper(HttpServletRequest request) throws IOException {

        InputStream inputStream = request.getInputStream();
        String contentBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        return JsonUtil.convertJsonToTypeRef(contentBody, new TypeReference<Map<String, String>>() {
        });

    }

    @Nullable
    protected Map<String, String> matchContentWrapper(Map<String, String> sourceContent, String targetContent) {

        if (!StringUtils.hasText(targetContent)) {
            Assert.hasText(targetContent, "targetContent parameter must not be empty or null");
        }

        final List<String> list = Arrays.asList(targetContent.split(","));

        return sourceContent.entrySet().stream()
                .filter(entry -> list.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

    @Nullable
    protected String obtainUsernameMap(Map<String, String> sourceContent) {
        return sourceContent.get(getUsernameParameter());
    }

    @Nullable
    protected String obtainPasswordMap(Map<String, String> sourceContent) {
        return sourceContent.get(getPasswordParameter());
    }

    @Nullable
    public void setTargetContent(String targetContent) {
        this.targetContent = targetContent;
    }

    @Override
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
        super.setPostOnly(postOnly);
    }

    protected void setHeadersProtection(HttpServletResponse response) {
        response.addHeader("X-Frame-Options", "DENY");
    }

}
