这是一个简化版的自定义ORM框架,其设计思路和功能确实类似于MyBatis-Plus,但功能相对简单一些。让我为您分析一下这个框架的主要特点:


### 核心功能特点

1. **链式查询构建器**
```java
// 类似MyBatis-Plus的查询构建器
List<User> users = new DBQueryV2<>(User.class)
    .where("status", 1)
    .whereLike("username", "test")
    .page(1, 10)
    .orderBy("createTime", false)
    .all();
```


2. **注解支持**
```java
@OriginName  // 表名注解
@FieldName   // 字段名注解
@PrimaryId   // 主键注解
```



3. **基础CRUD操作**


- 查询(Query)
- 查询(Query)
- 插入(Insert)
- 更新(Update)
- 条件构造


4. **核心功能组件**

- SqlSession: 数据库会话管理
- DBQueryV2: 增强版查询构建器
- ConnectionFactory: 数据库连接工厂
- HandleProxy: 数据库操作代理

