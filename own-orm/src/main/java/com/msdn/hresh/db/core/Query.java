package com.msdn.hresh.db.core;

import com.msdn.hresh.db.sql.Condition;
import com.msdn.hresh.db.sql.OrderBy;
import com.msdn.hresh.db.sql.SqlBuilder;
import com.msdn.hresh.db.util.ReflectUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询构建器
 * @author hresh
 * @date 2025/1/2 13:10
 */
public class Query<T> {
    private final Class<T> entityClass;
    private final List<Condition> conditions = new ArrayList<>();
    private final List<OrderBy> orderByList = new ArrayList<>();
    private Integer pageNum;
    private Integer pageSize;

    public Query(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Query<T> eq(String field, Object value) {
        conditions.add(new Condition(field, Condition.Operator.EQUAL, value));
        return this;
    }

    public Query<T> ne(String field, Object value) {
        conditions.add(new Condition(field, Condition.Operator.NOT_EQUAL, value));
        return this;
    }

    public Query<T> gt(String field, Object value) {
        conditions.add(new Condition(field, Condition.Operator.GREATER_THAN, value));
        return this;
    }

    public Query<T> lt(String field, Object value) {
        conditions.add(new Condition(field, Condition.Operator.LESS_THAN, value));
        return this;
    }

    public Query<T> like(String field, String value) {
        conditions.add(new Condition(field, Condition.Operator.LIKE, "%" + value + "%"));
        return this;
    }

    public Query<T> orderBy(String field, boolean asc) {
        orderByList.add(new OrderBy(field, asc));
        return this;
    }

    public Query<T> page(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        return this;
    }

    public List<T> list() {
        String sql = SqlBuilder.buildSelect(entityClass, conditions, orderByList, pageNum, pageSize);
        List<Object> parameters = SqlBuilder.getSqlParameters(null, conditions);
        
        try (Connection conn = OrmSession.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            return convertToEntityList(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    public T one() {
        this.pageNum = 1;
        this.pageSize = 1;
        List<T> list = list();
        return list.isEmpty() ? null : list.get(0);
    }

    private List<T> convertToEntityList(ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            T entity = convertToEntity(rs);
            result.add(entity);
        }
        return result;
    }

    private T convertToEntity(ResultSet rs) throws SQLException {
        try {
            T entity = entityClass.newInstance();
            List<java.lang.reflect.Field> fields = ReflectUtil.getMappedFields(entityClass);
            
            for (java.lang.reflect.Field field : fields) {
                String columnName = ReflectUtil.getColumnName(field);
                Object value = rs.getObject(columnName);
                ReflectUtil.setFieldValue(entity, field, value);
            }
            
            return entity;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create entity instance", e);
        }
    }
} 