package top.crossoverjie.cicada.db.reflect;

import lombok.extern.slf4j.Slf4j;
import top.crossoverjie.cicada.db.model.Model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Instance 类用于处理对象实例的反射操作
 * 主要功能包括：
 * 1. 将数据转换为指定类型的对象实例
 * 2. 处理对象的属性设置和获取
 */
@Slf4j
public class EntityMapper {

    /**
     * 将单个字段值转换为目标类型的实例
     * @param target 目标类型的Class对象
     * @param filedName 字段名
     * @param value 字段值
     * @param <T> 目标类型（必须继承自Model类）
     * @return 目标类型的实例
     */
    public static <T extends Model> T transfer(Class<T> target, String filedName, Object value) {
        // 参数校验
        if (filedName == null || value == null){
            throw new RuntimeException("argument is null!") ;
        }

        T obj = null;
        try {
            // 创建目标类型的实例
            obj = target.newInstance();
            // 将字段名转换为setter方法名（首字母大写）
            filedName = methodName(filedName);
            // 获取setter方法
            Method method = target.getMethod("set" + filedName, value.getClass());
            // 调用setter方法设置值
            method.invoke(obj, value);
            return obj;
        } catch (Exception e) {
            log.error("exception", e);
        }

        return obj;
    }

    /**
     * 将多个字段值转换为目标类型的实例
     * @param target 目标类型的Class对象
     * @param params 字段名和值的映射
     * @param <T> 目标类型（必须继承自Model类）
     * @return 目标类型的实例
     */
    public static <T extends Model> T transfer(Class<T> target, Map<String,Object> params){
        T obj = null;
        try {
            // 创建目标类型的实例
            obj = target.newInstance();
            // 遍历参数映射
            for (Map.Entry<String, Object> param : params.entrySet()) {
                // 将字段名转换为setter方法名
                String filedName = methodName(param.getKey());
                // 获取setter方法
                Method method = target.getMethod("set" + filedName, param.getValue().getClass());
                // 调用setter方法设置值
                method.invoke(obj, param.getValue());
            }
        } catch (Exception e) {
            log.error("exception", e);
        }
        return obj;
    }

    /**
     * 将字段名转换为方法名（首字母大写）
     * 例如：userName -> UserName
     * @param filedName 字段名
     * @return 转换后的方法名
     */
    private static String methodName(String filedName){
        // 获取首字母并转换为大写
        char c = filedName.charAt(0);
        filedName = filedName.replace(c, Character.toUpperCase(c));
        return filedName;
    }

    /**
     * 获取对象指定字段的值
     * @param obj 目标对象
     * @param field 字段对象
     * @return 字段值
     */
    public static Object getFiledValue(Object obj, Field field){
        try {
            // 获取getter方法
            Method method = obj.getClass().getDeclaredMethod("get" + methodName(field.getName()));
            // 调用getter方法获取值
            return method.invoke(obj);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        return null;
    }
}
