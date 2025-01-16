package top.crossoverjie.cicada.example.action;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import top.crossoverjie.cicada.base.annotation.CicadaAction;
import top.crossoverjie.cicada.base.annotation.CicadaRoute;
import top.crossoverjie.cicada.db.executor.ExecutorProxy;
import top.crossoverjie.cicada.db.executor.SqlExecutor;
import top.crossoverjie.cicada.db.query.QueryBuilder;
import top.crossoverjie.cicada.db.sql.EqualToCondition;
import top.crossoverjie.cicada.example.dto.UserDTO;
import top.crossoverjie.cicada.example.event.UserSaveEvent;
import top.crossoverjie.cicada.example.event.UserUpdateEvent;
import top.crossoverjie.cicada.example.model.User;
import top.crossoverjie.cicada.server.action.req.CicadaRequest;
import top.crossoverjie.cicada.server.action.res.ApiResponse;

import java.util.List;
import java.util.Objects;

/**
 * 路由处理Action
 */
@CicadaAction("routeAction")
@Slf4j
public class RouteAction {

    @CicadaRoute("getUser")
    public ApiResponse<List<User>> getUser(CicadaRequest request) {
        UserDTO userDTO = request.getBodyParameters().convertTo(UserDTO.class);
        log.info("getUser request: {}", userDTO);
        
        QueryBuilder<User> queryBuilder = new QueryBuilder<>(User.class);
        addUserQueryConditions(queryBuilder, userDTO);
        List<User> users = queryBuilder.all();

        return ApiResponse.success(users);
    }

    @CicadaRoute("queryUser")
    public ApiResponse<List<User>> queryUser(CicadaRequest request) {
        UserDTO userDTO = request.getBodyParameters().convertTo(UserDTO.class);
        log.info("queryUser request: {}", userDTO);

        QueryBuilder<User> queryBuilder = new QueryBuilder<>(User.class);
        addUserQueryConditions(queryBuilder, userDTO);
        List<User> users = queryBuilder.all();
        log.info("queryUser result: {}", JSON.toJSONString(users));

        return ApiResponse.success(users);
    }

    /**
     * 添加用户查询条件
     */
    private void addUserQueryConditions(QueryBuilder<User> queryBuilder, UserDTO userDTO) {
        if (Objects.nonNull(userDTO.getId())) {
            queryBuilder.addCondition(new EqualToCondition("id", userDTO.getId()));
        }
        
        if (Objects.nonNull(userDTO.getName()) && !userDTO.getName().trim().isEmpty()) {
            queryBuilder.addCondition(new EqualToCondition("name", userDTO.getName().trim()));
        }
        
        if (Objects.nonNull(userDTO.getCityId())) {
            queryBuilder.addCondition(new EqualToCondition("city_id", userDTO.getCityId()));
        }
        
    }

    @CicadaRoute("saveUser")
    public ApiResponse<Void> saveUser(CicadaRequest request) {
        UserDTO userDTO = request.getBodyParameters().convertTo(UserDTO.class);
        log.info("saveUser request: {}", userDTO);
        
        // 参数校验
        if (Objects.isNull(userDTO.getName()) || userDTO.getName().trim().isEmpty()) {
            return ApiResponse.error("400", "用户名不能为空");
        }
        if (Objects.isNull(userDTO.getPassword()) || userDTO.getPassword().trim().isEmpty()) {
            return ApiResponse.error("400", "密码不能为空");
        }
        
        SqlExecutor handle = (SqlExecutor) new ExecutorProxy<>(SqlExecutor.class)
                .getInstance(new UserSaveEvent());
        
        User user = convertToEntity(userDTO);
        handle.insert(user);
        
        return ApiResponse.success(null);
    }

    @CicadaRoute("updateUser")
    public ApiResponse<Integer> updateUser(CicadaRequest request) {
        UserDTO userDTO = request.getBodyParameters().convertTo(UserDTO.class);
        log.info("updateUser request: {}", userDTO);
        
        // 参数校验
        if (Objects.isNull(userDTO.getId())) {
            return ApiResponse.error("400", "用户ID不能为空");
        }
        
        SqlExecutor handle = (SqlExecutor) new ExecutorProxy<>(SqlExecutor.class)
                .getInstance(new UserUpdateEvent());
        
        User user = convertToEntity(userDTO);
        int count = handle.update(user);

        return ApiResponse.success(count);
    }

    @CicadaRoute("getUserText")
    public ApiResponse<UserDTO> getUserText(CicadaRequest request) {
        UserDTO userDTO = request.getQueryParameters().convertTo(UserDTO.class);
        log.info("getUserText request: {}", userDTO);

        // 参数校验
        if (Objects.isNull(userDTO.getName()) || userDTO.getName().trim().isEmpty()) {
            return ApiResponse.error("400", "用户名不能为空");
        }

        return ApiResponse.success(userDTO);
    }

    /**
     * DTO转换为实体类
     */
    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName() != null ? dto.getName().trim() : null);
        user.setPassword(dto.getPassword() != null ? dto.getPassword().trim() : null);
        return user;
    }
}
