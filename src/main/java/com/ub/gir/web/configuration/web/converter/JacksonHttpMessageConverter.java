/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.web.converter;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;


/**
 * <p>定義物件轉換成Json的格式與內容</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
public class JacksonHttpMessageConverter {

    public JacksonHttpMessageConverter() {
        super();
    }

    public HttpMessageConverter<Object> createJacksonHttpMessageConverter() {

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        jackson2HttpMessageConverter.setObjectMapper(objectMapper);

        return jackson2HttpMessageConverter;

    }

}
