package top.crossoverjie.cicada.db.query;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.InCondition;
import com.healthmarketscience.sqlbuilder.OrderObject;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import lombok.extern.slf4j.Slf4j;
import top.crossoverjie.cicada.db.annotation.OriginName;
import top.crossoverjie.cicada.db.model.Model;
import top.crossoverjie.cicada.db.reflect.Instance;
import top.crossoverjie.cicada.db.reflect.ReflectTools;
import top.crossoverjie.cicada.db.session.SqlSessionFactory;
import top.crossoverjie.cicada.db.sql.Condition;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * - 支持链式调用
 * - 实现条件查询、分页、排序等功能
 * - 处理结果集映射
 * @author hresh
 * @博客 https://juejin.cn/user/2664871918047063
 * @网站 https://www.hreshhao.com/
 * @date 2025/1/2 11:00
 */
@Slf4j
public class QueryBuilder<T extends Model> extends SqlSessionFactory {

    private final Class<T> targetClass;  // 改为 final

    /**
     * 查询条件列表
     */
    private final List<Condition> conditions = new ArrayList<>();  // 改为 final

    /**
     * 数据库表对象
     */
    private DbTable dbTable;

    /**
     * 列名到数据库列对象的映射
     */
    private final Map<String, DbColumn> columnMap = new HashMap<>();  // 改为 final

    /**
     * 分页参数
     */
    private Integer pageSize;
    private Integer pageNum;

    /**
     * 排序字段列表
     */
    private final List<OrderObject> orderByList = new ArrayList<>();  // 修改为 OrderObject 类型

    /**
     * 优化1: 添加构造函数，强制初始化必要参数
     */
    public QueryBuilder(Class<T> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }
        this.targetClass = targetClass;
        this.dbTable = super.origin().addTable(
                targetClass.getAnnotation(OriginName.class).value()
        );
    }

    /**
     * 优化2: 添加分页支持
     */
    public QueryBuilder<T> page(int pageNum, int pageSize) {
        if (pageNum <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Page parameters must be positive");
        }
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 优化3: 添加排序支持
     */
    public QueryBuilder<T> orderBy(String field, boolean ascending) {
        DbColumn column = columnMap.get(field);
        if (column != null) {
            orderByList.add(ascending ? new OrderObject(OrderObject.Dir.ASCENDING, column) : new OrderObject(OrderObject.Dir.DESCENDING, column));
        } else {
            log.warn("Column not found for ordering field: {}", field);
        }
        return this;
    }

    /**
     * 优化4: 优化查询方法，添加资源自动关闭
     */
    public List<T> all() {
        String sql = buildSQL();
        List<T> result = new ArrayList<>();

        try (Connection conn = super.origin().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            log.debug("Execute SQL: {}", sql);

            while (rs.next()) {
                result.add(mapResultSetToEntity(rs));
            }

        } catch (Exception e) {
            log.error("Query execution error", e);
            throw new RuntimeException("Failed to execute query", e);
        }

        return result;
    }

    /**
     * 优化5: 抽取结果集映射逻辑
     */
    private T mapResultSetToEntity(ResultSet rs) throws Exception {
        Map<String, Object> fields = new HashMap<>(8);

        for (Field field : targetClass.getDeclaredFields()) {
            String dbField = ReflectTools.getDbField(field);
            Object value = getValueFromResultSet(rs, field, dbField);
            if (value != null) {
                fields.put(field.getName(), value);
            }
        }

        return Instance.transfer(targetClass, fields);
    }


    /**
     * 优化7: 改进 SQL 构建方法
     */
    private String buildSQL() {
        SelectQuery selectQuery = new SelectQuery()
                .addFromTable(dbTable);

        // 添加查询字段
        addColumns(selectQuery);

        // 添加查询条件
        addConditions(selectQuery);

        // 添加排序
        if (!orderByList.isEmpty()) {
            for (OrderObject order : orderByList) {
                selectQuery.addCustomOrderings(order);
            }
        }

        // 构建基础 SQL
        String sql = selectQuery.validate().toString();

        // MySQL 风格的分页
        if (pageSize != null && pageNum != null) {
            int offset = (pageNum - 1) * pageSize;
            sql += String.format(" LIMIT %d OFFSET %d", pageSize, offset);
        }

        return sql;
    }

    /**
     * 优化9: 添加计数方法
     */
    public long count() {
        SelectQuery countQuery = new SelectQuery()
                .addFromTable(dbTable)
                .addCustomColumns("COUNT(1)");

        for (Condition condition : conditions) {
            addCondition(countQuery, condition);
        }

        try (Connection conn = super.origin().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery.toString())) {

            return rs.next() ? rs.getLong(1) : 0;
        } catch (Exception e) {
            log.error("Count query execution error", e);
            throw new RuntimeException("Failed to execute count query", e);
        }
    }

    /**
     * 优化10: 添加批量操作支持
     */
    public List<T> batchQuery(List<Condition> batchConditions) {
        List<T> results = new ArrayList<>();
        for (Condition condition : batchConditions) {
            results.addAll(
                    new QueryBuilder<>(targetClass)
                            .addCondition(condition)
                            .all()
            );
        }
        return results;
    }

    /**
     * 添加查询列
     *
     * @param selectQuery 查询对象
     */
    private void addColumns(SelectQuery selectQuery) {
        for (Field field : targetClass.getDeclaredFields()) {
            String dbField = ReflectTools.getDbField(field);
            DbColumn dbColumn = dbTable.addColumn(dbField);
            selectQuery.addColumns(dbColumn);
            columnMap.put(dbField, dbColumn);
        }
    }

    /**
     * 添加查询条件
     *
     * @param selectQuery 查询对象
     */
    private void addConditions(SelectQuery selectQuery) {
        for (Condition condition : conditions) {
            addCondition(selectQuery, condition);
        }
    }

    /**
     * 添加单个查询条件
     *
     * @param selectQuery 查询对象
     * @param condition   条件对象
     */
    private void addCondition(SelectQuery selectQuery, Condition condition) {
        Condition.Filter filter = condition.getCondition();
        DbColumn dbColumn = columnMap.get(filter.getFiled());

        if (dbColumn == null) {
            log.warn("Column not found for field: {}", filter.getFiled());
            return;
        }

        switch (filter.getOperator()) {
            case EQUAL:
                selectQuery.addCondition(BinaryCondition.equalTo(dbColumn, filter.getValue()));
                break;
            case NOT_EQUAL:
                selectQuery.addCondition(BinaryCondition.notEqualTo(dbColumn, filter.getValue()));
                break;
            case GREATER:
                selectQuery.addCondition(BinaryCondition.greaterThan(dbColumn, filter.getValue(), true));
                break;
            case LESS:
                selectQuery.addCondition(BinaryCondition.lessThan(dbColumn, filter.getValue(), true));
                break;
            case LIKE:
                selectQuery.addCondition(BinaryCondition.like(dbColumn, "%" + filter.getValue() + "%"));
                break;
            case IN:
                if (filter.getValue() instanceof Collection) {
                    selectQuery.addCondition(new InCondition(dbColumn, (Collection<?>) filter.getValue()));
                }
                break;
            default:
                log.warn("Unsupported operator: {}", filter.getOperator());
        }
    }

    /**
     * 添加查询条件
     *
     * @param condition 查询条件
     * @return 当前查询对象
     */
    public QueryBuilder<T> addCondition(Condition condition) {
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    /**
     * 获取值的类型安全方法
     *
     * @param rs      结果集
     * @param field   字段
     * @param dbField 数据库字段名
     * @return 字段值
     */
    private Object getValueFromResultSet(ResultSet rs, Field field, String dbField) throws SQLException {
        if (rs.getObject(dbField) == null) {
            return null;
        }

        Class<?> type = field.getType();
        try {
            if (type == String.class) {
                return rs.getString(dbField);
            } else if (type == Integer.class || type == int.class) {
                return rs.getInt(dbField);
            } else if (type == Long.class || type == long.class) {
                return rs.getLong(dbField);
            } else if (type == Double.class || type == double.class) {
                return rs.getDouble(dbField);
            } else if (type == Float.class || type == float.class) {
                return rs.getFloat(dbField);
            } else if (type == Boolean.class || type == boolean.class) {
                return rs.getBoolean(dbField);
            } else if (type == Date.class) {
                return rs.getTimestamp(dbField);
            } else if (type == BigDecimal.class) {
                return rs.getBigDecimal(dbField);
            } else if (type.isEnum()) {
                String value = rs.getString(dbField);
                return value != null ? Enum.valueOf((Class<? extends Enum>) type, value) : null;
            }
        } catch (SQLException e) {
            log.error("Error getting value for field: {} of type: {}", dbField, type, e);
            throw e;
        }

        throw new UnsupportedOperationException("Unsupported field type: " + type.getName());
    }

    /**
     * 自定义查询条件接口
     */
    private interface QueryCustomization {
        void apply(SelectQuery query);
    }

    /**
     * 优化6: 添加单条记录查询方法
     */
    public T one() {
        pageSize = 1;
        pageNum = 1;
        List<T> results = all();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 优化7: 添加条件构建器方法
     */
    public QueryBuilder<T> where(String field, Object value) {
        return addCondition(new Condition(field, Condition.Filter.Operator.EQUAL, value));
    }

    public QueryBuilder<T> whereLike(String field, String value) {
        return addCondition(new Condition(field, Condition.Filter.Operator.LIKE, value));
    }

    public QueryBuilder<T> whereIn(String field, Collection<?> values) {
        return addCondition(new Condition(field, Condition.Filter.Operator.IN, values));
    }

    /**
     * 优化8: 添加事务支持
     */
    public interface TransactionCallback<R> {
        R doInTransaction(Connection connection) throws SQLException;
    }

    public <R> R executeInTransaction(TransactionCallback<R> callback) {
        Connection connection = null;
        try {
            connection = super.origin().getConnection();
            connection.setAutoCommit(false);

            R result = callback.doInTransaction(connection);

            connection.commit();
            return result;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("Error rolling back transaction", ex);
                }
            }
            throw new RuntimeException("Transaction failed", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error closing connection", e);
                }
            }
        }
    }

    /**
     * 优化9: 添加批量插入方法
     */
    public int batchInsert(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }

        return executeInTransaction(connection -> {
            int totalInserted = 0;
            try (Statement stmt = connection.createStatement()) {
                for (T entity : entities) {
                    String sql = buildInsertSQL(entity);
                    stmt.addBatch(sql);
                }
                int[] results = stmt.executeBatch();
                for (int result : results) {
                    if (result > 0) {
                        totalInserted++;
                    }
                }
            }
            return totalInserted;
        });
    }

    /**
     * 构建插入SQL语句
     */
    private String buildInsertSQL(T entity) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        for (Field field : targetClass.getDeclaredFields()) {
            String dbField = ReflectTools.getDbField(field);
            field.setAccessible(true);

            try {
                Object value = field.get(entity);
                if (value != null) {
                    if (columns.length() > 0) {
                        columns.append(", ");
                        values.append(", ");
                    }
                    columns.append(dbField);
                    values.append(formatValue(value));
                }
            } catch (IllegalAccessException e) {
                log.error("Error accessing field: {}", field.getName(), e);
            }
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                dbTable.getTableNameSQL(),
                columns.toString(),
                values.toString());
    }

    /**
     * 格式化SQL值
     */
    private String formatValue(Object value) {
        if (value instanceof String || value instanceof Date) {
            return "'" + value.toString().replace("'", "''") + "'";
        } else if (value instanceof Enum) {
            return "'" + ((Enum<?>) value).name() + "'";
        }
        return String.valueOf(value);
    }
}
