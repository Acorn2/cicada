package com.msdn.hresh.db.core;

import com.msdn.hresh.db.sql.Condition;
import com.msdn.hresh.db.sql.SqlBuilder;
import com.msdn.hresh.db.util.ReflectUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 基础DAO操作类
 * @author hresh
 * @date 2025/1/2 13:20
 */
public class BaseDao<T> {
    protected final Class<T> entityClass;

    public BaseDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Query<T> query() {
        return new Query<>(entityClass);
    }

    public int insert(T entity) {
        String sql = SqlBuilder.buildInsert(entity);
        List<Object> parameters = SqlBuilder.getSqlParameters(entity, null);
        
        try (Connection conn = OrmSession.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert entity", e);
        }
    }

    public int update(T entity) {
        // 使用主键作为更新条件
        java.lang.reflect.Field pkField = ReflectUtil.getPrimaryKeyField(entityClass);
        Object pkValue = ReflectUtil.getFieldValue(entity, pkField);
        List<Condition> conditions = Collections.singletonList(
                new Condition(pkField.getName(), Condition.Operator.EQUAL, pkValue));
        
        String sql = SqlBuilder.buildUpdate(entity, conditions);
        List<Object> parameters = SqlBuilder.getSqlParameters(entity, conditions);
        
        try (Connection conn = OrmSession.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update entity", e);
        }
    }

    public int delete(T entity) {
        // 使用主键作为删除条件
        java.lang.reflect.Field pkField = ReflectUtil.getPrimaryKeyField(entityClass);
        Object pkValue = ReflectUtil.getFieldValue(entity, pkField);
        List<Condition> conditions = Collections.singletonList(
                new Condition(pkField.getName(), Condition.Operator.EQUAL, pkValue));
        
        String sql = SqlBuilder.buildDelete(entityClass, conditions);
        List<Object> parameters = SqlBuilder.getSqlParameters(null, conditions);
        
        try (Connection conn = OrmSession.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete entity", e);
        }
    }
} 