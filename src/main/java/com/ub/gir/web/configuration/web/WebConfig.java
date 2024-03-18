/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.web;


import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.Resource;

import com.ub.gir.web.configuration.file.FileSaveConfig;
import com.ub.gir.web.configuration.interceptor.PwdVerificationDateInterceptor;
import com.ub.gir.web.configuration.web.converter.WebMessageConverter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * <p>web 系統定義</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private WebMessageConverter webMessageConverter;

    @Resource
    private Validator defaultValidator;

    @Resource
    private PwdVerificationDateInterceptor pwdVerificationDateInterceptor;

    public WebConfig() {
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xml", MediaType.APPLICATION_XML);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(webMessageConverter.responseBodyStringHttpMessageConverter());
        converters.add(webMessageConverter.jacksonHttpMessageConverter());
        converters.add(webMessageConverter.xmlHttpMessageConverter());

        converters.stream()
                .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                .findFirst()
                .ifPresent(converter -> ((MappingJackson2HttpMessageConverter) converter)
                        .setDefaultCharset(StandardCharsets.UTF_8));
    }

    @Override
    public Validator getValidator() {
        return defaultValidator;
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.file.annotation.WebMvcConfigurer#addInterceptors(org.springframework.web.servlet.file.annotation.InterceptorRegistry)
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(pwdVerificationDateInterceptor)
                .addPathPatterns("/user/all")
                .addPathPatterns("/rec/all")
                .excludePathPatterns("/login")
                .excludePathPatterns("/pwd/user")
                .excludePathPatterns("/pwd/update")
                .excludePathPatterns("/assets/**");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/play/**")
                .addResourceLocations(ResourceUtils.FILE_URL_PREFIX + FileSaveConfig.getWithinYearFolder());

        registry.addResourceHandler("/hisplay/**")
                .addResourceLocations(ResourceUtils.FILE_URL_PREFIX + FileSaveConfig.getMoreThanYearFolder());

        registry.addResourceHandler("/keepplay/**")
                .addResourceLocations(ResourceUtils.FILE_URL_PREFIX + FileSaveConfig.getForeverSaveFolder());

        registry.addResourceHandler("/static/**")
                .addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/static/");

        registry.addResourceHandler("favicon.ico")
                .addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/static/");

        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/META-INF/resources/webjars/");
    }
}