package top.crossoverjie.cicada.db.core.handle;

import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import top.crossoverjie.cicada.db.executor.SqlExecutor;
import top.crossoverjie.cicada.db.executor.ExecutorProxy;
import top.crossoverjie.cicada.db.session.SqlSession;
import top.crossoverjie.cicada.db.listener.DataChangeEvent;
import top.crossoverjie.cicada.db.model.User;
import top.crossoverjie.cicada.db.pool.config.PoolConfigFactory;

import java.sql.Connection;
import java.sql.Statement;

@Slf4j
public class DBHandleImplTest {

    private static SqlSession sqlSession;

    @BeforeClass
    public static void init() {
        SqlSession.init("root", "root", 
            "jdbc:mysql://localhost:3306/ssm?charset=utf8mb4&useUnicode=true&characterEncoding=utf-8",
            PoolConfigFactory.getPoolConfig());
        sqlSession = SqlSession.getInstance();

        // 初始化测试数据
        try (Connection conn = sqlSession.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 清空现有数据
            stmt.execute("TRUNCATE TABLE `user`");
            
            // 插入测试数据
            stmt.execute("INSERT INTO user (id, name, description) VALUES (1, 'test', 'test description')");
            
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
    public void update() {
        try {
            User user = new User();
            user.setId(1);
            user.setName("updated name");
            
            SqlExecutor handle = (SqlExecutor) new ExecutorProxy(SqlExecutor.class)
                .getInstance(new DataChangeEvent() {
                    @Override
                    public void listener(Object obj) {
                        User user2 = (User) obj;
                        log.info("Data change callback: {}", user2);
                    }
                });
                
            int result = handle.update(user);
            Assert.assertTrue("Update should affect one row", result == 1);
            
        } catch (Exception e) {
            log.error("Error in update test", e);
            throw e;
        }
    }

    @Test
    public void insert() {
        try {
            User user = new User();
            user.setName("new user");
            user.setDescription("新用户");

            SqlExecutor handle = (SqlExecutor) new ExecutorProxy(SqlExecutor.class).getInstance();
            handle.insert(user);

        } catch (Exception e) {
            log.error("Error in insert test", e);
            throw e;
        }
    }

    @Test
    public void delete() {
        try {
            User user = new User();
            user.setId(1);
            
            SqlExecutor handle = (SqlExecutor) new ExecutorProxy(SqlExecutor.class).getInstance();
            int result = handle.delete(user);
            
            Assert.assertTrue("Delete should affect one row", result == 1);
            
        } catch (Exception e) {
            log.error("Error in delete test", e);
            throw e;
        }
    }

    @Test
    public void testWithDataChangeListener() {
        try {
            User user = new User();
            user.setId(1);
            user.setName("test with listener");
            
            final boolean[] listenerCalled = {false};
            
            SqlExecutor handle = (SqlExecutor) new ExecutorProxy(SqlExecutor.class)
                .getInstance(new DataChangeEvent() {
                    @Override
                    public void listener(Object obj) {
                        listenerCalled[0] = true;
                        User changedUser = (User) obj;
                        log.info("Data change detected: {}", changedUser);
                    }
                });
                
            handle.update(user);
            
            Assert.assertTrue("Listener should have been called", listenerCalled[0]);
            
        } catch (Exception e) {
            log.error("Error in listener test", e);
            throw e;
        }
    }
}