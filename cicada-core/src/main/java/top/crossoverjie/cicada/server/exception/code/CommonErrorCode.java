package top.crossoverjie.cicada.server.exception.code;

import top.crossoverjie.cicada.base.exception.ErrorCode;

/**
 * 通用错误码
 */
public enum CommonErrorCode implements ErrorCode {
    
    /** success */
    SUCCESS("9000", "success"),
    
    /** request Error */
    REQUEST_ERROR("7000", "Request Error"),
    
    /** 404 */
    NOT_FOUND("404", "Need to declare a method by using @CicadaRoute!");
    
    private final String code;
    private final String message;
    
    CommonErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    @Override
    public String getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public String getType() {
        return "COMMON";
    }
} 