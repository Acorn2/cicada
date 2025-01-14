package top.crossoverjie.cicada.db.executor;

/**
 * Function:
 * 定义基础的CRUD操作接口
 *
 * @author crossoverJie
 * Date: 2019-12-03 23:35
 * @since JDK 1.8
 */
public interface SqlExecutor {

    /** update model
     * @param obj model of db entity
     * @return affected rows
     */
    int update(Object obj) ;


    /**
     * insert model
     * @param obj model of db entity
     */
    void insert(Object obj) ;

    /**
     * delete model by primary key
     * @param obj model of db entity
     * @return affected rows
     */
    int delete(Object obj) ;

}
