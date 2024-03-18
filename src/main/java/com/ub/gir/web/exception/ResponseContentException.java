/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.exception;


import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;


/**
 * <p>例外訊息的包裝物件</p>
 *
 * @author : seimo_zhan
 */
public final class ResponseContentException extends ResponseStatusException {

    private Class<?> clazz;

    private Enum<?> constant;

    public <T> ResponseContentException(HttpStatus status, Class<T> clazz) {
        super(status);
        this.clazz = clazz;
    }

    public <E extends Enum<E>> ResponseContentException(HttpStatus status, Enum<E> constant) {
        super(status);
        this.constant = constant;
    }

    public <T> ResponseContentException(HttpStatus status, Class<T> clazz, @Nullable String reason) {
        super(status, reason);
        this.clazz = clazz;
    }

    public <E extends Enum<E>> ResponseContentException(HttpStatus status, Enum<E> constant, @Nullable String reason) {
        super(status, reason);
        this.constant = constant;
    }

    public <T> ResponseContentException(HttpStatus status, Class<T> clazz, @Nullable Throwable cause) {
        super(status, null, cause);
        this.clazz = clazz;
    }

    public <E extends Enum<E>> ResponseContentException(HttpStatus status, Enum<E> constant, @Nullable Throwable cause) {
        super(status, null, cause);
        this.constant = constant;
    }

    public <T> ResponseContentException(HttpStatus status, Class<T> clazz, @Nullable String reason, @Nullable Throwable cause) {
        super(status, reason, cause);
        this.clazz = clazz;
    }

    public <E extends Enum<E>> ResponseContentException(HttpStatus status, Enum<E> constant, @Nullable String reason, @Nullable Throwable cause) {
        super(status, reason, cause);
        this.constant = constant;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Enum<?> getConstant() {
        return constant;
    }

    @Override
    public String getMessage() {
        String msg = getReason();
        return NestedExceptionUtils.buildMessage(msg, getCause());
    }

}
