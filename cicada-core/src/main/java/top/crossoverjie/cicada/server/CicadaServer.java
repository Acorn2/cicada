package top.crossoverjie.cicada.server;

import top.crossoverjie.cicada.server.config.CicadaSetting;
import top.crossoverjie.cicada.server.netty.NettyServer;
import top.crossoverjie.cicada.server.scanner.InitializeHandleScanner;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/8/30 12:48
 * @since JDK 1.8
 */
public final class CicadaServer {


    /**
     * Start cicada server by path
     * @param clazz
     * @param path
     * @throws Exception
     */
    public static void start(Class<?> clazz,String path) throws Exception {
        // 1. 基础配置
        CicadaSetting.setting(clazz, path);

        // 2. 执行初始化处理器
        InitializeHandleScanner.executeApplicationHandlers();

        // 3. 启动服务器
        NettyServer.startCicada();
    }


    /**
     * Start the service through the port in the configuration file
     * @param clazz
     * @throws Exception
     */
    public static void start(Class<?> clazz) throws Exception {
        start(clazz,null);
    }

}
