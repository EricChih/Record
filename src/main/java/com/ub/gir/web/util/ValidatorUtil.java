/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.util;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;


/**
 * <p>物件內容驗證工具</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
@Slf4j
public abstract class ValidatorUtil {

    private static final HibernateValidatorConfiguration configuration = Validation.byProvider(HibernateValidator.class).configure();

    /**
     * Constructor
     */
    private ValidatorUtil() {
        throw new IllegalStateException("ValidatorUtil utility class");
    }

    public static <T> void validate(T object) {
        validate(object, true, Default.class);
    }

    public static <T> void validate(T object, boolean failFast) {
        validate(object, failFast, Default.class);
    }

    public static <T> void validate(T object, Class<?>... groups) {
        validate(object, true, groups);
    }

    public static <T> void validate(T object, boolean failFast, Class<?>... groups) {

        Validator validator = configuration
                .failFast(failFast)
                .buildValidatorFactory()
                .getValidator();

        Set<ConstraintViolation<T>> violations = validator.validate(object, groups);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

    }

    public static List<String> extractMessage(ConstraintViolationException e) {
        return extractMessage(e.getConstraintViolations());
    }

    public static List<String> extractMessage(Set<? extends ConstraintViolation<?>> constraintViolations) {
        return constraintViolations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
    }

    public static Map<String, String> extractPropertyAndMessage(ConstraintViolationException e) {
        return extractPropertyAndMessage(e.getConstraintViolations());
    }

    public static Map<String, String> extractPropertyAndMessage(Set<? extends ConstraintViolation<?>> constraintViolations) {
        return constraintViolations.stream()
                .collect(Collectors.toMap(k -> k.getPropertyPath().toString(), v -> v.getMessage(), (oldValue, newValue) -> newValue,
                        LinkedHashMap::new));
    }

    public static List<String> extractPropertyAndMessageAsList(ConstraintViolationException e) {
        return extractPropertyAndMessageAsList(e.getConstraintViolations(), " ");
    }

    public static List<String> extractPropertyAndMessageAsList(Set<? extends ConstraintViolation<?>> constraintViolations) {
        return extractPropertyAndMessageAsList(constraintViolations, " ");
    }

    public static List<String> extractPropertyAndMessageAsList(ConstraintViolationException e, String separator) {
        return extractPropertyAndMessageAsList(e.getConstraintViolations(), separator);
    }

    public static List<String> extractPropertyAndMessageAsList(Set<? extends ConstraintViolation<?>> constraintViolations, String separator) {
        return constraintViolations.stream()
                .map(e -> String.join(separator, e.getPropertyPath().toString(), e.getMessage()))
                .collect(Collectors.toList());
    }

}
