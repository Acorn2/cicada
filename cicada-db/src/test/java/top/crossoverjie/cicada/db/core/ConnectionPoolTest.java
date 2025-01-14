package top.crossoverjie.cicada.db.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import top.crossoverjie.cicada.db.pool.config.PoolConfigFactory;
import top.crossoverjie.cicada.db.session.SqlSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 连接池测试类
 */
public class ConnectionPoolTest {

    private SqlSession sqlSession;

    @Before
    public void setUp() {
        try {
            // 初始化 SqlSession
            SqlSession.init("root", "root", 
                "jdbc:mysql://localhost:3306/ssm?serverTimezone=Asia/Shanghai&charset=utf8mb4&useUnicode=true&characterEncoding=utf-8",
                PoolConfigFactory.getPoolConfig());
            sqlSession = SqlSession.getInstance();
            
            assertNotNull("SqlSession 初始化失败", sqlSession);
        } catch (Exception e) {
            fail("初始化 SqlSession 时发生错误: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        // 清理资源
        if (sqlSession != null) {
            try {
                sqlSession.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testBasicConnectionPool() throws SQLException {
        try {
            // 确保 sqlSession 不为空
            assertNotNull("SqlSession 不能为空", sqlSession);
            
            // 测试获取连接
            Connection conn = sqlSession.getConnection();
            assertNotNull("连接不应为空", conn);
            
            // 测试连接可用性
            assertTrue("连接应该可用", conn.isValid(1));
            
            // 关闭连接(实际是放回池中)
            conn.close();
        } catch (SQLException e) {
            fail("获取数据库连接时发生错误: " + e.getMessage());
        }
    }

    @Test
    public void testMultipleConnections() throws SQLException {
        // 测试获取多个连接
        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Connection conn = sqlSession.getConnection();
            assertNotNull("连接不应为空", conn);
            connections.add(conn);
        }

        // 验证所有连接都是可用的
        for (Connection conn : connections) {
            assertTrue("连接应该可用", conn.isValid(1));
        }

        // 关闭所有连接
        for (Connection conn : connections) {
            conn.close();
        }
    }

    @Test(expected = RuntimeException.class)
    public void testMaxConnections() throws SQLException {
        // 测试超过最大连接数限制
        List<Connection> connections = new ArrayList<>();
        try {
            for (int i = 0; i < 15; i++) { // 超过最大连接数(10)
                connections.add(sqlSession.getConnection());
            }
        } finally {
            // 清理连接
            for (Connection conn : connections) {
                conn.close();
            }
        }
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        // 测试并发访问
        int threadCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // 用于记录成功获取连接的次数
        AtomicInteger successCount = new AtomicInteger(0);
        
        // 创建多个线程同时获取连接
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待统一开始
                    Connection conn = sqlSession.getConnection();
                    if (conn != null) {
                        successCount.incrementAndGet();
                        Thread.sleep(100); // 模拟使用连接
                        conn.close();
                    }
                } catch (Exception e) {
                    // 预期部分请求会失败
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 开始并发测试
        endLatch.await(5, TimeUnit.SECONDS); // 等待所有线程完成
        executor.shutdown();

        // 验证成功获取的连接数不超过最大连接数
        assertTrue("成功获取的连接数应该小于等于最大连接数",
                successCount.get() <= 10);
    }

    @Test
    public void testConnectionReuse() throws SQLException, InterruptedException {
        // 测试连接重用
        Connection conn1 = sqlSession.getConnection();
        conn1.close();
        
        Connection conn2 = sqlSession.getConnection();
        // 验证是否获取到同一个连接（通过比较底层连接的hashCode）
        assertEquals("应该重用连接池中的连接",
                conn1.toString(), conn2.toString());
        
        conn2.close();
    }

    @Test
    public void testConnectionTimeout() {
        // 测试连接超时
        long startTime = System.currentTimeMillis();
        try {
            List<Connection> connections = new ArrayList<>();
            for (int i = 0; i < 15; i++) { // 超过最大连接数
                connections.add(sqlSession.getConnection());
            }
            fail("应该抛出异常");
        } catch (Exception e) {
            long timeSpent = System.currentTimeMillis() - startTime;
            assertTrue("应该在最大等待时间内返回",
                    timeSpent <= 3500); // maxWait + buffer
        }
    }
}
