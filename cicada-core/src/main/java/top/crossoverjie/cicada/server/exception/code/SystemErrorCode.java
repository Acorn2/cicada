package top.crossoverjie.cicada.server.exception.code;

/**
 * @author hresh
 * @博客 https://juejin.cn/user/2664871918047063
 * @网站 https://www.hreshhao.com/
 * @date 2025/1/15 10:51
 */

import top.crossoverjie.cicada.base.exception.ErrorCode;

/**
 * 系统错误码
 */
public enum SystemErrorCode implements ErrorCode {

    /** duplicate ioc impl*/
    DUPLICATE_IOC("8000", "Duplicate ioc impl error"),
    
    /** empty of package */
    NULL_PACKAGE("8001", "Your main class is empty of package"),

    SERVER_ERROR("500", "Server error"),
    
    /** IllegalArgumentException */
    ILLEGAL_PARAMETER("8002", "IllegalArgumentException: You can only have two parameters at most by using @CicadaRoute!");
    
    private final String code;
    private final String message;
    
    SystemErrorCode(String code, String message) {
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
        return "SYSTEM";
    }
}
