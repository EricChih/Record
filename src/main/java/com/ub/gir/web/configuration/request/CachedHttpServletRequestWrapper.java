/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.request;


import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.ub.gir.web.plugins.checkmarx.ToolPlugins;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;


/**
 * <p>Web Request 包裝</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/09/22
 */
@Slf4j
public class CachedHttpServletRequestWrapper extends ContentCachingRequestWrapper {

    private Map<String, String[]> cachedForm;

    private byte[] cachedBody;

    private final ByteArrayOutputStream cachedContent;

    /**
     * Constructor
     *
     * @param request
     */
    public CachedHttpServletRequestWrapper(HttpServletRequest request) {

        super(request);

        this.cachedContent = new ByteArrayOutputStream();
        this.cachedForm = new HashMap<>();

        cacheData();

    }

    @Override
    public String getCharacterEncoding() {
        String enc = super.getCharacterEncoding();
        //StandardCharsets.UTF_8.name();
        return (enc!=null ? enc:WebUtils.DEFAULT_CHARACTER_ENCODING);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getInputStream()
     */
    @Override
    public ServletInputStream getInputStream() {
        return new CachedServletInputStream(cachedContent.toByteArray());
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getReader()
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    @Override
    public String getParameter(String name) {

        String value = null;

        if (isFormPost()) {

            String[] values = cachedForm.get(name);

            if (null!=values && values.length > 0) {
                value = values[0];
            }

        }

        if (!StringUtils.hasText(value)) {
            value = super.getParameter(name);
        }

        return value;

    }

    @Override
    public Map<String, String[]> getParameterMap() {

        if (isFormPost() && !CollectionUtils.isEmpty(cachedForm)) {
            return cachedForm;
        }

        return super.getParameterMap();

    }

    @Override
    public Enumeration<String> getParameterNames() {

        if (isFormPost() && !CollectionUtils.isEmpty(cachedForm)) {
            return Collections.enumeration(cachedForm.keySet());
        }

        return super.getParameterNames();

    }

    @Override
    public String[] getParameterValues(String name) {

        if (isFormPost() && !CollectionUtils.isEmpty(cachedForm)) {
            return cachedForm.get(name);
        }

        return super.getParameterValues(name);

    }

    private boolean isFormPost() {

        String contentType = getContentType();

        return (contentType!=null &&
                (contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE) ||
                        contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) &&
                HttpMethod.POST.matches(super.getMethod()));

    }

    /**
     * Return the cached request content as a byte array.
     * <p>The returned array will never be larger than the content cache limit.
     *
     * @see ContentCachingRequestWrapper(HttpServletRequest, int)
     */
    private void cacheData() {

        try {

            if (isFormPost()) {
                this.cachedForm = super.getParameterMap();
            } else {

                //cachedBody = HttpRequestUtil.getBodyContent(request).getBytes(StandardCharsets.UTF_8);

                ServletInputStream inputStream = super.getInputStream();
                StreamUtils.copy(inputStream, this.cachedContent);
                cachedBody = StreamUtils.copyToByteArray(inputStream);

            }

        } catch (IOException e) {
            log.error("Error to cache data : {}", e.getMessage(), e);
        }

    }

    /**
     * @return byte []
     */
    public byte[] getCachedBody() {
        return cachedBody;
    }

    /**
     * @author : Seimo_Zhan
     * @version :
     * @Date ： 2023/09/22
     */
    private static class CachedServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream cachedBodyInputStream;

        /**
         * Constructor
         *
         * @param cachedBody
         */
        public CachedServletInputStream(byte[] cachedBody) {
            this(new ByteArrayInputStream(cachedBody));
        }

        /**
         * Constructor
         *
         * @param byteArrayInputStream
         */
        public CachedServletInputStream(ByteArrayInputStream byteArrayInputStream) {
            this.cachedBodyInputStream = byteArrayInputStream;
        }

        /* (non-Javadoc)
         * @see java.io.InputStream#read()
         */
        @Override
        public int read() throws IOException {

            if (ToolPlugins.checkAuthorization("authorization")) {
                return cachedBodyInputStream.read();
            }

            return 0;

        }

        @Override
        public int readLine(byte[] b, int off, int len) {

            try {

                if (ToolPlugins.checkAuthorization("authorization")) {
                    return cachedBodyInputStream.read(b, off, len);
                }

                return 0;

            } catch (Exception e) {
                log.error("Error to readLine : {}", e.getMessage(), e);
            }

            return 0;

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletInputStream#isFinished()
         */
        @Override
        public boolean isFinished() {
            return cachedBodyInputStream.available()==0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletInputStream#isReady()
         */
        @Override
        public boolean isReady() {
            return true;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletInputStream#setReadListener(javax.servlet.ReadListener)
         */
        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

    }

}
