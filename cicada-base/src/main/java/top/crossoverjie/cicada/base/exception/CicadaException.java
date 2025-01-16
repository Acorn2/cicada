package top.crossoverjie.cicada.base.exception;


/**
 * Cicada 统一异常基类
 */
public class CicadaException extends RuntimeException {

    private final ErrorCode errorCode;

    public CicadaException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CicadaException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CicadaException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public static boolean isResetByPeer(String msg) {
        if ("Connection reset by peer".equals(msg)) {
            return true;
        }
        return false;
    }

}
