package com.ub.gir.web.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author elliot
 * @version 1.0
 * @date 2023/4/6
 */
public enum ErrorMessages {
    // 錯誤類別的列舉
    MISSING_REQUIRED_FIELD(40001, "MISSING_REQUIRED_FIELD"),
    INVALID_FIELDS_REQUEST(40002, "INVALID_FIELDS_REQUEST"),
    RECORD_ALREADY_EXISTS(40003, "RECORD_ALREADY_EXISTS"),
    COULD_NOT_UPDATE_RESOURCE(40004, "COULD_NOT_UPDATE_RESOURCE"),
    COULD_NOT_DELETE_RECORD(40005, "COULD_NOT_DELETE_RECORD"),
    COULD_NOT_CREATE_REQUEST(40006, "COULD_NOT_CREATE_REQUEST"),
    DATA_EXIST_RELATED(40007, "DATA_EXIST_RELATED"),
    RECORD_NOT_EXIST(40008, "RECORD_NOT_EXIST"),
    RELATION_DATA_NOT_EXIST(40009, "RELATION_DATA_NOT_EXIST"),
    REQUEST_DUPLICATE_DATA(40010, "REQUEST_DUPLICATE_DATA"),
    REQUEST_MAX_UPLOAD_SIZE_EXCEED(40011, "MAX_UPLOAD_SIZE_EXCEED"),
    REQUEST_PARSE_DATA_ERROR(40012, "PARSE_DATA_ERROR"),
    RESOURCE_BEING_USED(40013, "RESOURCE_BEING_USED"),

    AUTHENTICATION_FAILED(40101, "AUTHENTICATION_FAILED"),

    FORBIDDEN(40301, "FORBIDDEN"),
    FORBIDDEN_API_NO_AUTH(40302, "FORBIDDEN_API_NO_AUTH"),

    RESOURCE_NOT_FOUND(40401, "RESOURCE_NOT_FOUND"),

    HTTP_REQUEST_METHOD_NOT_SUPPORTED(40501, "HTTP_REQUEST_METHOD_NOT_SUPPORTED"),

    INTERNAL_SERVER_ERROR(50001, "INTERNAL_SERVER_ERROR"),
    NULL_POINTER_EXCEPTION(50002, "NULL_POINTER_EXCEPTION");

    private int code;
    private String errorMessage;

    /**
     * @param errorCode    錯誤碼
     * @param errorMessage 錯誤訊息
     */
    ErrorMessages(int errorCode, String errorMessage) {
        this.code = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 取得 Error code
     *
     * @return 錯誤碼
     */
    public int getCode() {
        return code;
    }

    /**
     * 設定 Error code
     *
     * @param code 錯誤碼
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 轉為物件 {ErrorCode:100, ErrorMessage:""}
     *
     * @return 錯誤訊息物件
     */
    public Object toObject() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("ErrorCode", getCode());
        hashMap.put("ErrorMessage", getErrorMessage());
        return hashMap;
    }

    /**
     * 轉為物件 {ErrorCode:100, ErrorMessage:"", ErrorDetail:""}
     *
     * @param fieldErrorWithMessages 欄位錯誤訊息
     * @return 錯誤訊息物件
     */
    public Object toObjectByDetail(
        Map<String, Set<String>> fieldErrorWithMessages) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("ErrorCode", getCode());
        hashMap.put("ErrorMessage", getErrorMessage());
        hashMap.put("ErrorDetail", fieldErrorWithMessages);
        return hashMap;
    }

    /**
     * 轉為物件 {ErrorCode:100, ErrorMessage:"", ErrorPrompt:""}
     *
     * @param errorPrompt 錯誤訊息
     * @return 錯誤訊息物件
     */
    public Object toObjectWithPrompt(Object errorPrompt) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("ErrorCode", getCode());
        hashMap.put("ErrorMessage", getErrorMessage());
        hashMap.put("ErrorPrompt", errorPrompt);
        return hashMap;
    }

    public Object toObjectWithCustomMessage(String customMessage) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("ErrorCode", getCode());
        hashMap.put("ErrorMessage", customMessage);
        return hashMap;
    }
}