package top.crossoverjie.cicada.db.core.handle;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import top.crossoverjie.cicada.db.core.SqlSession;
import top.crossoverjie.cicada.db.listener.DataChangeListener;
import top.crossoverjie.cicada.db.model.User;

@Slf4j
public class CGlibHandleProxyTest {

    @Test
    public void testUpdate() {
        // 初始化数据库连接
        SqlSession.init("root", "root", 
            "jdbc:mysql://localhost:3306/ssm?charset=utf8mb4&useUnicode=true&characterEncoding=utf-8");

        // 准备测试数据
        User user = new User();
        user.setId(1);
        user.setName("abc");

        // 创建 CGLIB 代理实例
        DBHandler handle = new CGlibHandleProxy<>(DBHandler.class)
            .getInstance(new DataChangeListener() {
                @Override
                public void listener(Object obj) {
                    User user2 = (User) obj;
                    log.info("CGLIB callback: " + user2.toString());
                }
            });

        // 执行更新操作
        int result = handle.update(user);
        System.out.println("Update result: " + result);
    }

    @Test
    public void testInsert() {
        // 初始化数据库连接
        SqlSession.init("root", "root", 
            "jdbc:mysql://localhost:3306/ssm?charset=utf8mb4&useUnicode=true&characterEncoding=utf-8");

        // 准备测试数据
        User user = new User();
        user.setName("cglib_test");
        user.setDescription("Testing CGLIB proxy");

        // 创建代理实例（不带监听器）
        DBHandler handle = new CGlibHandleProxy<>(DBHandler.class).getInstance();
        
        // 执行插入操作
        handle.insert(user);
    }

    @Test
    public void testCacheAndClear() {
        // 测试缓存机制
        DBHandler handler1 = new CGlibHandleProxy<>(DBHandler.class).getInstance();
        DBHandler handler2 = new CGlibHandleProxy<>(DBHandler.class).getInstance();
        
        // 验证是否是同一个代理实例
        System.out.println("Same proxy instance: " + (handler1 == handler2));
        
        // 清除缓存
        CGlibHandleProxy.clearProxyCache();
        
        DBHandler handler3 = new CGlibHandleProxy<>(DBHandler.class).getInstance();
        // 验证清除缓存后是否是新的实例
        System.out.println("New proxy instance: " + (handler1 != handler3));
    }
} 