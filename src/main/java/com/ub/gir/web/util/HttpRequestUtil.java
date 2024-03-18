/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;


/**
 * <p></p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/2/16
 */
@Slf4j
public class HttpRequestUtil {

    //private static final Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);

    /**
     * Constructor
     */
    private HttpRequestUtil() {
        throw new IllegalStateException("HttpRequestUtil utility class");
    }

    /**
     * @return String
     */
    public static String getBodyContent(HttpServletRequest request) {

        StringBuilder sb = new StringBuilder();

        try (InputStream inputStream = request.getInputStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine())!=null) {
                sb.append(line);
            }

        } catch (IOException e) {
            log.error("Error getBodyContent : {}", e.getMessage(), e);
        }

        return sb.toString();

    }

}
