package com.msdn.hresh.db.sql;

import lombok.Getter;

/**
 * SQL查询条件
 * @author hresh
 * @date 2025/1/2 12:30
 */
@Getter
public class Condition {
    private final String field;
    private final Operator operator;
    private final Object value;

    public Condition(String field, Operator operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public enum Operator {
        EQUAL("="),
        NOT_EQUAL("!="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_EQUAL(">="),
        LESS_EQUAL("<="),
        LIKE("LIKE"),
        IN("IN"),
        NOT_IN("NOT IN"),
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }
} 