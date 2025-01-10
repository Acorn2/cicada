package com.msdn.hresh.db.util;

import com.msdn.hresh.db.annotation.FieldName;
import com.msdn.hresh.db.annotation.Ignore;
import com.msdn.hresh.db.annotation.PrimaryKey;
import com.msdn.hresh.db.annotation.TableName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 反射工具类
 * @author hresh
 * @date 2025/1/2 12:10
 */
public class ReflectUtil {

    /**
     * 获取类的所有字段（包括父类字段）
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    /**
     * 获取类的所有需要映射的字段（排除@Ignore注解的字段）
     */
    public static List<Field> getMappedFields(Class<?> clazz) {
        return getAllFields(clazz).stream()
                .filter(field -> !field.isAnnotationPresent(Ignore.class))
                .collect(Collectors.toList());
    }

    /**
     * 获取主键字段
     */
    public static Field getPrimaryKeyField(Class<?> clazz) {
        return getAllFields(clazz).stream()
                .filter(field -> field.isAnnotationPresent(PrimaryKey.class))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No primary key found in class " + clazz.getName()));
    }

    /**
     * 获取表名
     */
    public static String getTableName(Class<?> clazz) {
        TableName annotation = clazz.getAnnotation(TableName.class);
        if (annotation != null) {
            return annotation.value();
        }
        return StringUtil.camelToUnderline(clazz.getSimpleName());
    }

    /**
     * 获取字段对应的数据库列名
     */
    public static String getColumnName(Field field) {
        FieldName annotation = field.getAnnotation(FieldName.class);
        if (annotation != null) {
            return annotation.value();
        }
        return StringUtil.camelToUnderline(field.getName());
    }

    /**
     * 设置字段值
     */
    public static void setFieldValue(Object obj, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set field value", e);
        }
    }

    /**
     * 获取字段值
     */
    public static Object getFieldValue(Object obj, Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value", e);
        }
    }
} 