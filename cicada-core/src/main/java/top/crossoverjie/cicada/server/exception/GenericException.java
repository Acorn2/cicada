package top.crossoverjie.cicada.server.exception;

import top.crossoverjie.cicada.base.exception.CicadaException;
import top.crossoverjie.cicada.server.exception.code.CommonErrorCode;
import top.crossoverjie.cicada.base.exception.ErrorCode;

import java.io.Serializable;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2018/8/25 15:27
 * @since JDK 1.8
 */
public class GenericException extends CicadaException implements Serializable {
    private static final long serialVersionUID = 1L;

    public GenericException(ErrorCode errorCode) {
        super(errorCode);
    }

    public GenericException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public GenericException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public GenericException(String message) {
        super(CommonErrorCode.REQUEST_ERROR, message);
    }

}