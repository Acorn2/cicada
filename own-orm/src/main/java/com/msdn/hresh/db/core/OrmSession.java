package com.msdn.hresh.db.core;

import com.msdn.hresh.db.util.DbUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ORM会话管理
 * @author hresh
 * @date 2025/1/2 13:00
 */
public class OrmSession {
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    /**
     * 初始化数据库连接
     */
    public static void init(String driver, String url, String username, String password) {
        DbUtil.init(driver, url, username, password);
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() {
        Connection conn = connectionHolder.get();
        if (conn == null) {
            conn = DbUtil.getConnection();
            connectionHolder.set(conn);
        }
        return conn;
    }

    /**
     * 开启事务
     */
    public static void beginTransaction() {
        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to begin transaction", e);
        }
    }

    /**
     * 提交事务
     */
    public static void commit() {
        try {
            Connection conn = getConnection();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to commit transaction", e);
        }
    }

    /**
     * 回滚事务
     */
    public static void rollback() {
        try {
            Connection conn = getConnection();
            conn.rollback();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to rollback transaction", e);
        }
    }

    /**
     * 关闭连接
     */
    public static void close() {
        Connection conn = connectionHolder.get();
        if (conn != null) {
            DbUtil.close(conn);
            connectionHolder.remove();
        }
    }
} 