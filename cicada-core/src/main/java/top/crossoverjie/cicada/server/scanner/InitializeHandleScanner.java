package top.crossoverjie.cicada.server.scanner;

import org.slf4j.Logger;
import top.crossoverjie.cicada.base.log.LoggerBuilder;
import top.crossoverjie.cicada.server.lifecycle.InitializeHandle;
import top.crossoverjie.cicada.server.config.AppConfig;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 初始化处理器扫描器
 * 职责：
 * 1. 扫描所有InitializeHandle实现类
 * 2. 执行初始化处理
 */
public class InitializeHandleScanner {
    
    private static final Logger LOGGER = LoggerBuilder.getLogger(InitializeHandleScanner.class);

    /**
     * 执行应用的所有初始化处理器
     */
    public static void executeApplicationHandlers() throws Exception {
        String rootPackage = AppConfig.getInstance().getRootPackageName();
        if (rootPackage == null) {
            LOGGER.warn("Root package is not set, skipping initialize handlers");
            return;
        }
        scanAndExecute(rootPackage);
    }

    /**
     * 扫描并执行指定包下的初始化处理器
     * @param packageName 包名
     */
    private static void scanAndExecute(String packageName) throws Exception {
        // 1. 扫描所有InitializeHandle实现类
        List<Class<?>> initHandlerClasses = ClasLoadersScanner.getInitHandles(packageName);
        
        if (initHandlerClasses.isEmpty()) {
            LOGGER.info("No InitializeHandle implementation found in package: {}", packageName);
            return;
        }

        // 2. 实例化并按order排序
        List<InitializeHandle> sortedHandlers = initHandlerClasses.stream()
            .map(clazz -> {
                try {
                    return (InitializeHandle) clazz.newInstance();
                } catch (Exception e) {
                    LOGGER.error("Failed to instantiate handler: {}", clazz.getName(), e);
                    return null;
                }
            })
            .filter(handler -> handler != null)
            .sorted(Comparator.comparingInt(InitializeHandle::getOrder))
            .collect(Collectors.toList());

        // 3. 按顺序执行
        for (InitializeHandle handler : sortedHandlers) {
            executeHandler(handler);
        }
    }

    /**
     * 执行单个初始化处理器
     * @param handler 处理器实例
     */
    private static void executeHandler(InitializeHandle handler) throws Exception {
        LOGGER.info("Executing initializer [{}] with order [{}]", 
            handler.getName(), handler.getOrder());
        
        try {
            handler.handle();
            LOGGER.info("Successfully executed initializer: {}", handler.getName());
        } catch (Exception e) {
            LOGGER.error("Failed to execute initializer: {} with order {}", 
                handler.getName(), handler.getOrder(), e);
            throw e;
        }
    }
} 