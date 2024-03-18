/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.web.converter;


import java.nio.charset.StandardCharsets;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;


/**
 * <p>定義物件轉換成String的格式與內容</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
public class ResponseBodyStringHttpMessageConverter {

    public ResponseBodyStringHttpMessageConverter() {
        super();
    }

    public HttpMessageConverter<String> createStringHttpMessageConverter() {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        return converter;
    }

}
