package top.crossoverjie.cicada.db.session;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.crossoverjie.cicada.db.pool.ConnectionPool;
import top.crossoverjie.cicada.db.pool.config.PoolConfig;

import java.sql.Connection;

/**
 * Function:
 * 管理数据库连接配置和连接实例
 * 提供表操作接口
 *
 * @author crossoverJie
 * Date: 2019-11-19 22:57
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SqlSession {

    private Connection connection;

    @Getter
    private String userName;

    @Getter
    private String pwd;

    @Getter
    private String url;

    private static SqlSession session;

    private static DbSchema schema;

    private static ConnectionPool connectionPool;

    public static SqlSession getInstance() {
        return session;
    }

    public static void init(String userName, String pwd, String url, PoolConfig poolConfig) {
        session = new SqlSession(userName, pwd, url);
        String database = getDataBaseName(url);
        schema = new DbSpec().addSchema(database);
        
        // 初始化连接池时传入 session 实例
        connectionPool = new ConnectionPool(poolConfig, session);
    }


    private SqlSession(String userName, String pwd, String url) {
        this.userName = userName;
        this.pwd = pwd;
        this.url = url;
    }

    public Connection getConnection() {
        return connectionPool.getConnection(this);
    }

    public DbTable addTable(String tableName){
        return schema.addTable(tableName) ;
    }

    public void close() {
        if (connectionPool != null) {
            try {
                // 关闭连接池
                connectionPool.shutdown();
                connectionPool = null;
                
                // 清理会话实例
                session = null;
                schema = null;
                
                log.info("Successfully closed SqlSession and connection pool");
            } catch (Exception e) {
                log.error("Error while closing SqlSession", e);
                throw new RuntimeException("Failed to close SqlSession", e);
            }
        }
    }

    private static String getDataBaseName(String url) {
        try {
            // 更安全的数据库名称提取方法
            String[] parts = url.split("/");
            String dbNameWithParams = parts[parts.length - 1];
            int questionMarkIndex = dbNameWithParams.indexOf("?");
            
            if (questionMarkIndex != -1) {
                return dbNameWithParams.substring(0, questionMarkIndex);
            } else {
                return dbNameWithParams;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("无法从URL中解析数据库名称: " + url, e);
        }
    }
}
