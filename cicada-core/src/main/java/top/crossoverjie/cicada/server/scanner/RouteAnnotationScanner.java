package top.crossoverjie.cicada.server.scanner;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import top.crossoverjie.cicada.base.log.LoggerBuilder;
import top.crossoverjie.cicada.base.annotation.CicadaAction;
import top.crossoverjie.cicada.base.annotation.CicadaRoute;
import top.crossoverjie.cicada.server.config.AppConfig;
import top.crossoverjie.cicada.server.context.CicadaContext;
import top.crossoverjie.cicada.base.exception.CicadaException;
import top.crossoverjie.cicada.server.exception.code.CommonErrorCode;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 路由扫描器：负责扫描和管理所有路由映射
 * 主要功能：
 * 1. 扫描指定包下的所有路由注解
 * 2. 维护URL路径和处理方法的映射关系
 * 3. 处理默认路由响应
 */
public class RouteAnnotationScanner {

    // 存储路由映射关系：URL路径 -> Method对象
    private static Map<String, Method> routes = null;

    // 单例实例
    private volatile static RouteAnnotationScanner routerScanner;

    // 应用配置实例
    private AppConfig appConfig = AppConfig.getInstance();

    private static final Logger LOGGER = LoggerBuilder.getLogger(RouteAnnotationScanner.class);

    /**
     * 获取RouterScanner单例
     * 使用双重检查锁定模式确保线程安全
     */
    public static RouteAnnotationScanner getInstance() {
        if (routerScanner == null) {
            synchronized (RouteAnnotationScanner.class) {
                if (routerScanner == null) {
                    routerScanner = new RouteAnnotationScanner();
                }
            }
        }
        return routerScanner;
    }

    private RouteAnnotationScanner() {
    }

    /**
     * 路由方法查找
     * @param queryStringDecoder 请求解码器，包含请求路径信息
     * @return 匹配的处理方法
     * @throws Exception 当找不到匹配的路由时抛出异常
     */
    public Method routeMethod(QueryStringDecoder queryStringDecoder) throws Exception {
        // 延迟初始化路由表
        if (routes == null) {
            routes = new HashMap<>(16);
            loadRouteMethods(appConfig.getRootPackageName());
        }

        // 检查是否为默认首页请求
        boolean defaultResponse = defaultResponse(queryStringDecoder.path());
        if (defaultResponse) {
            return null;
        }

        // 查找匹配的路由方法
        Method method = routes.get(queryStringDecoder.path());
        if (method == null) {
            throw new CicadaException(CommonErrorCode.NOT_FOUND);
        }

        return method;
    }

    /**
     * 处理默认路由响应
     * 当访问根路径时返回默认的欢迎页面
     * @param path 请求路径
     * @return 是否是默认路由
     */
    private boolean defaultResponse(String path) {
        if (appConfig.getRootPath().equals(path)) {
            CicadaContext.getContext().html("<center> Hello Cicada <br/><br/>" +
                    "Power by <a href='https://github.com/TogetherOS/cicada'>@Cicada</a> </center>");
            return true;
        }
        return false;
    }

    /**
     * 加载所有路由方法
     * 扫描指定包下的所有类：
     * 1. 查找带有@CicadaAction注解的类
     * 2. 查找带有@CicadaRoute注解的方法
     * 3. 生成URL路径与方法的映射关系
     * 
     * @param packageName 要扫描的包名
     */
    private void loadRouteMethods(String packageName) throws Exception {
        // 扫描指定包下的所有类
        Set<Class<?>> classes = ClasLoadersScanner.getClasses(packageName);
        LOGGER.info("Scanning routes in package: {}", packageName);

        for (Class<?> aClass : classes) {
            Method[] declaredMethods = aClass.getMethods();

            for (Method method : declaredMethods) {
                // 查找带有@CicadaRoute注解的方法
                CicadaRoute annotation = method.getAnnotation(CicadaRoute.class);
                if (annotation == null) {
                    continue;
                }

                // 获取类上的@CicadaAction注解
                CicadaAction cicadaAction = aClass.getAnnotation(CicadaAction.class);
                // 构建完整的URL路径：根路径 + Action路径 + Route路径
                routes.put(appConfig.getRootPath() + "/" + cicadaAction.value() + "/" + annotation.value(), method);
            }
        }
    }
}
