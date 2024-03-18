/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.web.converter;


import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;


/**
 * <p>組合所有Message Converter可以取得的類別</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
@Component
public class WebMessageConverter {

    public WebMessageConverter() {
        super();
    }

    public HttpMessageConverter<Object> jacksonHttpMessageConverter() {
        final JacksonHttpMessageConverter jacksonHttpMessageConverter = new JacksonHttpMessageConverter();
        return jacksonHttpMessageConverter.createJacksonHttpMessageConverter();
    }

    public HttpMessageConverter<Object> xmlHttpMessageConverter() {
        final XmlHttpMessageConverter xmlHttpMessageConverter = new XmlHttpMessageConverter();
        return xmlHttpMessageConverter.createXmlHttpMessageConverter();
    }

    public HttpMessageConverter<byte[]> responseBodyByteArrayHttpMessageConverter() {
        final ResponseBodyByteArrayHttpMessageConverter responseBodyByteArrayHttpMessageConverter = new ResponseBodyByteArrayHttpMessageConverter();
        return responseBodyByteArrayHttpMessageConverter.createByteArrayHttpMessageConverter();
    }

    public HttpMessageConverter<String> responseBodyStringHttpMessageConverter() {
        final ResponseBodyStringHttpMessageConverter responseBodyStringHttpMessageConverter = new ResponseBodyStringHttpMessageConverter();
        return responseBodyStringHttpMessageConverter.createStringHttpMessageConverter();
    }

}
