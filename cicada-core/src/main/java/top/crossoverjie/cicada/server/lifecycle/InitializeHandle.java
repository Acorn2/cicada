package top.crossoverjie.cicada.server.lifecycle;

/**
 * initialize something about db/redis/kafka etc.
 */
public abstract class InitializeHandle {

    /**
     * 执行初始化操作
     * 例如：数据库连接、缓存客户端、消息队列等
     *
     * @throws Exception 初始化过程中的异常
     */
    public abstract void handle() throws Exception;

    /**
     * 获取初始化优先级
     * 默认优先级为0，子类可以覆盖此方法
     */
    public int getOrder() {
        return 0;
    }

    /**
     * 获取初始化器名称
     * 默认使用类名，子类可以覆盖此方法
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }
    
}
