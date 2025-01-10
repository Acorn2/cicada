package top.crossoverjie.cicada.db.core.handle;

import lombok.extern.slf4j.Slf4j;
import top.crossoverjie.cicada.db.listener.DataChangeListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 数据库操作代理类
 * 主要用于:
 * 1. 创建数据库操作接口的代理实例
 * 2. 实现数据变更监听功能
 *
 * @author crossoverJie
 * Date: 2019-12-04 00:00
 * @since JDK 1.8
 */
@Slf4j
public class HandleProxy<T> {

    /** 被代理的接口类型 */
    private Class<T> clazz;

    /**
     * 构造函数
     * @param clazz 需要代理的接口类
     */
    public HandleProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    /** 数据变更监听器 */
    private DataChangeListener listener;

    /**
     * 获取代理实例（带监听器）
     * @param listener 数据变更监听器
     * @return 代理对象实例
     */
    public T getInstance(DataChangeListener listener) {
        this.listener = listener;
        return (T) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(), 
            new Class[] {clazz}, 
            new ProxyInvocation(DBHandler.class)
        );
    }

    /**
     * 获取代理实例（不带监听器）
     * @return 代理对象实例
     */
    public T getInstance() {
        return (T) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(), 
            new Class[] {clazz}, 
            new ProxyInvocation(DBHandler.class)
        );
    }

    /**
     * 代理调用处理器内部类
     * 负责处理代理对象的方法调用
     */
    private class ProxyInvocation implements InvocationHandler {

        /** 实际执行数据库操作的对象 */
        private Object target;

        /**
         * 构造函数
         * @param clazz 实际处理类的Class对象
         */
        public ProxyInvocation(Class clazz) {
            try {
                // 实例化实际的数据库操作处理类
                this.target = clazz.newInstance();
            } catch (Exception e) {
                log.error("创建数据库操作处理类实例失败, exception={}", e);
            }
        }

        /**
         * 代理方法调用的处理
         * @param proxy 代理对象
         * @param method 被调用的方法
         * @param args 方法参数
         * @return 方法执行结果
         * @throws Throwable 执行异常
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 调用实际的数据库操作方法
            Object invoke = method.invoke(target, args);
            
            // 如果设置了监听器，触发数据变更通知
            if (null != listener) {
                listener.listener(args[0]);
            }
            
            return invoke;
        }
    }
}
