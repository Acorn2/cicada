package top.crossoverjie.cicada.server.route;

import top.crossoverjie.cicada.server.action.param.Param;
import top.crossoverjie.cicada.server.action.req.CicadaRequest;
import top.crossoverjie.cicada.server.bean.CicadaBeanManager;
import top.crossoverjie.cicada.server.context.CicadaContext;
import top.crossoverjie.cicada.base.exception.CicadaException;
import top.crossoverjie.cicada.server.exception.code.SystemErrorCode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/11/13 21:18
 * @since JDK 1.8
 */
public final class MethodInvokeProcessor {

    private volatile static MethodInvokeProcessor methodInvokeProcessor;

    private final CicadaBeanManager cicadaBeanManager = CicadaBeanManager.getInstance() ;

    /**
     * 获取RouteProcess单例
     * 使用双重检查锁定确保线程安全
     */
    public static MethodInvokeProcessor getInstance() {
        if (methodInvokeProcessor == null) {
            synchronized (MethodInvokeProcessor.class) {
                if (methodInvokeProcessor == null) {
                    methodInvokeProcessor = new MethodInvokeProcessor();
                }
            }
        }
        return methodInvokeProcessor;
    }

    /**
     * 核心方法：执行路由方法调用
     * 1. 解析路由参数
     * 2. 获取对应的Bean实例
     * 3. 反射调用目标方法
     * @param method 要调用的方法
     * @return 方法调用的返回值
     * @throws Exception 调用过程中的异常
     */
    public Object invoke(Method method) throws Exception {
        if (method == null) {
            return null;
        }

        // 获取当前请求上下文中的请求对象
        CicadaRequest request = CicadaContext.getRequest();
        
        // 解析路由参数，同时传入request对象以获取所有参数
        Object[] parameters = parseRouteParameter(method, request);
        
        // 获取方法所属的类实例
        Object bean = cicadaBeanManager.getBean(method.getDeclaringClass().getName());
        
        // 调用方法并返回结果
        if (parameters == null) {
            return method.invoke(bean);
        } else {
            return method.invoke(bean, parameters);
        }
    }

    /**
     * 解析路由参数的核心逻辑：
     * 1. 检查参数个数(不能超过2个)
     * 2. 处理特殊类型参数(CicadaContext)
     * 3. 处理自定义POJO参数：
     *    - 通过反射创建实例
     *    - 解析请求参数并注入到POJO字段
     *
     * @param method
     * @param request
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     */
    private Object[] parseRouteParameter(Method method, CicadaRequest request) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length == 0) {
            return null;
        }

        if (parameterTypes.length > 2) {
            throw new CicadaException(SystemErrorCode.ILLEGAL_PARAMETER);
        }

        Object[] instances = new Object[parameterTypes.length];

        for (int i = 0; i < instances.length; i++) {
            // 处理 CicadaContext 参数
            if (parameterTypes[i] == CicadaContext.class) {
                instances[i] = CicadaContext.getContext();
            }
            // 处理 CicadaRequest 参数
            else if (CicadaRequest.class.isAssignableFrom(parameterTypes[i])) {
                instances[i] = request;
            }
            // 处理自定义 POJO 参数
            else {
                Class<?> parameterType = parameterTypes[i];
                Object instance = parameterType.newInstance();

                // 获取所有参数（包括查询参数和请求体参数）
                Param allParams = request.getAllParameters();
                
                // 遍历并设置字段值
                for (String key : allParams.keySet()) {
                    try {
                        Field field = parameterType.getDeclaredField(key);
                        field.setAccessible(true);
                        field.set(instance, parseFieldValue(field, allParams.getString(key)));
                    } catch (NoSuchFieldException e) {
                        // 如果字段不存在，继续处理下一个参数
                        continue;
                    }
                }
                instances[i] = instance;
            }
        }

        return instances;
    }

    /**
     * 字段值解析方法：
     * 1. 处理空值情况
     * 2. 处理基本数据类型的默认值
     * 3. 支持类型：
     *    - 基本数据类型及其包装类
     *    - String
     *    - BigDecimal
     * 4. 将字符串值转换为对应的类型
     */
    private Object parseFieldValue(Field field, String value) {
        if (value == null) {
            return null;
        }

        Class<?> type = field.getType();
        if ("".equals(value)) {
            boolean base = type.equals(int.class) || type.equals(double.class) ||
                    type.equals(short.class) || type.equals(long.class) ||
                    type.equals(byte.class) || type.equals(float.class);
            if (base) {
                return 0;
            }
        }
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(String.class)) {
            return value;
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return Float.parseFloat(value);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(value);
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (type.equals(Short.class) || type.equals(short.class)) {
            return Short.parseShort(value);
        } else if (type.equals(Byte.class) || type.equals(byte.class)) {
            return Byte.parseByte(value);
        } else if (type.equals(BigDecimal.class)) {
            return new BigDecimal(value);
        }

        return null;
    }

}
