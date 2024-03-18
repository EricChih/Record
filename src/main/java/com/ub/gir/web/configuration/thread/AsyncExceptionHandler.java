/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.thread;


import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;


/**
 * <p></p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/4/20
 */
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    /**
     * Constructor
     */
    public AsyncExceptionHandler() {
        super();
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler#handleUncaughtException(java.lang.Throwable, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {

        log.info("Exception Cause - {}", ex.getMessage());
        log.info("Method name - {}", method.getName());

        for (Object param : params) {
            log.info("Parameter value - {}", param);
        }

    }

}
