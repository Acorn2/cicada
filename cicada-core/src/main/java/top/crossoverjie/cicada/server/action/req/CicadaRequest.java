package top.crossoverjie.cicada.server.action.req;

import top.crossoverjie.cicada.server.action.cookie.HttpCookie;
import top.crossoverjie.cicada.server.action.param.Param;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/10/5 00:40
 * @since JDK 1.8
 */
public interface CicadaRequest {

    /**
     * get request method
     * @return
     */
    String getMethod() ;

    /**
     * get request url
     * @return
     */
    String getUrl() ;

    /**
     * get cookie by key
     * @param key
     * @return return cookie by key
     */
    HttpCookie getCookie(String key) ;

    /**
     * 获取URL查询参数
     */
    Param getQueryParameters();

    /**
     * 获取请求体参数
     */
    Param getBodyParameters();

    /**
     * 获取所有参数（查询参数 + 请求体参数）
     */
    Param getAllParameters();

}
