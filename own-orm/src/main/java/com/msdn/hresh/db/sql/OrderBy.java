package com.msdn.hresh.db.sql;

import lombok.Getter;

/**
 * SQL排序
 * @author hresh
 * @date 2025/1/2 12:35
 */
@Getter
public class OrderBy {
    private final String field;
    private final boolean asc;

    public OrderBy(String field, boolean asc) {
        this.field = field;
        this.asc = asc;
    }
} 