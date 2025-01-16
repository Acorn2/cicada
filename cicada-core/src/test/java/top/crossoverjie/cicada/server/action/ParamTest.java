package top.crossoverjie.cicada.server.action;

import org.junit.Test;
import top.crossoverjie.cicada.server.action.param.ParamMap;

import static org.junit.Assert.assertEquals;

/**
 * @author hresh
 * @博客 https://juejin.cn/user/2664871918047063
 * @网站 https://www.hreshhao.com/
 * @date 2025/1/15 14:36
 */
public class ParamTest {

    @Test
    public void testGetString() {
        ParamMap param = new ParamMap();
        param.put("name", "test");
        assertEquals("test", param.getString("name"));
    }

    @Test
    public void testGetInteger() {
        ParamMap param = new ParamMap();
        param.put("age", "18");
        assertEquals(Integer.valueOf(18), param.getInteger("age"));
    }

}
