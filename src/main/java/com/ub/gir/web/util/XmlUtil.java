/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.util;


import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


/**
 * <p>XML 轉換工具</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
public abstract class XmlUtil {

    private XmlUtil() {
        throw new IllegalStateException("XmlUtil utility class");
    }

    @SuppressWarnings ( "unchecked" )
    public static <T> T xmlStringToOject( String xmlString, Class<T> clazz ) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(clazz);

        Unmarshaller unmarshaller = context.createUnmarshaller();

        Reader reader = new StringReader(xmlString);

        return (T) unmarshaller.unmarshal(reader);

    }

}
