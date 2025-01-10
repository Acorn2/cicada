package top.crossoverjie.cicada.db.core.handle;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import top.crossoverjie.cicada.db.listener.DataChangeListener;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库操作代理类 (CGLIB实现版本)
 *
 * 本项目中目前采用JDK动态代理，因为定义了DBHandler接口，如果想要使用CGLIB代理，
 * 
 * @author crossoverJie
 * @param <T> 被代理类的类型
 */
@Slf4j
public class CGlibHandleProxy<T> {
    
    /** 被代理的类 */
    private final Class<T> targetClass;
    
    /** 数据变更监听器 */
    private DataChangeListener listener;
    
    /** 代理类缓存 */
    private static final ConcurrentHashMap<Class<?>, Object> PROXY_CACHE = new ConcurrentHashMap<>();

    /**
     * 构造函数
     * @param targetClass 需要代理的类
     */
    public CGlibHandleProxy(Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }
        this.targetClass = targetClass;
    }

    /**
     * 获取代理实例（带监听器）
     * @param listener 数据变更监听器
     * @return 代理对象实例
     */
    public T getInstance(DataChangeListener listener) {
        this.listener = listener;
        return getInstance();
    }

    /**
     * 获取代理实例
     * @return 代理对象实例
     */
    @SuppressWarnings("unchecked")
    public T getInstance() {
        try {
            // 尝试从缓存获取
            Object cachedProxy = PROXY_CACHE.get(targetClass);
            if (cachedProxy != null) {
                return (T) cachedProxy;
            }

            // 创建新的代理实例
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(targetClass);
            enhancer.setCallback(new CglibInterceptor());
            
            // 创建代理对象
            T proxy = (T) enhancer.create();
            
            // 放入缓存
            PROXY_CACHE.putIfAbsent(targetClass, proxy);
            
            return proxy;
        } catch (Exception e) {
            log.error("Failed to create CGLIB proxy for class: {}", targetClass.getName(), e);
            throw new RuntimeException("Failed to create CGLIB proxy", e);
        }
    }

    /**
     * CGLIB 方法拦截器
     */
    private class CglibInterceptor implements MethodInterceptor {
        
        @Override
        public Object intercept(Object obj, Method method, Object[] args, 
                              MethodProxy proxy) throws Throwable {
            Object result = null;
            boolean isExceptionThrown = false;
            
            try {
                // 前置处理
                beforeMethod(method, args);
                
                // 调用原方法
                result = proxy.invokeSuper(obj, args);

                // 后置处理
                afterMethod(method, args, result);
                
                return result;
            } catch (Exception e) {
                isExceptionThrown = true;
                handleException(method, args, e);
                throw e;
            } finally {
                // 最终处理
                finallyMethod(method, args, result, isExceptionThrown);
            }
        }
        
        /**
         * 方法执行前的处理
         */
        private void beforeMethod(Method method, Object[] args) {
            if (log.isDebugEnabled()) {
                log.debug("Before executing method: {}", method.getName());
            }
        }
        
        /**
         * 方法执行后的处理
         */
        private void afterMethod(Method method, Object[] args, Object result) {
            // 触发监听器
            if (listener != null && args != null && args.length > 0) {
                try {
                    listener.listener(args[0]);
                } catch (Exception e) {
                    log.error("Error in listener callback", e);
                }
            }
        }
        
        /**
         * 异常处理
         */
        private void handleException(Method method, Object[] args, Exception e) {
            log.error("Error executing method: {}", method.getName(), e);
        }
        
        /**
         * 最终处理
         */
        private void finallyMethod(Method method, Object[] args, 
                                 Object result, boolean isExceptionThrown) {
            if (log.isDebugEnabled()) {
                log.debug("Finished executing method: {}, isException: {}", 
                         method.getName(), isExceptionThrown);
            }
        }
    }
    
    /**
     * 清除代理缓存
     */
    public static void clearProxyCache() {
        PROXY_CACHE.clear();
    }
}
