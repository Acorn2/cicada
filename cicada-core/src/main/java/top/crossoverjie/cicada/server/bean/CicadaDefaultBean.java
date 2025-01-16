package top.crossoverjie.cicada.server.bean;

import top.crossoverjie.cicada.base.bean.CicadaBeanFactory;

/**
 * 默认Bean工厂实现
 * 特点：
 * 1. 提供最基础的Bean实例化功能
 * 2. 每次获取Bean都会创建新实例（不是单例）
 * 3. 通过反射机制实现类的实例化
 */
public class CicadaDefaultBean implements CicadaBeanFactory {

    /**
     * 注册Bean实例
     * 注意：当前实现为空，意味着不保存Bean实例
     */
    @Override
    public void register(Object object) {

    }

    /**
     * 根据类名获取Bean实例
     * @param name 完整类名
     * @return 新创建的实例
     */
    @Override
    public Object getBean(String name) throws Exception {
        Class<?> aClass = Class.forName(name);
        return aClass.newInstance();
    }

    @Override
    public <T> T getBean(Class<T> clazz) throws Exception {
        return clazz.newInstance();
    }

    @Override
    public void releaseBean() {
    }
}
