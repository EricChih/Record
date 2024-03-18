/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.exception;


import org.springframework.lang.Nullable;


/**
 * <p>業務異常</p>
 *
 * @author : Seimo_Zhan
 */
public class BusinessException extends RuntimeException {

    @Nullable
    private Enum<?> constant;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public <E extends Enum<E>> BusinessException(@Nullable Enum<E> constant) {
        this.constant = constant;
    }

    public <E extends Enum<E>> BusinessException(@Nullable Enum<E> constant, String message) {
        super(message);
        this.constant = constant;
    }

    public <E extends Enum<E>> BusinessException(@Nullable Enum<E> constant, String message, Throwable cause) {
        super(message, cause);
        this.constant = constant;
    }

    public Enum<?> getConstant() {
        return constant;
    }

}
