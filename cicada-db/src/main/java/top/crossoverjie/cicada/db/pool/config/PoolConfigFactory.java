package top.crossoverjie.cicada.db.pool.config;

/**
 * @author hresh
 * @博客 https://juejin.cn/user/2664871918047063
 * @网站 https://www.hreshhao.com/
 * @date 2025/1/14 11:03
 */
public class PoolConfigFactory {

    private static final PoolConfig DEFAULT_CONFIG = createDefaultConfig();

    private static PoolConfig createDefaultConfig() {
        PoolConfig config = new PoolConfig();
        config.setInitialSize(5);
        config.setMinIdle(5);
        config.setMaxActive(10);
        config.setMaxWait(3000);
        config.setTimeBetweenEvictionRunsMillis(60000);
        config.setMinEvictableIdleTimeMillis(300000);
        config.setValidationQuery("SELECT 1");
        return config;
    }

    public static PoolConfig getPoolConfig() {
        return DEFAULT_CONFIG;
    }

}
