package top.crossoverjie.cicada.server.action.param;

import com.fasterxml.jackson.databind.ObjectMapper;
import top.crossoverjie.cicada.server.exception.GenericException;

import java.util.HashMap;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/9/2 11:30
 * @since JDK 1.8
 */
public class ParamMap extends HashMap<String,Object> implements Param {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    @Override
    public <T> T convertTo(Class<T> clazz) {
        try {
            // 先将Map转为JSON字符串，再转为目标对象
            String json = OBJECT_MAPPER.writeValueAsString(this);
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new GenericException("Failed to convert parameters to " + clazz.getName());
        }
    }

    @Override
    public String getString(String param) {
        return super.get(param).toString();
    }

    @Override
    public Integer getInteger(String param) {
        return Integer.parseInt(super.get(param).toString());
    }

    @Override
    public Long getLong(String param) {
        return (Long) super.get(param);
    }

    @Override
    public Double getDouble(String param) {
        return (Double) super.get(param);
    }

    @Override
    public Float getFloat(String param) {
        return (Float) super.get(param);
    }

    @Override
    public Boolean getBoolean(String param) {
        return (Boolean) super.get(param);
    }
}
