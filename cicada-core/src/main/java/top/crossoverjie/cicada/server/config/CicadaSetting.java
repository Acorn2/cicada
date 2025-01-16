package top.crossoverjie.cicada.server.config;

import top.crossoverjie.cicada.server.bean.CicadaBeanManager;
import top.crossoverjie.cicada.server.config.configuration.ApplicationConfiguration;
import top.crossoverjie.cicada.server.config.holder.ConfigurationHolder;
import top.crossoverjie.cicada.server.config.loader.ConfigurationLoader;
import top.crossoverjie.cicada.server.constant.CicadaConstant;
import top.crossoverjie.cicada.server.exception.GenericException;
import top.crossoverjie.cicada.server.thread.ThreadLocalHolder;

import static top.crossoverjie.cicada.server.constant.CicadaConstant.SystemProperties.APPLICATION_THREAD_MAIN_NAME;
import static top.crossoverjie.cicada.server.constant.CicadaConstant.SystemProperties.LOGO;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/9/10 20:29
 * @since JDK 1.8
 */
public final class CicadaSetting {

    /**
     * @param clazz
     * @param rootPath
     * @throws Exception
     */
    public static void setting(Class<?> clazz, String rootPath) throws Exception {
        // 1. 显示Logo
        showLogo();
        
        // 2. 初始化配置
        initializeConfig(clazz, rootPath);
        
        // 3. 初始化Bean
        CicadaBeanManager.getInstance().initBean(rootPath);
        
    }
    
    private static void initializeConfig(Class<?> clazz, String rootPath) throws Exception {
        // 1. 设置应用基础信息
        ThreadLocalHolder.setLocalTime(System.currentTimeMillis());
        AppConfig.getInstance().setRootPackageName(clazz);
        
        // 2. 加载所有配置
        ConfigurationLoader.loadAll(AppConfig.getInstance().getRootPackageName());
        
        // 3. 设置应用配置
        setAppConfig(rootPath);
    }
    
    private static void setAppConfig(String rootPath) {
        ApplicationConfiguration appConfig = 
            (ApplicationConfiguration) ConfigurationHolder.getConfiguration(ApplicationConfiguration.class);
            
        // 设置根路径
        rootPath = (rootPath != null) ? rootPath : appConfig.get(CicadaConstant.ROOT_PATH);
        if (rootPath == null) {
            throw new GenericException("No [cicada.root.path] exists");
        }
        AppConfig.getInstance().setRootPath(rootPath);
        
        // 设置端口
        String port = appConfig.get(CicadaConstant.CICADA_PORT);
        if (port == null) {
            throw new GenericException("No [cicada.port] exists");
        }
        AppConfig.getInstance().setPort(Integer.parseInt(port));
    }
    
    private static void showLogo() {
        System.out.println(LOGO);
        Thread.currentThread().setName(APPLICATION_THREAD_MAIN_NAME);
    }
}
