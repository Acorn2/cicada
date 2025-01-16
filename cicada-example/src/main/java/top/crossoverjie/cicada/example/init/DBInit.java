package top.crossoverjie.cicada.example.init;

import lombok.extern.slf4j.Slf4j;
import top.crossoverjie.cicada.db.pool.config.PoolConfigFactory;
import top.crossoverjie.cicada.db.session.SqlSession;
import top.crossoverjie.cicada.server.lifecycle.InitializeHandle;
import top.crossoverjie.cicada.server.config.configuration.ApplicationConfiguration;
import top.crossoverjie.cicada.server.config.holder.ConfigurationHolder;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2020-02-18 01:40
 * @since JDK 1.8
 */
@Slf4j
public class DBInit extends InitializeHandle {

    @Override
    public int getOrder() {
        return 1; // 数据库优先初始化
    }
    
    @Override
    public String getName() {
        return "DatabaseInitializer";
    }
    
    @Override
    public void handle() throws Exception {
        ApplicationConfiguration config = 
            (ApplicationConfiguration) ConfigurationHolder.getConfiguration(ApplicationConfiguration.class);
            
        SqlSession.init(
            config.get("db.username"),
            config.get("db.pwd"),
            config.get("db.url"),
            PoolConfigFactory.getPoolConfig()
        );
        
        log.info("Database initialization completed");
    }
}
