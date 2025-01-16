package top.crossoverjie.cicada.example.action;

import lombok.extern.slf4j.Slf4j;
import top.crossoverjie.cicada.example.dto.UserDTO;
import top.crossoverjie.cicada.server.action.param.Param;
import top.crossoverjie.cicada.server.action.req.CicadaRequest;
import top.crossoverjie.cicada.server.action.res.CicadaResponse;
import top.crossoverjie.cicada.base.annotation.CicadaAction;
import top.crossoverjie.cicada.base.annotation.CicadaRoute;

/**
 * @author hresh
 * @博客 https://juejin.cn/user/2664871918047063
 * @网站 https://www.hreshhao.com/
 * @date 2025/1/15 14:33
 */
@Slf4j
@CicadaAction("userAction")
public class UserAction {

    @CicadaRoute("getUser")
    public void getUser(CicadaRequest request) {
        // 使用参数
        String name = request.getBodyParameters().getString("name");
        log.info("getUser request: {}", name);

        UserDTO userDTO = request.getBodyParameters().convertTo(UserDTO.class);
        log.info("saveUser request: {}", userDTO);

    }

    @CicadaRoute("create")
    public void createUser(CicadaRequest request, CicadaResponse response) {
        // POST 请求体参数
        String name = request.getBodyParameters().getString("name");
        Integer age = request.getBodyParameters().getInteger("age");

        // GET 查询参数
        String type = request.getQueryParameters().getString("type");

        // 获取所有参数（合并GET和POST的参数）
        Param allParams = request.getAllParameters();
    }

}
