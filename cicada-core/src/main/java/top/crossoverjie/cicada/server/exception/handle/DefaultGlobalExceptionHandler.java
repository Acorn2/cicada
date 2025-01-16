package top.crossoverjie.cicada.server.exception.handle;

import org.slf4j.Logger;
import top.crossoverjie.cicada.base.exception.CicadaException;
import top.crossoverjie.cicada.base.log.LoggerBuilder;
import top.crossoverjie.cicada.server.action.res.ApiResponse;
import top.crossoverjie.cicada.server.context.CicadaContext;
import top.crossoverjie.cicada.server.exception.code.SystemErrorCode;

/**
 * 默认全局异常处理器
 */
public class DefaultGlobalExceptionHandler implements GlobalHandelException {
    
    private static final Logger LOGGER = LoggerBuilder.getLogger(DefaultGlobalExceptionHandler.class);

    @Override
    public void resolveException(CicadaContext context, Exception e) {
        LOGGER.error("System error", e);
        
        ApiResponse response = new ApiResponse();
        
        // 根据异常类型动态处理
        if (e instanceof CicadaException) {
            CicadaException ce = (CicadaException) e;
            response.setCode(ce.getErrorCode().getCode());
            response.setMessage(ce.getMessage());
        } else {
            response.setCode(SystemErrorCode.SERVER_ERROR.getCode());
            response.setMessage(SystemErrorCode.SERVER_ERROR.getMessage());
        }
        
        context.json(response);
    }
} 