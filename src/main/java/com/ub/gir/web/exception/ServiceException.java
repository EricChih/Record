/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.exception;


import org.springframework.lang.Nullable;


/**
 * <p>服務異常</p>
 *
 * @author : Seimo_Zhan
 */
public class ServiceException extends BusinessException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public <E extends Enum<E>> ServiceException(@Nullable Enum<E> constant) {
        super(constant);
    }

    public <E extends Enum<E>> ServiceException(@Nullable Enum<E> constant, String message) {
        super(constant, message);
    }

    public <E extends Enum<E>> ServiceException(@Nullable Enum<E> constant, String message, Throwable cause) {
        super(constant, message, cause);
    }

}
