package top.crossoverjie.cicada.db.pool;

import lombok.extern.slf4j.Slf4j;
import top.crossoverjie.cicada.db.connection.ConnectionManager;
import top.crossoverjie.cicada.db.pool.config.PoolConfig;
import top.crossoverjie.cicada.db.session.SqlSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConnectionPool implements ConnectionManager {

    private final BlockingQueue<PooledConnection> connectionPool;
    private final PoolConfig config;
    private final AtomicInteger activeCount = new AtomicInteger(0);
    private volatile boolean isShutdown = false;
    private final SqlSession sqlSession;

    public ConnectionPool(PoolConfig config, SqlSession sqlSession) {
        this.config = config;
        this.sqlSession = sqlSession;
        this.connectionPool = new ArrayBlockingQueue<>(config.getMaxActive());
        initializePool();
        startEvictionThread();
    }

    @Override
    public Connection getConnection(SqlSession sqlSession) {
        if (isShutdown) {
            throw new IllegalStateException("连接池已关闭");
        }

        try {
            // 尝试从池中获取连接
            PooledConnection conn = connectionPool.poll(config.getMaxWait(), TimeUnit.MILLISECONDS);
            
            if (conn != null) {
                if (!isConnectionValid(conn)) {
                    // 连接无效,创建新连接
                    conn = createConnection(sqlSession);
                }
                activeCount.incrementAndGet();
                return conn;
            }

            // 池中无可用连接且未达到最大连接数,创建新连接
            if (activeCount.get() < config.getMaxActive()) {
                conn = createConnection(sqlSession);
                activeCount.incrementAndGet();
                return conn;
            }

            throw new SQLException("无法获取数据库连接:已达到最大连接数");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取连接被中断", e);
        } catch (SQLException e) {
            throw new RuntimeException("获取连接失败", e);
        }
    }

    private void initializePool() {
        try {
            for (int i = 0; i < config.getInitialSize(); i++) {
                PooledConnection conn = createConnection(sqlSession);
                if (conn != null) {
                    connectionPool.offer(conn);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("初始化连接池失败", e);
        }
    }

    private PooledConnection createConnection(SqlSession session) throws SQLException {
        if (session == null) {
            throw new SQLException("SqlSession cannot be null");
        }
        
        Connection conn = DriverManager.getConnection(
            session.getUrl(), 
            session.getUserName(),
            session.getPwd()
        );
        return new PooledConnection(conn, this);
    }

    private boolean isConnectionValid(PooledConnection conn) {
        try {
            return conn.isValid(2000); // 2秒超时
        } catch (SQLException e) {
            return false;
        }
    }

    void releaseConnection(PooledConnection conn) {
        if (!isShutdown && conn != null) {
            connectionPool.offer(conn);
            activeCount.decrementAndGet();
        }
    }

    private void startEvictionThread() {
        Thread evictionThread = new Thread(() -> {
            while (!isShutdown) {
                try {
                    Thread.sleep(config.getTimeBetweenEvictionRunsMillis());
                    evictIdleConnections();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        evictionThread.setDaemon(true);
        evictionThread.start();
    }

    private void evictIdleConnections() {
        long now = System.currentTimeMillis();
        connectionPool.removeIf(conn -> {
            if (conn.getLastUsedTime() + config.getMinEvictableIdleTimeMillis() < now) {
                try {
                    conn.getRealConnection().close();
                    return true;
                } catch (SQLException e) {
                    log.error("关闭空闲连接失败", e);
                }
            }
            return false;
        });
    }

    public void shutdown() {
        isShutdown = true;
        connectionPool.forEach(conn -> {
            try {
                conn.getRealConnection().close();
            } catch (SQLException e) {
                log.error("关闭连接失败", e);
            }
        });
        connectionPool.clear();
    }

    @Override
    public void close() {
        shutdown();
    }
} 