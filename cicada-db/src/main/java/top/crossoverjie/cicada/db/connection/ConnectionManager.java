package top.crossoverjie.cicada.db.connection;

import top.crossoverjie.cicada.db.session.SqlSession;

import java.sql.Connection;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2020-02-28 00:35
 * @since JDK 1.8
 */
public interface ConnectionManager {

    /**
     * 获取数据库连接
     * @param sqlSession 数据库会话
     * @return 连接对象
     */
    Connection getConnection(SqlSession sqlSession);
    
    /**
     * 关闭连接工厂
     */
    void close();
}
