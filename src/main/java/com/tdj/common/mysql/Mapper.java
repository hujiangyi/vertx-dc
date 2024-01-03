package com.tdj.common.mysql;

import io.vertx.sqlclient.templates.RowMapper;

import java.util.Map;
import java.util.function.Function;

public abstract class Mapper<T> {
    protected abstract RowMapper<T> resultMappper();
    protected abstract Function<T, Map<String, Object>> paramMapper();
}
