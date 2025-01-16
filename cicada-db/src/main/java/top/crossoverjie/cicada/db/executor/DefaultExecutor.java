package top.crossoverjie.cicada.db.executor;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import lombok.extern.slf4j.Slf4j;
import top.crossoverjie.cicada.db.annotation.OriginName;
import top.crossoverjie.cicada.db.annotation.PrimaryId;
import top.crossoverjie.cicada.db.reflect.EntityMapper;
import top.crossoverjie.cicada.db.reflect.ReflectionUtils;
import top.crossoverjie.cicada.db.session.SqlSessionFactory;
import top.crossoverjie.cicada.db.model.Model;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Function:
 *- 实现具体的CRUD操作
 * - 处理SQL构建和执行
 *
 * @author crossoverJie
 * Date: 2019-12-03 23:53
 * @since JDK 1.8
 */
@Slf4j
public class DefaultExecutor extends SqlSessionFactory implements SqlExecutor {

    private DbTable dbTable;



    @Override
    public int update(Object obj) {
        if (obj instanceof Model) {
            dbTable = super.origin().addTable(obj.getClass().getAnnotation(OriginName.class).value());

            Map<DbColumn, Integer> primaryCondition = new HashMap<>(1);
            UpdateQuery updateQuery = new UpdateQuery(dbTable);
            for (Field field : obj.getClass().getDeclaredFields()) {
                String dbField = ReflectionUtils.getDbField(field);

                Object filedValue = EntityMapper.getFiledValue(obj, field);
                if (null == filedValue) {
                    continue;
                }

                if (field.getAnnotation(PrimaryId.class) != null) {
                    primaryCondition.put(dbTable.addColumn(dbField), (Integer) filedValue);
                } else {
                    DbColumn dbColumn = dbTable.addColumn(dbField);
                    updateQuery.addSetClause(dbColumn, filedValue);
                }

            }
            for (Map.Entry<DbColumn, Integer> entry : primaryCondition.entrySet()) {
                updateQuery.addCondition(BinaryCondition.equalTo(entry.getKey(), entry.getValue()));
            }

            Statement statement = null;
            try {
                statement = super.origin().getConnection().createStatement();
                log.debug("execute sql>>>>>{}", updateQuery.validate().toString());
                return statement.executeUpdate(updateQuery.toString());
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                try {
                    statement.close();
                } catch (SQLException e) {
                    log.error("SQLException", e);
                }
            }
            return 0;
        } else {
            return 0;
        }
    }

    @Override
    public void insert(Object obj) {
        dbTable = super.origin().addTable(obj.getClass().getAnnotation(OriginName.class).value());
        InsertQuery insertSelectQuery = new InsertQuery(dbTable);
        List<Field> values = new ArrayList<>();

        for (Field field : obj.getClass().getDeclaredFields()) {
            String dbField = ReflectionUtils.getDbField(field);
            Object filedValue = EntityMapper.getFiledValue(obj, field);
            if (null == filedValue) {
                continue;
            }

            insertSelectQuery.addPreparedColumns(dbTable.addColumn(dbField));
            values.add(field);
        }

        log.debug("execute sql>>>>>{}", insertSelectQuery.validate().toString());
        StringBuilder sb = new StringBuilder();
        PreparedStatement statement = null;
        try {
            statement = super.origin().getConnection().prepareStatement(insertSelectQuery.toString());
            for (int i = 0; i < values.size(); i++) {
                Field value = values.get(i);
                if (value.getType() == Integer.class) {
                    statement.setInt(i+1, (Integer) EntityMapper.getFiledValue(obj, value));
                }
                if (value.getType() == String.class) {
                    statement.setString(i+1, (String) EntityMapper.getFiledValue(obj, value));
                }
                sb.append(value.getName() + "=" + EntityMapper.getFiledValue(obj, value) + "\t") ;
            }
            log.debug("params >>>>>>>>>[{}]", sb.toString());
            statement.execute() ;
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }

    }

    @Override
    public int delete(Object obj) {
        if (!(obj instanceof Model)) {
            return 0;
        }

        try {
            // 获取表名
            dbTable = super.origin().addTable(obj.getClass().getAnnotation(OriginName.class).value());
            DeleteQuery deleteQuery = new DeleteQuery(dbTable);

            // 查找主键字段
            Field primaryKeyField = null;
            Object primaryKeyValue = null;
            
            for (Field field : obj.getClass().getDeclaredFields()) {
                if (field.getAnnotation(PrimaryId.class) != null) {
                    primaryKeyField = field;
                    primaryKeyValue = EntityMapper.getFiledValue(obj, field);
                    break;
                }
            }

            if (primaryKeyField == null || primaryKeyValue == null) {
                log.error("No primary key found or primary key value is null");
                return 0;
            }

            // 添加主键条件
            String dbField = ReflectionUtils.getDbField(primaryKeyField);
            DbColumn primaryKeyColumn = dbTable.addColumn(dbField);
            deleteQuery.addCondition(BinaryCondition.equalTo(primaryKeyColumn, primaryKeyValue));

            // 执行删除操作
            try (Statement statement = super.origin().getConnection().createStatement()) {
                String sql = deleteQuery.validate().toString();
                log.debug("execute sql>>>>>{}", sql);
                return statement.executeUpdate(sql);
            }

        } catch (SQLException e) {
            log.error("Delete operation failed", e);
            throw new RuntimeException("Delete operation failed", e);
        } catch (Exception e) {
            log.error("Unexpected error during delete operation", e);
            throw new RuntimeException("Unexpected error during delete operation", e);
        }
    }
}
