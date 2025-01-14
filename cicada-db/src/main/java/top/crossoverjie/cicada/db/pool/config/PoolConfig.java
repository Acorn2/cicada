package top.crossoverjie.cicada.db.pool.config;

import lombok.Data;

@Data
public class PoolConfig {

    // 初始连接数
    private int initialSize = 5;
    
    // 最小空闲连接数
    private int minIdle = 5;
    
    // 最大连接数 
    private int maxActive = 20;
    
    // 获取连接最大等待时间(毫秒)
    private long maxWait = 60000;
    
    // 空闲连接检测间隔时间(毫秒)
    private long timeBetweenEvictionRunsMillis = 60000;
    
    // 连接最小生存时间(毫秒)
    private long minEvictableIdleTimeMillis = 300000;
    
    // 测试连接是否可用的SQL
    private String validationQuery = "SELECT 1";

} 