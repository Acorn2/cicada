package top.crossoverjie.cicada.db.connection;

import lombok.extern.slf4j.Slf4j;
import top.crossoverjie.cicada.db.session.SqlSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Function:
 * 默认的数据库连接实现
 * 基于JDBC的标准连接实现
 * 使用DriverManager获取连接
 *
 * @author crossoverJie
 * Date: 2020-02-28 00:37
 * @since JDK 1.8
 */
@Slf4j
public class JdbcConnection implements ConnectionManager {

    private Connection connection;

    @Override
    public Connection getConnection(SqlSession sqlSession) {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(sqlSession.getUrl(), sqlSession.getUserName(), sqlSession.getPwd());
            } catch (Exception e) {
               log.error("Exception", e);
            }
        }
        return connection;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                log.error("关闭数据库连接失败", e);
            }
        }
    }
}
