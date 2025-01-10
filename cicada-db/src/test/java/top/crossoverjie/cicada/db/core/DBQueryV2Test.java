package top.crossoverjie.cicada.db.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import top.crossoverjie.cicada.db.model.User;
import top.crossoverjie.cicada.db.sql.Condition;

import java.util.Arrays;
import java.util.List;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
public class DBQueryV2Test {

    @BeforeClass
    public static void initDatabase() {
        SqlSession.init("root", "root", "jdbc:mysql://localhost:3306/ssm?charset=utf8mb4");
        SqlSession instance = SqlSession.getInstance();

        try (Connection conn = instance.getConnection();
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

    @Before
    public void init() {
        SqlSession.init("root", "root", "jdbc:mysql://localhost:3306/ssm?charset=utf8mb4");
    }

    @Test
    public void testQueryAll() {
        List<User> users = new DBQueryV2<>(User.class)
                .all();
        for (User user : users) {
            log.info(user.toString());
        }
    }

    @Test
    public void testQueryWithConditions() {
        List<User> users = new DBQueryV2<>(User.class)
                .where("password", "abc123")
                .where("id", 1)
                .all();
        for (User user : users) {
            log.info(user.toString());
        }
    }

    @Test
    public void testQueryWithPagination() {
        List<User> users = new DBQueryV2<>(User.class)
                .page(1, 10)
                .orderBy("id", true)
                .all();
        for (User user : users) {
            log.info(user.toString());
        }
    }

    @Test
    public void testQueryOne() {
        User user = new DBQueryV2<>(User.class)
                .where("id", 1)
                .one();
        log.info(user != null ? user.toString() : "User not found");
    }

    @Test
    public void testQueryWithLike() {
        List<User> users = new DBQueryV2<>(User.class)
                .whereLike("username", "test")
                .all();
        for (User user : users) {
            log.info(user.toString());
        }
    }

    @Test
    public void testQueryWithIn() {
        List<User> users = new DBQueryV2<>(User.class)
                .whereIn("id", Arrays.asList(1, 2, 3))
                .all();
        for (User user : users) {
            log.info(user.toString());
        }
    }

    @Test
    public void testCount() {
        long count = new DBQueryV2<>(User.class)
                .where("password", "abc123")
                .count();
        log.info("Total count: {}", count);
    }

    @Test
    public void testComplexQuery() {
        List<User> users = new DBQueryV2<>(User.class)
                .where("status", 1)
                .whereLike("username", "test")
                .page(1, 10)
                .orderBy("createTime", false)
                .orderBy("id", true)
                .all();
        for (User user : users) {
            log.info(user.toString());
        }
    }
} 