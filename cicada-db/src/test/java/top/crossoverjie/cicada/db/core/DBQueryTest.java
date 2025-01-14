package top.crossoverjie.cicada.db.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import top.crossoverjie.cicada.db.model.User;
import top.crossoverjie.cicada.db.pool.config.PoolConfigFactory;
import top.crossoverjie.cicada.db.query.QueryBuilder;
import top.crossoverjie.cicada.db.session.SqlSession;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class DBQueryTest {

    private static SqlSession sqlSession;

    @BeforeClass
    public static void initDatabase() {
        SqlSession.init("root", "root", "jdbc:mysql://localhost:3306/ssm?charset=utf8mb4", PoolConfigFactory.getPoolConfig());
        sqlSession = SqlSession.getInstance();

        try (Connection conn = sqlSession.getConnection();
             Statement stmt = conn.createStatement()) {

            // 清空现有数据
            stmt.execute("TRUNCATE TABLE `user`");

            // 插入测试数据
            String[] insertData = {
                    "INSERT INTO user (id, name, password, city_id, description) VALUES (1, 'test1', 'abc123', 1, 'test user 1')",
                    "INSERT INTO user (id, name, password, city_id, description) VALUES (2, 'test2', 'abc123', 1, 'test user 2')",
                    "INSERT INTO user (id, name, password, city_id, description) VALUES (3, 'test3', 'abc456', 2, 'test user 3')",
                    "INSERT INTO user (id, name, password, city_id, description) VALUES (4, 'admin', 'admin123', 1, 'administrator')",
                    "INSERT INTO user (id, name, password, city_id, description) VALUES (5, 'testuser', 'test123', 3, 'test user 5')"
            };

            for (String insert : insertData) {
                stmt.execute(insert);
            }

            log.info("Test data initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize test data", e);
            throw new RuntimeException("Data initialization failed", e);
        }
    }

    @AfterClass
    public static void cleanup() {
        if (sqlSession != null) {
            try {
                sqlSession.close();
                log.info("Connection pool closed successfully");
            } catch (Exception e) {
                log.error("Error closing connection pool", e);
            }
        }
    }

    @After
    public void tearDown() {
        // 每个测试方法后确保所有连接都已归还到连接池
        try {
            Thread.sleep(100); // 给一点时间让连接归还
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testQueryAll() {
        try {
            List<User> users = new QueryBuilder<>(User.class)
                    .all();
            for (User user : users) {
                log.info(user.toString());
            }
        } catch (Exception e) {
            log.error("Error in testQueryAll", e);
            throw e;
        }
    }

    @Test
    public void testQueryWithConditions() {
        try {
            List<User> users = new QueryBuilder<>(User.class)
                    .where("password", "abc123")
                    .where("id", 1)
                    .all();
            for (User user : users) {
                log.info(user.toString());
            }
        } catch (Exception e) {
            log.error("Error in testQueryWithConditions", e);
            throw e;
        }
    }

    @Test
    public void testQueryWithPagination() {
        try {
            List<User> users = new QueryBuilder<>(User.class)
                    .page(1, 10)
                    .orderBy("id", true)
                    .all();
            for (User user : users) {
                log.info(user.toString());
            }
        } catch (Exception e) {
            log.error("Error in testQueryWithPagination", e);
            throw e;
        }
    }

    @Test
    public void testQueryOne() {
        try {
            User user = new QueryBuilder<>(User.class)
                    .where("id", 1)
                    .one();
            log.info(user != null ? user.toString() : "User not found");
        } catch (Exception e) {
            log.error("Error in testQueryOne", e);
            throw e;
        }
    }

    @Test
    public void testQueryWithLike() {
        try {
            List<User> users = new QueryBuilder<>(User.class)
                    .whereLike("username", "test")
                    .all();
            for (User user : users) {
                log.info(user.toString());
            }
        } catch (Exception e) {
            log.error("Error in testQueryWithLike", e);
            throw e;
        }
    }

    @Test
    public void testQueryWithIn() {
        try {
            List<User> users = new QueryBuilder<>(User.class)
                    .whereIn("id", Arrays.asList(1, 2, 3))
                    .all();
            for (User user : users) {
                log.info(user.toString());
            }
        } catch (Exception e) {
            log.error("Error in testQueryWithIn", e);
            throw e;
        }
    }

    @Test
    public void testCount() {
        try {
            long count = new QueryBuilder<>(User.class)
                    .where("password", "abc123")
                    .count();
            log.info("Total count: {}", count);
        } catch (Exception e) {
            log.error("Error in testCount", e);
            throw e;
        }
    }

    @Test
    public void testComplexQuery() {
        try {
            List<User> users = new QueryBuilder<>(User.class)
                    .where("status", 1)
                    .whereLike("username", "test")
                    .page(1, 10)
                    .orderBy("createTime", false)
                    .orderBy("id", true)
                    .all();
            for (User user : users) {
                log.info(user.toString());
            }
        } catch (Exception e) {
            log.error("Error in testComplexQuery", e);
            throw e;
        }
    }
} 