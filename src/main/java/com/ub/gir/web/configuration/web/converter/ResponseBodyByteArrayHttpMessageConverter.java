/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.web.converter;


import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;


/**
 * <p>定義物件轉換成Byte Array的格式與內容</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
public class ResponseBodyByteArrayHttpMessageConverter {

    public ResponseBodyByteArrayHttpMessageConverter() {
        super();
    }

    public HttpMessageConverter<byte[]> createByteArrayHttpMessageConverter() {
        ByteArrayHttpMessageConverter converter = new ByteArrayHttpMessageConverter();
        return converter;
    }

}
