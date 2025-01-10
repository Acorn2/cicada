package com.msdn.hresh.db.sql;

import com.msdn.hresh.db.util.ReflectUtil;
import com.msdn.hresh.db.util.StringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL构建器
 * @author hresh
 * @date 2025/1/2 12:40
 */
public class SqlBuilder {

    /**
     * 构建查询SQL
     */
    public static String buildSelect(Class<?> entityClass, List<Condition> conditions, 
                                   List<OrderBy> orderByList, Integer pageNum, Integer pageSize) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(ReflectUtil.getTableName(entityClass));
        
        // WHERE条件
        if (conditions != null && !conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(buildWhereClause(conditions));
        }
        
        // ORDER BY
        if (orderByList != null && !orderByList.isEmpty()) {
            sql.append(" ORDER BY ");
            sql.append(buildOrderByClause(orderByList));
        }
        
        // 分页
        if (pageNum != null && pageSize != null) {
            sql.append(" LIMIT ").append(pageSize);
            sql.append(" OFFSET ").append((pageNum - 1) * pageSize);
        }
        
        return sql.toString();
    }

    /**
     * 构建插入SQL
     */
    public static String buildInsert(Object entity) {
        Class<?> entityClass = entity.getClass();
        List<Field> fields = ReflectUtil.getMappedFields(entityClass);
        
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(ReflectUtil.getTableName(entityClass));
        
        String columns = fields.stream()
                .map(ReflectUtil::getColumnName)
                .collect(Collectors.joining(", ", "(", ")"));
        
        String values = fields.stream()
                .map(field -> "?")
                .collect(Collectors.joining(", ", "(", ")"));
        
        sql.append(columns).append(" VALUES ").append(values);
        
        return sql.toString();
    }

    /**
     * 构建更新SQL
     */
    public static String buildUpdate(Object entity, List<Condition> conditions) {
        Class<?> entityClass = entity.getClass();
        List<Field> fields = ReflectUtil.getMappedFields(entityClass);
        
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(ReflectUtil.getTableName(entityClass));
        sql.append(" SET ");
        
        String setClause = fields.stream()
                .map(field -> ReflectUtil.getColumnName(field) + " = ?")
                .collect(Collectors.joining(", "));
        sql.append(setClause);
        
        if (conditions != null && !conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(buildWhereClause(conditions));
        }
        
        return sql.toString();
    }

    /**
     * 构建删除SQL
     */
    public static String buildDelete(Class<?> entityClass, List<Condition> conditions) {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(ReflectUtil.getTableName(entityClass));
        
        if (conditions != null && !conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(buildWhereClause(conditions));
        }
        
        return sql.toString();
    }

    /**
     * 构建WHERE子句
     */
    private static String buildWhereClause(List<Condition> conditions) {
        return conditions.stream()
                .map(condition -> {
                    String field = StringUtil.camelToUnderline(condition.getField());
                    if (condition.getValue() == null) {
                        return field + " " + condition.getOperator().getSymbol();
                    }
                    return field + " " + condition.getOperator().getSymbol() + " ?";
                })
                .collect(Collectors.joining(" AND "));
    }

    /**
     * 构建ORDER BY子句
     */
    private static String buildOrderByClause(List<OrderBy> orderByList) {
        return orderByList.stream()
                .map(orderBy -> StringUtil.camelToUnderline(orderBy.getField()) + 
                        (orderBy.isAsc() ? " ASC" : " DESC"))
                .collect(Collectors.joining(", "));
    }

    /**
     * 获取SQL参数值列表
     */
    public static List<Object> getSqlParameters(Object entity, List<Condition> conditions) {
        List<Object> parameters = new ArrayList<>();
        
        // 添加实体字段值
        if (entity != null) {
            List<Field> fields = ReflectUtil.getMappedFields(entity.getClass());
            for (Field field : fields) {
                parameters.add(ReflectUtil.getFieldValue(entity, field));
            }
        }
        
        // 添加条件值
        if (conditions != null) {
            for (Condition condition : conditions) {
                if (condition.getValue() != null) {
                    parameters.add(condition.getValue());
                }
            }
        }
        
        return parameters;
    }
} 