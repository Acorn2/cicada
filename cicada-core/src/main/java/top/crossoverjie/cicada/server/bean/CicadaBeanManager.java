package top.crossoverjie.cicada.server.bean;

import org.slf4j.Logger;
import top.crossoverjie.cicada.base.bean.CicadaBeanFactory;
import top.crossoverjie.cicada.base.log.LoggerBuilder;
import top.crossoverjie.cicada.server.exception.handle.DefaultGlobalExceptionHandler;
import top.crossoverjie.cicada.server.exception.handle.GlobalHandelException;
import top.crossoverjie.cicada.server.scanner.ClasLoadersScanner;

import java.util.Map;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/11/14 01:41
 * @since JDK 1.8
 */
/**
 * Bean管理器 - 核心类
 * 职责：
 * 1. 管理所有Bean的生命周期
 * 2. 提供Bean的注册和获取功能
 * 3. 处理全局异常
 */
public final class CicadaBeanManager {
    private final static Logger LOGGER = LoggerBuilder.getLogger(CicadaBeanManager.class);

    private CicadaBeanManager(){
    }

    private static volatile CicadaBeanManager cicadaBeanManager;

    private static CicadaBeanFactory cicadaBeanFactory ;

    private GlobalHandelException handelException = new DefaultGlobalExceptionHandler();

    public static CicadaBeanManager getInstance() {
        if (cicadaBeanManager == null) {
            synchronized (CicadaBeanManager.class) {
                if (cicadaBeanManager == null) {
                    cicadaBeanManager = new CicadaBeanManager();
                }
            }
        }
        return cicadaBeanManager;
    }

    /**
     * 初始化Bean工厂
     * @param packageName 需要扫描的包名
     * 执行步骤：
     * 1. 扫描指定包下的所有@CicadaBean注解的类
     * 2. 通过SPI机制获取Bean工厂实现
     * 3. 实例化并注册所有扫描到的类
     */
    public void initBean(String packageName) throws Exception {
        // 1. 扫描指定包下的所有带有@CicadaBean注解的类
        Map<String, Class<?>> cicadaBean = ClasLoadersScanner.getCicadaBean(packageName);

        // 2. 获取Bean工厂实现（通过SPI机制）
        cicadaBeanFactory = ClasLoadersScanner.getCicadaBeanFactory() ;

        // 3. 将扫描到的类实例化，并注册到Bean工厂中
        for (Map.Entry<String, Class<?>> classEntry : cicadaBean.entrySet()) {
            // 3.1 实例化类
            Object instance = classEntry.getValue().newInstance();
            // 3.2 将实例化后的类注册到Bean工厂中
            cicadaBeanFactory.register(instance) ;

            // 3.3 如果类实现了GlobalHandelException接口，则设置异常处理
            // 特殊处理：注册全局异常处理器
            if (ClasLoadersScanner.isInterface(classEntry.getValue(), GlobalHandelException.class)){
                GlobalHandelException exception = (GlobalHandelException) instance;
                CicadaBeanManager.getInstance().exceptionHandle(exception);
            }
        }

    }


    /**
     * get route bean
     * @param name
     * @return
     * @throws Exception
     */
    public Object getBean(String name) {
        try {
            return cicadaBeanFactory.getBean(name) ;
        } catch (Exception e) {
            LOGGER.error("get bean error",e);
        }
        return null ;
    }

    /**
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T getBean(Class<T> clazz) {
        try {
            return cicadaBeanFactory.getBean(clazz) ;
        } catch (Exception e) {
            LOGGER.error("get bean error",e);
        }
        return null ;
    }

    /**
     * release all beans
     */
    public void releaseBean(){
        cicadaBeanFactory.releaseBean();
    }

    /**
     * 设置自定义异常处理器
     * 如果用户没有自定义，则使用默认实现
     */
    public void exceptionHandle(GlobalHandelException ex) {
        if (ex != null) {
            this.handelException = ex;
        }
    }

    public GlobalHandelException exceptionHandle() {
        return handelException;
    }
}
