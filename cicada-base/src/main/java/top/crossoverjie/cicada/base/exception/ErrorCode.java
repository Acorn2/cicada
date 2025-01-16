package top.crossoverjie.cicada.base.exception;

/**
 * @author hresh
 * @博客 https://juejin.cn/user/2664871918047063
 * @网站 https://www.hreshhao.com/
 * @date 2025/1/15 10:51
 */
/**
 * 统一错误码接口
 */
public interface ErrorCode {
    /**
     * 获取错误码
     */
    String getCode();

    /**
     * 获取错误信息
     */
    String getMessage();

    /**
     * 获取错误类型
     */
    String getType();
}
