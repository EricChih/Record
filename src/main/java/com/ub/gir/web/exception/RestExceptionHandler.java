package com.ub.gir.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author elliot
 * @version 1.0
 * @date 2023/4/6
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class RestExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(
            EntityNotFoundException ex) {
        log.error("Error: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.RESOURCE_NOT_FOUND.toObject());
    }

    @ExceptionHandler(javassist.NotFoundException.class)
    protected ResponseEntity<Object> handleNotFound(
            javassist.NotFoundException ex) {
        log.error("Error: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.RESOURCE_NOT_FOUND.toObjectWithPrompt(ex.getLocalizedMessage()));
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    protected ResponseEntity<Object> handleEmptyResultDataAccessException(
            EmptyResultDataAccessException ex) {
        log.error("Error: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.RELATION_DATA_NOT_EXIST.toObject());
    }

    @ExceptionHandler(ParseException.class)
    protected ResponseEntity<Object> handleParseException(
            ParseException ex) {
        log.error("Error: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.REQUEST_PARSE_DATA_ERROR.toObject());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {
        String duplicateKeyMessage = "insert duplicate key";
        String conflictedForeignKey = "INSERT statement conflicted with the FOREIGN KEY constraint";
        String message = ex.getCause().getCause().getMessage();
        log.error("Error: ", ex);
        if (message.contains(conflictedForeignKey)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorMessages.COULD_NOT_CREATE_REQUEST.toObject());
        }
        if (message.contains(duplicateKeyMessage)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.RECORD_ALREADY_EXISTS.toObject());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.COULD_NOT_UPDATE_RESOURCE.toObject());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    protected ResponseEntity<Object> handleDuplicateKeyException(
            DuplicateKeyException ex) {
        log.error("Error: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.RECORD_ALREADY_EXISTS.toObject());
    }

    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<Object> handleNullPointerException(
            NullPointerException ex) {
        log.error("Error: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.NULL_POINTER_EXCEPTION.toObject());
    }

    @ExceptionHandler(NonUniqueResultException.class)
    protected ResponseEntity<Object> handleNonUniqueResultException(
            NonUniqueResultException ex) {
        log.error("Error: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.RECORD_ALREADY_EXISTS.toObject());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    protected ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException exception,
                                                           HttpServletRequest request, HttpServletResponse response) {
        log.error("Error: ", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.RESOURCE_NOT_FOUND.toObject());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleServerException(Exception exception,
                                                           HttpServletRequest request, HttpServletResponse response) {
        String commaSeparateFailed = "was expecting comma to separate Array entries";
        String message = exception.getLocalizedMessage();
        log.error("Error: ", exception);
        if (message.contains(commaSeparateFailed)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorMessages.REQUEST_PARSE_DATA_ERROR.toObject());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.INTERNAL_SERVER_ERROR.toObject());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<Object> handleMaxUploadSizeException(MaxUploadSizeExceededException exception,
                                                                  HttpServletRequest request,
                                                                  HttpServletResponse response) {
        log.error("Error: ", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessages.REQUEST_MAX_UPLOAD_SIZE_EXCEED.toObject());
    }

    @ExceptionHandler(IOException.class)
    protected ResponseEntity<Object> handleIOException(IOException ioException) {
        log.error("Error: ", ioException);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.RESOURCE_NOT_FOUND.toObject());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        Map<String, Set<String>> fieldErrorWithMessages = bindingResult.getFieldErrors().stream()
                .collect(Collectors.groupingBy(FieldError::getField,
                        Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toSet())));
        log.error("Error: ", ex);
        Object errorMessage = ErrorMessages.INVALID_FIELDS_REQUEST.toObjectByDetail(fieldErrorWithMessages);

        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Error: ", ex);
        ErrorMessages err = ErrorMessages.INVALID_FIELDS_REQUEST;
        err.setErrorMessage(ex.getMessage());
        return ResponseEntity.badRequest().body(err.toObject());
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<?> handleServiceException(ServiceException ex) {
        log.error("Error: ", ex);
        ErrorMessages err = ErrorMessages.REQUEST_PARSE_DATA_ERROR;
        return ResponseEntity.badRequest().body(err.toObjectWithCustomMessage(ex.getMessage()));
    }
}