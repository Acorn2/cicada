package top.crossoverjie.cicada.db.core;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import lombok.extern.slf4j.Slf4j;
import top.crossoverjie.cicada.db.annotation.OriginName;
import top.crossoverjie.cicada.db.model.Model;
import top.crossoverjie.cicada.db.reflect.Instance;
import top.crossoverjie.cicada.db.reflect.ReflectTools;
import top.crossoverjie.cicada.db.sql.Condition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库查询核心类
 * 实现链式查询构建和执行
 *
 * @author crossoverJie
 * @param <T> 查询结果的实体类型
 */
@Slf4j
public final class DBQuery<T extends Model> extends SqlSessionFactory {

    /** 目标实体类 */
    private Class<T> targetClass;

    /** 查询条件列表 */
    private List<Condition> conditions = new ArrayList<>();

    /** 数据库表对象 */
    private DbTable dbTable;

    /** 列名到数据库列对象的映射 */
    private Map<String,DbColumn> columnMap = new HashMap<>();

    /**
     * 初始化查询对象
     * @param clazz 目标实体类
     * @return 当前查询对象
     */
    public DBQuery query(Class<T> clazz) {
        this.targetClass = clazz;
        // 获取表名并创建表对象
        dbTable = super.origin().addTable(targetClass.getAnnotation(OriginName.class).value());
        return this;
    }

    /**
     * 添加查询条件
     * @param condition 查询条件
     * @return 当前查询对象
     */
    public DBQuery addCondition(Condition condition) {
        conditions.add(condition);
        return this;
    }

    /**
     * 执行查询并返回所有结果
     * @return 查询结果列表
     */
    public List<T> all() {
        List<T> result = null;
        String sql = buildSQL();
        Statement statement = null;
        try {
            statement = super.origin().getConnection().createStatement();
            log.debug("execute sql>>>>>{}", sql);
            ResultSet resultSet = statement.executeQuery(sql);
            result = new ArrayList<>();

            // 遍历结果集
            Map<String, Object> fields = new HashMap<>(8);
            while (resultSet.next()) {
                // 遍历实体类的所有字段
                for (Field field : targetClass.getDeclaredFields()) {
                    // 获取数据库字段名
                    String dbField = ReflectTools.getDbField(field);

                    // 获取对应的 getter 方法
                    Method method = resultSet.getClass().getMethod(
                        ReflectTools.getMethod(field.getType().getName()), 
                        String.class
                    );

                    // 从结果集中获取值
                    Object value = method.invoke(resultSet, dbField);
                    fields.put(field.getName(), value);
                }

                // 将数据转换为实体对象
                T transfer = Instance.transfer(targetClass, fields);
                result.add(transfer);
            }
            resultSet.close();
        } catch (Exception e) {
            log.error("Query execution error", e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                log.error("Statement closing error", e);
            }
        }

        return result;
    }

    /**
     * 获取查询结果的第一条记录
     * @return 单个实体对象
     */
    public T first() {
        return this.all().get(0);
    }

    /**
     * 构建 SQL 查询语句
     * @return SQL 语句字符串
     */
    private String buildSQL() {
        // 创建 SELECT 查询对象
        SelectQuery selectQuery = new SelectQuery();

        // 添加查询字段
        for (Field field : targetClass.getDeclaredFields()) {
            String dbField = ReflectTools.getDbField(field);
            DbColumn dbColumn = dbTable.addColumn(dbField);
            selectQuery.addColumns(dbColumn);
            columnMap.put(dbField, dbColumn);
        }

        // 添加查询条件
        for (Condition condition : conditions) {
            Condition.Filter filter = condition.getCondition();
            DbColumn dbColumn = columnMap.get(filter.getFiled());
            selectQuery.addCondition(
                BinaryCondition.equalTo(dbColumn, filter.getValue())
            );
        }

        // 验证并返回 SQL 语句
        return selectQuery.validate().toString();
    }
}
