package top.crossoverjie.cicada.db.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2019-11-28 00:49
 * @since JDK 1.8
 */
public class Condition {

    private Filter condition;

    public Condition(String field, Object value) {
        this.condition = new Filter(field, Filter.Operator.EQUAL, value);
    }

    public Condition(String field, Filter.Operator operator, Object value) {
        this.condition = new Filter(field, operator, value);
    }

    public int type(){
        return 0 ;
    }

    public Condition process(String property, Object value){
        return this ;
    }

    public Filter getCondition(){
        return condition;
    }

    @Data
    @NoArgsConstructor
    public static class Filter{
        private String filed ;
        private Operator operator;
        private Object value ;

        public Filter(String filed, Object value) {
            this.filed = filed;
            this.value = value;
        }

        public Filter(String filed, Operator operator, Object value) {
            this.filed = filed;
            this.operator = operator;
            this.value = value;
        }

        /**
         * SQL操作符枚举
         */
        public enum Operator {
            EQUAL("="),          // 等于
            NOT_EQUAL("!="),     // 不等于
            GREATER(">"),        // 大于
            LESS("<"),           // 小于
            GREATER_EQUAL(">="), // 大于等于
            LESS_EQUAL("<="),    // 小于等于
            LIKE("LIKE"),        // 模糊匹配
            IN("IN"),           // 包含
            NOT_IN("NOT IN"),    // 不包含
            IS_NULL("IS NULL"),  // 为空
            NOT_NULL("IS NOT NULL"); // 不为空

            private final String symbol;

            Operator(String symbol) {
                this.symbol = symbol;
            }

            public String getSymbol() {
                return symbol;
            }
        }

        // Getters
        public String getFiled() {
            return filed;
        }

        public Operator getOperator() {
            return operator;
        }

        public Object getValue() {
            return value;
        }
    }

}
