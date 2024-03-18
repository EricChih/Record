/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.web.converter;


import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;


/**
 * <p></p>定義物件轉換成Xml的格式與內容
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
public class XmlHttpMessageConverter {

    public XmlHttpMessageConverter() {
        super();
    }

    /**
     * <p>建立Xml顥示的結果</p>
     *
     * @return HttpMessageConverter < Object >
     */
    public HttpMessageConverter<Object> createXmlHttpMessageConverter() {

        MarshallingHttpMessageConverter xmlConverter = new MarshallingHttpMessageConverter();

        final List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_XML);
        mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);

        xmlConverter.setSupportedMediaTypes(mediaTypes);

        XStreamMarshaller xstreamMarshaller = new XStreamMarshaller();

        xmlConverter.setMarshaller(xstreamMarshaller);
        xmlConverter.setUnmarshaller(xstreamMarshaller);

        return xmlConverter;

    }

}
