package com.msdn.hresh.db.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接工具类
 * @author hresh
 * @date 2025/1/2 12:20
 */
public class DbUtil {
    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    public static void init(String driver, String url, String username, String password) {
        DbUtil.driver = driver;
        DbUtil.url = url;
        DbUtil.username = username;
        DbUtil.password = password;
        
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load database driver", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
} 