package top.crossoverjie.cicada.base.bean;

/**
 * Function:
 * 定义Bean工厂接口
 * 通过SPI机制支持插件式扩展
 * 用户可以自定义Bean管理逻辑
 *
 * @author crossoverJie
 *         Date: 2018/11/14 01:06
 * @since JDK 1.8
 */
public interface CicadaBeanFactory {

    /**
     * Register into bean Factory
     * @param object
     */
    void register(Object object);

    /**
     * Get bean from bean Factory
     * @param name
     * @return
     * @throws Exception
     */
    Object getBean(String name) throws Exception;

    /**
     * get bean by class type
     * @param clazz
     * @param <T>
     * @return bean
     * @throws Exception
     */
    <T> T getBean(Class<T> clazz) throws Exception;

    /**
     * release all beans
     */
    void releaseBean() ;
}
