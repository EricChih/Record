/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.response;


import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import lombok.extern.slf4j.Slf4j;


/**
 * <p>Web Response 包裝</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/09/22
 */
@Slf4j
public class CachedHttpServletResponseWrapper extends HttpServletResponseWrapper {

    /**
     * Constructor
     */
    public CachedHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#flushBuffer()
     */
    @Override
    public void flushBuffer() throws IOException {
        getResponse().flushBuffer();
    }

}
